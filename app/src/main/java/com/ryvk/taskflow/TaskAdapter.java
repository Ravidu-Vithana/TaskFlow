package com.ryvk.taskflow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> taskList;
    private Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_card, parent, false);
        return new TaskAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());

        String todayDate = Validation.todayDate();
        String tomorrowDate = Validation.tomorrowDate();

        if(todayDate.equals(task.getDate())){
            task.setDate("Today");
        }else if(tomorrowDate.equals(task.getDate())){
            task.setDate("Tomorrow");
        }

        holder.dateTextView.setText(task.getDate());

        if(task.getPriority() == Task.LOW_PRIORITY){
            holder.priority1.setVisibility(View.VISIBLE);
        }else if(task.getPriority() == Task.MEDIUM_PRIORITY){
            holder.priority1.setVisibility(View.VISIBLE);
            holder.priority2.setVisibility(View.VISIBLE);
        }else if(task.getPriority() == Task.HIGH_PRIORITY){
            holder.priority1.setVisibility(View.VISIBLE);
            holder.priority2.setVisibility(View.VISIBLE);
            holder.priority3.setVisibility(View.VISIBLE);
        }

        String description = task.getDescription() != null && !task.getDescription().isBlank() ? task.getDescription() : "No description available.";

        holder.viewMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtils.showAlert(context,task.getTitle(),description);
            }
        });

        holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(task.getId() != null){

                        task.setCompleted(true);

                        AlertUtils.showLoader(context);

                        Gson gson = new Gson();

                        String MOCK_URL = context.getResources().getString(R.string.mock_api_url);

                        OkHttpClient client = new OkHttpClient();

                        RequestBody requestBody = RequestBody.create(gson.toJson(task), MediaType.get("application/json; charset=utf-8"));

                        Request request = new Request.Builder()
                                .url(MOCK_URL + "/tasks/"+task.getId())
                                .patch(requestBody)
                                .build();

                        new Thread(()->{
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                    System.out.println("Request Failed: " + e.getMessage());
                                    ((Activity) context).runOnUiThread(AlertUtils::hideLoader);
                                    ((Activity) context).runOnUiThread(()->AlertUtils.showAlert(context,"Error","Request Failed."));
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    ((Activity) context).runOnUiThread(AlertUtils::hideLoader);
                                    if (response.isSuccessful()) {
                                        ((Activity) context).runOnUiThread(()->{
                                            taskList.remove(task);
                                            notifyDataSetChanged();
                                            if(taskList == null || taskList.isEmpty()){
                                                RecyclerView recyclerView = ((Activity) context).findViewById(R.id.recyclerView);
                                                TextView noTasksFound = ((Activity) context).findViewById(R.id.noTasksFound);
                                                ((Activity) context).runOnUiThread(()->{
                                                    recyclerView.setVisibility(View.INVISIBLE);
                                                    noTasksFound.setVisibility(View.VISIBLE);
                                                });
                                            }
                                            Toast.makeText(context,"Task Completed.",Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        System.out.println("Error: " + response.code());
                                        ((Activity) context).runOnUiThread(()->AlertUtils.showAlert(context,"Error","Request Failed."));
                                    }
                                }
                            });
                        }).start();

                    }else {
                        AlertUtils.showAlert(context,"Error","Error occured. Please try again later.");
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        Button viewMoreButton;
        ImageView priority1;
        ImageView priority2;
        ImageView priority3;
        RadioButton radioButton;

        public ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.textView8);
            dateTextView = view.findViewById(R.id.textView9);
            viewMoreButton = view.findViewById(R.id.button2);
            priority1 = view.findViewById(R.id.priority1);
            priority2 = view.findViewById(R.id.priority2);
            priority3 = view.findViewById(R.id.priority3);
            radioButton = view.findViewById(R.id.radioButton);
        }
    }
}
