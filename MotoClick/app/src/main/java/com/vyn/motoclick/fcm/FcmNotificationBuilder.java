package com.vyn.motoclick.fcm;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;



/**
 * Created by Yurka on 15.06.2017.
 */

public class FcmNotificationBuilder {
    final String LOG_TAG = "myLogs";
 //   public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SERVER_API_KEY = "AAAAyFdIRL8:APA91bH00ocsJs0Gy60rt1RirjrG9f9I5RKoGne4MMSONK0vJaKJvqdEPYtHEgRafxQ64agfks3BuQwrgb2SsdbryfZwO22Q-NRMyY0vfszbBsAzw2eWZQ52-1JI3cXkxBa12Vy1HDvC";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_KEY = "key=" + SERVER_API_KEY;
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    // json related keys
    private static final String KEY_TO = "to";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_DATA = "data";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private String mTitle;
    private String mMessage;
    private String mUsername;
    private String mUid;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public FcmNotificationBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public FcmNotificationBuilder username(String username) {
        mUsername = username;
        return this;
    }

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
        return this;
    }

    public FcmNotificationBuilder firebaseToken(String firebaseToken) {
        mFirebaseToken = firebaseToken;
        return this;
    }

    public FcmNotificationBuilder receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFirebaseToken = receiverFirebaseToken;
        return this;
    }

    public void send() {
     /*   RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_JSON, getValidJsonBody().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "FCM " + requestBody.toString());
        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTH_KEY)
                .url(FCM_URL)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });*/
    }

    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject jsonObjectBody = new JSONObject();
        jsonObjectBody.put(KEY_TO, mReceiverFirebaseToken);
        JSONObject jsonObjectData = new JSONObject();
        jsonObjectData.put(KEY_TITLE, mTitle);
        jsonObjectData.put(KEY_TEXT, mMessage);
        jsonObjectData.put(KEY_USERNAME, mUsername);
        jsonObjectData.put(KEY_UID, mUid);
        jsonObjectData.put(KEY_FCM_TOKEN, mFirebaseToken);
        jsonObjectBody.put(KEY_DATA, jsonObjectData);

        return jsonObjectBody;
    }
}