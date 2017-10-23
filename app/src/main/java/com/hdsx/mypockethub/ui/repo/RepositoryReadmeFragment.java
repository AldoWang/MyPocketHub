package com.hdsx.mypockethub.ui.repo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;

import com.hdsx.mypockethub.ui.DialogFragment;
import com.hdsx.mypockethub.ui.WebView;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.repositories.RepositoryContentService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.hdsx.mypockethub.Intents.EXTRA_REPOSITORY;

public class RepositoryReadmeFragment extends DialogFragment {

    private WebView webView;
    private Repository repository;

    private static final String PAGE_START = "<!DOCTYPE html><html lang=\"en\"> <head> <title></title>" +
            "<meta charset=\"UTF-8\"> " +
            "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\"/>" +
            "<script src=\"intercept.js\"></script>" +
            "<link href=\"github.css\" rel=\"stylesheet\"> </head> <body>";

    private static final String PAGE_END = "</body></html>";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new WebView(getActivity());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        webView = (WebView) view;

        repository = getParcelableExtra(EXTRA_REPOSITORY);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "Readme");
        ServiceGenerator.createService(getActivity(), RepositoryContentService.class)
                .getReadmeHtml(repository.owner().login(), repository.name(), null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<String>>() {
                    @Override
                    public void accept(@NonNull Response<String> response) throws Exception {
                        String baseUrl = String.format("https://github.com/%s/%s/raw/%s/"
                                , repository.owner().login(), repository.name(), "master");
                        String data = PAGE_START + response.body() + PAGE_END;
                        webView.loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8", null);
                    }
                });
    }

    @JavascriptInterface
    public void startIntercept() {
        webView.startIntercept();
    }

    @JavascriptInterface
    public void stopIntercept() {
        webView.stopIntercept();
    }

}
