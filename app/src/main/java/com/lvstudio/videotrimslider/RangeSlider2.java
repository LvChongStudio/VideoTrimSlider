package com.lvstudio.videotrimslider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * @author lvstudio
 */
public class RangeSlider2 extends ViewGroup {

    private static final int DEFAULT_LINE_SIZE = 1;
    private static final int DEFAULT_THUMB_WIDTH = 7;
    private static final int DEFAULT_NOTICE_SIZE = 20;
    private static final int DEFAULT_TICK_START = 0;
    private static final int DEFAULT_TICK_END = 5;
    private static final int DEFAULT_TICK_INTERVAL = 1;
    private static final int DEFAULT_MASK_BACKGROUND = 0xA0000000;
    private static final int DEFAULT_LINE_COLOR = 0xFF000000;

    private final Paint mLinePaint, mBgPaint, mTextPaint;
    private final ThumbView mLeftThumb, mRightThumb;

    private int mTouchSlop;
    private int mOriginalX, mLastX;

    private int mThumbWidth;

    private int mTickStart = DEFAULT_TICK_START;
    private int mTickEnd = DEFAULT_TICK_END;
    private int mTickInterval = DEFAULT_TICK_INTERVAL;
    private int mTickCount = (mTickEnd - mTickStart) / mTickInterval;

    private float mLineSize;

    private boolean mIsDragging;
    private boolean mIsSelectMoving;

    private OnRangeChangeListener mRangeChangeListener;

    public RangeSlider2(Context context) {
        this(context, null);
    }

