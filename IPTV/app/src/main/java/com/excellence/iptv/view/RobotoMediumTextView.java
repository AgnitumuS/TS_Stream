package com.excellence.iptv.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * RobotoMediumTextView
 *
 * @author ggz
 * @date 2018/4/3
 */

public class RobotoMediumTextView extends AppCompatTextView {

    public RobotoMediumTextView(Context context) {
        super(context);
        init(context);
    }

    public RobotoMediumTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RobotoMediumTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Typeface typeface = Typeface.createFromAsset(
                context.getAssets(), "Roboto-Medium.ttf");
        setTypeface(typeface);
    }
}
