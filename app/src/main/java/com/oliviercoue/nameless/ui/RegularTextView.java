package com.oliviercoue.nameless.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Olivier on 01/03/2016.
 *
 */
public class RegularTextView extends TextView {

    public RegularTextView(Context context) {
        super(context);
        init();
    }

    public RegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "FiraSans-Regular.otf");
        setTypeface(tf);
    }
}
