package com.solidparts.warehouse;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class MessageManager {
    public void show(Context context, String message, boolean goBack) {
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();
    }
}
