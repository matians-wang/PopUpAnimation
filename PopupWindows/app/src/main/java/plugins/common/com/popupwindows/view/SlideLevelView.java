package plugins.common.com.popupwindows.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import plugins.common.com.popupwindows.R;

/**
 * Created by b916 on 17-2-17.
 */

public class SlideLevelView extends View {

    private int mScreenHeight;
    private int mScreenWidth;
    private Drawable drawableBackground;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;
    private int mItemIndicatorWidth;
    private int mItemIndicatorHeight;
    private boolean mIsFirstDraw = true;
    private Drawable drawableItemBackground;
    private int mPosition;
    private SlideIndicator mSlideIndicator;
    private boolean mIsSelected = false;
    private int contentW;
    private int contentH;
    private boolean mFirstDraw = true;
    private Rect mRect;
    private Paint mIndicatorPaint;
    private int mHalfItemWidth;
    private int mHalfItemHeight;
    private int mPerWidth;
    private SlildePosition mClosedIndex = SlildePosition.MIDDLE;
    private boolean mIsAnimationPlay;
    private OnSeekPositionListener mOnSeekPositionListener;
    private int mHalfBackgroundHeight;


    public SlideLevelView(Context context) {
        this(context,null);
    }

    public SlideLevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        mIsFirstDraw = true;
        mIndicatorPaint = new Paint();
        mRect = new Rect();
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.slidemoment, defStyleAttr, 0);
        drawableBackground = typedArray.getDrawable(R.styleable.slidemoment_slide_base_background);
        drawableItemBackground = typedArray.getDrawable(R.styleable.slidemoment_slide_item_background);
        marginLeft = (int)typedArray.getDimension(R.styleable.slidemoment_slide_base_margin_left, 0.0f);
        marginRight = (int)typedArray.getDimension(R.styleable.slidemoment_slide_base_margin_right, 0.0f);
        marginTop = (int)typedArray.getDimension(R.styleable.slidemoment_slide_base_margin_top, 0.0f);
        marginBottom = (int)typedArray.getDimension(R.styleable.slidemoment_slide_base_margin_bottom, 0.0f);
        mItemIndicatorWidth = (int) (typedArray.getDimension(R.styleable.slidemoment_slide_item_width, 0.0f));
        mItemIndicatorHeight = (int) typedArray.getDimension(R.styleable.slidemoment_slide_item_height, 0.0f);
        mSlideIndicator = new SlideIndicator(mItemIndicatorWidth, mItemIndicatorHeight);
        mSlideIndicator.setSlideDrawable(drawableItemBackground);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        contentW = widthSize - getPaddingLeft() - getPaddingRight();
        contentH = heightSize - getPaddingTop() - getPaddingBottom();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPerWidth = (w - marginLeft - marginRight) / SlildePosition.values().length;
        mHalfBackgroundHeight = drawableBackground.getIntrinsicHeight()/2;
        System.out.println("back==="+drawableBackground.getIntrinsicHeight());
        System.out.println("backMin==="+drawableBackground.getMinimumHeight());
        seekPosition();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFirstDraw) {
            Rect rectBackground = new Rect();
            rectBackground.set(marginLeft, marginTop, contentW - marginRight, contentH - marginBottom);
            drawableBackground.setBounds(rectBackground);
            mFirstDraw = false;
            mHalfItemWidth = mItemIndicatorWidth /2;
            mHalfItemHeight = mItemIndicatorHeight/2;
        }
        drawableBackground.draw(canvas);
        mRect.set(mSlideIndicator.getCurX() , marginTop+ mHalfBackgroundHeight - mHalfItemHeight, mSlideIndicator.getCurX() + mItemIndicatorWidth, marginTop + mHalfBackgroundHeight+ mHalfItemHeight);

        mSlideIndicator.getSlideDrawble().setBounds(mRect);
        mSlideIndicator.getSlideDrawble().draw(canvas);

    }

    public void setItemPosition(SlildePosition slildePosition) {
        //this.mPosition = position;
        mClosedIndex = slildePosition;
        startAnimation(mSlideIndicator, 1000, slildePosition);
    }


    public enum SlildePosition {
        LEFT,MIDDLE,RIGHT;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsAnimationPlay) {
            return true;
        }
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //judge the action down position
                mIsSelected = checkPoint(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                move(x);
                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;

        }
        invalidate();
        if (mIsSelected) {

        }
        return true;
    }

    private void reset() {
        mClosedIndex = getClosedIndex(mSlideIndicator);
        startAnimation(mSlideIndicator,1000, mClosedIndex);
    }

    public interface OnSeekPositionListener {
        void onSeekPosition(SlildePosition slildePosition);
    }

    public void setOnSeekPositionListener(OnSeekPositionListener onSeekPositionListener) {
        this.mOnSeekPositionListener = onSeekPositionListener;
    }

    private void seekPosition() {
        if (mOnSeekPositionListener != null)
            mOnSeekPositionListener.onSeekPosition(mClosedIndex);
    }

    private void move(int x) {
        if ( x <= contentW - mItemIndicatorWidth) {
            mSlideIndicator.setCurX(x);
        }
        if (mSlideIndicator.getEnableTouch()) {

            return;
        }
    }

    public int getSlideDistance(SlildePosition slildePosition) {
        switch (slildePosition) {
            case LEFT:
                return getPaddingLeft()  + mPerWidth *0;
            case MIDDLE:
                return (int)(getPaddingLeft()  + mPerWidth * 1.5f - mHalfItemWidth);
            case RIGHT:
                return getPaddingLeft()  + mPerWidth *3 - mItemIndicatorWidth;
        }
        return 0;
    }

    private SlildePosition getClosedIndex(SlideIndicator slideIndicator) {
        int curX = slideIndicator.getCurX();
        int distance = Integer.MAX_VALUE;
        String slidePosition = "";
        for (SlildePosition slildePosition : SlildePosition.values()) {
            int abs = Math.abs(curX - getSlideDistance(slildePosition));
            if (abs <= distance) {
                distance = abs;
                slidePosition = slildePosition.name();
            }
        }
        return SlildePosition.valueOf(slidePosition);
    }

    private void startAnimation(SlideIndicator slideIndicator, long duration, SlildePosition slildePosition) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(slideIndicator, "curX", slideIndicator.getCurX(),getSlideDistance(slildePosition));
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mIsAnimationPlay = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                SlideIndicator indicator = (SlideIndicator)((ObjectAnimator) animation).getTarget();
                indicator.setCurX(getSlideDistance(mClosedIndex));
                mIsAnimationPlay = false;
                seekPosition();
            }


        });
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        objectAnimator.start();
    }

    private boolean checkPoint(int x, int y) {
        boolean contains = mSlideIndicator.getRect().contains(x, y);
        int right = mSlideIndicator.getRect().right;
        int left = mSlideIndicator.getRect().left;
        int top = mSlideIndicator.getRect().top;
        int bottom = mSlideIndicator.getRect().bottom;
        System.out.println("right =="+right+"left"+left+"top=="+top+"bottom=="+bottom);
        if (contains) {
            //命中目标,设置目标可以进行拖动
            mSlideIndicator.setEnableTouch(true);
            return true;
        }
        return false;
    }


    public class SlideIndicator {
        private int mSlideWidth;
        private int mSlideHeight;
        private Drawable mSlideDrawable;
        private int mCurX;
        private int mCurY;
        private Rect mRect = new Rect();
        private boolean mEnableTouch;

        public SlideIndicator(int slideWidth, int slideHeight) {
            this.mSlideWidth = slideWidth;
            this.mSlideHeight = slideHeight;
        }

        public void setSlideDrawable(Drawable drawable) {
            this.mSlideDrawable = drawable;
        }

        public Drawable getSlideDrawble() {
            return mSlideDrawable;
        }

        public Rect getRect() {
            mRect.set(mCurX-mSlideWidth, mCurY - mSlideHeight, mCurX+ mSlideWidth, mCurY+mSlideHeight);
            return mRect;
        }

        public void setEnableTouch(boolean enableTouch) {
            mEnableTouch = enableTouch;
        }

        public boolean getEnableTouch() {
            return mEnableTouch;
        }

        public void setCurX(int curX) {
            this.mCurX = curX;
        }

        public void setCurY(int curY) {
            this.mCurY = curY;
        }

        public int getCurX() {
            return mCurX;
        }

        public int getCurY() {
            return mCurY;
        }
    }
}
