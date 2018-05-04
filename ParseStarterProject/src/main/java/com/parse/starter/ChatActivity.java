package com.parse.starter;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    String activeFriend;

    ArrayList<Message> messages = new ArrayList<>();

    MessageAdapter messageAdapter;

    public void sendChat(View view){

        final EditText chatEditText = (EditText) findViewById(R.id.chatEditText);

        ParseObject message = new ParseObject("Message");

        final String messageContent = chatEditText.getText().toString();

        message.put("sender", ParseUser.getCurrentUser().getUsername());
        message.put("recipient", activeFriend);
        message.put("message", messageContent);

        chatEditText.setText("");

        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if(e == null){

                    messages.add(new Message(messageContent, ParseUser.getCurrentUser().getUsername(), activeFriend));

                    messageAdapter.notifyDataSetChanged();

                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        activeFriend = intent.getStringExtra("username");

        setTitle("Message History w/ " + activeFriend);

        ListView chatListView = (ListView) findViewById(R.id.chatListView);

        messageAdapter = new MessageAdapter(this, messages);

        chatListView.setAdapter(messageAdapter);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                getMessageLog();

                handler.postDelayed(this, 2000);
            }
        }, 1500);
    }

    public void getMessageLog() {
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Message");

        query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recipient", activeFriend);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Message");

        query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("sender", activeFriend);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);

        query.orderByAscending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(e == null){

                    if(objects.size() > 0){

                        messages.clear();

                        for(ParseObject message : objects){

                            String messageContent = message.getString("message");
                            String messageSender = message.getString("sender");
                            String messageRecipient = message.getString("recipient");


                            messages.add(new Message(messageContent, messageSender, messageRecipient));
                        }

                        messageAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
