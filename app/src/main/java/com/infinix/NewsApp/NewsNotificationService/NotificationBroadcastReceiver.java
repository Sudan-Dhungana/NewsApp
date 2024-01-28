package com.infinix.NewsApp.NewsNotificationService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.infinix.NewsApp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Fetch recommended news using Volley and PHP API
        String ip_address = context.getString(R.string.ip_address);
        String url = ip_address + "NewsApp/fetch_recommended_news.php";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // Parse the JSON response to get the recommended news data
                    try {
                        int post_id = response.getInt("post_id");
                        String title = response.getString("title");
                        String image = response.getString("image");
                        String content = response.getString("content");

                        // Create and display the notification with the recommended news
                        showNotification(context, title, image, content, post_id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error
                    Log.e("NotificationBroadcastReceiver", "Error fetching recommended news: " + error.getMessage());
                });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    private void showNotification(Context context, String title, String image, String content, int post_id) {
        // Generate a unique notification ID using the current timestamp
        int notificationId = (int) System.currentTimeMillis();
        // Implement the notification creation and display here using NotificationManager and NotificationCompat.Builder
        // You can follow the Android documentation for creating notifications: https://developer.android.com/training/notify-user/build-notification
        String channelId = "news_recommendation_channel";
        String channelName = "News Recommendation";
        // For demonstration purposes, I'm showing a simple notification here.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Set the title and content of the notification
        String notificationTitle = "Recommended News";
        String notificationContent = "Check out the latest news recommendations!";

        // Use Picasso to download and set the image as the large icon
        Picasso.get().load(context.getString(R.string.ip_address) +
                "NewsApp/admin/" + image).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // When the image is downloaded successfully, create the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.nav_techtrends_article)
                        .setLargeIcon(bitmap)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(defaultSoundUri);

                // Create an Intent to open the URL of the news_details.php page with the post_id parameter
                String newsDetailsUrl = context.getString(R.string.ip_address)
                        + "NewsApp/news_details.php?id=" + post_id;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsDetailsUrl));

                // Create a PendingIntent to open the URL when the notification is clicked
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                notificationManager.notify(notificationId, builder.build());
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.placeholder_image)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationContent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Set the notification priority to high
                        .setAutoCancel(true);

                // Show the notification without the large icon
                notificationManager.notify(notificationId, builder.build());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }
}
