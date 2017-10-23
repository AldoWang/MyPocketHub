package com.hdsx.mypockethub.ui.issue;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.hdsx.mypockethub.R;
import com.hdsx.mypockethub.ui.StyledText;
import com.hdsx.mypockethub.util.AvatarLoader;
import com.hdsx.mypockethub.util.TypefaceUtils;
import com.meisolsson.githubsdk.model.IssueState;
import com.meisolsson.githubsdk.model.Label;

import java.util.Date;
import java.util.List;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;

public abstract class IssueListAdapter<V> extends SingleTypeAdapter<V> {

    protected AvatarLoader avatars;
    private final TextView numberView;
    private int numberWidth;
    protected static final int MAX_LABELS = 8;

    public IssueListAdapter(LayoutInflater inflater, int viewId, Object[] items
            , AvatarLoader avatars) {
        super(inflater, viewId);
        this.avatars = avatars;
        numberView = (TextView) inflater.inflate(viewId, null).findViewById(R.id.tv_issue_number);
        setItems(items);
    }

    @Override
    public void setItems(Object[] items) {
        super.setItems(items);
        computeNumberWidth(items);
    }

    @SuppressWarnings("unchecked")
    private void computeNumberWidth(final Object[] items) {
        int[] numbers = new int[items.length];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = getNumber((V) items[i]);
        }
        int digits = Math.max(TypefaceUtils.getMaxDigits(numbers), 4);
        numberWidth = TypefaceUtils.getWidth(numberView, digits)
                + numberView.getPaddingLeft() + numberView.getPaddingRight();
    }

    protected abstract int getNumber(V item);

    protected void updateNumber(int number, IssueState state, int paintFlag, int viewIndex) {
        TextView view = textView(viewIndex);
        view.setText(String.valueOf(number));
        if (state.equals(IssueState.closed)) {
            view.setPaintFlags(paintFlag | STRIKE_THRU_TEXT_FLAG);
        } else {
            view.setPaintFlags(paintFlag);
        }
        view.getLayoutParams().width = numberWidth;
    }

    protected void updateReporter(String reporter, Date date, int viewIndex) {
        StyledText reporterText = new StyledText();
        reporterText.bold(reporter);
        reporterText.append(' ');
        reporterText.append(date);
        setText(viewIndex, reporterText);
    }

    protected void updateLabels(final List<Label> labels, final int viewIndex) {
        if (labels != null && !labels.isEmpty()) {
            int size = Math.min(labels.size(), MAX_LABELS);
            for (int i = 0; i < size; i++) {
                String color = labels.get(i).color();
                if (!TextUtils.isEmpty(color)) {
                    View view = view(viewIndex + i);
                    view.setBackgroundColor(Color.parseColor('#' + color));
                    view.setVisibility(View.VISIBLE);
                } else {
                    setGone(viewIndex + i, true);
                }
            }
            for (int i = size; i < MAX_LABELS; i++) {
                setGone(viewIndex + i, true);
            }
        } else {
            for (int i = 0; i < MAX_LABELS; i++) {
                setGone(viewIndex + i, true);
            }
        }
    }

}
