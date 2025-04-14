package com.ryvk.taskflow;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ryvk.taskflow.databinding.ActivityHomeBinding;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppBarConfiguration appBarConfiguration;
    private ActivityHomeBinding binding;
    private List<Task> taskList = new ArrayList<>();
    private EditText searchField;
    private TextView noTasksFound;
    private boolean pauseDataFetch = false;
    private Thread dataFetchingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageButton.setOnClickListener(this::showMenuPopup);
        recyclerView = binding.recyclerView;

        noTasksFound = findViewById(R.id.noTasksFound);

        searchField = findViewById(R.id.editTextText5);
        ImageButton searchButton = findViewById(R.id.imageButton2);
        searchButton.setOnClickListener(v->{
            Utils.hideKeyboard(searchField);
            fetchTasks();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HomeActivity.this, NewTaskActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        User user = User.getSPUser(HomeActivity.this);
        if(user == null){
            Intent i = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }else if(!pauseDataFetch && (dataFetchingThread == null || !dataFetchingThread.isAlive())){
            Log.d("TAG", "onResume: fetching..................");
            fetchTasks();
            pauseDataFetch = true;
        }
    }

    private void fetchTasks(){

        AlertUtils.showLoader(HomeActivity.this);

        User user = User.getSPUser(HomeActivity.this);

        String MOCK_URL = getResources().getString(R.string.mock_api_url);

        OkHttpClient client = new OkHttpClient();

        String searchQuery = "";

        if(searchField.getText().toString() != null && !searchField.getText().toString().isBlank()){
            searchQuery = "&title="+searchField.getText().toString();
        }

        // Create request
        Request request = new Request.Builder()
                .url(MOCK_URL + "/tasks?user_email="+user.getEmail()+"&completed=false&sortBy=date&order=asc"+searchQuery)
                .build();

        dataFetchingThread = new Thread(()->{
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    pauseDataFetch = true;
                    e.printStackTrace();
                    runOnUiThread(AlertUtils::hideLoader);
                    runOnUiThread(()->AlertUtils.showAlert(HomeActivity.this,"Error","Request Failed."));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    pauseDataFetch = true;
                    AlertUtils.hideLoader();
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Task>>(){}.getType();
                        taskList = gson.fromJson(response.body().string(), taskListType);
                        runOnUiThread(()->updateUI());
                    } else {
                        if(response.code() == 404){
                            runOnUiThread(()->{
                                recyclerView.setVisibility(View.INVISIBLE);
                                noTasksFound.setVisibility(View.VISIBLE);
                                pauseDataFetch = false;
                            });
                        }else{
                            runOnUiThread(()->AlertUtils.showAlert(HomeActivity.this,"Error","Request Failed."));
                        }
                    }
                }
            });
        });
        dataFetchingThread.start();

    }

    private void updateUI(){
        TaskAdapter adapter = new TaskAdapter(taskList, HomeActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        noTasksFound.setVisibility(View.INVISIBLE);
        AlertUtils.hideLoader();
        pauseDataFetch = false;
    }

    public void showMenuPopup(View view) {
        PopupMenu popup = new PopupMenu(HomeActivity.this, view);

        String profile = "Profile";
        String settings = "Settings";

        popup.getMenu().add(profile);
        popup.getMenu().add(settings);

        popup.setOnMenuItemClickListener(item -> {
            if(item.getTitle().equals(profile)){
                Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(i);
            }else if(item.getTitle().equals(settings)){
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(i);
            }else{
                AlertUtils.showAlert(HomeActivity.this,"Error","Error occurred. Please try again.");
            }

            return true;
        });

        popup.show();
    }

}