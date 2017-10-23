package com.hdsx.mypockethub.core;

import com.meisolsson.githubsdk.model.Page;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import io.reactivex.Single;
import retrofit2.Response;

public class PageIterator<V> implements Iterator<List>, Iterable<List> {

    private GitHubRequest<Response<Page<V>>> request;
    private Integer nextPage;
    private Integer lastPage;

    public PageIterator(GitHubRequest<Response<Page<V>>> request, int nextPage) {
        this.request = request;
        this.nextPage = nextPage;
    }

    @Override
    public boolean hasNext() {
        return (nextPage != null && nextPage == 1) || lastPage != null;
    }

    @Override
    public List<V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            Single<Response<Page<V>>> client = request.execute(nextPage);
            Page<V> response = client.blockingGet().body();
            nextPage++;
            nextPage = response.next();
            lastPage = response.last();
            return response.items();
        }
    }

    @Override
    public Iterator<List> iterator() {
        return this;
    }

    public interface GitHubRequest<V> {

        Single<V> execute(int page);

    }
}
