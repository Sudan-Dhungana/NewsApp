package com.infinix.NewsApp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.infinix.NewsApp.MainActivities.activity_read_full_news;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsRVAdapter extends RecyclerView.Adapter<NewsRVAdapter.ViewHolder> {
    private final ArrayList<Articles> articlesArrayList;
    private final Context context;

    public NewsRVAdapter(ArrayList<Articles> articlesArrayList, Context context) {
        this.articlesArrayList = articlesArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_rv_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsRVAdapter.ViewHolder holder, int position) {
        Articles articles = articlesArrayList.get(position);
        holder.subTitleTV.setText(articles.getDescription());
        holder.titleTV.setText(articles.getTitle());
        holder.publishedTV.setText(R.string.published_at);
        // The input date string in yyyy-MM-dd'T'HH:mm:ss'Z' format
        String date_s = articles.getPublishedAt();

        // The input date format
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        // Parse the input date string into a Date object
        Date date;
        try {
            date = inputFormat.parse(date_s);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // The output date format, for nepali use - Locale.forLanguageTag("ne-NP")
        SimpleDateFormat outputFormat = new SimpleDateFormat
                ("EEEE, MMMM d, yyyy 'at' h:mma", Locale.US);

        // Format the Date object into a new date string
        assert date != null;
        String outputDate = outputFormat.format(date);
        holder.publishedTV.append(" " + outputDate + " ");

        // for image
        Picasso.get().load(articles.getUrlToImage()).placeholder(R.drawable.placeholder_image).into(holder.newsIV);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, activity_read_full_news.class);
            intent.putExtra("title", articles.getTitle());
            intent.putExtra("content", articles.getContent());
            intent.putExtra("desc", articles.getDescription());
            intent.putExtra("image", articles.getUrlToImage());
            intent.putExtra("url", articles.getUrl());
            intent.putExtra("publishedAt", articles.getPublishedAt());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articlesArrayList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTV;
        private final TextView subTitleTV;
        private final ImageView newsIV;
        private final TextView publishedTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.idTVNewsHeading);
            subTitleTV = itemView.findViewById(R.id.idTVSubTitle);
            newsIV = itemView.findViewById(R.id.idIVNews);
            publishedTV = itemView.findViewById(R.id.idPublishedAt);
        }
    }
}
