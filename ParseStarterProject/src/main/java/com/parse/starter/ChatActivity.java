package com.parse.starter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    final int MEDIA_STORAGE_IMAGE_CODE = 1;
    final int CAMERA_USAGE_CODE = 2;

    String activeFriend;

    ArrayList<Message> messages = new ArrayList<>();

    MessageAdapter messageAdapter;

    public void sendChat(View view){

        final EditText chatEditText = (EditText) findViewById(R.id.chatEditText);

        ParseObject message = new ParseObject("Message");

        final String messageContent = chatEditText.getText().toString();

        if(!messageContent.equals("")) {

            if (messageContent.contains("https")) {

                message.put("message", "My current location: " + messageContent.substring(messageContent.indexOf("h")));

            } else {

                message.put("message", messageContent);

            }

            message.put("sender", ParseUser.getCurrentUser().getUsername());
            message.put("recipient", activeFriend);

            chatEditText.setText("");

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            message.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    if (e == null) {

                        messages.add(new Message(messageContent, ParseUser.getCurrentUser().getUsername(), activeFriend));

                        messageAdapter.notifyDataSetChanged();

                    }
                }
            });
        }
    }

    public void searchImageInStorage(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, MEDIA_STORAGE_IMAGE_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                searchImageInStorage();

            }
        }
    }

    public void redirectActivity(View view){

        if(view.equals(findViewById(R.id.chatLocationButton))) {
            Intent intent = new Intent(ChatActivity.this, PinpointLocationActivity.class);
            startActivity(intent);
        }else if(view.equals(findViewById(R.id.chatImagesButton))) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                }
            }

            searchImageInStorage();

        }else if(view.equals(findViewById(R.id.chatCameraButton))) {
            Intent intent = new Intent(ChatActivity.this, PinpointLocationActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != RESULT_CANCELED) {
            if (requestCode == MEDIA_STORAGE_IMAGE_CODE && resultCode == RESULT_OK && data != null) {

                Uri selectedImage = data.getData();

                try {

                    final Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    byte[] byteArray = stream.toByteArray();

                    ParseFile file = new ParseFile("image.png", byteArray);

                    ParseObject message = new ParseObject("Message");

                    message.put("image", file);
                    message.put("sender", ParseUser.getCurrentUser().getUsername());
                    message.put("recipient", activeFriend);

                    message.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null) {

                                messages.add(new Message(bitmap, ParseUser.getCurrentUser().getUsername(), activeFriend));

                                messageAdapter.notifyDataSetChanged();

                            }
                        }
                    });

                } catch (IOException e) {

                    e.printStackTrace();

                }

            } else if (requestCode == CAMERA_USAGE_CODE && resultCode == RESULT_OK && data != null) {

                //code...

            }
        }
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

                handler.postDelayed(this, 3000);
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

                            final String messageSender;
                            final String messageRecipient;

                            messageSender = message.getString("sender");
                            messageRecipient = message.getString("recipient");

                            if(message.getString("image") == null) {

                                String messageContentString = message.getString("message");

                                messages.add(new Message(messageContentString, messageSender, messageRecipient));

                            } else {

                                ParseFile parseFile = (ParseFile) message.get("image");

                                parseFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] data, ParseException e) {

                                        if(e == null && data != null){

                                            Bitmap messageContentBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                            Log.i("InfoZOR", messageContentBitmap.toString());

                                            messages.add(new Message(messageContentBitmap, messageSender, messageRecipient));

                                        }
                                    }
                                });
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
