package com.example.test_task;

import android.content.Context;
import android.util.AttributeSet;

public class SquareImageView extends androidx.appcompat.widget.AppCompatImageView {

    private String imageURL = "";

    public String GetImageURL() {
        return this.imageURL;
    }

    public void SetImageURL(String url) {
        this.imageURL = url;
    }

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

}