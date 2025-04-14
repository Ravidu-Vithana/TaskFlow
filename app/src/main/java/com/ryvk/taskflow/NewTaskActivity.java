package com.ryvk.taskflow;

import static java.security.AccessController.getContext;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewTaskActivity extends AppCompatActivity {

    private EditText dueDateField;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User user = User.getSPUser(NewTaskActivity.this);

        EditText titleField = findViewById(R.id.editTextText9);
        EditText descriptionField = findViewById(R.id.editTextText10);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        dueDateField = findViewById(R.id.editTextText11);
        dueDateField.setOnClickListener(v -> showDatePickerDialog());

        Button saveButton = findViewById(R.id.button6);
        saveButton.setOnClickListener(v->{

            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedButton = findViewById(selectedId);

            if(titleField.getText().toString().isEmpty()){
                titleField.setError("Title is required");
            }else if(dueDateField.getText().toString().isEmpty()) {
                dueDateField.setError("Due date is required");
            }else if(selectedId == -1){
                AlertUtils.showAlert(NewTaskActivity.this,"Error","Please select a priority");
            }else{

                AlertUtils.showLoader(NewTaskActivity.this);

                String priorityText = selectedButton.getText().toString();
                int priority = 0;
                if(priorityText.equals("Low")){
                    priority = Task.LOW_PRIORITY;
                }else if(priorityText.equals("Medium")){
                    priority = Task.MEDIUM_PRIORITY;
                }else if(priorityText.equals("High")){
                    priority = Task.HIGH_PRIORITY;
                }

                Task task = new Task(
                        titleField.getText().toString(),
                        selectedDate,
                        descriptionField.getText().toString(),
                        priority,
                        user.getEmail()
                        );

                Gson gson = new Gson();

                String MOCK_URL = getResources().getString(R.string.mock_api_url);

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(gson.toJson(task), MediaType.get("application/json; charset=utf-8"));

                // Create request
                Request request = new Request.Builder()
                        .url(MOCK_URL + "/tasks")
                        .post(requestBody)
                        .build();

                new Thread(()->{
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            System.out.println("Request Failed: " + e.getMessage());
                            runOnUiThread(AlertUtils::hideLoader);
                            runOnUiThread(()->AlertUtils.showAlert(NewTaskActivity.this,"Error","Request Failed."));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(AlertUtils::hideLoader);
                            if (response.isSuccessful()) {
                                finish();
                            } else {
                                System.out.println("Error: " + response.code());
                                runOnUiThread(()->AlertUtils.showAlert(NewTaskActivity.this,"Error","Request Failed."));
                            }
                        }
                    });
                }).start();
            }
        });
    }

    private void showDatePickerDialog() {
        selectedDate = null;
        dueDateField.setText("");

        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                NewTaskActivity.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = String.format("%04d/%02d/%02d", selectedYear, (selectedMonth + 1), selectedDay);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    try {
                        LocalDate d1 = LocalDate.parse(Validation.todayDate(), formatter);
                        LocalDate d2 = LocalDate.parse(selectedDate, formatter);
                        int dateComparison = d1.compareTo(d2);

                        if(dateComparison > 0){
                            AlertUtils.showAlert(NewTaskActivity.this,"Error","Due date cannot be in the past");
                        }else{
                            dueDateField.setText(selectedDate);
                            this.selectedDate = selectedDate;
                        }

                    } catch (DateTimeParseException e) {
                        Log.d("dates", "showDatePickerDialog: "+selectedDate+" ---> "+Validation.todayDate());
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}