package com.hdsx.mypockethub.core.code;

import android.content.Context;

import com.hdsx.mypockethub.core.ref.RefUtils;
import com.meisolsson.githubsdk.core.ServiceGenerator;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.git.GitCommit;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.model.git.GitTree;
import com.meisolsson.githubsdk.service.git.GitService;
import com.meisolsson.githubsdk.service.repositories.RepositoryService;

import java.io.IOException;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import retrofit2.Response;

public class RefreshTreeTask {

    private Context context;
    private Repository repository;
    private GitReference reference;

    public RefreshTreeTask(Context context, Repository repository, GitReference reference) {
        this.context = context;
        this.repository = repository;
        this.reference = reference;
    }

    public Single<FullTree> refresh() {
        final GitService gitService = ServiceGenerator.createService(context, GitService.class);
        return getBranch(reference)
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull String branch) throws Exception {
                        return branch.replace("heads/", "");
                    }
                })
                .flatMap(new Function<String, SingleSource<GitReference>>() {

                    @Override
                    public SingleSource<GitReference> apply(@NonNull String branch) throws Exception {
                        return getValidRef(gitService, reference, branch);
                    }
                })
                .flatMap(new Function<GitReference, SingleSource<RefreshTreeModel>>() {
                    @Override
                    public SingleSource<RefreshTreeModel> apply(@NonNull GitReference reference) throws Exception {
                        return gitService.getGitCommit(repository.owner().login(), repository.name(), reference.object().sha())
                                .map(new Function<Response<GitCommit>, GitCommit>() {
                                    @Override
                                    public GitCommit apply(@NonNull Response<GitCommit> reponse) throws Exception {
                                        return reponse.body();
                                    }
                                })
                                .zipWith(Single.just(reference), new BiFunction<GitCommit, GitReference, RefreshTreeModel>() {
                                    @Override
                                    public RefreshTreeModel apply(@NonNull GitCommit gitCommit, @NonNull GitReference reference) throws Exception {
                                        return new RefreshTreeModel(gitCommit, reference);
                                    }
                                });
                    }
                })
                .flatMap(new Function<RefreshTreeModel, SingleSource<FullTree>>() {

                    @Override
                    public SingleSource<FullTree> apply(@NonNull RefreshTreeModel model) throws Exception {
                        return gitService.getGitTreeRecursive(repository.owner().login(), repository.name(), model.getCommit().tree().sha())
                                .map(new Function<Response<GitTree>, GitTree>() {
                                    @Override
                                    public GitTree apply(@NonNull Response<GitTree> response) throws Exception {
                                        return response.body();
                                    }
                                })
                                .zipWith(Single.just(model.ref), new BiFunction<GitTree, GitReference, FullTree>() {
                                    @Override
                                    public FullTree apply(@NonNull GitTree gitTree, @NonNull GitReference reference) throws Exception {
                                        return new FullTree(gitTree, reference);
                                    }
                                });
                    }
                });
    }

    private Single<GitReference> getValidRef(GitService service, GitReference ref, String branch) {
        if (!isValidRef(ref)) {
            return service.getGitReference(repository.owner().login(), repository.name(), branch)
                    .map(new Function<Response<GitReference>, GitReference>() {

                        @Override
                        public GitReference apply(@NonNull Response<GitReference> response) throws Exception {
                            if (response.isSuccessful()) {
                                GitReference fetchedRef = response.body();
                                if (isValidRef(fetchedRef))
                                    return fetchedRef;
                                else
                                    throw new IOException("Reference does not have associated commit SHA-1");
                            } else {
                                throw new IOException("Request for Git Reference was unsuccessful");
                            }
                        }
                    });
        }
        return Single.just(ref);
    }

    private boolean isValidRef(GitReference ref) {
        return ref != null && ref.object() != null && ref.object().sha() != null;
    }

    private Single<String> getBranch(GitReference reference) {
        String branch = RefUtils.getPath(reference);
        if (branch == null) {
            branch = repository.defaultBranch();
            if (branch == null) {
                return ServiceGenerator.createService(context, RepositoryService.class)
                        .getRepository(repository.owner().login(), repository.name())
                        .map(new Function<Response<Repository>, String>() {
                            @Override
                            public String apply(@NonNull Response<Repository> response) throws Exception {
                                return response.body().defaultBranch();
                            }
                        });
            }
        }
        return Single.just(branch);
    }

    private class RefreshTreeModel {

        private GitReference ref;
        private GitCommit commit;

        public RefreshTreeModel(GitCommit commit, GitReference ref) {
            this.commit = commit;
            this.ref = ref;
        }

        public GitReference getRef() {
            return ref;
        }

        public void setRef(GitReference ref) {
            this.ref = ref;
        }

        public GitCommit getCommit() {
            return commit;
        }

        public void setCommit(GitCommit commit) {
            this.commit = commit;
        }

    }

}
