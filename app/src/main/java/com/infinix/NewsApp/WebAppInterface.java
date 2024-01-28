package com.infinix.NewsApp;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    private final Context mContext;

    public WebAppInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void shareLink(String link) {
        // Handle the shared link in your Android app
        // For example, you can show a native share dialog
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link);
        mContext.startActivity(Intent.createChooser(intent, "Share via"));
    }
}