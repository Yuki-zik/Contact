package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SideLetterBar extends View {
    private static final String[] LETTERS = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    };

    private Paint paint;
    private int itemHeight;
    private int textSize;
    private OnLetterChangedListener listener;

    public interface OnLetterChangedListener {
        void onLetterChanged(String letter);
        void onLetterGone();
    }

    public SideLetterBar(Context context) {
        super(context);
        init();
    }

    public SideLetterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GRAY);
        textSize = (int) (getResources().getDisplayMetrics().density * 12);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = (int) (textSize * 2);
        itemHeight = height / LETTERS.length;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        itemHeight = height / LETTERS.length;

        for (int i = 0; i < LETTERS.length; i++) {
            paint.setColor(Color.GRAY);
            paint.setAlpha(160); // 设置透明度
            paint.setFakeBoldText(false);
            float x = width / 2f;
            float y = itemHeight * i + itemHeight / 2f + textSize / 2f;
            canvas.drawText(LETTERS[i], x, y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                int position = (int) (y / itemHeight);
                if (position >= 0 && position < LETTERS.length) {
                    if (listener != null) {
                        listener.onLetterChanged(LETTERS[position]);
                    }
                    // 当触摸时，绘制选中效果
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (listener != null) {
                    listener.onLetterGone();
                }
                // 触摸结束，恢复正常效果
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void setOnLetterChangedListener(OnLetterChangedListener listener) {
        this.listener = listener;
    }
} 
