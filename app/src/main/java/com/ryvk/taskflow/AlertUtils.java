package com.ryvk.taskflow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;

public class AlertUtils {
    private static AlertDialog loaderDialog;

    public static void showLoader(Context context) {
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            builder.setView(inflater.inflate(R.layout.dialog_loader, null));
            builder.setCancelable(false);

            if (loaderDialog != null && loaderDialog.isShowing()) {
                loaderDialog.dismiss();
                loaderDialog = null;
            }
            loaderDialog = builder.create();
            loaderDialog.show();
        }
    }

    public static void hideLoader() {
        if (loaderDialog != null && loaderDialog.isShowing()) {
            loaderDialog.dismiss();
            loaderDialog = null;
        }
    }
    public static AlertDialog showAlert(Context context, String title, String message) {
        if(context instanceof Activity){
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
            return alertDialog;
        }
        return null;
    }
    public static void showConfirmDialog(Context context, String title, String message,
                                         DialogInterface.OnClickListener yesListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", yesListener)
                .setNegativeButton("No", null)
                .show();
    }
    public static AlertDialog showExitConfirmationDialog(Context context) {
        if (context instanceof Activity) {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Exit Confirmation")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> ((Activity) context).finish())
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
            return alertDialog;
        }
        return null;
    }
}
