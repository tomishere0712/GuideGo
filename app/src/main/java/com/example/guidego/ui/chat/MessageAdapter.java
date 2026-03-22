package com.example.guidego.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidego.R;
import com.example.guidego.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<ChatMessage> messages = new ArrayList<>();
    private final String currentUserId;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        return currentUserId.equals(msg.getSenderId()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).tvContent.setText(msg.getContent());
        } else {
            ReceivedViewHolder rh = (ReceivedViewHolder) holder;
            rh.tvContent.setText(msg.getContent());
            if (msg.getSenderName() != null) {
                rh.tvSenderName.setText(msg.getSenderName());
            }
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        SentViewHolder(@NonNull View v) {
            super(v);
            tvContent = v.findViewById(R.id.tv_message_content);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvSenderName;
        ReceivedViewHolder(@NonNull View v) {
            super(v);
            tvContent = v.findViewById(R.id.tv_message_content);
            tvSenderName = v.findViewById(R.id.tv_sender_name);
        }
    }
}

