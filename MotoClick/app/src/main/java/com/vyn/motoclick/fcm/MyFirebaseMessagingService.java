package com.vyn.motoclick.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.vyn.motoclick.R;
import com.vyn.motoclick.activities.ChatActivity;
import com.vyn.motoclick.activities.MapsActivity;
import com.vyn.motoclick.events.PushNotificationEvent;
import com.vyn.motoclick.utils.Constants;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Yurka on 15.06.2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    final String LOG_TAG = "myLogs";
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //     Log.d(TAG, "From: " + remoteMessage.getFrom());



     //   Log.d(LOG_TAG, "onMessageReceived   "+remoteMessage.getNotification().getBody() );

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(LOG_TAG, "if   " );
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("text");
            String username = remoteMessage.getData().get("username");
            String uid = remoteMessage.getData().get("uid");
            String fcmToken = remoteMessage.getData().get("fcm_token");

            // Don't show notification if chat activity is open.
            if (!MapsActivity.isChatActivityOpen()) {
                sendNotification(title,
                        message,
                        username,
                        uid,
                        fcmToken);
            } else {
                EventBus.getDefault().post(new PushNotificationEvent(title,
                        message,
                        username,
                        uid,
                        fcmToken));
            }
        }
        else if(!remoteMessage.getNotification().getBody().isEmpty()){
            Log.d(LOG_TAG, "else   " );
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */

    private void sendNotification(String title,
                                  String message,
                                  String receiver,
                                  String receiverUid,
                                  String firebaseToken) {

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrate = new long[] { 0, 300, 200, 300, 200, 300 };

        Log.d(LOG_TAG, "sendNotification title "+title );
        Log.d(LOG_TAG, "sendNotification  receiver "+receiver );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(receiver)
                .setContentText(message)
                .setNumber(4)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(vibrate)
                .setLights(Color.BLUE,1, 0)
                .setDefaults(Notification.DEFAULT_LIGHTS)
              //  .setNumber(1)
                .setContentIntent(pendingIntent);


        notificationManager.notify(101, notificationBuilder.build());
    }
}