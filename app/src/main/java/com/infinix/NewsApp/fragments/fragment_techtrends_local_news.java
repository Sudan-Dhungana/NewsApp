package com.infinix.NewsApp.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.infinix.NewsApp.R;
import com.infinix.NewsApp.WebAppInterface;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_techtrends_local_news#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_techtrends_local_news extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    // Create a variable to store the file chooser result
    private ValueCallback<Uri[]> mUploadMessage;

    public fragment_techtrends_local_news() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_techtrends_local_news.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_techtrends_local_news newInstance(String param1, String param2) {
        fragment_techtrends_local_news fragment = new fragment_techtrends_local_news();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_techtrends_local_news, container, false);

        SharedPreferences sharedPreferences =
                requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String ip_address = getActivity().getString(R.string.ip_address).trim();
        String session_username = sharedPreferences.getString("username", null);
        Toast.makeText(getContext(), "Session Username: " + session_username, Toast.LENGTH_LONG).show();

        //String serverURL = ip_address + "NewsApp/techNews.php?session_username=" + session_username;

        WebView webView = rootView.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();

        // Create an instance of ActivityResultLauncher with a callback function
        ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Check if the result is OK
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Get the selected file URI from the data intent
                        Intent data = result.getData();
                        Uri fileUri = data == null ? null : data.getData();

                        // Pass the selected file URI to the file chooser callback
                        mUploadMessage.onReceiveValue(new Uri[]{fileUri});
                        mUploadMessage = null;
                    }
                });

        // Override the onShowFileChooser method to handle the file chooser request
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // Check if there is an existing file chooser result
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }

                // Store the new file chooser result
                mUploadMessage = filePathCallback;

                // Create and launch the intent for the file chooser result
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/image");
                startForResult.launch(intent);

                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {


            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Get a reference to the root layout
                androidx.constraintlayout.widget.ConstraintLayout layout = rootView.findViewById(R.id.display);

                // Create a progress bar
                ProgressBar progressBar = new ProgressBar(rootView.getContext(), null, android.R.attr.progressBarStyleHorizontal);
                progressBar.setIndeterminate(false);
                progressBar.setMax(100);
                progressBar.setProgress(50);
                progressBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));

                // Add the progress bar to the layout
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                layout.addView(progressBar, params);

                // Show the progress bar
                progressBar.setVisibility(View.VISIBLE);

                // Hide the progress bar
                progressBar.setVisibility(View.GONE);

                // Update the progress value
                progressBar.setProgress(75);
            }
        });

        //String ip_address = getString(R.string.ip_address).trim();
        String url = ip_address + "NewsApp/index.php";

        // this method is called when the webView has finished loading the URL
        webSettings.setJavaScriptEnabled(true);
        webSettings.getAllowContentAccess();
        webSettings.getAllowFileAccess();
        webSettings.getAllowFileAccessFromFileURLs();
        webSettings.setDatabaseEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // Attach the JavaScript interface to the WebView
        webView.addJavascriptInterface(new WebAppInterface(getContext()), "Android");
        webView.loadUrl(url);
        // Inflate the layout for this fragment
        return rootView;
    }
}