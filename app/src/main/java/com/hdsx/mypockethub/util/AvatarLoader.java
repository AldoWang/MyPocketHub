/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hdsx.mypockethub.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.widget.ImageView;

import com.hdsx.mypockethub.R;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.meisolsson.githubsdk.model.User;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Avatar utilities
 */
public class AvatarLoader {

    static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    private static final String TAG = "AvatarLoader";

    private static final float CORNER_RADIUS_IN_DIP = 3;

    private final Context context;
    private final Picasso p;

    private final float cornerRadius;

    private final RoundedCornersTransformation transformation = new RoundedCornersTransformation();

    /**
     * The max size of avatar images, used to rescale images to save memory.
     */
    private static int avatarSize = 0;

    /**
     * Create avatar helper
     *
     * @param context
     */
    public AvatarLoader(final Context context) {
        this.context = context.getApplicationContext();

        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(context.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        p = new Picasso.Builder(context).downloader(new OkHttp3Downloader(client)).build();

        float density = context.getResources().getDisplayMetrics().density;
        cornerRadius = CORNER_RADIUS_IN_DIP * density;

        if (avatarSize == 0) {
            avatarSize = getMaxAvatarSize(context);
        }

        // TODO remove this eventually
        // Delete the old cache
        final File avatarDir = new File(context.getCacheDir(), "avatars/github.com");
        if (avatarDir.isDirectory()) {
            deleteCache(avatarDir);
        }
    }

    public void bind(ActionBar actionBar, User user) {
        bind(actionBar, new AtomicReference<>(user));
    }

    public void bind(final ActionBar actionBar, AtomicReference<User> reference) {
        if (reference == null)
            return;

        User user = reference.get();
        if (user == null)
            return;

        String avatarUrl = user.avatarUrl();
        if (TextUtils.isEmpty(avatarUrl))
            return;

        if (avatarUrl.contains("?") && !avatarUrl.contains("gravatar")) {
            avatarUrl = avatarUrl.substring(0, avatarUrl.indexOf('?'));
        }

        String url = avatarUrl;
        Flowable.just(url)
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(@NonNull String s) throws Exception {
                        return Bitmap.createScaledBitmap(p.load(s).get(), avatarSize, avatarSize, false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(@NonNull Bitmap bitmap) throws Exception {
                        actionBar.setIcon(new BitmapDrawable(context.getResources(), ImageUtils.roundCorners(bitmap, cornerRadius)));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                    }
                });
    }

    /**
     * Bind view to image at URL
     *
     * @param view The ImageView that is to display the user's avatar.
     * @param user A User object that points to the desired user.
     */
    public void bind(final ImageView view, final User user) {
        bind(view, getAvatarUrl(user));
    }

    private void bind(final ImageView view, String url) {
        if (url == null) {
            p.load(R.drawable.spinner_inner).resize(avatarSize, avatarSize).into(view);
            return;
        }

        if (url.contains("?") && !url.contains("gravatar")) {
            url = url.substring(0, url.indexOf("?"));
        }

        p.load(url)
                .placeholder(R.drawable.gravatar_icon)
                .resize(avatarSize, avatarSize)
                .transform(transformation)
                .into(view);
    }

    private String getAvatarUrl(User user) {
        if (user == null) {
            return null;
        }

        String avatarUrl = user.avatarUrl();
        if (TextUtils.isEmpty(avatarUrl)) {
            avatarUrl = getAvatarUrl(GravatarUtils.getHash(user.email()));
        }
        return avatarUrl;
    }

    private String getAvatarUrl(String id) {
        if (!TextUtils.isEmpty(id)) {
            return "http://gravatar.com/avatar/" + id + "?d=404";
        } else {
            return null;
        }
    }

    private int getMaxAvatarSize(final Context context) {
        int[] attrs = {android.R.attr.layout_height};
        TypedArray array = context.getTheme().obtainStyledAttributes(R.style.AvatarXLarge, attrs);
        int size = array.getLayoutDimension(0, 100);
        array.recycle();
        return size;
    }

    private boolean deleteCache(final File cache) {
        if (cache.isDirectory()) {
            for (File f : cache.listFiles()) {
                deleteCache(f);
            }
        }
        return cache.delete();
    }


    public class RoundedCornersTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            return ImageUtils.roundCorners(source, cornerRadius);
        }

        @Override
        public String key() {
            return "RoundedCornersTransformation";
        }
    }
}
