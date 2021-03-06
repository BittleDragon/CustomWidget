package com.rxt.roundrectprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ProgressBar;

import java.math.BigDecimal;


/**
 * Desc: 圆角矩形进度条
 * Company: xuehai
 * Copyright: Copyright (c) 2018
 *
 * @author raoxuting
 * @since 2018/04/15 17/28
 */

public class RoundRectProgressbar extends ProgressBar {

    public static final String TAG = "Progressbar";
    /**
     * 默认进度颜色
     */
    public static final String DEFAULT_PROGRESS_COLOR = "#ffca61";
    /**
     * 默认进度条背景颜色
     */
    public static final String DEFAULT_BACKGROUND_COLOR = "#fff3e1";
    /**
     * 默认百分比字体颜色
     */
    public static final String DEFAULT_PERCENT_COLOR = "#333333";
    /**
     * 默认百分比字体大小
     */
    public static final float DEFAULT_PERCENT_TEXTSIZE = 14f;
    /**
     * 默认圆角半径
     */
    private static final int DEFAULT_RECT_RADIUS = 10;
    /**
     * 当前进度的颜色
     */
    public int currentProgressColor;
    /**
     * 进度背景颜色
     */
    public int progressBgColor;
    /**
     * 当前百分比字体颜色
     */
    public int currentPercentColor;
    /**
     * 当前百分比字体大小
     */
    public float currentPercentTextSize;
    /**
     * 进度条背景画笔
     */
    private Paint bgPaint;
    /**
     * 边框线画笔
     */
    private Paint strokePaint;
    /**
     * 进度画笔
     */
    private Paint progressPaint;
    /**
     * 进度百分比画笔
     */
    private Paint percentPaint;
    /**
     * 进度条背景矩形, 当前进度矩形
     */
    private RectF fullSizeProgressbarRectf, progressRectf;
    /**
     * 进度条矩形圆角半径（进度条高度的一半）
     */
    private float rectRadius;
    private Path path;
    private float[] radii;

    public RoundRectProgressbar(Context context) {
        this(context, null);
    }

