package com.hdsx.mypockethub.ui.ref;

import android.util.Log;

import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.PageIterator.GitHubRequest;
import com.hdsx.mypockethub.core.ref.RefUtils;
import com.hdsx.mypockethub.ui.BaseActivity;
import com.hdsx.mypockethub.util.RxPageUtil;
import com.hdsx.mypockethub.util.ToastUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Page;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.service.git.GitService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import retrofit2.Response;

public class RefDialog {

    private static final String TAG = "RefDialog";
    private BaseActivity activity;
    private Single<List<GitReference>> refSingle;
    private int requestCode;

    public RefDialog(final BaseActivity activity, int requestCode, final Repository repository) {
        this.activity = activity;
        this.requestCode = requestCode;

        GitHubRequest<Response<Page<GitReference>>> gitHubRequest
                = new GitHubRequest<Response<Page<GitReference>>>() {
            @Override
            public Single<Response<Page<GitReference>>> execute(int page) {
                return ServiceGenerator.createService(activity, GitService.class)
                        .getGitReferences(repository.owner().login(), repository.name(), page);
            }
        };

        refSingle = RxPageUtil.getAllPages(gitHubRequest, 1)
                .flatMap(new Function<Page<GitReference>, ObservableSource<GitReference>>() {
                    @Override
                    public ObservableSource<GitReference> apply(@NonNull Page<GitReference> page) throws Exception {
                        return Observable.fromIterable(page.items());
                    }
                })
                .filter(new Predicate<GitReference>() {
                    @Override
                    public boolean test(@NonNull GitReference reference) throws Exception {
                        return RefUtils.isValid(reference);
                    }
                })
                .toSortedList(new Comparator<GitReference>() {
                    @Override
                    public int compare(GitReference o1, GitReference o2) {
                        return String.CASE_INSENSITIVE_ORDER.compare(o1.ref(), o2.ref());
                    }
                })
                .cache();
    }

    public void show(final GitReference selectedRef) {
        refSingle.subscribe(new Consumer<List<GitReference>>() {
            @Override
            public void accept(@NonNull List<GitReference> refs) throws Exception {
                int selected = -1;
                if (selectedRef != null) {
                    String ref = selectedRef.ref();
                    for (int i = 0; i < refs.size(); i++) {
                        String candidate = refs.get(i).ref();
                        if (ref.equals(candidate) || ref.equals(RefUtils.getName(candidate))) {
                            selected = i;
                            break;
                        }
                    }
                }
                RefDialogFragment.show(activity, requestCode, activity.getString(R.string.select_ref)
                        , null, new ArrayList<>(refs), selected);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable e) throws Exception {
                Log.d(TAG, "Exception loading references", e);
                ToastUtils.show(R.string.error_refs_load);
            }
        });
    }

}
