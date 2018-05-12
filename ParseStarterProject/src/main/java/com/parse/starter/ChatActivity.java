package com.parse.starter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    static final int MEDIA_STORAGE_IMAGE_CODE = 1;
    static final int CAMERA_USAGE_CODE = 2;

    String activeFriend;

    ArrayList<Message> messages = new ArrayList<>();
    MessageAdapter messageAdapter;

    //camera utility variables
    private Bitmap cameraImageBitmap;
    private String mCurrentPhotoPath;

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

    public void startCameraActivity(){

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("Error", "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, CAMERA_USAGE_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MEDIA_STORAGE_IMAGE_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                searchImageInStorage();

            }

        } else if (requestCode ==  CAMERA_USAGE_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                startCameraActivity();

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

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MEDIA_STORAGE_IMAGE_CODE);

                }
            }

            searchImageInStorage();

        }else if(view.equals(findViewById(R.id.chatCameraButton))) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_USAGE_CODE);

                }
            }

            startCameraActivity();

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

                    ParseObject imageMessage = new ParseObject("Image");

                    imageMessage.put("image", file);
                    imageMessage.put("sender", ParseUser.getCurrentUser().getUsername());
                    imageMessage.put("recipient", activeFriend);

                    imageMessage.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null) {

                                ParseObject autoResponse = new ParseObject("Message");

                                final String autoResponseMessage = ParseUser.getCurrentUser().getUsername() + " has uploaded an image! Press and hold his/her name in your friend list to see it!";

                                autoResponse.put("message", autoResponseMessage);
                                autoResponse.put("sender", ParseUser.getCurrentUser().getUsername());
                                autoResponse.put("recipient", activeFriend);

                                autoResponse.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        messages.add(new Message(autoResponseMessage, ParseUser.getCurrentUser().getUsername(), activeFriend));

                                        messageAdapter.notifyDataSetChanged();

                                    }
                                });
                            }
                        }
                    });

                } catch (IOException e) {

                    e.printStackTrace();

                }

            } else if (requestCode == CAMERA_USAGE_CODE && resultCode == RESULT_OK && data != null) {

                if (requestCode == CAMERA_USAGE_CODE && resultCode == RESULT_OK) {
                    try {

                        cameraImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        cameraImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                        byte[] byteArray = stream.toByteArray();

                        ParseFile file = new ParseFile("image.png", byteArray);

                        ParseObject imageMessage = new ParseObject("Image");

                        imageMessage.put("image", file);
                        imageMessage.put("sender", ParseUser.getCurrentUser().getUsername());
                        imageMessage.put("recipient", activeFriend);

                        imageMessage.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                if (e == null) {

                                    ParseObject autoResponse = new ParseObject("Message");

                                    final String autoResponseMessage = ParseUser.getCurrentUser().getUsername() + " has uploaded a photo! Press and hold his/her name in your friend list to see it!";

                                    autoResponse.put("message", autoResponseMessage);
                                    autoResponse.put("sender", ParseUser.getCurrentUser().getUsername());
                                    autoResponse.put("recipient", activeFriend);

                                    autoResponse.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            messages.add(new Message(autoResponseMessage, ParseUser.getCurrentUser().getUsername(), activeFriend));

                                            messageAdapter.notifyDataSetChanged();

                                        }
                                    });
                                }
                            }
                        });

                    } catch (IOException e) {

                        e.printStackTrace();

                    }
                }

            }
        }
    }

    //helper function to create the image file taken from camera, credits: https://stackoverflow.com/users/4034572/albert-vila-calvo
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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

                if (ParseUser.getCurrentUser() != null) {
                    getMessageLog();
                }

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

                            String messageSender = message.getString("sender");
                            String messageRecipient = message.getString("recipient");
                            String messageContentString = message.getString("message");

                            messages.add(new Message(messageContentString, messageSender, messageRecipient));

                        }

                        messageAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
