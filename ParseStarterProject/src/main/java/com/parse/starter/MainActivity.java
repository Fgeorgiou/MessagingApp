/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {

    Boolean loginModeActive = false;

    public void redirectIfLoggedIn(){

        if(ParseUser.getCurrentUser() != null){

            Intent intent = new Intent(MainActivity.this, FriendListActivity.class);
            startActivity(intent);

        }
    }

    public void toggleLoginMode(View view){

        Button loginSignupButton = (Button) findViewById(R.id.loginSignupButton);

        TextView toggleLoginModeTextView = (TextView) findViewById(R.id.toggleLoginModeTextView);

        if(loginModeActive){

            loginModeActive = false;
            loginSignupButton.setText("Sign Up");
            toggleLoginModeTextView.setText("or Login");

        }else{

            loginModeActive = true;
            loginSignupButton.setText("Log In");
            toggleLoginModeTextView.setText("or Sign up");

        }
    }

    public void signupLogin(View view){

        EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        if(loginModeActive) {

            ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {

                    if(e == null){

                        redirectIfLoggedIn();

                    } else {

                        String message = e.getMessage();

                        if(message.toLowerCase().contains("java")) {

                            message = e.getMessage().substring(e.getMessage().indexOf(" "));

                        }

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    }
                }
            });

        } else {

            ParseUser user = new ParseUser();

            user.setUsername(usernameEditText.getText().toString());
            user.setPassword(passwordEditText.getText().toString());

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {

                    if (e == null) {

                        redirectIfLoggedIn();

                    } else {

                        String message = e.getMessage();

                        if(message.toLowerCase().contains("java")) {

                            message = e.getMessage().substring(e.getMessage().indexOf(" "));

                        }

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    }

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("MessagingApp Login");

        redirectIfLoggedIn();

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }
}