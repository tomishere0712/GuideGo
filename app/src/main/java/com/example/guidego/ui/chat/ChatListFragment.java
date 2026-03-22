package com.example.guidego.ui.chat;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.guidego.model.ChatRoom;
import com.example.guidego.ui.chat.ChatActivity;
import com.example.guidego.utils.Constants;
import com.example.guidego.utils.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;
    private ChatListAdapter adapter;
    private TokenManager tokenManager;

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

        adapter = new ChatListAdapter(tokenManager.getUserId());
        adapter.setListener(chat -> openChat(chat));
        binding.rvChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChats.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats();
    }

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
                            adapter.setChats(response.body());
                            binding.rvChats.setVisibility(View.VISIBLE);
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

    private void openChat(ChatRoom chat) {
        // App chỉ dành cho Tourist → tên hiển thị luôn là hướng dẫn viên
        String displayName = (chat.getGuideName() != null && !chat.getGuideName().isEmpty())
                ? chat.getGuideName()
                : "Hướng dẫn viên";

        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(Constants.EXTRA_CHAT_ID, chat.getId());
        intent.putExtra(Constants.EXTRA_CHAT_NAME, displayName);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


