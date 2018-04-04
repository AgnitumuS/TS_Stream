package com.excellence.iptv.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/4/3
 */

public class RobotoTextView extends AppCompatTextView {

    public RobotoTextView(Context context) {
        super(context);
        Typeface typefaceypeface = Typeface.createFromAsset(
                context.getAssets(), "Roboto-Medium.ttf");
    }


}
