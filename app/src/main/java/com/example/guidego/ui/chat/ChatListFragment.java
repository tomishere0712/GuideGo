package com.example.guidego.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.FragmentChatListBinding;
import com.example.guidego.model.ChatMessage;
import com.example.guidego.model.ChatRoom;
import com.example.guidego.model.User;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {

    private static final String TAG = "ChatListFragment";

    private FragmentChatListBinding binding;
    private ChatListAdapter adapter;
    private TokenManager tokenManager;
    private HubConnection hubConnection;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager(requireContext());

        // Pass role so adapter shows the OTHER party's name
        adapter = new ChatListAdapter(tokenManager.getUserRole());
        adapter.setListener(chat -> openChat(chat));
        binding.rvChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChats.setAdapter(adapter);

        loadChats();
        connectSignalR();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats();
        // Reconnect if disconnected (e.g. after returning from ChatActivity)
        if (hubConnection == null
                || hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
            connectSignalR();
        }
    }

    // ─── SignalR ─────────────────────────────────────────────────────────────────

    private void connectSignalR() {
        String token = tokenManager.getToken();
        if (token == null) return;

        // Clean up previous connection if any
        disconnectSignalR();

        String hubUrl = ApiClient.getInstance(requireContext()).getHubUrl();
        hubConnection = HubConnectionBuilder.create(hubUrl)
                .withAccessTokenProvider(Single.just(token))
                .build();

        // Lắng nghe tin nhắn mới từ bất kỳ phòng chat nào → cập nhật preview
        hubConnection.on("ReceiveMessage", message -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> adapter.updateLastMessage(message));
        }, ChatMessage.class);

        disposables.add(
                hubConnection.start()
                        .subscribe(
                                () -> Log.d(TAG, "ChatList SignalR connected"),
                                err -> Log.w(TAG, "ChatList SignalR failed: " + err.getMessage())
                        )
        );
    }

    private void disconnectSignalR() {
        disposables.clear();
        if (hubConnection != null) {
            hubConnection.stop().subscribe(() -> {}, e -> {});
            hubConnection = null;
        }
    }

    // ─── HTTP ────────────────────────────────────────────────────────────────────

    private void loadChats() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
        binding.rvChats.setVisibility(View.GONE);

        ApiClient.getInstance(requireContext()).getApiService()
                .getMyChats()
                .enqueue(new Callback<List<ChatRoom>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ChatRoom>> call,
                                           @NonNull Response<List<ChatRoom>> response) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().isEmpty()) {
                            List<ChatRoom> chats = response.body();
                            adapter.setChats(chats);
                            binding.rvChats.setVisibility(View.VISIBLE);

                            // If Guide role: touristName may be missing → enrich from API
                            if (Constants.ROLE_GUIDE.equals(tokenManager.getUserRole())) {
                                enrichTouristNames(chats);
                            }
                        } else {
                            binding.emptyState.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ChatRoom>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * For each chat room where touristName is missing:
     * 1. If touristId is present → try getUserById.
     * 2. Fallback (touristId absent OR getUserById fails) → fetch messages and pick the
     *    first senderName that does not belong to the current guide.
     */
    private void enrichTouristNames(List<ChatRoom> chats) {
        String myUserId = tokenManager.getUserId();
        for (ChatRoom chat : chats) {
            boolean nameMissing = chat.getTouristName() == null
                    || chat.getTouristName().isEmpty()
                    || "Du khách".equals(chat.getTouristName());
            if (!nameMissing) continue;

            String touristId = chat.getTouristId();
            if (touristId != null && !touristId.isEmpty()) {
                ApiClient.getInstance(requireContext()).getApiService()
                        .getUserById(touristId)
                        .enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(@NonNull Call<User> call,
                                                   @NonNull Response<User> response) {
                                if (!isAdded()) return;
                                if (response.isSuccessful() && response.body() != null
                                        && response.body().getFullName() != null
                                        && !response.body().getFullName().isEmpty()) {
                                    chat.setTouristName(response.body().getFullName());
                                    adapter.notifyDataSetChanged();
                                } else {
                                    // getUserById succeeded but name empty, or returned 4xx
                                    enrichFromMessages(chat, myUserId);
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                                if (isAdded()) enrichFromMessages(chat, myUserId);
                            }
                        });
            } else {
                // touristId not in response → get name from chat messages
                enrichFromMessages(chat, myUserId);
            }
        }
    }

    /**
     * Loads messages for the given chat room and extracts the tourist's name
     * from the first message whose senderId differs from the current guide's userId.
     */
    private void enrichFromMessages(ChatRoom chat, String myUserId) {
        ApiClient.getInstance(requireContext()).getApiService()
                .getChatMessages(chat.getId())
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ChatMessage>> call,
                                           @NonNull Response<List<ChatMessage>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            for (ChatMessage msg : response.body()) {
                                String senderId = msg.getSenderId();
                                String senderName = msg.getSenderName();
                                if (senderId != null && !senderId.equals(myUserId)
                                        && senderName != null && !senderName.isEmpty()) {
                                    chat.setTouristName(senderName);
                                    if (chat.getTouristId() == null || chat.getTouristId().isEmpty()) {
                                        chat.setTouristId(senderId);
                                    }
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<ChatMessage>> call, @NonNull Throwable t) {}
                });
    }

    private void openChat(ChatRoom chat) {
        String displayName = adapter.getOtherPartyName(chat);
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CHAT_ID, chat.getId());
        intent.putExtra(Constants.EXTRA_CHAT_NAME, displayName);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disconnectSignalR();
        binding = null;
    }
}
