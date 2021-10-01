package com.musiclover.newmusic.ui.page.librarypage.genre;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.musiclover.newmusic.loader.medialoader.GenreLoader;
import com.musiclover.newmusic.model.Genre;
import com.musiclover.newmusic.model.Song;
import com.musiclover.newmusic.ui.widget.bubblepicker.model.PickerItem;
import com.musiclover.newmusic.ui.widget.bubblepicker.rendering.PickerAdapter;
import com.musiclover.newmusic.util.MusicUtil;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class GenrePickerAdapter extends PickerAdapter<Genre> {

    public GenrePickerAdapter(Context context) {
        super(context);
    }

    @Override
    public boolean onBindItem(PickerItem item, boolean create, int i) {
        super.onBindItem(item,create,i);
        Genre genre = mData.get(i);
        item.setTitle(genre.name);
        item.setRadiusUnit(genre.songCount);
        // Glide
        ArrayList<Song> songs = GenreLoader.getSongs(mContext,genre.id);
        Glide.with(mContext).load(MusicUtil.getMediaStoreAlbumCoverUri(songs.get(0).albumId)).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                item.setBackgroundImage(resource);
                GenrePickerAdapter.this.notifyBackImageUpdated(i);
            }
        });

        return true;
    }
}
