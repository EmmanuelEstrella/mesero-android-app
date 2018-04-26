package com.example.emmanuel.myapplication.Logic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.emmanuel.myapplication.MainActivity;
import com.example.emmanuel.myapplication.NotificationHelper;
import com.example.emmanuel.myapplication.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by Emmanuel on 19/04/2018.
 */

public class FireBaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
        String Tag = "message";
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(Tag, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(Tag, "Message data payload: " + remoteMessage.getData());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(Tag, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        NotificationHelper helper = new NotificationHelper(this);

        String orderId = remoteMessage.getData().get("order_id");
        String robotId = remoteMessage.getData().get("robot_id");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("order", true);
        intent.putExtra("order_id", orderId);
        intent.putExtra("robot_id", robotId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT );

        helper.getManager().notify(100, helper.getNotification("¡Buen Provecho!", "Su orden ha sido entregada.", pendingIntent).build());

        if(MainActivity.getInstance() != null && MainActivity.getInstance().isActivityVisible()){
            MainActivity.getInstance().displayOrderReceived( orderId, robotId);
        }




        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }




}
