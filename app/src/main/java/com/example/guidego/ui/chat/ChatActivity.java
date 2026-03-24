package com.example.guidego.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.guidego.api.ApiClient;
import com.example.guidego.databinding.ActivityChatBinding;
import com.example.guidego.model.ChatMessage;
import com.example.guidego.model.request.SendMessageRequest;
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

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;
    private TokenManager tokenManager;
    private HubConnection hubConnection;
    private String chatId;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);
        chatId = getIntent().getStringExtra(Constants.EXTRA_CHAT_ID);
        String chatName = getIntent().getStringExtra(Constants.EXTRA_CHAT_NAME);

        if (chatId == null) { finish(); return; }

        binding.tvChatName.setText(chatName != null ? chatName : "Tin nhắn");
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSend.setOnClickListener(v -> sendMessage());

        setupRecyclerView();
        loadMessages();
        connectSignalR();
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(tokenManager.getUserId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getInstance(this).getApiService()
                .getChatMessages(chatId)
                .enqueue(new Callback<List<ChatMessage>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ChatMessage>> call,
                                           @NonNull Response<List<ChatMessage>> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            messageAdapter.setMessages(response.body());
                            scrollToBottom();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ChatMessage>> call, @NonNull Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void connectSignalR() {
        String token = tokenManager.getToken();
        if (token == null) return;

        String hubUrl = ApiClient.getInstance(this).getHubUrl();

        hubConnection = HubConnectionBuilder.create(hubUrl)
                .withAccessTokenProvider(Single.just(token))
                .build();

        // Lắng nghe tin nhắn mới từ server
        hubConnection.on("ReceiveMessage", message -> {
            if (chatId.equals(message.getChatId())) {
                runOnUiThread(() -> {
                    messageAdapter.addMessage(message);
                    scrollToBottom();
                });
            }
        }, ChatMessage.class);

        // Kết nối → sau khi connected thì join phòng chat (send() là void, gọi trực tiếp)
        disposables.add(
                hubConnection.start()
                        .doOnComplete(() -> {
                            hubConnection.send("JoinChat", chatId);
                            Log.d(TAG, "Joined chat room: " + chatId);
                        })
                        .subscribe(
                                () -> Log.d(TAG, "SignalR connected"),
                                error -> {
                                    Log.e(TAG, "SignalR connect failed", error);
                                    runOnUiThread(() ->
                                            Toast.makeText(this, "Không thể kết nối real-time",
                                                    Toast.LENGTH_SHORT).show()
                                    );
                                }
                        )
        );
    }

    private void sendMessage() {
        String content = binding.etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        binding.btnSend.setEnabled(false);
        binding.etMessage.setText("");

        ApiClient.getInstance(this).getApiService()
                .sendMessage(new SendMessageRequest(chatId, content))
                .enqueue(new Callback<ChatMessage>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatMessage> call,
                                           @NonNull Response<ChatMessage> response) {
                        binding.btnSend.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            // Thêm trực tiếp nếu SignalR không echo về sender
                            messageAdapter.addMessage(response.body());
                            scrollToBottom();
                        } else {
                            Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn",
                                    Toast.LENGTH_SHORT).show();
                            binding.etMessage.setText(content);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatMessage> call, @NonNull Throwable t) {
                        binding.btnSend.setEnabled(true);
                        Toast.makeText(ChatActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                        binding.etMessage.setText(content);
                    }
                });
    }

    private void scrollToBottom() {
        int count = messageAdapter.getItemCount();
        if (count > 0) binding.rvMessages.smoothScrollToPosition(count - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
        if (hubConnection != null) {
            if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
                hubConnection.send("LeaveChat", chatId); // void — fire and forget
            }
            hubConnection.stop().subscribe(() -> {}, e -> Log.e(TAG, "Stop error", e));
        }
    }
}
