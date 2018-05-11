package com.parse.starter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageGalleryActivity extends AppCompatActivity {

    String activeFriend;
    LinearLayout linearLayout;

    MessageAdapter imageMessageAdapter;
    ArrayList<Message> imageMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        Intent intent = getIntent();

        activeFriend = intent.getStringExtra("username");

        setTitle("Shared Images w/ " + activeFriend);

        ListView imageListView = (ListView) findViewById(R.id.imageListView);

        imageMessageAdapter = new MessageAdapter(this, imageMessages);

        imageListView.setAdapter(imageMessageAdapter);

        getImageLog();
    }

    public void getImageLog() {
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Image");

        query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recipient", activeFriend);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Image");

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

                        imageMessages.clear();

                        for (ParseObject object : objects) {

                            ParseFile file = (ParseFile) object.get("image");

                            file.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {
                                    if(e == null && data != null){

                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                        imageMessages.add(new Message(bitmap, ParseUser.getCurrentUser().getUsername(), activeFriend));

                                        imageMessageAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}
