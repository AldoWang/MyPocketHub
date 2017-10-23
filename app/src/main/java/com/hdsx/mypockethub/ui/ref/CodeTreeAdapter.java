package com.hdsx.mypockethub.ui.ref;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;
import android.view.View;

import com.github.kevinsawicki.wishlist.MultiTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.core.code.FullTree.Entry;
import com.hdsx.mypockethub.core.code.FullTree.Folder;
import com.hdsx.mypockethub.core.commit.CommitUtils;
import com.hdsx.mypockethub.util.ServiceUtils;

public class CodeTreeAdapter extends MultiTypeAdapter {

    private final int TYPE_TREE = 0;
    private final int TYPE_BLOB = 1;
    private Context context;
    private static final int INDENTED_PADDING = 16;

    private int paddingLeft;

    private int paddingRight;

    private int paddingTop;

    private int paddingBottom;

    private boolean indented;
    private final int indentedPaddingLeft;

    public CodeTreeAdapter(Activity activity) {
        super(activity);
        context = activity;
        indentedPaddingLeft = ServiceUtils.getIntPixels(INDENTED_PADDING, context.getResources());
    }

    public CodeTreeAdapter setIndented(boolean indented) {
        this.indented = indented;
        return this;
    }

    @Override
    protected View initialize(final int type, View view) {
        view = super.initialize(type, view);

        paddingLeft = view.getPaddingLeft();
        paddingRight = view.getPaddingRight();
        paddingTop = view.getPaddingTop();
        paddingBottom = view.getPaddingBottom();
        return view;
    }

    @Override
    protected int getChildLayoutId(int type) {
        switch (type) {
            case TYPE_TREE:
                return R.layout.folder_item;
            case TYPE_BLOB:
                return R.layout.blob_item;
            default:
                return -1;
        }
    }

    @Override
    protected int[] getChildViewIds(int type) {
        switch (type) {
            case TYPE_BLOB:
                return new int[]{R.id.tv_file, R.id.tv_size};
            case TYPE_TREE:
                return new int[]{R.id.tv_folder, R.id.tv_folders, R.id.tv_files};
            default:
                return null;
        }
    }

    @Override
    protected void update(int position, Object item, int type) {
        if (indented) {
            updater.view.setPadding(indentedPaddingLeft, paddingTop, paddingRight, paddingBottom);
        } else {
            updater.view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }

        switch (type) {
            case TYPE_BLOB:
                Entry file = (Entry) item;
                setText(0, file.name);
                setText(1, Formatter.formatFileSize(context, file.entry.size()));
                break;
            case TYPE_TREE:
                Folder folder = (Folder) item;
                setText(0, CommitUtils.getName(folder.name));
                setNumber(1, folder.folders.size());
                setNumber(2, folder.files.size());
                break;
        }
    }

    public void setItems(Folder folder) {
        clear();

        addItems(TYPE_TREE, folder.folders.values());
        addItems(TYPE_BLOB, folder.files.values());
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

}
