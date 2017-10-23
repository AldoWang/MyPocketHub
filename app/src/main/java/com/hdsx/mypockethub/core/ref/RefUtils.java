package com.hdsx.mypockethub.core.ref;

import android.text.TextUtils;

import com.meisolsson.githubsdk.model.git.GitReference;

public class RefUtils {

    private static final String PREFIX_REFS = "refs/";

    private static final String PREFIX_PULL = PREFIX_REFS + "pull/";

    private static final String PREFIX_TAG = PREFIX_REFS + "tags/";

    private static final String PREFIX_HEADS = PREFIX_REFS + "heads/";


    public static String getPath(GitReference ref) {
        if (ref == null)
            return null;

        String name = ref.ref();
        if (!TextUtils.isEmpty(name) && name.startsWith(PREFIX_REFS))
            return name.substring(name.indexOf(PREFIX_REFS));
        else
            return name;
    }

    public static String getName(GitReference ref) {
        if (ref != null) {
            return getName(ref.ref());
        } else {
            return null;
        }
    }

    public static String getName(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        if (name.startsWith(PREFIX_HEADS)) {
            return name.substring(PREFIX_HEADS.length());
        } else if (name.startsWith(PREFIX_TAG)) {
            return name.substring(PREFIX_TAG.length());
        } else if (name.startsWith(PREFIX_REFS)) {
            return name.substring(PREFIX_REFS.length());
        } else {
            return name;
        }
    }

    public static boolean isTag(final GitReference ref) {
        return ref != null && isTag(ref.ref());
    }

    public static boolean isTag(final String name) {
        return !TextUtils.isEmpty(name) && name.startsWith(PREFIX_TAG);
    }

    public static boolean isValid(GitReference reference) {
        if (reference == null)
            return false;
        String name = reference.ref();
        return !TextUtils.isEmpty(name) && !name.startsWith(PREFIX_PULL);
    }

}
