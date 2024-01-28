package com.infinix.NewsApp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    private final TextView commentTextView;
    private final TextView userNameTextView;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        commentTextView = itemView.findViewById(R.id.comment_text_view);
        userNameTextView = itemView.findViewById(R.id.user_name_text_view);
    }

    public void bind(Comment comment) {
        commentTextView.setText(comment.getText());
        userNameTextView.setText(comment.getUserName());
    }
}
