package com.excellence.iptv.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.excellence.iptv.R;

import java.util.ArrayList;
import java.util.List;


/**
 * FlowLayout
 *
 * @author ggz
 * @date 2018/4/3
 */

public class FlowLayout extends ViewGroup {
    private static final String TAG = "FlowLayout";

    private LayoutInflater mInflater;

    private boolean isEditMode = false;
    private List<String> mHistoryTagList = new ArrayList<>();

    private OnTagClickListener mOnTagClickListener;

    public FlowLayout(Context context) {
        super(context);
        mInflater = LayoutInflater.from(getContext());
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(getContext());
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(getContext());
    }

    /**
     * generateLayoutParams
     */
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    /**
     * 负责设置子控件的测量模式和大小
     * 根据所有子控件设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获得它的父容器为它设置的测量模式和大小
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // 如果是 warp_content 情况下，记录宽和高
        int width = 0;
        int height = 0;

        // 记录每一行的宽和高
        int lineWidth = 0;
        int lineHeight = 0;

        // 得到内部元素的个数
        int count = getChildCount();

        // 遍历每个子元素
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            // 测量子 View 的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到 LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            // 子 view 占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            // 子 view 占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > sizeWidth) {
                // 取最大的宽度
                width = Math.max(width, lineWidth);
                // 重新开启新行，开始记录
                lineWidth = childWidth;
                // 叠加当前高度
                height += lineHeight;
                // 开启记录下一行的高度
                lineHeight = childHeight;
            } else {
                // 叠加行宽
                lineWidth += childWidth;
                // 得到当前最大高度
                lineHeight = Math.max(lineHeight, childHeight);
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前 lineWidth 做比较
            if (i == count - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }
        }

        setMeasuredDimension((modeWidth == MeasureSpec.EXACTLY) ? sizeWidth : width,
                (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }


    /**
     * 存储所有的View，按行记录
     */
    private ArrayList<ArrayList<View>> mAllViews = new ArrayList<>();
    /**
     * 记录每一行的最大高度
     */
    private ArrayList<Integer> mLineHeight = new ArrayList<>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        // 存储每一行所有的 childView
        ArrayList<View> lineViews = new ArrayList<>();
        int count = getChildCount();
        // 遍历所有 childView
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果需要换行
            if (lineWidth + childWidth + lp.leftMargin + lp.rightMargin > width) {
                // 记录这一行所有的 View 以及最大高度
                mLineHeight.add(lineHeight);
                // 将当前行的 childView 保存，然后开启新的 ArrayList 保存下一行的 childView
                mAllViews.add(lineViews);

                // 重置行宽和行高
                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                // 重置lineViews集合
                lineViews = new ArrayList<>();
            }
            // 不需要换行，则累加
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }
        // 记录最后一行
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);

        // 设置子view的位置
        int left = 0;
        int top = 0;

        // 有多少行
        int lineNum = mAllViews.size();
        for (int i = 0; i < lineNum; i++) {
            // 每一行的所有的views
            lineViews = mAllViews.get(i);
            // 当前行的最大高度
            lineHeight = mLineHeight.get(i);

            int lineViewSize = lineViews.size();
            for (int j = 0; j < lineViewSize; j++) {
                View child = lineViews.get(j);
                // 判断 子 view 的状态
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                //计算 childView 的 left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + childWidth;
                int bc = tc + childHeight;

                Log.e(TAG, child + " , l = " + lc + " , t = " + t + " , r ="
                        + rc + " , b = " + bc);

                // 子 view 布局
                child.layout(lc, tc, rc, bc);

                // 同一行 view 坐起点坐标的变换
                left += childWidth + lp.leftMargin + lp.rightMargin;
            }
            // 换行时将 left 重置
            left = 0;
            // top 要加上上一行的行高
            top += lineHeight;
        }
    }


    /**
     * 设置一组标签
     */
    public void setListData(List<String> list) {
        int count = list.size();
        mHistoryTagList = list;

        for (int i = 0; i < count; i++) {
            View view = (View) mInflater.inflate(R.layout.search_item, this, false);
            final String text = list.get(i);
            TextView textView = view.findViewById(R.id.tv_search_item_name);
            textView.setText(text);

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnTagClickListener != null) {
                        mOnTagClickListener.onClick(text);
                    }
                }
            });
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    editMode(true);
                    return true;
                }
            });

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(view.getLayoutParams());
            lp.setMargins(30, 15, 0, 0);
            view.setLayoutParams(lp);
            this.addView(view);
        }
    }

    /**
     * 添加单个标签
     */
    public void addTag(final String text) {

        if (getIsEditMode()) {
            editMode(false);
        }

        for (int i = 0; i < mHistoryTagList.size(); i++) {
            if (text.equals(mHistoryTagList.get(i))) {
                return;
            }
        }
        mHistoryTagList.add(text);

        View view = (View) mInflater.inflate(R.layout.search_item, this, false);
        TextView textView = view.findViewById(R.id.tv_search_item_name);
        textView.setText(text);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnTagClickListener != null) {
                    mOnTagClickListener.onClick(text);
                }
            }
        });
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editMode(true);
                return true;
            }
        });

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(view.getLayoutParams());
        lp.setMargins(30, 15, 0, 0);
        view.setLayoutParams(lp);
        this.addView(view);
    }

    /**
     * 删除所有标签
     */
    public void cleanTag() {
        this.mHistoryTagList.clear();
        this.removeAllViews();
    }

    /**
     * 删除标签
     */
    public void deleteTag(int position) {
        mHistoryTagList.remove(position);
        this.removeView(getChildAt(position));
    }

    public void editMode(boolean isRight) {
        isEditMode = isRight;
        int count = getChildCount();
        // 遍历所有 childView
        for (int i = 0; i < count; i++) {
            final int position = i;
            View view = getChildAt(i);
            ImageView imageView = view.findViewById(R.id.iv_search_item_delete);
            if (isRight) {
                imageView.setVisibility(VISIBLE);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTag(position);
                    }
                });
            } else {
                imageView.setVisibility(INVISIBLE);
            }
        }
    }

    public boolean getIsEditMode() {
        return isEditMode;
    }

    public List<String> getHistoryTagList() {
        return mHistoryTagList;
    }

    public interface OnTagClickListener {
        void onClick(String text);
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        this.mOnTagClickListener = onTagClickListener;
    }
}
