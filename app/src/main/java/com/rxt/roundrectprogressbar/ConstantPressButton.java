package com.rxt.roundrectprogressbar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.Executors;

/**
 * Desc:长按view
 *
 * @author raoxuting
 *         since 2018/6/4
 */

public class ConstantPressButton extends android.support.v7.widget.AppCompatButton {

    public interface OnLongPressListener {
        void onLongPress(View view);
    }

    private OnLongPressListener longPressListener;

    public void setLongPressListener(OnLongPressListener longPressListener) {
        this.longPressListener = longPressListener;
    }

    public ConstantPressButton(Context context) {
        this(context, null);
    }

    public ConstantPressButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConstantPressButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        while (ConstantPressButton.this.isPressed()) {
                            long currentTimeMillis = System.currentTimeMillis();
                            long periodTime = currentTimeMillis - startTime;
                            if (periodTime == 100) {
                                if (longPressListener != null) {
                                    post(new Runnable() {
                                        @Override
                                        public void run() {
                                            longPressListener.onLongPress(ConstantPressButton.this);
                                        }
                                    });
                                }
                                startTime = currentTimeMillis;
                            }
                        }
                    }
                });
                return false;
            }
        });
    }


}
