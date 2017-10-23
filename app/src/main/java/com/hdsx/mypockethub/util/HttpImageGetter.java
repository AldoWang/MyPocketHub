package com.hdsx.mypockethub.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.text.TextUtils;
import android.widget.TextView;

import com.hdsx.mypockethub.R;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.request.RequestMarkdown;
import com.meisolsson.githubsdk.service.misc.MarkdownService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HttpImageGetter implements ImageGetter {

    private Context context;
    private Map<Object, CharSequence> fullHtmlCache = new HashMap<>();
    private Map<Object, CharSequence> rawHtmlCache = new HashMap<>();
    private LoadingImageGetter loading;

    @Inject
    public HttpImageGetter(Context context) {
        this.context = context;
        loading = new LoadingImageGetter(context, 24);
    }

    @Override
    public Drawable getDrawable(String source) {
        return null;
    }

/*
    public HttpImageGetter bind(final TextView view, final String html, final Object id) {
        if (TextUtils.isEmpty(html))
            return hide(view);

        CharSequence encoded = fullHtmlCache.get(id);
        if (encoded != null) {
            return show(view, encoded);
        }

        encoded = rawHtmlCache.get(id);
        if (encoded == null) {
            if (!html.matches("<[a-z][\\s\\S]*>")) {
                RequestMarkdown requestMarkdown = RequestMarkdown.builder().text(html).build();
                ServiceGenerator.createService(context, MarkdownService.class)
                        .renderMarkdown(requestMarkdown)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Response<String>>() {
                            @Override
                            public void accept(@NonNull Response<String> response) throws Exception {
//                                continueBind(view, response.body(), id);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
//                                continueBind(view, html, id);
                            }
                        });
            } else {
                return continueBind(view, html, id);
            }
        }
        return continueBind(view, html, id);
    }
*/

    /*private HttpImageGetter continueBind(TextView view, String html, Object id) {
        CharSequence encoded = HtmlUtils.encode(html, loading);
        if (containsImages(html)) {
            rawHtmlCache.put(id, encoded);
        } else {
            rawHtmlCache.remove(id);
            fullHtmlCache.put(id, encoded);
            return show(view, encoded);
        }

        if (TextUtils.isEmpty(html))
            return hide(view);

        show(view, encoded);
        view.setTag(id);

    }*/

    private HttpImageGetter show(TextView view, CharSequence html) {
        if (TextUtils.isEmpty(html))
            return hide(view);

        view.setText(trim(html));
        view.setVisibility(VISIBLE);
        view.setTag(null);
        return this;
    }

    private CharSequence trim(CharSequence val) {
        if (val.charAt(val.length() - 1) == '\n' && val.charAt(val.length() - 2) == '\n')
            val = val.subSequence(0, val.length() - 2);
        return val;
    }

    private HttpImageGetter hide(final TextView view) {
        view.setText(null);
        view.setVisibility(GONE);
        view.setTag(null);
        return this;
    }

    private static boolean containsImages(final String html) {
        return html.contains("<img");
    }

    class LoadingImageGetter implements ImageGetter {

        private Drawable image;

        public LoadingImageGetter(Context context, int size) {
            int imageSize = ServiceUtils.getIntPixels(size, context.getResources());
            image = context.getResources().getDrawable(R.drawable.image_loading_icon);
            image.setBounds(0, 0, imageSize, imageSize);
        }

        @Override
        public Drawable getDrawable(String source) {
            return image;
        }
    }

}
