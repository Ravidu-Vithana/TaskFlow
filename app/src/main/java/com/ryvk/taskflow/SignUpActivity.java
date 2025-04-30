package com.ryvk.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText nameField = findViewById(R.id.editTextText3);
        EditText mobileField = findViewById(R.id.editTextText4);
        EditText emailField = findViewById(R.id.editTextText);
        EditText passwordField = findViewById(R.id.editTextText2);
        EditText conPasswordField = findViewById(R.id.editTextText6);

        Button signupButton = findViewById(R.id.button);
        signupButton.setOnClickListener(v -> {

            boolean errorExists = false;

            if(nameField.getText().toString().isBlank()){
                nameField.setError("Name is required");
                errorExists = true;
            }else if(!Validation.isLettersWithSpacesOnly(nameField.getText().toString())){
                nameField.setError("Name should contain only letters!");
                Log.d("values", "onCreate: "+nameField.getText().toString());
                errorExists = true;
            }
            if(mobileField.getText().toString().isBlank()){
                mobileField.setError("Mobile is required");
                errorExists = true;
            }else if(!Validation.isInteger(mobileField.getText().toString())){
                mobileField.setError("Mobile must be an integer");
                errorExists = true;
            }else if(mobileField.getText().toString().length() != 10){
                mobileField.setError("Mobile must have 10 digits");
                errorExists = true;
            }
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
            if(conPasswordField.getText().toString().isBlank()){
                conPasswordField.setError("Confirm Password is required");
                errorExists = true;
            }
            if(!passwordField.getText().toString().equals(conPasswordField.getText().toString())){
                passwordField.setError("Passwords do not match");
                conPasswordField.setError("Passwords do not match");
                errorExists = true;
            }

            if(!errorExists){

                AlertUtils.showLoader(SignUpActivity.this);

                User user = new User();
                user.setName(nameField.getText().toString());
                user.setMobile(mobileField.getText().toString());
                user.setEmail(emailField.getText().toString());
                user.setPassword(passwordField.getText().toString());

                Gson gson = new Gson();

                String MOCK_URL = getResources().getString(R.string.mock_api_url);

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(gson.toJson(user), MediaType.get("application/json; charset=utf-8"));

                // Create request
                Request request = new Request.Builder()
                        .url(MOCK_URL + "/users")
                        .post(requestBody)
                        .build();

                new Thread(()->{
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            System.out.println("Request Failed: " + e.getMessage());
                            runOnUiThread(AlertUtils::hideLoader);
                            runOnUiThread(()->AlertUtils.showAlert(SignUpActivity.this,"Error","Request Failed."));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(AlertUtils::hideLoader);
                            if (response.isSuccessful()) {
                                Intent i = new Intent(SignUpActivity.this, HomeActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                System.out.println("Error: " + response.code());
                                runOnUiThread(()->AlertUtils.showAlert(SignUpActivity.this,"Error","Request Failed."));
                            }
                        }
                    });
                }).start();
            }
        });

        Button loginButton = findViewById(R.id.button3);
        loginButton.setOnClickListener(v -> {
            Intent i = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

    }
}