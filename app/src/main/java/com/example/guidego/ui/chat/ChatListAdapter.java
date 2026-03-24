package com.example.guidego.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.model.ChatRoom;
import com.example.guidego.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatRoom chat);
    }

    private List<ChatRoom> chats = new ArrayList<>();
    private OnChatClickListener listener;
    private final boolean isGuide;  // true → show tourist name; false → show guide name

    public ChatListAdapter(String currentUserRole) {
        this.isGuide = Constants.ROLE_GUIDE.equals(currentUserRole);
    }

    public void setListener(OnChatClickListener listener) { this.listener = listener; }

    public void setChats(List<ChatRoom> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    /** Returns the display name for the other party in this chat */
    public String getOtherPartyName(ChatRoom chat) {
        if (isGuide) {
            // Guide views → show tourist name
            return (chat.getTouristName() != null && !chat.getTouristName().isEmpty())
                    ? chat.getTouristName() : "Du khách";
        } else {
            // Tourist views → show guide name
            return (chat.getGuideName() != null && !chat.getGuideName().isEmpty())
                    ? chat.getGuideName() : "Hướng dẫn viên";
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom chat = chats.get(position);
        String displayName = getOtherPartyName(chat);

        holder.tvName.setText(displayName);
        holder.tvAvatar.setText(String.valueOf(displayName.charAt(0)).toUpperCase());
        holder.tvLastMessage.setText(
                (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty())
                        ? chat.getLastMessage()
                        : "Bắt đầu cuộc trò chuyện...");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(chat);
        });
    }

    @Override
    public int getItemCount() { return chats.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvAvatar;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName        = v.findViewById(R.id.tv_chat_name);
            tvLastMessage = v.findViewById(R.id.tv_last_message);
            tvAvatar      = v.findViewById(R.id.tv_avatar_letter);
        }
    }
}
