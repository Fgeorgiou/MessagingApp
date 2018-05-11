package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    List<Message> messages;
    Context context;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        ImageMessageViewHolder imageHolder = new ImageMessageViewHolder();
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);


        if(message.getText() == null) { // if the message is an image message then inflate for image

            if (message.getSender().equals(ParseUser.getCurrentUser().getUsername())) { // this message was sent by us so let's create a basic chat bubble on the right

                convertView = messageInflater.inflate(R.layout.item_message_sent_image, null);

                imageHolder.messageImage = (ImageView) convertView.findViewById(R.id.message_image_sent);
                imageHolder.messageImage.setImageBitmap(message.getImage());

            } else { // this message was sent by someone else so let's create an advanced chat bubble on the left

                convertView = messageInflater.inflate(R.layout.item_message_received_image, null);

                imageHolder.messageImage = (ImageView) convertView.findViewById(R.id.message_image_received);
                imageHolder.messageImage.setImageBitmap(message.getImage());

            }

        } else if (message.getImage() == null){ //else inflate as a normal message

            if (message.getSender().equals(ParseUser.getCurrentUser().getUsername())) { // this message was sent by us so let's create a basic chat bubble on the right

                convertView = messageInflater.inflate(R.layout.item_message_sent, null);

                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.messageBody.setText(message.getText());

            } else { // this message was sent by someone else so let's create an advanced chat bubble on the left

                convertView = messageInflater.inflate(R.layout.item_message_received, null);

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.name.setText(message.getSender());

                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.messageBody.setText(message.getText());

            }
        }

        return convertView;

    }
}

class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
}

class ImageMessageViewHolder {
    public TextView name;
    public ImageView messageImage;
}