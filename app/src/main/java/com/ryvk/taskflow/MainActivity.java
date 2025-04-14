package com.ryvk.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http2.ErrorCode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Utils.setDarkMode(Utils.isDarkModeEnabled(MainActivity.this));

        EditText emailField = findViewById(R.id.editTextText);
        EditText passwordField = findViewById(R.id.editTextText2);

        Button loginButton = findViewById(R.id.button);
        loginButton.setOnClickListener(v -> {

            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            boolean errorExists = false;

            if(emailField.getText().toString().isBlank()){
                emailField.setError("Email is required");
                errorExists = true;
            } else if(!Validation.isEmailValid(emailField.getText().toString())){
                emailField.setError("Email is invalid");
                errorExists = true;
            }

            if(passwordField.getText().toString().isBlank()){
                passwordField.setError("Password is required");
                errorExists = true;
            }

            if(!errorExists){
                AlertUtils.showLoader(MainActivity.this);

                String MOCK_URL = getResources().getString(R.string.mock_api_url);

                OkHttpClient client = new OkHttpClient();

                // Create request
                Request request = new Request.Builder()
                        .url(MOCK_URL + "/users?email="+email+"&password="+password)
                        .addHeader("Content-Type","application/json")
                        .build();

                new Thread(()->{
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(AlertUtils::hideLoader);
                            e.printStackTrace();
                            System.out.println("Request Failed: " + e.getMessage());
                            runOnUiThread(()->AlertUtils.showAlert(MainActivity.this,"Error","Request Failed!."));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(AlertUtils::hideLoader);
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                Type userListType = new TypeToken<List<User>>(){}.getType();

                                String responseText = response.body().string();
                                List<User> users = gson.fromJson(responseText, userListType);
                                Log.d("HomeActivity", "onResponse: "+responseText);

                                User authenticatedUser = findUserByCredentials(users, email, password);

                                if (authenticatedUser != null) {
                                    runOnUiThread(()->{
                                        Intent i = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(i);
                                        finish();
                                        authenticatedUser.updateSPUser(MainActivity.this,authenticatedUser);
                                    });
                                } else {
                                    runOnUiThread(()->AlertUtils.showAlert(MainActivity.this,"Error","Invalid login credentials."));
                                }

                            } else {
                                System.out.println("Error: " + response.code());
                                if(response.code() == 404){
                                    runOnUiThread(()->AlertUtils.showAlert(MainActivity.this,"Error","Invalid login credentials."));
                                }
                            }
                        }
                    });
                }).start();
            }
        });

        Button signupButton = findViewById(R.id.button3);
        signupButton.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(i);
            finish();
        });

        checkLoggedInUser();

    }

    private void checkLoggedInUser(){
        User user = User.getSPUser(MainActivity.this);
        if(user != null){
            Intent i = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }
    }

    private User findUserByCredentials(List<User> users, String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) &&
                    user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}