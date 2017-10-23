package com.hdsx.mypockethub.core.issue;

import android.os.Parcel;
import android.os.Parcelable;

import com.meisolsson.githubsdk.model.Label;
import com.meisolsson.githubsdk.model.Milestone;
import com.meisolsson.githubsdk.model.Repository;
import com.meisolsson.githubsdk.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueFilter implements Parcelable, Serializable {

    /**
     * Ascending direction sort order
     */
    public static final String DIRECTION_ASCENDING = "asc";
    /**
     * Descending direction sort order
     */
    public static final String DIRECTION_DESCENDING = "desc";
    /**
     * Issue body field name
     */
    public static final String FIELD_BODY = "body";
    /**
     * Sort direction of output
     */
    public static final String FIELD_DIRECTION = "direction";
    /**
     * Filter field key
     */
    public static final String FIELD_FILTER = "filter";
    /**
     * Since date field
     */
    public static final String FIELD_SINCE = "since";
    /**
     * Sort field key
     */
    public static final String FIELD_SORT = "sort";
    /**
     * Issue title field name
     */
    public static final String FIELD_TITLE = "title";
    /**
     * Filter by assigned issues for user
     */
    public static final String FILTER_ASSIGNED = "assigned";
    /**
     * Filter by issue assignee
     */
    public static final String FILTER_ASSIGNEE = "assignee";
    /**
     * Filter by created issues by user
     */
    public static final String FILTER_CREATED = "created";
    /**
     * Filter by issue's labels
     */
    public static final String FILTER_LABELS = "labels";
    /**
     * Filter by user mentioned in issue
     */
    public static final String FILTER_MENTIONED = "mentioned";
    /**
     * Filter by issue's milestone
     */
    public static final String FILTER_MILESTONE = "milestone";
    /**
     * Filter by issue's state
     */
    public static final String FILTER_STATE = "state";
    /**
     * Filter by subscribed issues for user
     */
    public static final String FILTER_SUBSCRIBED = "subscribed";

    private final Repository repository;
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7310646589186299063L;
    /**
     * Sort by commented on at
     */
    public static final String SORT_COMMENTS = "comments";
    /**
     * Sort by created at
     */
    public static final String SORT_CREATED = "created";
    /**
     * Sort by updated at
     */
    public static final String SORT_UPDATED = "updated";


    public static final String STATE_CLOSED = "closed";

    public static final String STATE_OPEN = "open";

    private String direction;

    private String sortType;

    private List<Label> labels;

    private Milestone milestone;

    private User assignee;

    private boolean open;

    public IssueFilter(Repository repository) {
        this.repository = repository;
        open = true;
        direction = DIRECTION_DESCENDING;
        sortType = SORT_CREATED;
    }

    public IssueFilter(Parcel in) {
        this.repository = in.readParcelable(Repository.class.getClassLoader());
        labels = new ArrayList<>();
        in.readList(labels, Label.class.getClassLoader());
        milestone = in.readParcelable(Milestone.class.getClassLoader());
        assignee = in.readParcelable(User.class.getClassLoader());
        open = in.readByte() != 0;
        direction = in.readString();
        sortType = in.readString();
    }

    public Map<String, Object> toFilterMap() {
        Map<String, Object> filter = new HashMap<>();
        filter.put(FIELD_SORT, sortType);
        filter.put(FIELD_DIRECTION, direction);

        if (assignee != null) {
            filter.put(FILTER_ASSIGNEE, assignee.login());
        }

        if (milestone != null) {
            filter.put(FILTER_MILESTONE, Integer.toString(milestone.number()));
        }

        if (labels != null && !labels.isEmpty()) {
            StringBuilder labelsQuery = new StringBuilder();
            for (Label label : labels) {
                labelsQuery.append(label.name()).append(',');
            }
            filter.put(FILTER_LABELS, labelsQuery.toString());
        }

        if (open) {
            filter.put(FILTER_STATE, STATE_OPEN);
        } else {
            filter.put(FILTER_STATE, STATE_CLOSED);
        }
        return filter;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public boolean isOpen() {
        return open;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public IssueFilter setAssignee(User assignee) {
        this.assignee = assignee;
        return this;
    }

    public User getAssignee() {
        return assignee;
    }

    public static final Creator<IssueFilter> CREATOR = new Creator<IssueFilter>() {

        public IssueFilter createFromParcel(Parcel in) {
            return new IssueFilter(in);
        }

        public IssueFilter[] newArray(int size) {
            return new IssueFilter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

}
