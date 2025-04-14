package com.ryvk.taskflow;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User user = User.getSPUser(this);
        EditText nameField = findViewById(R.id.editTextText5);
        EditText mobileField = findViewById(R.id.editTextText8);
        EditText emailField = findViewById(R.id.editTextText7);
        if (user != null) {
            nameField.setText(user.getName());
            mobileField.setText(user.getEmail());
            emailField.setText(user.getMobile());
        }

        Button updateButton = findViewById(R.id.button4);
        updateButton.setOnClickListener(v -> {
            String name = nameField.getText().toString();
            String email = mobileField.getText().toString();
            String mobile = emailField.getText().toString();

            boolean errorExists = false;

            if(name.isBlank()){
                nameField.setError("Name is required");
                errorExists = true;
            }else if(!Validation.isLettersWithSpacesOnly(name)){
                nameField.setError("Name should contain only letters!");
                Log.d("values", "onCreate: "+nameField.getText().toString());
                errorExists = true;
            }
            if(mobile.isBlank()){
                mobileField.setError("Mobile is required");
                errorExists = true;
            }else if(!Validation.isInteger(mobile)){
                mobileField.setError("Mobile must be an integer");
            }
            if(email.isBlank()){
                emailField.setError("Email is required");
                errorExists = true;
            } else if(!Validation.isEmailValid(email)){
                emailField.setError("Email is invalid");
                errorExists = true;
            }

            if(!errorExists){
                AlertUtils.showLoader(ProfileActivity.this);

                user.setName(name);
                user.setMobile(mobile);
                user.setEmail(email);

                Gson gson = new Gson();

                String MOCK_URL = getResources().getString(R.string.mock_api_url);

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(gson.toJson(user), MediaType.get("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(MOCK_URL + "/users/"+user.getId())
                        .patch(requestBody)
                        .build();

                new Thread(()->{
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            System.out.println("Request Failed: " + e.getMessage());
                            runOnUiThread(AlertUtils::hideLoader);
                            runOnUiThread(()->AlertUtils.showAlert(ProfileActivity.this,"Error","Request Failed."));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(AlertUtils::hideLoader);
                            if (response.isSuccessful()) {
                                user.updateSPUser(ProfileActivity.this,user);
                                runOnUiThread(()->{
                                    Toast.makeText(ProfileActivity.this,"Profile Updated.",Toast.LENGTH_SHORT).show();
                                    Utils.hideKeyboard(ProfileActivity.this);
                                });
                            } else {
                                System.out.println("Error: " + response.code());
                                runOnUiThread(()->AlertUtils.showAlert(ProfileActivity.this,"Error","Request Failed."));
                            }
                        }
                    });
                }).start();
            }

        });

    }
}