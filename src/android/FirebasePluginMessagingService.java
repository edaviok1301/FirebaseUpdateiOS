package org.apache.cordova.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.app.Notification;
import android.text.TextUtils;
import android.content.ContentResolver;
import android.graphics.Color;
import android.media.AudioAttributes;
import me.leolin.shortcutbadger.ShortcutBadger;
import amazonia.iu.com.amlibrary.client.IUApp;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;
import java.util.Random;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.entel.movil.R;
import co.acoustic.mobile.push.sdk.api.fcm.FcmApi;
import co.acoustic.mobile.push.sdk.api.MceSdk;

public class FirebasePluginMessagingService extends FirebaseMessagingService {

  private static final String TAG = "FirebasePlugin";
  protected static final String KEY = "badge";

  /**
   * Get a string from resources without importing the .R package
   * 
   * @param name Resource Name
   * @return Resource
   */
  private String getStringResource(String name) {
    return this.getString(
    this.getResources().getIdentifier(
      name, "string", this.getPackageName()));
  }

  public void onNewToken(String s) {
    super.onNewToken(s);
    // BEGIN: Add code to refresh IU Token.
    IUApp.refreshFCMToken(this);
    // END
  }
  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {

    Log.d(TAG, "FirebasePluginMessagingService onMessageReceived called");
    Log.e("DATA",remoteMessage.getData().toString());
    Log.e("IsNotificationAcoustic:", String.valueOf(FcmApi.isFcmMessage(remoteMessage)));


    //Begin Acoustic Notification
    if(FcmApi.isFcmMessage(remoteMessage)){
	if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
        	MceSdk.getNotificationsClient().getNotificationsPreference().setIcon(getApplicationContext(), getApplicationInfo().icon);
        	MceSdk.getNotificationsClient().getNotificationsPreference().setLargeIcon(getApplicationContext(),getApplicationInfo().icon);
        }else{
        	MceSdk.getNotificationsClient().getNotificationsPreference().setIcon(getApplicationContext(), (R.mipmap.ic_launcher));
        	MceSdk.getNotificationsClient().getNotificationsPreference().setLargeIcon(getApplicationContext(),(R.mipmap.ic_launcher));
        }	    
      	FcmApi.handleMceFcmMessage(getApplicationContext(), remoteMessage);
    
    }
    //END
    else{    
     // BEGIN: Enable IU to handle messages targeted for IU
     // Add the below line on 1st line of the method.
     if (IUApp.handleFCMMessage(this, remoteMessage)) {
      return;
      }
    
     // END

    // [START_EXCLUDE]
    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    // [END_EXCLUDE]
    
    

    // Pass the message to the receiver manager so any registered receivers can decide to handle it
    boolean wasHandled = FirebasePluginMessageReceiverManager.onMessageReceived(remoteMessage);
    if (wasHandled) {
      Log.d(TAG, "Message was handled by a registered receiver");

      // Don't process the message in this method.
      return;
    }

    // TODO(developer): Handle FCM messages here.
    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    String title;
    String text;
    String id;
    String sound = null;
    String lights = null;
	  String image = null;
	  String bigIcon = null;
    Map<String, String> data = remoteMessage.getData();
    
    if (remoteMessage.getNotification() != null) {
      title = remoteMessage.getNotification().getTitle();
      text = remoteMessage.getNotification().getBody();
      id = remoteMessage.getMessageId();
    } else {
      title = data.get("title");
      text = data.get("text");
      id = data.get("id");
      sound = data.get("sound");
      lights = data.get("lights"); //String containing hex ARGB color, miliseconds on, miliseconds off, example: '#FFFF00FF,1000,3000'
	  image = data.get("image");
	  bigIcon = data.get("bigIcon");

      if (TextUtils.isEmpty(text)) {
        text = data.get("body");
      }
      if (TextUtils.isEmpty(text)) {
        text = data.get("message");
      }
    }

    if (TextUtils.isEmpty(id)) {
      Random rand = new Random();
      int n = rand.nextInt(50) + 1;
      id = Integer.toString(n);
    }

    String badge = data.get("badge");
   Log.d(TAG, "Hola warrior");
    Log.d(TAG, "From: " + remoteMessage.getFrom());
    Log.d(TAG, "Notification Message id: " + id);
    Log.d(TAG, "Notification Message Title: " + title);
    Log.d(TAG, "Notification Message Body/Text: " + text);
    Log.d(TAG, "Notification Message Sound: " + sound);
    Log.d(TAG, "Notification Message Lights: " + lights);
	Log.d(TAG, "Notification Message Image: " + image);
	Log.d(TAG, "Notification Message BigIcon: " + image);
  Log.d(TAG, "Notification Badge: " + badge);

    if (badge != null && !badge.isEmpty()) {
      setBadgeNumber(badge);
    }

    // TODO: Add option to developer to configure if show notification when app on foreground
    if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title) || (!data.isEmpty())) {
      //boolean showNotification = (FirebasePlugin.inBackground() || !FirebasePlugin.hasNotificationsCallback()) && (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title));
      //Log.d(TAG, "showNotification: " + (showNotification ? "true" : "false"));
      //sendNotification(id, title, text, data, showNotification, sound, lights, image, bigIcon);
	  sendNotification(id, title, text, data, true, sound, lights, image, bigIcon);
    }
  }
  }//END onMessageReceived

  private void sendNotification(String id, String title, String messageBody, Map<String, String> data, boolean showNotification, String sound, String lights, String imageUri, String bigIconUri) {
    Bundle bundle = new Bundle();
    for (String key : data.keySet()) {
      bundle.putString(key, data.get(key));
    }

    if (showNotification) {
      Intent intent = new Intent(this, OnNotificationOpenReceiver.class);
      intent.putExtras(bundle);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

      String channelId = this.getStringResource("default_notification_channel_id");
      String channelName = this.getStringResource("default_notification_channel_name");
      Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	  
	  NotificationCompat.BigPictureStyle notificationStylePicture; 
	  NotificationCompat.BigTextStyle notificationStyleText;
	  
      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
      notificationBuilder
        .setContentTitle(title)
        .setContentText(messageBody)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setAutoCancel(true)
        .setSound(defaultSoundUri)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_MAX);
		
	  
	  if (TextUtils.isEmpty(imageUri)) {
		notificationStyleText = new NotificationCompat.BigTextStyle()
                .bigText(messageBody);
		notificationBuilder.setStyle(notificationStyleText);
      }else{
		notificationStylePicture = new NotificationCompat.BigPictureStyle();
		notificationStylePicture.setSummaryText(messageBody);
		Bitmap bitmapImage = getBitmapfromUrl(imageUri);
		notificationStylePicture.bigPicture(bitmapImage);
		notificationBuilder.setStyle(notificationStylePicture);
	  }

      // Title
      if (TextUtils.isEmpty(title) && android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M) {
        int stringId = getApplicationInfo().labelRes;
        String appName = stringId == 0 ? getApplicationInfo().nonLocalizedLabel.toString() : getString(stringId);
        notificationBuilder.setContentTitle(appName);
      }

      // Icon
      int resID = getResources().getIdentifier("notification_icon", "drawable", getPackageName());
      if (resID != 0) {
        notificationBuilder.setSmallIcon(resID);
      } else {
        if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP) {
          resID = getResources().getIdentifier("icon", "mipmap", getPackageName());
          if (resID != 0) {
            notificationBuilder.setSmallIcon(resID);
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), resID));
          } else {
            notificationBuilder.setSmallIcon(getApplicationInfo().icon);
          }
        } else {
          notificationBuilder.setSmallIcon(getApplicationInfo().icon);
        }
      }
	  //Override LargeIcon
	  if (!TextUtils.isEmpty(bigIconUri)) {
		notificationBuilder.setLargeIcon(getBitmapfromUrl(bigIconUri));
	  }

      // Sound
      if (sound != null) {
        Log.d(TAG, "sound before path is: " + sound);

        Uri soundPath = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
          + "://" + getPackageName() + "/raw/" + sound);

        Log.d(TAG, "Parsed sound is: " + soundPath.toString());

        notificationBuilder.setSound(soundPath);
      } else {
        Log.d(TAG, "Sound was null ");
      }

      int lightArgb = 0;
      if (lights != null) {
        try {
          String[] lightsComponents = lights.replaceAll("\\s", "").split(",");
          if (lightsComponents.length == 3) {
            lightArgb = Color.parseColor(lightsComponents[0]);
            int lightOnMs = Integer.parseInt(lightsComponents[1]);
            int lightOffMs = Integer.parseInt(lightsComponents[2]);
            
            notificationBuilder.setLights(lightArgb, lightOnMs, lightOffMs);
          }
        } catch (Exception e) { }
      }

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        int accentID = getResources().getIdentifier("accent", "color", getPackageName());
        Log.d(TAG, "AccentId: " + Integer.toString(accentID));
        notificationBuilder.setColor(getResources().getColor(accentID, null));
      }

      Notification notification = notificationBuilder.build();

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        int iconID = android.R.id.icon;
        int notiID = getResources().getIdentifier("notification_big", "drawable", getPackageName());
        if (notification.contentView != null) {
          notification.contentView.setImageViewResource(iconID, notiID);
        }
      }

      NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      // Since android Oreo notification channel is needed.
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        List<NotificationChannel> channels = notificationManager.getNotificationChannels();

        boolean channelExists = false;
        for (int i = 0; i < channels.size(); i++) {
          if (channelId.equals(channels.get(i).getId())) {
            channelExists = true;
          }
        }
        
        if (!channelExists) {
          NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
          channel.enableLights(true);
          channel.enableVibration(true);
          channel.setShowBadge(true);
          
          if (lights != null) {
            Log.d(TAG, "lightArgb: " + Integer.toString(lightArgb));
            channel.setLightColor(lightArgb);
          }
          
          if (sound != null) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
            Uri soundPath = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getPackageName() + "/raw/" + sound);
            channel.setSound(soundPath, attributes);
          }
          
          notificationManager.createNotificationChannel(channel);
        }
      }

      notificationManager.notify(id.hashCode(), notification);
    } else {
      bundle.putBoolean("tap", false);
      bundle.putString("title", title);
      bundle.putString("body", messageBody);
      FirebasePlugin.sendNotification(bundle, this.getApplicationContext());
    }
  }

  private void setBadgeNumber(String badge) {
    try {
      int count = 0;
      Context applicationContext = getApplicationContext();

      if (isPositiveInteger(badge)) {
        count = Integer.parseInt(badge);
        applyBadgeCount(applicationContext, count);
      } else {
        if (badge.startsWith("++") || badge.startsWith("--")) {
          int delta = 0;
          int currentBadgeNumber = getCurrentBadgeNumber(applicationContext);
          boolean toIncrement = badge.startsWith("++");
          badge = badge.substring(2);
          if (badge.isEmpty()) {
            delta = 1;
          } else {
            delta = Integer.parseInt(badge);
          }
          count = toIncrement ? currentBadgeNumber + delta : currentBadgeNumber - delta;
          if (count < 0) {
            count = 0;
          }
          applyBadgeCount(applicationContext, count);
        }
      }
    } catch (Exception e) {
      Log.e(TAG, e.getLocalizedMessage(), e);
    }
  }

  private void applyBadgeCount(Context context, int count) {
    Log.d(TAG, "Applying badge count: " + count);
    ShortcutBadger.applyCount(context, count);
    SharedPreferences.Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
    editor.putInt(KEY, count);
    editor.apply();
  }

  private int getCurrentBadgeNumber(Context context) {
    SharedPreferences settings = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
    int currentBadgeNumber = settings.getInt(KEY, 0);
    Log.d(TAG, "Current badge count: " + currentBadgeNumber);
    return currentBadgeNumber;
  }

  // Defaults radix = 10
  private static boolean isPositiveInteger(String s) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (Character.digit(s.charAt(i), 10) < 0) {
        return false;
      }
    }
    return true;
  }
  
	public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
 
        } catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
 
        }
    }
}
