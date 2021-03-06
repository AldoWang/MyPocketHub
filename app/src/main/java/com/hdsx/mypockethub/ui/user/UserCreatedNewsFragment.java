package com.hdsx.mypockethub.ui.user;

import com.hdsx.mypockethub.core.PageIterator;
import com.hdsx.mypockethub.core.PageIterator.GitHubRequest;
import com.hdsx.mypockethub.core.ResourcePager;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.GitHubEvent;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.service.activity.EventService;

import io.reactivex.Single;
import retrofit2.Response;

public class UserCreatedNewsFragment extends UserNewsFragment {

    @Override
    protected ResourcePager<GitHubEvent> createPager() {
        return new EventPager() {
            @Override
            protected PageIterator<GitHubEvent> createIterator(int page, int size) {
                return new PageIterator<>(new GitHubRequest<Response<Page<GitHubEvent>>>() {
                    @Override
                    public Single<Response<Page<GitHubEvent>>> execute(int page1) {
                        return ServiceGenerator.createService(getContext(), EventService.class)
                                .getUserPerformedEvents(org.login(), page1);
                    }
                }, page);
            }
        };
    }

}
