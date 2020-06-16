package com.xys.zxinglib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * 自定义雷达图
 * 1、构造函数（初始化）
 * 2、onMeasure（测量大小）
 * 3、onSizeChanged（确定大小）
 * 4、onLayout（子view的位置，如果包含子view的话）
 * 5、onDraw（绘制内容）
 * 6、暴露给外部的接口
 */
public class CustomRadarView extends View {
    //宽度
    private int mWidth;
    //高度
    private int mHeight;
    //原点坐标
    private int mCenterX;
    private int mCenterY;
    //网格半径
    private float mRadius;
    //网格圈数
    private int mCount = 10;
    //Paint
    private Paint mLinePaint;
    private Paint mValuePaint;
    private Paint mTextPaint;
    //颜色值
    private int mLineColor = Color.GRAY;
    private int mValueColor = Color.BLUE;
    private int mTextColor = Color.BLACK;

    //最后一圈网格坐标点集合
    private ArrayList<LastPoint> mLastPoints = new ArrayList<LastPoint>();
    //数据坐标点集合
    private ArrayList<LastPoint> mDataPoints = new ArrayList<LastPoint>();
    //文本集合
    private String[] mTitles = new String[]{"科目A", "科目A1", "科目1", "科目D", "科目E", "科目F"};

    public CustomRadarView(Context context) {
        super(context);
        init(context);
    }

    public CustomRadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomRadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        //中心坐标
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        mRadius = (Math.min(mWidth, mHeight) / 2 * 0.9f);
        postInvalidate();
    }

    private void init(Context context) {
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(mLineColor);

        mValuePaint = new Paint();
        mValuePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mValuePaint.setStrokeWidth(2);
        mValuePaint.setColor(mValueColor);

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(28);
        mTextPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //移动原点坐标
        canvas.translate(mCenterX, mCenterY);
        //画网格线
        drawLine(canvas);
        //画网格对角线
        drawGridLine(canvas);
        //画文本
        drawText(canvas);
        //画数据线
        drawDataLine(canvas);

    }

    /**
     * 画网格
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        Path path = new Path();
        //网格线之间的间距
        float distance = mRadius / (mCount - 1);
        for (int i = 0; i < mCount; i++) {//外面的网格图形
            float currentRadius = i * distance;//当前半径
            if (i == mCount - 1) {
                //存储最后一圈网格的点的坐标
                mLastPoints.add(new LastPoint(currentRadius, 0));
                mLastPoints.add(new LastPoint(currentRadius / 2, -currentRadius));
                mLastPoints.add(new LastPoint(-currentRadius / 2, -currentRadius));
                mLastPoints.add(new LastPoint(-currentRadius, 0));
                mLastPoints.add(new LastPoint(-currentRadius / 2, currentRadius));
                mLastPoints.add(new LastPoint(currentRadius / 2, currentRadius));
            }
            //6个点坐标组成一个网格图形
            path.lineTo(currentRadius, 0);
            //设置上一次操作的坐标点
            path.moveTo(currentRadius, 0);
            path.lineTo(currentRadius / 2, -currentRadius);
            path.lineTo(-currentRadius / 2, -currentRadius);
            path.lineTo(-currentRadius, 0);
            path.lineTo(-currentRadius / 2, currentRadius);
            path.lineTo(currentRadius / 2, currentRadius);
            path.close();
            canvas.drawPath(path, mLinePaint);
        }
    }

    /**
     * 画网格对角线
     *
     * @param canvas
     */
    private void drawGridLine(Canvas canvas) {
        Path path = new Path();
        for (int i = 0; i < mLastPoints.size(); i++) {
            path.reset();
            LastPoint point = mLastPoints.get(i);
            float x = point.x;
            float y = point.y;
            path.lineTo(x, y);
            canvas.drawPath(path, mLinePaint);
        }
    }

    /**
     * 画文本
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        for (int i = 0; i < mLastPoints.size(); i++) {
            //文本长度
            float dis = mTextPaint.measureText(mTitles[i]);

            LastPoint point = mLastPoints.get(i);
            float x = point.x;
            float y = point.y;
            if (i == 2 || i == 3 || i == 4) {
                //左边绘制文本：文本显示在坐标左边
                x = x - dis;
            }
            if (y > 0) {
                y += 18;
            }
            canvas.drawText(mTitles[i], x, y, mTextPaint);
        }
    }

    /**
     * 画数据线:填充区域
     *
     * @param canvas
     */
    private void drawDataLine(Canvas canvas) {
        if (mDataPoints == null || mDataPoints.size() == 0)
            return;
        Path path = new Path();
        for (int i = 0; i < mDataPoints.size(); i++) {
            LastPoint point = mDataPoints.get(i);
            float x = point.x;
            float y = point.y;
            path.lineTo(x, y);
            if (i == 0) {//将上一次操作点移到第一个点坐标，保证最后调用close，形成一个封闭的形状
                path.moveTo(x, y);
            }
            mValuePaint.setAlpha(255);
            //画小圆点
            canvas.drawCircle(x, y, 8, mValuePaint);
        }
        path.close();
        mValuePaint.setAlpha(127);
        canvas.drawPath(path, mValuePaint);

    }

    /**
     * 设置数据
     *
     * @param points
     */
    public void setData(ArrayList<LastPoint> points) {
        mDataPoints = points;
        invalidate();
    }

    /**
     * 设置文本
     *
     * @param titles
     */
    public void setTitles(String[] titles) {
        mTitles = titles;
        invalidate();
    }

    /**
     * 设置圈数
     *
     * @param count
     */
    public void setCount(int count) {
        mCount = count;
        invalidate();
    }

    /**
     * 设置网格线颜色
     *
     * @param color
     */
    public void setLineColor(int color) {
        mLineColor = color;
        mLinePaint.setColor(mLineColor);
        invalidate();
    }

    /**
     * 设置填充区域颜色
     *
     * @param color
     */
    public void setValueColor(int color) {
        mValueColor = color;
        mValuePaint.setColor(mValueColor);
        invalidate();
    }

    /**
     * 设置文本颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        mTextColor = color;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }


    /**
     * 坐标点
     */
    public static class LastPoint {
        private float x;
        private float y;

        public LastPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}