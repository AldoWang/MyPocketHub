package com.hdsx.mypockethub.util;

import com.hdsx.mypockethub.core.PageIterator.GitHubRequest;
import com.meisolsson.githubsdk.model.Page;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class RxPageUtil {

    public static <B> Observable<Page<B>> getAllPages(final GitHubRequest<Response<Page<B>>> pagedSingleCall, int i) {
        return pagedSingleCall.execute(i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(new Function<Response<Page<B>>, ObservableSource<Page<B>>>() {
                    @Override
                    public ObservableSource<Page<B>> apply(@NonNull Response<Page<B>> response) throws Exception {
                        Page<B> page = response.body();
                        if (page.next() == null)
                            return Observable.just(page);
                        return Observable.just(page).concatWith(getAllPages(pagedSingleCall, page.next()));
                    }
                });
    }

}