    public RoundRectProgressbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRectProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RoundRectProgressbar);
        //进度条背景颜色
        progressBgColor = typedArray.getColor(R.styleable.
                RoundRectProgressbar_progress_unreached_color, Color.parseColor(DEFAULT_BACKGROUND_COLOR));
        //当前进度颜色
        currentProgressColor = typedArray.getColor(R.styleable.
                RoundRectProgressbar_progress_reached_color, Color.parseColor(DEFAULT_PROGRESS_COLOR));
        currentPercentColor = typedArray.getColor(R.styleable.RoundRectProgressbar_progress_percent_color,
                Color.parseColor(DEFAULT_PERCENT_COLOR));
        float defaultTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_PERCENT_TEXTSIZE,
                context.getResources().getDisplayMetrics());
        typedArray.getDimension(R.styleable.RoundRectProgressbar_progress_percent_textSize, defaultTextSize);
        typedArray.recycle();

        initPaint();

        fullSizeProgressbarRectf = new RectF();
        progressRectf = new RectF();

        rectRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_RECT_RADIUS,
                getResources().getDisplayMetrics());

        path = new Path();

    }

    private void initPaint() {
        bgPaint = new Paint();
        bgPaint.setColor(progressBgColor);
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setColor(currentProgressColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(1);

        progressPaint = new Paint();
        progressPaint.setColor(currentProgressColor);
        progressPaint.setAntiAlias(true);

        percentPaint = new Paint();
        percentPaint.setColor(currentPercentColor);
        percentPaint.setTextSize(currentPercentTextSize);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            //高度为wrap_content, 直接设置为2倍默认半径
            height = (int) (2 * rectRadius);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else if (heightMode == MeasureSpec.EXACTLY) {
            rectRadius = height / 2f;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //左上，左下圆角
        radii = new float[]{rectRadius, rectRadius, 0f, 0f, 0f, 0f, rectRadius, rectRadius};

        correctPercentTextSize(h);
    }

    /**
     * 百分比字体大小校正
     * @param h 进度条高度
     */
    private void correctPercentTextSize(int h) {
        float textHeight = percentPaint.descent() - percentPaint.ascent();
        if (textHeight > h) {
            percentPaint.setTextSize(percentPaint.getTextSize() - 1);
            correctPercentTextSize(h);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        int progressbarWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        //save之后，可以调用Canvas的平移、放缩、旋转、错切、裁剪等操作
        canvas.save();
        //平移到paddingLeft的地方再开始绘制
        canvas.translate(getPaddingLeft(), 0);

        //绘制进度条背景
        canvas.drawRoundRect(fullSizeProgressbarRectf, rectRadius, rectRadius, bgPaint);

        //绘制边框线
        fullSizeProgressbarRectf.set(0, 0, progressbarWidth, rectRadius * 2);
        canvas.drawRoundRect(fullSizeProgressbarRectf, rectRadius, rectRadius, strokePaint);

        drawProgress(progressbarWidth);

        //绘制进度条中间的百分比数值
        float rate = getProgress() * 1f / getMax() * 100;
        double roundedRate = new BigDecimal(rate).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        String percent = roundedRate + "%";
        float textWidth = percentPaint.measureText(percent);
        float x = (progressbarWidth - textWidth) / 2;
        float textHeight = percentPaint.descent() - percentPaint.ascent();
        float y = rectRadius - textHeight / 2;
        canvas.drawText(percent, x, y, percentPaint);

        canvas.drawPath(path, progressPaint);
        //用来恢复Canvas之前保存的状态
        canvas.restore();

    }

    /**
     * 绘制进度
     * @param progressbarWidth 进度条总宽度
     */
    private void drawProgress(int progressbarWidth) {
        //绘制当前进度
        float currentProgressWidth = progressbarWidth * getProgress() * 1f / getMax();
        path.reset();
        if (currentProgressWidth < rectRadius) {
            //进度宽度小于圆角半径
            //计算当前矩形高度
            double halfRectHeight = Math.sqrt(Math.pow(rectRadius, 2) - Math.pow(rectRadius - currentProgressWidth, 2));
            Log.i(TAG, "onDraw: " + halfRectHeight);

            double halfSweepAngleValue = Math.asin(halfRectHeight / rectRadius);
            float halfSweepAngle = (float) Math.toDegrees(halfSweepAngleValue);

            progressRectf.set(0, 0, rectRadius * 2, getHeight());
            path.addArc(progressRectf, 180 - halfSweepAngle, halfSweepAngle * 2);
        } else if (currentProgressWidth >= progressbarWidth - rectRadius) {
            //进度大于圆角半径 + 中部矩形长度
            float rightRadiiX = currentProgressWidth + rectRadius - progressbarWidth;
            float rightRadiiY = (float) (rectRadius - Math.sqrt(Math.pow(rectRadius, 2) - Math.pow(rightRadiiX, 2)));
            radii[2] = rightRadiiX;
            radii[3] = rightRadiiY;
            radii[4] = rightRadiiX;
            radii[5] = rightRadiiY;

            progressRectf.set(0, 0, currentProgressWidth, rectRadius * 2);
            path.addRoundRect(progressRectf, radii, Path.Direction.CW);
        } else {
            //进度宽度在中间矩形区域
            //右上，右下直角
            radii[2] = 0f;
            radii[3] = 0f;
            radii[4] = 0f;
            radii[5] = 0f;

            progressRectf.set(0, 0, currentProgressWidth, rectRadius * 2);
            path.addRoundRect(progressRectf, radii, Path.Direction.CW);
        }
    }

    public void setCurrentProgressColor(@ColorInt int currentProgressColor) {
        this.currentProgressColor = currentProgressColor;
        progressPaint.setColor(currentProgressColor);
    }
}