package com.remitty.caronz.FCM;


import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.remitty.caronz.Notification.Config;
import com.remitty.caronz.R;
import com.remitty.caronz.messages.ChatActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "MyFirebaseMsgService";

    NotificationManager notificationManager;

    @Override
    public void onNewToken(String s) {
        Log.d("fcm token", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM Body: ", remoteMessage.getData().toString());
        Log.d("FCM1 Body: ", remoteMessage.getNotification().getBody().toString());
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        if(notification != null)
            sendNotification(notification.getBody());

        if(remoteMessage.getData() != null) {
            Intent intent1 = new Intent();
            try {
                intent1.setAction(Config.PUSH_NOTIFICATION);
                JSONObject response = new JSONObject(remoteMessage.getData().toString());
                JSONObject data = response.getJSONObject("data");

                intent1.putExtra("kind", data.optString("kind"));
                intent1.putExtra("img", data.optString("img"));
                intent1.putExtra("sender", data.optString("sender"));
                intent1.putExtra("channel", data.optString("channel"));
                intent1.putExtra("name", data.optString("name"));
                intent1.putExtra("text", data.optString("text"));
                sendBroadcast(intent1);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        if (remoteMessage.getData() != null) {
//            utils.print(TAG, "From: " + remoteMessage.getFrom());
//            utils.print(TAG, "Notification Message Body: " + remoteMessage.getData());
//            //Calling method to generate notification
//            sendNotification(remoteMessage.getData().get("message"));
//        }else{
//            utils.print(TAG,"FCM Notification failed");
//        }
    }

    //This method is only generating push notification
    private void sendNotification(String messageBody) {
        if(messageBody == null) {
            return;
        }
        if (messageBody.equals("") || messageBody.isEmpty()) {
            return;
        }
//        if (!isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
//            utils.print(TAG,"foreground");

        createNotificationChannel();

            Intent intent = new Intent(this, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("push", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setOngoing(true)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent);

            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder), 1);



//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            notificationManager.notify(0, notificationBuilder.build());

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(111, notificationBuilder.build());
//        startForeground(111, notificationBuilder.build());
//        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name ="channel_name";// getString(R.string.channel_name);
            String description = "descriptoin";//getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String CHANNEL_ID = getPackageName();
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            Log.d("channel", channel.toString());
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            notificationBuilder.setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary));
//            return R.drawable.notification_white;
//        }else {
//            return R.mipmap.ic_launcher;
//        }
            return R.mipmap.ic_launcher;
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}

