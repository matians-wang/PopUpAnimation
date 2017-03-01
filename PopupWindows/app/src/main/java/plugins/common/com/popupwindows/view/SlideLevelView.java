package plugins.common.com.popupwindows.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;


import plugins.common.com.popupwindows.R;

/**
 * Created by b916 on 17-2-17.
 */

public class SlideLevelView extends View {

    private Drawable drawableBackground;
    private int mItemIndicatorWidth;
    private int mItemIndicatorHeight;
    private Drawable drawableItemBackground;
    private SlideIndicator mSlideIndicator;
    private boolean mIsSelected = false;
    private int contentW;
    private int contentH;
    private Rect mRect;
    private int mHalfItemWidth;
    private int mHalfItemHeight;
    private int mPerWidth;
    private SlildePosition mClosedIndex = SlildePosition.MIDDLE;
    public boolean mIsAnimationPlay;
    private OnSeekPositionListener mOnSeekPositionListener;
    private int mHalfBackgroundHeight;
    private static final int DEFALUT_TIME = 1000;
    private ObjectAnimator objectAnimator;


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
        mRect = new Rect();
        drawableBackground = getBackground();
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.slidemoment, defStyleAttr, 0);
        drawableItemBackground = typedArray.getDrawable(R.styleable.slidemoment_slide_item_background);
        int itemWidth = (int) (typedArray.getDimension(R.styleable.slidemoment_slide_item_width, 0.0f));
        mItemIndicatorWidth = itemWidth == 0 ? drawableItemBackground.getIntrinsicWidth() : itemWidth;
        int itemHeight = (int) typedArray.getDimension(R.styleable.slidemoment_slide_item_height, 0.0f);
        mItemIndicatorHeight = itemHeight == 0 ? drawableItemBackground.getIntrinsicHeight() : itemHeight;
        mHalfItemWidth = mItemIndicatorWidth /2;
        mHalfItemHeight = mItemIndicatorHeight/2;
        mSlideIndicator = new SlideIndicator(mItemIndicatorWidth, mItemIndicatorHeight);
        mSlideIndicator.setSlideDrawable(drawableItemBackground);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        contentW = widthSize - getPaddingLeft() - getPaddingRight();
        contentH = heightSize - getPaddingTop() - getPaddingBottom();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int heightMeasure;
        int widthMeasure;
        if (layoutParams == null) {
            throw  new RuntimeException("layoutparams is null");
        }
        if (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            //matchparent
            heightMeasure = MeasureSpec.makeMeasureSpec(contentH, MeasureSpec.EXACTLY);
        } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //wrapcontent, we just follow drawable dip height
            heightMeasure = MeasureSpec.makeMeasureSpec(drawableBackground.getIntrinsicHeight(), MeasureSpec.AT_MOST);
        } else {
            //exactly dip size
            heightMeasure = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY);
            //如果我们指定dip值，那么就以dip作为高度基准，来决定我们的indicator，paddingtop值。
            mHalfBackgroundHeight = layoutParams.height /2;
        }
        if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            widthMeasure = MeasureSpec.makeMeasureSpec(contentW, MeasureSpec.EXACTLY);
        } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            widthMeasure = MeasureSpec.makeMeasureSpec(drawableBackground.getIntrinsicWidth(), MeasureSpec.AT_MOST);
        } else {
            widthMeasure = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasure,heightMeasure);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPerWidth = (w - getPaddingLeft() - getPaddingRight()) / SlildePosition.values().length;
        mHalfBackgroundHeight = drawableBackground.getIntrinsicHeight()/2;
        seekPosition();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRect.set(mSlideIndicator.getCurX() , getPaddingTop()+ mHalfBackgroundHeight - mHalfItemHeight, mSlideIndicator.getCurX() + mItemIndicatorWidth, getPaddingTop() + mHalfBackgroundHeight+ mHalfItemHeight);
        mSlideIndicator.getSlideDrawble().setBounds(mRect);
        mSlideIndicator.getSlideDrawble().draw(canvas);
    }

    public void setItemPosition(SlildePosition slildePosition) {
        mClosedIndex = slildePosition;
        startAnimation(mSlideIndicator, DEFALUT_TIME, slildePosition);
    }


    public enum SlildePosition {
        LEFT(1),MIDDLE(2),RIGHT(3);
        private int slidePosition;
        private SlildePosition(int position) {
            this.slidePosition = position;
        }

        public static SlildePosition valueSlideOf(int slidePosition) {
            switch (slidePosition) {
                case 1:
                    return LEFT;
                case 2:
                    return MIDDLE;
                case 3:
                    return RIGHT;
                default:
                    return MIDDLE;
            }
        }

        public int getPositionValue() {
            return this.slidePosition;
        }
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
        if (mIsSelected) {
            invalidate();
        }
        return true;
    }

    private void reset() {
        mIsSelected = false;
        if (mSlideIndicator.getEnableTouch()) {
            mClosedIndex = getClosedIndex(mSlideIndicator);
            startAnimation(mSlideIndicator,DEFALUT_TIME, mClosedIndex);
        }
        mSlideIndicator.setEnableTouch(false);
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
        if (mSlideIndicator.getEnableTouch()) {
            //因为png图片的不规整，png外围有透明部分，*1.5是除去png右边透明部分。*0.5除去png左边透明部分
            if ( x > - mHalfItemWidth *0.5 && x <= contentW - mHalfItemWidth *1.5) {
                mSlideIndicator.setCurX(x);
            }
        }
    }

    public int getSlideDistance(SlildePosition slildePosition) {
        switch (slildePosition) {
            case LEFT:
                return (int) (getPaddingLeft()  + mPerWidth *0 - mHalfItemWidth*0.5);
            case MIDDLE:
                return (int)(getPaddingLeft()  + mPerWidth * 1.5f - mHalfItemWidth);
            case RIGHT:
                return (int) (getPaddingLeft()  + mPerWidth *3 - mHalfItemWidth *1.5);//*1.5是除去png透明部分。
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
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        objectAnimator = ObjectAnimator.ofInt(slideIndicator, "curX", slideIndicator.getCurX(),getSlideDistance(slildePosition));
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
        private int mCurX = (int) (0 - mHalfItemWidth * 0.5); //*0.5除去png左边透明部分
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
            mRect.set(mCurX, mCurY, mCurX+ mSlideWidth, mCurY+mSlideHeight);
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
