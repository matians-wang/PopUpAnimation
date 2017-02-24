package plugins.common.com.popupwindows.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
        mPerWidth = (w -getPaddingLeft() - getPaddingRight() - mItemIndicatorWidth )/3;

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
        System.out.println("cux=="+mSlideIndicator.getCurX());
        System.out.println("marginTop===="+marginTop);
        mRect.set(mSlideIndicator.getCurX()- mHalfItemWidth + 40, marginTop+ 20, mSlideIndicator.getCurX() + mHalfItemWidth, marginTop + mItemIndicatorHeight);

        mSlideIndicator.getSlideDrawble().setBounds(mRect);
        mSlideIndicator.getSlideDrawble().draw(canvas);

    }

    public void setItemPosition(int position) {
        this.mPosition = position;
    }


    public enum SlildePosition {
        LEFT,MIDDLE,RIGHT;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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

        SlildePosition closedIndex = getClosedIndex(mSlideIndicator);
        System.out.println("closeIndex==="+closedIndex);
        startAnimation(mSlideIndicator,1000,closedIndex);
    }

    private void move(int x) {
        if ( x < contentW) {
            mSlideIndicator.setCurX(x);
        }
        if (mSlideIndicator.getEnableTouch()) {

            return;
        }
    }

    public int getSlideDistance(SlildePosition slildePosition) {
        switch (slildePosition) {
            case LEFT:
                return getPaddingLeft() + mHalfItemWidth + mPerWidth *0;
            case MIDDLE:
                return getPaddingLeft() + mHalfItemWidth + mPerWidth * 1;
            case RIGHT:
                return getPaddingLeft() + mHalfItemWidth + mPerWidth *2;
        }
        return 0;
    }

    private SlildePosition getClosedIndex(SlideIndicator slideIndicator) {
        int curX = slideIndicator.getCurX();
        int distance = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i< SlildePosition.values().length; i++) {

            System.out.println("i==="+i);
        }
        return SlildePosition.MIDDLE;
    }

    private void startAnimation(SlideIndicator slideIndicator, long duration, SlildePosition slildePosition) {
        System.out.println("getSlideDistance(slildePosition)==="+getSlideDistance(slildePosition));
        System.out.println("getCurx=="+slideIndicator.getCurX());
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(slideIndicator, "mCurX", slideIndicator.getCurX(),getSlideDistance(slildePosition));
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
System.out.println("animation start");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

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


    private Bitmap getLargeBitmap(int resId, int picSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),resId,options);
        int simpleSize = options.outWidth /picSize;
        if (simpleSize > 1) {
            simpleSize = 1;
        }
        options.inSampleSize = simpleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resId, options), picSize, picSize, true);
    }

    private Bitmap getSmallBitmap(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        return Bitmap.createScaledBitmap(bitmap, mScreenWidth-50, bitmap.getHeight(), false);
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
