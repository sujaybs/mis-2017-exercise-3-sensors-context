package com.example.sujaybshalawadi.mis3;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MusicRetriever {
    private ContentResolver mContentResolver;
    private List<Item> mItems = new ArrayList<>();

    public MusicRetriever(ContentResolver cr) {
        mContentResolver = cr;
    }

    public void prepare() {
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
    }

    public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    public Item getItem(int i) {
        if (mItems.isEmpty() || mItems.size() < i)
            return null;

        return mItems.get(i);
    }

    public static class Item {
        long id;
        String artist;
        String title;
        String album;
        long duration;

        public Item(long id, String artist, String title, String album, long duration) {
            this.id = id;
            this.artist = artist;
            this.title = title;
            this.album = album;
            this.duration = duration;
        }

        public long getId() {
            return id;
        }

        public String getArtist() {
            return artist;
        }

        public String getTitle() {
            return title;
        }

        public String getAlbum() {
            return album;
        }

        public long getDuration() {
            return duration;
        }

        public Uri getURI() {
            return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
    }
}