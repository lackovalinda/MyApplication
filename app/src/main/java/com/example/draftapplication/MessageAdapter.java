package com.example.draftapplication;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;

public class MessageAdapter extends FirebaseListAdapter<ChatMessage>{

    String myId;

    private ChatMessage activity;

    public MessageAdapter(@NonNull FirebaseListOptions options) {
        super(options);
    }

    protected void populateView(@NonNull View v, ChatMessage model, int position) {
        TextView messageText = (TextView) v.findViewById(R.id.message_text);
        TextView messageUser = (TextView) v.findViewById(R.id.message_user);
        TextView messageTime = (TextView) v.findViewById(R.id.message_time);
        messageText.setText(model.getMessageText());
        messageUser.setText(model.getMessageUser());
        messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ChattingActivity ch = new ChattingActivity();
        ChatMessage chatMessage = (ChatMessage)getItem(position);
        ch.getUserId();

        //if (chatMessage.getMessageUserID().equals("HY95Ptm5hdNy9q3vVNTEOGgyjdk1"))
            view = ch.getLayoutInflater().inflate(R.layout.my_msg, viewGroup, false);
        //else
          //  view = ch.getLayoutInflater().inflate(R.layout.another_msg, viewGroup, false);

        //generating view
        populateView(view, chatMessage, position);

        return view;
    }


    @Override
    public int getViewTypeCount() {
        // return the total number of view types. this value should never change
        // at runtime
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // return a value between 0 and (getViewTypeCount - 1)
        return position % 2;
    }
}
