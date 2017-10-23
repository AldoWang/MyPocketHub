package com.hdsx.mypockethub.ui.repo;

import android.content.Context;

import com.hdsx.mypockethub.Intents;
import com.hdsx.mypockethub.core.PageIterator;
import com.hdsx.mypockethub.core.ResourcePager;
import com.hdsx.mypockethub.ui.user.EventPager;
import com.hdsx.mypockethub.ui.user.NewsFragment;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.service.activity.EventService;

import io.reactivex.Single;
import retrofit2.Response;

public class RepositoryNewsFragment extends NewsFragment {

    private Repository repository;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        repository = getParcelableExtra(Intents.EXTRA_REPOSITORY);
    }

    @Override
    protected ResourcePager<GitHubEvent> createPager() {
        return new EventPager() {
            @Override
            protected PageIterator<GitHubEvent> createIterator(int page, int size) {
                return new PageIterator<>(new PageIterator.GitHubRequest<Response<Page<GitHubEvent>>>() {
                    @Override
                    public Single<Response<Page<GitHubEvent>>> execute(int page1) {
                        return ServiceGenerator.createService(getActivity(), EventService.class)
                                .getRepositoryEvents(repository.owner().login(), repository.name(), page1);
                    }
                }, page);
            }
        };
    }

}
