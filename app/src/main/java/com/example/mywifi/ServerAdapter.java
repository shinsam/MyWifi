package com.example.mywifi;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.InetAddress;
import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.ViewHolder> {

    private List<InetAddress> serverList;
    private OnItemLongClickListener onItemLongClickListener;

    public ServerAdapter(List<InetAddress> serverList) {
        this.serverList = serverList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.server_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InetAddress serverAddress = serverList.get(position);
        holder.serverAddressTextView.setText(serverAddress.toString());
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(v, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return serverList.size();
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serverAddressTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serverAddressTextView = itemView.findViewById(R.id.server_address);
        }
    }
}