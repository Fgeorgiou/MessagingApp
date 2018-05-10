package com.parse.starter;

import android.graphics.Bitmap;

import com.parse.ParseUser;

public class Message {

    private String text; // message body
    private Bitmap image; // message image
    private String messageSender; // message sender
    private String messageRecipient; // message recipient

    public Message(String text, String messageSender, String messageRecipient) {
        this.text = text;
        this.messageSender = messageSender;
        this.messageRecipient = messageRecipient;
    }

    public Message(Bitmap image, String messageSender, String messageRecipient) {
        this.image = image;
        this.messageSender = messageSender;
        this.messageRecipient = messageRecipient;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return messageSender;
    }

    public String getRecipient() {
        return messageRecipient;
    }

    public Bitmap getImage() {
        return image;
    }

    public boolean isBelongsToCurrentUser() {

        if(messageSender.equals(ParseUser.getCurrentUser().getUsername())) {
            return true;
        }

        return false;
    }
}
