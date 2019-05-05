package com.example.draftapplication;
import java.util.Date;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private String messageUserID;

    public ChatMessage(String messageText, String messageUser, String messageUserID) {
        if (messageText.equals("")){
            this.messageText = "bez textu";
        }
        else {
            this.messageText = messageText;
        }

        this.messageUser = messageUser;

        this.messageUserID = messageUserID;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(){
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {

        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageUserID() {
        return messageUserID;
    }

    public void setMessageUserID(String messageUserID) {
        this.messageUserID = messageUserID;
    }
}
