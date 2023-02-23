package com.example.weatherapp;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class RemoteViewsTarget implements Target {
    private RemoteViews remoteViews;
    private int viewId;
    private Context context;
    private int[] appWidgetIds;

    public RemoteViewsTarget(Context context, RemoteViews remoteViews, int viewId, int[] appWidgetIds) {
        this.context = context;
        this.remoteViews = remoteViews;
        this.viewId = viewId;
        this.appWidgetIds = appWidgetIds;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        remoteViews.setImageViewBitmap(viewId, bitmap);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

}
