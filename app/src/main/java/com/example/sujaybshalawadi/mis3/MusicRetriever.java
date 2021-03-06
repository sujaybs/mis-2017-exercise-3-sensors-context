package com.example.sujaybshalawadi.mis3;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

class MusicRetriever {
    private ContentResolver mContentResolver;
    private List<Item> mItems = new ArrayList<>();

    MusicRetriever(ContentResolver cr) {
        mContentResolver = cr;
    }

    void prepare() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
        if (cur == null) {
            Log.e(getClass().getName(), "Failed to retrieve music: cursor is null :-(");
            return;
        }

        if (!cur.moveToFirst()) {
            Log.e(getClass().getName(), "Failed to move cursor to first row (no query results).");
            return;
        }

        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);

        mItems.add(new Item(
                cur.getLong(idColumn),
                cur.getString(artistColumn),
                cur.getString(titleColumn),
                cur.getString(albumColumn),
                cur.getLong(durationColumn)));

        while (cur.moveToNext()) {
            mItems.add(new Item(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getLong(durationColumn)));
        }

        cur.close();
    }

    ArrayList<Item> getItems() {
        return (ArrayList<Item>) mItems;
    }

    static class Item {
        long id;
        String artist;
        String title;
        String album;
        long duration;

        Item(long id, String artist, String title, String album, long duration) {
            this.id = id;
            this.artist = artist;
            this.title = title;
            this.album = album;
            this.duration = duration;
        }

        Uri getURI() {
            return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
    }
}