    public RangeSlider2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeSlider2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RangeSlider, 0, 0);
        mThumbWidth = array.getDimensionPixelOffset(R.styleable.RangeSlider_thumbWidth, DEFAULT_THUMB_WIDTH);
        mLineSize = array.getDimensionPixelOffset(R.styleable.RangeSlider_lineHeight, DEFAULT_LINE_SIZE);
        mBgPaint = new Paint();
        mBgPaint.setColor(array.getColor(R.styleable.RangeSlider_maskColor, DEFAULT_MASK_BACKGROUND));

        mLinePaint = new Paint();
        mLinePaint.setColor(array.getColor(R.styleable.RangeSlider_lineColor, DEFAULT_LINE_COLOR));

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(array.getDimensionPixelSize(R.styleable.RangeSlider_noticeTextSize, DEFAULT_NOTICE_SIZE));
        mTextPaint.setAntiAlias(true);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        Drawable lDrawable = array.getDrawable(R.styleable.RangeSlider_leftThumbDrawable);
        Drawable rDrawable = array.getDrawable(R.styleable.RangeSlider_rightThumbDrawable);
        mLeftThumb = new ThumbView(context, mThumbWidth, lDrawable == null ? new ColorDrawable(DEFAULT_LINE_COLOR) : lDrawable);
        mRightThumb = new ThumbView(context, mThumbWidth, rDrawable == null ? new ColorDrawable(DEFAULT_LINE_COLOR) : rDrawable);
        setTickCount(array.getInteger(R.styleable.RangeSlider_tickCount, DEFAULT_TICK_END));
        setRangeIndex(array.getInteger(R.styleable.RangeSlider_leftThumbIndex, DEFAULT_TICK_START),
                array.getInteger(R.styleable.RangeSlider_rightThumbIndex, mTickCount));
        array.recycle();

        addView(mLeftThumb);
        addView(mRightThumb);

        setWillNotDraw(false);
    }

    public void setThumbWidth(int thumbWidth) {
        mThumbWidth = thumbWidth;
        mLeftThumb.setThumbWidth(thumbWidth);
        mRightThumb.setThumbWidth(thumbWidth);
    }

    public void setLeftThumbDrawable(Drawable drawable) {
        mLeftThumb.setThumbDrawable(drawable);
    }

    public void setRightThumbDrawable(Drawable drawable) {
        mRightThumb.setThumbDrawable(drawable);
    }

    public void setLineColor(int color) {
        mLinePaint.setColor(color);
    }

    public void setLineSize(float lineSize) {
        mLineSize = lineSize;
    }

    public void setMaskColor(int color) {
        mBgPaint.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLeftThumb.measure(widthMeasureSpec, heightMeasureSpec);
        mRightThumb.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int lThumbWidth = mLeftThumb.getMeasuredWidth();
        final int lThumbHeight = mLeftThumb.getMeasuredHeight();
        mLeftThumb.layout(0, 0, lThumbWidth, lThumbHeight);
        mRightThumb.layout(0, 0, lThumbWidth, lThumbHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        moveThumbByIndex(mLeftThumb, mLeftThumb.getRangeIndex());
        moveThumbByIndex(mRightThumb, mRightThumb.getRangeIndex());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        final int lThumbWidth = mLeftThumb.getMeasuredWidth();
        final float lThumbOffset = mLeftThumb.getX();
        final float rThumbOffset = mRightThumb.getX();

        final float lineTop = mLineSize;
        final float lineBottom = height - mLineSize;


        // top line
        canvas.drawRect(lThumbWidth + lThumbOffset,
                0,
                rThumbOffset,
                lineTop, mLinePaint);

        // bottom line
        canvas.drawRect(lThumbWidth + lThumbOffset,
                lineBottom,
                rThumbOffset,
                height, mLinePaint);


        if (mIsSelectMoving) {
            canvas.drawRect(lThumbOffset + mThumbWidth, lineTop, rThumbOffset, lineBottom, mLinePaint);
        } else {
            canvas.drawRect(lThumbOffset + mThumbWidth, lineTop, rThumbOffset, lineBottom, mBgPaint);
        }

        String notice = (100f * (mRightThumb.getRangeIndex() - mLeftThumb.getRangeIndex()) / mTickCount) + "%";
        float noticeLength = mTextPaint.measureText(notice);
        canvas.drawText(notice,
                (rThumbOffset + mThumbWidth + lThumbOffset) / 2 - noticeLength / 2,
                getHeight() / 2 - ((mTextPaint.descent() + mTextPaint.ascent()) / 2),
                mTextPaint);

        canvas.drawText((100f * mLeftThumb.getRangeIndex() / mTickCount) + "%",
                lThumbOffset + mThumbWidth,
                (mTextPaint.descent() + mTextPaint.ascent()),
                mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        boolean handle = false;
        mIsSelectMoving = false;

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();

                mLastX = mOriginalX = x;
                mIsDragging = false;

                if (!mLeftThumb.isPressed() && mLeftThumb.inInTarget(x, y)) {
                    mLeftThumb.setPressed(true);
                    handle = true;
                } else if (!mRightThumb.isPressed() && mRightThumb.inInTarget(x, y)) {
                    mRightThumb.setPressed(true);
                    handle = true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsDragging = false;
                mOriginalX = mLastX = 0;
                getParent().requestDisallowInterceptTouchEvent(false);
                if (mLeftThumb.isPressed()) {
                    releaseLeftThumb();
                    invalidate();
                    handle = true;
                } else if (mRightThumb.isPressed()) {
                    releaseRightThumb();
                    invalidate();
                    handle = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                x = (int) event.getX();

                if (!mIsDragging && Math.abs(x - mOriginalX) > mTouchSlop) {
                    mIsDragging = true;
                }
                if (mIsDragging) {
                    int moveX = x - mLastX;
                    if (mLeftThumb.isPressed()) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        moveLeftThumbByPixel(moveX);
                        handle = true;
                        invalidate();
                    } else if (mRightThumb.isPressed()) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        moveRightThumbByPixel(moveX);
                        handle = true;
                        invalidate();
                    }
                }

                mLastX = x;
                break;
            default:
                break;
        }

        return handle;
    }

    private boolean isValidTickCount(int tickCount) {
        return (tickCount > 1);
    }

    private boolean indexOutOfRange(int leftThumbIndex, int rightThumbIndex) {
        return (leftThumbIndex < 0 || leftThumbIndex > mTickCount
                || rightThumbIndex < 0
                || rightThumbIndex > mTickCount);
    }

    private float getRangeLength() {
        int width = getMeasuredWidth();
        if (width < mThumbWidth) {
            return 0;
        }
        return width - mThumbWidth;
    }

    private float getIntervalLength() {
        return getRangeLength() / mTickCount;
    }

    public int getNearestIndex(float x) {
        return Math.round(x / getIntervalLength());
    }

    public int getLeftIndex() {
        return mLeftThumb.getRangeIndex();
    }

    public int getRightIndex() {
        return mRightThumb.getRangeIndex();
    }

    private void notifyRangeChange() {
        if (mRangeChangeListener != null) {
            mRangeChangeListener.onRangeChange(this, mLeftThumb.getRangeIndex(), mRightThumb.getRangeIndex(), mTickCount);
        }
    }

    public void setRangeChangeListener(OnRangeChangeListener rangeChangeListener) {
        mRangeChangeListener = rangeChangeListener;
    }

    /**
     * Sets the tick count in the RangeSlider.
     *
     * @param count Integer specifying the number of ticks.
     */
    public void setTickCount(int count) {
        int tickCount = (count - mTickStart) / mTickInterval;
        if (isValidTickCount(tickCount)) {
            mTickEnd = count;
            mTickCount = tickCount;
            mRightThumb.setTickIndex(mTickCount);
        } else {
            throw new IllegalArgumentException("tickCount less than 2; invalid tickCount.");
        }
    }

    /**
     * The location of the thumbs according by the supplied index.
     * Numbered from 0 to mTickCount - 1 from the left.
     *
     * @param leftIndex  Integer specifying the index of the left thumb
     * @param rightIndex Integer specifying the index of the right thumb
     */
    public void setRangeIndex(int leftIndex, int rightIndex) {
        if (indexOutOfRange(leftIndex, rightIndex)) {
            throw new IllegalArgumentException(
                    "Thumb index left " + leftIndex + ", or right " + rightIndex
                            + " is out of bounds. Check that it is greater than the minimum ("
                            + mTickStart + ") and less than the maximum value ("
                            + mTickEnd + ")");
        } else {
            if (mLeftThumb.getRangeIndex() != leftIndex) {
                mLeftThumb.setTickIndex(leftIndex);
            }
            if (mRightThumb.getRangeIndex() != rightIndex) {
                mRightThumb.setTickIndex(rightIndex);
            }
        }
    }

    private boolean moveThumbByIndex(ThumbView view, int index) {
        view.setX(index * getIntervalLength());
        if (view.getRangeIndex() != index) {
            view.setTickIndex(index);
            return true;
        }
        return false;
    }

    private void moveLeftThumbByPixel(int pixel) {
        float x = mLeftThumb.getX() + pixel;
        float interval = getIntervalLength();
        float start = mTickStart / mTickInterval * interval;
        float end = mTickEnd / mTickInterval * interval;

        if (x < start) {
            x = start;
        }


        if (x >= start && x <= end && x < mRightThumb.getX() - mThumbWidth) {
            mLeftThumb.setX(x);
            int index = getNearestIndex(x);
            if (mLeftThumb.getRangeIndex() != index) {
                mLeftThumb.setTickIndex(index);
                notifyRangeChange();
            }
        }
    }

    private boolean canMoveLeft() {
        float x = mLeftThumb.getX();
        float interval = getIntervalLength();
        float start = mTickStart / mTickInterval * interval;
        float end = mTickEnd / mTickInterval * interval;

        if (x > start && x < end && x < mRightThumb.getX() - mThumbWidth) {
            return true;
        } else {
            return false;
        }
    }

    private void moveRightThumbByPixel(int pixel) {
        float x = mRightThumb.getX() + pixel;
        float interval = getIntervalLength();
        float start = mTickStart / mTickInterval * interval;
        float end = mTickEnd / mTickInterval * interval;

        if (x > end) {
            x = end;
        }

        if (x >= start && x <= end && x > mLeftThumb.getX() + mThumbWidth) {
            mRightThumb.setX(x);
            int index = getNearestIndex(x);
            if (mRightThumb.getRangeIndex() != index) {
                mRightThumb.setTickIndex(index);
                notifyRangeChange();
            }
        }
    }

    private boolean canMoveRight() {
        float x = mRightThumb.getX();
        float interval = getIntervalLength();
        float start = mTickStart / mTickInterval * interval;
        float end = mTickEnd / mTickInterval * interval;

        if (x > start && x < end && x > mLeftThumb.getX() + mThumbWidth) {
            return true;
        } else {
            return false;
        }
    }

    private void releaseLeftThumb() {
        int index = getNearestIndex(mLeftThumb.getX());
        int endIndex = mRightThumb.getRangeIndex();
        if (index >= endIndex) {
            index = endIndex - 1;
        }
        if (moveThumbByIndex(mLeftThumb, index)) {
            notifyRangeChange();
        }
        mLeftThumb.setPressed(false);
    }

    private void releaseRightThumb() {
        int index = getNearestIndex(mRightThumb.getX());
        int endIndex = mLeftThumb.getRangeIndex();
        if (index <= endIndex) {
            index = endIndex + 1;
        }
        if (moveThumbByIndex(mRightThumb, index)) {
            notifyRangeChange();
        }
        mRightThumb.setPressed(false);
    }

    public interface OnRangeChangeListener {
        void onRangeChange(RangeSlider2 view, int leftPinIndex, int rightPinIndex, int totalCount);
    }

}
