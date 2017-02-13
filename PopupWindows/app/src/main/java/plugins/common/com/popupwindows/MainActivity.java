package plugins.common.com.popupwindows;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListPopView popView;
    private AnimatorSet animatorSet;
    private WindowManager.LayoutParams params;
    private WindowManager wm;
    private boolean isExpanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button)findViewById(R.id.button);
        popView = (ListPopView)findViewById(R.id.list_pop);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExpanded) {
                    expandContent();
                } else {
                    collapseContent();
                }

            }
        });

        List<ListItem> cardItems = new ArrayList<>();
        cardItems.add(new ListItem().setResName("test1").setResId(R.mipmap.head_test_a));
        cardItems.add(new ListItem().setResName("test2").setResId(R.mipmap.head_test_b));
        cardItems.add(new ListItem().setResName("test3").setResId(R.mipmap.head_test_c));
        cardItems.add(new ListItem().setResName("test4").setResId(R.mipmap.head_test_d));
        popView.setList(cardItems);
        popView.initAfter();
    }

    private void expandContent() {
        AnimatorSet animatorSet = new AnimatorSet();
        isExpanded = true;
        popView.onExpandAnimator(animatorSet);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                popView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                popView.setVisibility(View.VISIBLE);
                isExpanded = true;
            }
        });
        animatorSet.start();
    }
    private void collapseContent() {
        AnimatorSet animatorSet = new AnimatorSet();
        isExpanded = false;
        popView.onCollapseAnimator(animatorSet);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                popView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                popView.setVisibility(View.GONE);
                isExpanded = false;
            }
        });
        animatorSet.start();
    }
}
