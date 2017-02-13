package plugins.common.com.popupwindows.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by b916 on 17-2-7.
 */

public class AnimationView extends View {

    private ValueAnimator valueAnimator;
    private Paint paint;
    private int currentRadius;
    private int measuredWidth;
    private int measuredHeight;
    private int radius;
    private int minRadius;
    private PorterDuffXfermode porterDuffXfermode;
    private OnViewAnimatorListener onViewAnimatorListener;
    private View view;
    private static final int DURATION_DEFAULT = 300;
    private Bitmap viewBitmap;

    public AnimationView(Context context) {
        super(context);
        init();
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        valueAnimator = new ValueAnimator();
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        paint = new Paint();
        paint.setAntiAlias(true);//抗锯齿
        paint.setColor(Color.GRAY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentRadius = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    public ValueAnimator getOpenValueAnimator(long duration) {
        valueAnimator.removeAllListeners();
        valueAnimator.setIntValues(minRadius, radius);
        valueAnimator.setDuration(duration);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (onViewAnimatorListener != null) {
                    onViewAnimatorListener.onAnimatorOpenStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                clearAnimation();
                if (onViewAnimatorListener != null) {
                    onViewAnimatorListener.onAnimatorOpenEnd();
                }
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator(0.6f));
        return valueAnimator;
    }

    public ValueAnimator getOpenValueAnimator() {
        return getOpenValueAnimator(DURATION_DEFAULT);
    }

    public ValueAnimator getCloseValueAnimator() {
        return getCloseValueAninmator(DURATION_DEFAULT);
    }

    public ValueAnimator getCloseValueAninmator(long duration) {
        valueAnimator.removeAllListeners();
        valueAnimator.setIntValues(radius, minRadius);
        valueAnimator.setDuration(duration);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (onViewAnimatorListener != null)
                onViewAnimatorListener.onAnimatorCloseStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                clearAnimation();
                if (onViewAnimatorListener != null) {
                    onViewAnimatorListener.onAnimatorCloseEnd();
                }
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator(0.6f));
        return valueAnimator;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        generateViewBitmap();
        canvas.drawColor(Color.TRANSPARENT);
        paint.setXfermode(null);
        canvas.drawCircle(measuredWidth - minRadius, measuredHeight - minRadius, currentRadius, paint);
        paint.setXfermode(porterDuffXfermode);
        if (viewBitmap != null) {
            //canvas.drawBitmap(viewBitmap, 0, 0 ,paint);
        }
    }

    public void setMinRadius(int minRadius) {
        this.minRadius = minRadius;
    }

    private void generateViewBitmap() {
        if (viewBitmap == null) {
            Bitmap bitmap = convertViewToBitmap(view);
            if (bitmap == null) {
                return;
            }
            viewBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
    }

    private Bitmap convertViewToBitmap(View view) {
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache();
    }


    public void initBeforeOnDraw() {
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
        radius = (int) Math.sqrt(measuredWidth * measuredWidth + measuredHeight * measuredHeight);//矩形直角线距离为元的半径。
        currentRadius = radius;
        generateViewBitmap();
        invalidate();
    }

    public void setDrawView(View view) {
        this.view = view;
    }

    public void setOnViewAnimatorListener (OnViewAnimatorListener onViewAnimatorListener) {
        this.onViewAnimatorListener = onViewAnimatorListener;
    }

    public interface OnViewAnimatorListener {
        public void onAnimatorOpenStart();
        public void onAnimatorOpenEnd();
        public void onAnimatorCloseStart();
        public void onAnimatorCloseEnd();
    }
}
