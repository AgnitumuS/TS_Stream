package com.excellence.iptv.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.excellence.iptv.R;

/**
 * FavoriteImageView
 *
 * @author ggz
 * @date 2018/4/20
 */

public class FavImageView extends AppCompatImageView {


    private boolean isEditMode = false;
    private Rect mRect;
    private Paint mPaint;

    public FavImageView(Context context) {
        super(context);
    }

    public FavImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FavImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mRect == null) {
            mRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        if (mPaint == null) {
            mPaint = new Paint();
        }

        if (isEditMode) {
            mPaint.setColor(getResources().getColor(R.color.favorite_item_edit_mode_iv_color));
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRect(mRect, mPaint);
        }

    }

    public void setEditMode(boolean isShow) {
        isEditMode = isShow;
        // 重绘
        invalidate();
    }

}
