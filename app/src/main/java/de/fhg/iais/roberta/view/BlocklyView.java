package de.fhg.iais.roberta.view;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

public class BlocklyView extends WebView {
    private String TAG = BlocklyView.class.getSimpleName();

    /**
     * Constructs a new WebView with a Context object.
     *
     * @param context a Context object used to access application assets
     */
    public BlocklyView(Context context) {
        super(context);
    }

    @Override
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                int scrollRangeX, int scrollRangeY, int maxOverScrollX,
                                int maxOverScrollY, boolean isTouchEvent) {
        Log.d(TAG, "" + isTouchEvent);
        return false;
    }

    @Override
    public void scrollTo(int x, int y) {
        // Log.d(TAG, "scrollTo");
    }

    @Override
    public void computeScroll() {
        // Log.d(TAG, "computeScroll");
    }
}
