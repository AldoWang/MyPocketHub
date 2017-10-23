package com.hdsx.mypockethub.ui.user;

import android.os.Bundle;

import com.meisolsson.githubsdk.model.User;

import static com.hdsx.mypockethub.Intents.EXTRA_USER;

public abstract class UserNewsFragment extends NewsFragment implements OrganizationSelectionListener {

    protected User org;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (getActivity() instanceof OrganizationSelectionProvider) {
            org = ((OrganizationSelectionProvider) getActivity()).addListener(this);
        }

        if (getArguments() != null && getArguments().containsKey("org"))
            org = getArguments().getParcelable("org");

        if (org != null && savedInstanceState != null)
            org = (User) savedInstanceState.get(EXTRA_USER);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected boolean viewUser(User user) {
        if (org.id() != user.id()) {
            startActivity(UserViewActivity.createIntent(user));
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (org != null)
            outState.putParcelable(EXTRA_USER, org);
    }

    @Override
    public void onOrganizationSelected(User organization) {

    }

}
