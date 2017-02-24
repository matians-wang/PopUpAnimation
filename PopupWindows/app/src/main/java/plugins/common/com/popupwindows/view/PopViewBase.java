package plugins.common.com.popupwindows.view;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by b916 on 17-2-7.
 */

public abstract class PopViewBase extends FrameLayout implements AnimationView.OnViewAnimatorListener{

    private View contentView;
    private AnimationView animationView;

    public PopViewBase(Context context) {
        super(context);
    }

    public PopViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initAfter() {
        contentView = getContentView();
        FrameLayout fl = new FrameLayout(getContext());
        fl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        fl.addView(contentView);
        animationView = new AnimationView(getContext());
        animationView.setLayoutParams(contentView.getLayoutParams());
        animationView.setDrawView(contentView);
        animationView.setOnViewAnimatorListener(this);
        fl.addView(animationView);
        removeAllViews();
        addView(fl);
        setMeasureAllChildren(true);
        measure(MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));
        animationView.setLayoutParams(new LayoutParams(getMeasuredWidth(), getMeasuredHeight()));
        animationView.setMinRadius(0);
        animationView.initBeforeOnDraw();
    }

    protected abstract View getContentView();

    @Override
    public void onAnimatorOpenStart() {
        contentView.setVisibility(GONE);
        animationView.setVisibility(VISIBLE);
    }

    @Override
    public void onAnimatorOpenEnd() {
        animationView.setVisibility(GONE);
        contentView.setVisibility(VISIBLE);
    }

    @Override
    public void onAnimatorCloseStart() {
        contentView.setVisibility(GONE);
        animationView.setVisibility(VISIBLE);
    }

    @Override
    public void onAnimatorCloseEnd() {
        contentView.setVisibility(VISIBLE);
        animationView.setVisibility(GONE);
    }

    public void onExpandAnimator(AnimatorSet animatorSet) {
        if (animationView != null) {
            animatorSet.playTogether(animationView.getOpenValueAnimator());
        }
    }

    public void onCollapseAnimator(AnimatorSet animatorSet) {
        if (animationView != null) {
            animatorSet.playTogether(animationView.getCloseValueAnimator());
        }
    }
}
