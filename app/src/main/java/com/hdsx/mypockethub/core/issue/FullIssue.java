package com.hdsx.mypockethub.core.issue;

import com.meisolsson.githubsdk.model.GitHubComment;
import com.meisolsson.githubsdk.model.Issue;
import com.meisolsson.githubsdk.model.IssueEvent;

import java.util.Collection;

public class FullIssue {

    private final Issue issue;
    private final Collection<GitHubComment> comments;
    private final Collection<IssueEvent> events;

    public FullIssue(Issue issue, Collection<GitHubComment> comments, Collection<IssueEvent> events) {
        this.issue = issue;
        this.comments = comments;
        this.events = events;
    }

    public Issue getIssue() {
        return issue;
    }

    public Collection<GitHubComment> getComments() {
        return comments;
    }

    public Collection<IssueEvent> getEvents() {
        return events;
    }

}
