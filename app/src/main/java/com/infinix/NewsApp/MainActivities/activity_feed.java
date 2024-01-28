package com.infinix.NewsApp.MainActivities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.infinix.NewsApp.Articles;
import com.infinix.NewsApp.CategoryRVAdapter;
import com.infinix.NewsApp.CategoryRVModel;
import com.infinix.NewsApp.NewsModel;
import com.infinix.NewsApp.NewsNotificationService.NotificationBroadcastReceiver;
import com.infinix.NewsApp.NewsRVAdapter;
import com.infinix.NewsApp.R;
import com.infinix.NewsApp.RetrofitAPI;
import com.infinix.NewsApp.UserLoginRegister.UserLoginActivity;
import com.infinix.NewsApp.fragments.fragment_news_feed;
import com.infinix.NewsApp.fragments.fragment_techtrends_local_news;
import com.infinix.NewsApp.fragments.fragment_userprofile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class activity_feed extends AppCompatActivity implements CategoryRVAdapter.CategoryClickInterface {
    FloatingActionButton btnFloatReadBtn;
    private ProgressBar loadingBar;
    private ArrayList<Articles> articlesArrayList;
    private ArrayList<CategoryRVModel> catRVModelArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private NewsRVAdapter newsRvAdapter;
    private TextToSpeech tts;
    private boolean isSpeaking = false;
    private String selectedCountry = "us"; // default value

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        scheduleDailyNotification(); // notification for tech trends feed

        SharedPreferences sharedPreferences = getSharedPreferences
                ("user_session", MODE_PRIVATE);
        String session_id = sharedPreferences.getString("session_id", null);

        // check session id if no session goto intent login
        // check if the session id is not null
        if (session_id != null) {
            Toast.makeText(this, "Session Established! " + session_id, Toast.LENGTH_SHORT).show();
        } else {
            // the session id is null, redirect to login activity
            Intent intent = new Intent(activity_feed.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        }


        RecyclerView newsRV = findViewById(R.id.idRVNews);
        RecyclerView categoryRV = findViewById(R.id.idRVCategories);
        loadingBar = findViewById(R.id.loadingNewsBP);
        btnFloatReadBtn = findViewById(R.id.btnFloatReadBtn);

        tts = new TextToSpeech(this, this::onInit);

        articlesArrayList = new ArrayList<>();

        // create an array adapter and pass the required parameter - recently added for country selection code
        // in our case pass the context, drop down layout , and array.
        // get reference to the string array that we just created - added code recently
        String[] country_code = getResources().getStringArray(R.array.Choose_Country);
        String[] country_names = getResources().getStringArray(R.array.Country_Names);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, country_names);
        // get reference to the autocomplete text view
        AutoCompleteTextView autocompleteTV = findViewById(R.id.autoCompleteTextView);
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter);

        autocompleteTV.setOnItemClickListener((parent, view, position, id) -> {
            // update the selected country from the parent adapter
            //selectedCountry = parent.getItemAtPosition(position).toString();
            selectedCountry = country_code[position];
            // call getNews with the current category to refresh the news for the selected country
            getNews(catRVModelArrayList.get(position).getCategory());
        });


        btnFloatReadBtn.setOnClickListener(v -> {
            // Check the state of the text-to-speech engine
            if (isSpeaking) {
                // If it is speaking, stop it and change the state
                tts.stop();
                btnFloatReadBtn.setImageResource(R.drawable.baseline_play);
                isSpeaking = false;
            } else {
                // If it is not speaking, speak the text and change the state
                StringBuilder textToSpeak = new StringBuilder();
                for (Articles article : articlesArrayList) {
                    textToSpeak.append(article.getTitle()).append(". ").append(article.getDescription()).append(". ");
                }
                // Call the speakLongText () method instead of tts.speak ()
                speakLongText(textToSpeak.toString());
                // Change the image drawable to baseline_pause
                btnFloatReadBtn.setImageResource(R.drawable.baseline_pause);
                isSpeaking = true;
            }
        });

        articlesArrayList = new ArrayList<>();
        catRVModelArrayList = new ArrayList<>();
        newsRvAdapter = new NewsRVAdapter(articlesArrayList, this);
        categoryRVAdapter = new CategoryRVAdapter(catRVModelArrayList, this);

        newsRV.setLayoutManager(new LinearLayoutManager(this));
        newsRV.setAdapter(newsRvAdapter);

        categoryRV.setAdapter(categoryRVAdapter);

        getNews("All");
        getCategories();
        newsRvAdapter.notifyDataSetChanged();

        // for bottom navigation -anonymous inner class instead of implementing the interface
        BottomNavigationView bottomNavigationView = findViewById(R.id.btnNav);
        loadFragment(new fragment_news_feed());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // handle item selection here
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.idNewsFeed) {
                fragment = new fragment_news_feed();
                btnFloatReadBtn.setVisibility(View.VISIBLE);
            } else if (itemId == R.id.idTechTrendsLocal) {
                fragment = new fragment_techtrends_local_news();
                btnFloatReadBtn.setVisibility(View.INVISIBLE);
            } else if (itemId == R.id.profile) {
                fragment = new fragment_userprofile();
                btnFloatReadBtn.setVisibility(View.INVISIBLE);
            }
            if (fragment != null) {
                loadFragment(fragment);
            }
            return true;
        });
    }

    void loadFragment(Fragment fragment) {
        //to attach fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout, fragment).commit();
    }

    // A method to split and speak a long text
    private void speakLongText(String text) {
        // Check if the text is empty
        if (text == null || text.isEmpty()) {
            return;
        }
        // Get the maximum speech input length
        int maxLength = TextToSpeech.getMaxSpeechInputLength();
        // Check if the text is longer than the limit
        if (text.length() > maxLength) {
            // Split the text into two parts
            String firstPart = text.substring(0, maxLength);
            String secondPart = text.substring(maxLength);
            // Speak the first part with a unique utterance ID
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.5f); // Set the volume to 50%
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, -1f); // Set the panning to left only

            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longText");
            tts.speak(firstPart, TextToSpeech.QUEUE_FLUSH, params, "longText");
            // Set a progress listener to speak the second part
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Do nothing
                }

                @Override
                public void onDone(String utteranceId) {
                    // Check if the utterance ID matches
                    if (utteranceId.equals("longText")) {
                        // Speak the second part recursively
                        speakLongText(secondPart);
                        btnFloatReadBtn.setImageResource(R.drawable.baseline_play);

                    }
                }

                @Override
                public void onError(String utteranceId) {
                    // Do nothing
                    Toast.makeText(activity_feed.this, "Error: tts failed!" + utteranceId, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Speak the text normally
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "longText");
        }
    }

    private void onInit(int status) {
        // Check if TextToSpeech is successfully initialized
        if (status == TextToSpeech.SUCCESS) {
            // Set the language of the text-to-speech engine
            int result = tts.setLanguage(Locale.UK);
            // Check if the language is supported
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language Not Supported", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Don't forget to shutdown TextToSpeech!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getCategories() {
        catRVModelArrayList.add(new CategoryRVModel("All",
                "https://plus.unsplash.com/premium_photo-1675543245580-a9cad4a0e298?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MXx8RXZlcnl0aGluZ3xlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60"));
        catRVModelArrayList.add(new CategoryRVModel("Technology",
                "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80"));
        catRVModelArrayList.add(new CategoryRVModel("Science",
                "https://images.unsplash.com/photo-1614508568879-876f2cde2237?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTR8fHNjaWVuY2UlMjBhbmQlMjB0ZWNobm9sb2d5fGVufDB8fDB8fHww&auto=format&fit=crop&w=500&q=60"));
        catRVModelArrayList.add(new CategoryRVModel("Sports",
                "https://images.unsplash.com/photo-1592592878585-abcaaaaf7cd1?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTd8fFNwb3J0c3xlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60"));
        catRVModelArrayList.add(new CategoryRVModel("General",
                "https://images.unsplash.com/photo-1515096788709-a3cf4ce0a4a6?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxjb2xsZWN0aW9uLXBhZ2V8MjB8MTU2Nzc3Mnx8ZW58MHx8fHx8&auto=format&fit=crop&w=500&q=60"));
        catRVModelArrayList.add(new CategoryRVModel("Business",
                "https://images.unsplash.com/photo-1664575599730-0814817939de?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OXx8QnVzaW5lc3N8ZW58MHx8MHx8fDA%3D&auto=format&fit=crop&w=500&q=60"));
        catRVModelArrayList.add(new CategoryRVModel("Entertainment",
                "https://images.unsplash.com/photo-1567593810070-7a3d471af022?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8ZW50ZXJ0YWlubWVudHxlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60"));
        catRVModelArrayList.add(new CategoryRVModel("Health",
                "https://images.unsplash.com/photo-1494390248081-4e521a5940db?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjZ8fEhlYWx0aHxlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60"));
        categoryRVAdapter.notifyDataSetChanged();
    }

    private void getNews(String category) {
        loadingBar.setVisibility(View.VISIBLE);
        articlesArrayList.clear();
        String myAPI = "273b99c0ee0f49f3bb5be57089621136";

        String categoryURL = "https://newsapi.org/v2/top-headlines?country=" + selectedCountry + "&category=" +
                category + "&apikey=" + myAPI;
        String url = "https://newsapi.org/v2/top-headlines?country=" +
                selectedCountry + "&excludeDomains=stackoverflow.com&sortBy=publishedAt&language=en&apikey=" + myAPI;

        String baseURL = "https://newsapi.org/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<NewsModel> callRequest;
        if (category.equals("All")) {
            callRequest = retrofitAPI.getAllNews(url);
        } else {
            callRequest = retrofitAPI.getNewsByCategory(categoryURL);
        }
        callRequest.enqueue(new Callback<NewsModel>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<NewsModel> call, @NonNull Response<NewsModel> response) {
                NewsModel newsModel = response.body();
                loadingBar.setVisibility(View.GONE);
                assert newsModel != null;
                ArrayList<Articles> articles = newsModel.getArticles();
                for (int i = 0; i < articles.size(); i++) {
                    articlesArrayList.add(new Articles(
                            articles.get(i).getTitle(),
                            articles.get(i).getDescription(),
                            articles.get(i).getUrlToImage(),
                            articles.get(i).getUrl(),
                            articles.get(i).getContent(),
                            articles.get(i).getPublishedAt()));
                }
                newsRvAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<NewsModel> call, @NonNull Throwable t) {
                if (t instanceof HttpException) {
                    // The server returned an HTTP error code
                    HttpException httpException = (HttpException) t;
                    if (httpException.code() == 429) {
                        // The api limit has been reached
                        try {
                            // Get the error data as a JSON string
                            String errorJson = Objects.requireNonNull(
                                    Objects.requireNonNull(httpException.response()).
                                            errorBody()).string();
                            // Parse the JSON string as a JSON object
                            JSONObject jsonObject = new JSONObject(errorJson);
                            // Get the error message and code from the JSON object
                            String errorMessage = jsonObject.getString("message");
                            String errorCode = jsonObject.getString("code");
                            // Display the error message to the user
                            Toast.makeText(activity_feed.this, "[Status]: Failed to get News!" + errorMessage, Toast.LENGTH_LONG).show();
                            Toast.makeText(activity_feed.this, "[Error]: " + errorCode, Toast.LENGTH_SHORT).show();
                        } catch (IOException | JSONException e) {
                            // Something went wrong while reading or parsing the JSON
                            e.printStackTrace();
                            Toast.makeText(activity_feed.this, "[Error]: Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Some other HTTP error occurred
                        Toast.makeText(activity_feed.this, "[Error]: " + httpException.message(), Toast.LENGTH_SHORT).show();
                        Log.e("Retrofit", "Error: " + httpException.message());
                    }
                } else {
                    // Some other network or IO error occurred
                    Toast.makeText(activity_feed.this, "[Error]: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Retrofit", "Error: " + t.getMessage());
                }
            }
        });
    }

    @Override
    public void onCategoryClick(int position) {
        String category = catRVModelArrayList.get(position).getCategory();
        getNews(category);
    }

    private void scheduleDailyNotification() {
        // Schedule the notification to trigger every day at the desired time
        // Here, I'm setting it to trigger at 8:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);

        Intent notificationIntent = new Intent(this, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing or show a message
        Toast.makeText(this, "You cannot go back to the login activity",
                Toast.LENGTH_SHORT).show();
    }
}
