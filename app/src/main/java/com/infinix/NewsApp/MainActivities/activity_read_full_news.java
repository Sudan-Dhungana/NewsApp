package com.infinix.NewsApp.MainActivities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.infinix.NewsApp.Comment;
import com.infinix.NewsApp.CommentAdapter;
import com.infinix.NewsApp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class activity_read_full_news extends AppCompatActivity {
    WebView webView;
    DatabaseReference database = FirebaseDatabase.getInstance(
            "https://techtrends-849a2-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).getReference().child("comments");
    private String url;
    private EditText commentEditText;
    private CommentAdapter commentAdapter;
    private List<Comment> comments;
    private String newsCardId;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_full_news);

        newsCardId = getIntent().getStringExtra("publishedAt");

        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("desc");
        String content = getIntent().getStringExtra("content");
        String imageURL = getIntent().getStringExtra("image");
        url = getIntent().getStringExtra("url");

        TextView txtTitle = findViewById(R.id.txtForFullNews);
        TextView txtSubTitle = findViewById(R.id.txtForFullNewsSubTitle);
        TextView txtContent = findViewById(R.id.txtContent);

        ImageView imgFullNews = findViewById(R.id.imgForFullNews);

        Button btnFullNews = findViewById(R.id.btnReadFullNews);

        txtTitle.setText(title);
        txtSubTitle.setText(desc);
        txtContent.setText(content);

        Picasso.get().load(imageURL).into(imgFullNews);
        btnFullNews.setOnClickListener(view -> {
            // I n tent intent = new Intent(Intent.ACTION_VIEW);
            // int ent.setData(Uri.parse(url));
            //startActivity(intent);
            // get a reference to the webView
            webView = findViewById(R.id.webView);

            // set a WebViewClient for the webView
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    // this method is called when the webView has finished loading the URL
                    // hide the button
                    btnFullNews.setVisibility(View.INVISIBLE);
                }
            });

            // load the URL into the webView
            webView.loadUrl(url);

            // change the button text to Loading
            String txtLoading = "Loading";
            btnFullNews.setText(txtLoading);
        });


        //MaterialCardView commentCard = findViewById(R.id.comment_card);
        commentEditText = findViewById(R.id.comment_edit_text);
        RecyclerView commentRecyclerView = findViewById(R.id.comment_recycler_view);

        comments = new ArrayList<>(); // You can also populate the list from a data source
        commentAdapter = new CommentAdapter(comments);
        commentRecyclerView.setAdapter(commentAdapter);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String text = commentEditText.getText().toString();
                if (!text.isEmpty()) {
                    // You can also get the user name from a data source
                    SharedPreferences sharedPreferences = getSharedPreferences
                            ("user_session", MODE_PRIVATE);
                    String username = sharedPreferences.getString("username", null);
                    Comment comment = new Comment(text, "@" + username);
                    saveComment(comment);
                    commentAdapter.notifyDataSetChanged();
                    commentEditText.setText("");
                }
                return true;
            }
            return false;
        });

    }

    public void saveComment(Comment comment) {
        DatabaseReference commentsRef = database.child(newsCardId);
        String commentKey = commentsRef.push().getKey(); // Generate a unique key for each comment
        Toast.makeText(this, "Comment key: " + commentKey, Toast.LENGTH_SHORT).show();
        if (commentKey != null) {
            commentsRef.child(commentKey).setValue(comment) // Save the comment object under the key
                    .addOnCompleteListener(task -> {
                        // Handle success
                        comments.clear();
                        loadComments();

                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                    });
        }
    }


    public void loadComments() {
        // Clear the existing comments list
        comments.clear();

        DatabaseReference newsCardCommentsRef = database.child(newsCardId);
        // Query the comments node under the news card ID to get only the relevant comments
        ValueEventListener valueEventListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    // Get the comment object from the snapshot
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        // Add it to your list of comments
                        comments.add(comment);
                    }
                }
                // Notify your adapter that the data has changed
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

            // ...
        };

        // Attach the listener to the newsCardCommentsRef reference
        newsCardCommentsRef.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load comments when the activity is resumed
        loadComments();
    }
}
