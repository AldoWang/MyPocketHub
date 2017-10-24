package com.hdsx.mypockethub.core.code;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hdsx.mypockethub.core.commit.CommitUtils;
import com.hdsx.mypockethub.core.ref.RefUtils;
import com.meisolsson.githubsdk.model.git.GitEntryType;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.meisolsson.githubsdk.model.git.GitTree;
import com.meisolsson.githubsdk.model.git.GitTreeEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

public class FullTree {

    public GitTree gitTree;
    public GitReference reference;
    public Folder root;
    public String branch;

    public FullTree(GitTree gitTree, GitReference reference) {
        this.gitTree = gitTree;
        this.reference = reference;
        branch = RefUtils.getName(reference);

        root = new Folder();
        List<GitTreeEntry> entries = gitTree.tree();
        //entries are returned in order
        if (entries != null && entries.size() > 0) {
            for (GitTreeEntry entry : entries) {
                root.add(entry);
            }
        }
    }

    public static class Folder extends Entry {

        public Map<String, Entry> files = new HashMap<>();
        public Map<String, Folder> folders = new HashMap<>();

        public Folder() {
        }

        public Folder(GitTreeEntry entry, Folder parent) {
            super(entry, parent);
        }

        public void add(GitTreeEntry entry) {
            String path = entry.path();
            if (TextUtils.isEmpty(path))
                return;

            if (entry.type() == GitEntryType.blob) {
                String[] segments = path.split("/");
                if (segments.length > 1) {
                    Folder folder = folders.get(segments[0]);
                    if (folder != null)
                        folder.addFile(entry, segments, 1);
                } else if (segments.length == 1) {
                    Entry file = new Entry(entry, this);
                    files.put(file.name, file);
                }
            } else if (entry.type() == GitEntryType.tree) {
                String[] segments = path.split("/");
                if (segments.length > 1) {
                    Folder folder = folders.get(segments[0]);
                    if (folder != null) {
                        folder.addFolder(entry, segments, 1);
                    }
                } else if (segments.length == 1) {
                    Folder fold = new Folder(entry, this);
                    folders.put(fold.name, fold);
                }
            }
        }

        private void addFolder(GitTreeEntry entry, String[] segments, int index) {
            if (index == segments.length - 1) {
                Folder folder = new Folder(entry, this);
                folders.put(folder.name, folder);
            } else {
                Folder folder = folders.get(segments[index]);
                if (folder != null) {
                    folder.addFolder(entry, segments, index + 1);
                }
            }
        }

        private void addFile(GitTreeEntry entry, String[] pathSegments, int index) {
            if (index == pathSegments.length - 1) {
                Entry file = new Entry(entry, this);
                files.put(file.name, file);
            } else {
                Folder folder = folders.get(pathSegments[index]);
                if (folder != null)
                    folder.addFile(entry, pathSegments, index + 1);
            }
        }

    }

    public static class Entry implements Comparable<Entry> {

        public GitTreeEntry entry;
        public Folder parent;
        public String name;

        public Entry() {
        }

        public Entry(GitTreeEntry entry, Folder parent) {
            this.entry = entry;
            this.parent = parent;
            this.name = CommitUtils.getName(entry.path());
        }

        @Override
        public int compareTo(@NonNull Entry anthor) {
            return CASE_INSENSITIVE_ORDER.compare(this.name, anthor.name);
        }

    }

}
