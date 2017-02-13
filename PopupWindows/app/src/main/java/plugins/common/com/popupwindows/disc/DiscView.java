package plugins.common.com.popupwindows.disc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import plugins.common.com.popupwindows.R;

/**
 * Created by b916 on 17-2-9.
 */

public class DiscView extends FrameLayout {

    private int screenWidth;
    private int screenHeight;
    private ImageView discBlackgound;
    private ViewPager discViewPager;
    private int lastPositionOffsetPixels;
    private int currentItem;
    private PlayStatuChange playStatusChangeListener;
    //music info list
    private List<MusicData> musicDataList = new ArrayList<>();
    //disc pic list
    private List<View> discLayoutList = new ArrayList<>();
    //disc animation list
    private List<ObjectAnimator> discAnimationList = new ArrayList<>();
    private ImageView needle;
    public static final int DURATION_NEEDLE_ANIAMTOR = 500;
    private NeedleAnimationStatus needleAnimationStatus = NeedleAnimationStatus.STILL_FAR_END;
    private MusicPlayStatus playStatus;
    private boolean ifNeedToStopNeedleAnimation = false;//标记唱针复位以后，是否还向唱片移动
    private boolean viewPagerIsOffset = false;
    private ObjectAnimator needleAnimator;

    public DiscView(Context context) {
        this(context,null);
    }

    public DiscView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = DisplayUtils.getScreenWidth(context);
        screenHeight = DisplayUtils.getScreenHeight(context);
    }

    //need to hook music status
    private enum ChangePlayStatus {
        PLAY,PAUSE,NEXT,LAST,STOP
    }

    //music play status, play ,pause, stop
    private enum MusicPlayStatus {
        PLAY,PAUSE,STOP
    }

    private enum NeedleAnimationStatus {
        //moving from disc to faraway
        MOVE_FAR_END,
        //moving from end to disc
        MOVE_NEAR_END,
        //still,leave disc
        STILL_FAR_END,
        //still, close disc
        STILL_NEAR_END
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //初始化唱片背景
        initDiscBlankBackground();
        //初始化pagerview
        initPagerView();
        //初始化唱针
        initNeedleView();
        //初始化唱针动画
        initNeedleAnimation();

    }

    private void initPagerView() {
        discViewPager = (ViewPager) findViewById(R.id.vpDiscContain);
        discViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        discViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //left slide
                if (lastPositionOffsetPixels > positionOffsetPixels) {
                    if (positionOffset < 0.5) {
                        notifyMusicInfoChanged(position);
                    } else {
                        //slide to endpoint then just get current item position
                        notifyMusicInfoChanged(discViewPager.getCurrentItem());
                    }
                }
                //right slide
                else if (lastPositionOffsetPixels < positionOffsetPixels ) {
                    //slide to the next pager
                    if (positionOffset > 0.5) {
                        notifyMusicInfoChanged(position + 1);
                    } else {
                        notifyMusicInfoChanged(position);
                    }
                }
                lastPositionOffsetPixels = positionOffsetPixels;
            }

            @Override
            public void onPageSelected(int position) {
                //cancel other Disc play animation exception current pager
                resetOtherDiscAnimation(position);
                //change the current disc pic
                notifyDiscPicChange(position);
                //switch play status accround to current position
                if (position > currentItem) {
                    //play next song
                    notifyPlayStatusChange(ChangePlayStatus.NEXT);
                } else {
                    //play last song
                    notifyPlayStatusChange(ChangePlayStatus.LAST);
                }
                currentItem = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                animatorPageScroll(state);
            }
        });
    }

    private void animatorPageScroll(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_IDLE:
            case ViewPager.SCROLL_STATE_SETTLING:
                viewPagerIsOffset = false;
                if (playStatus == MusicPlayStatus.PLAY) {
                    //play needle animation play
                    playAnimator();
                }
                break;
            case ViewPager.SCROLL_STATE_DRAGGING:
                //view pager drag
                viewPagerIsOffset = true;
                stopAnimation();

        }
    }

    private void stopAnimation() {
        //playing then switch to stop
        if (needleAnimationStatus == NeedleAnimationStatus.STILL_NEAR_END) {
            int item = discViewPager.getCurrentItem();
            stopNeedleAnimation(item);
        }
        //needle near to disc then stop to play
        if (needleAnimationStatus == NeedleAnimationStatus.MOVE_NEAR_END) {
            needleAnimator.reverse();
            //if near to disc animation still continue, and then we reverse animation,this will not to invoke animationonStart method
            //we should manually set the needle animation status to faraway disc
            needleAnimationStatus = NeedleAnimationStatus.MOVE_FAR_END;
        }
    }

    private void stopNeedleAnimation(int item) {
        ObjectAnimator objectAnimator = discAnimationList.get(item);
        //pause disc rotate
        objectAnimator.pause();
        //reverse needle faraway disc
        needleAnimator.reverse();
    }

    private void playAnimator() {
        //if needle site on faraway disc then play needle animation
        if (needleAnimationStatus == NeedleAnimationStatus.STILL_FAR_END) {
            needleAnimator.start();
        }
        // if needle moving to faraway disc,then waiting for animation end, then begin to start animation
        if (needleAnimationStatus == NeedleAnimationStatus.MOVE_FAR_END) {
            //set flag for this mount when animation end it will be start needle close animation
            ifNeedToStopNeedleAnimation = true;
        }
    }

    public interface PlayStatuChange {
        void playStatusInfoChange(String name, String auth);
        void playStatusPicChange(int picResId);
        void playStatusChange(ChangePlayStatus changePlayStatus);
    }


    public void setOnPlayStatusChangeListener(PlayStatuChange playStatusChangeListener) {
        this.playStatusChangeListener = playStatusChangeListener;
    }

    private void resetOtherDiscAnimation(int position) {
        for (int i = 0; i< discLayoutList.size();i++){
            if (i == position)
                continue;
            discAnimationList.get(i).cancel();
            discLayoutList.get(i).setRotation(0);
        }
    }

    private void notifyDiscPicChange(int position) {
        if (playStatusChangeListener != null) {
            MusicData musicData = musicDataList.get(position);
            playStatusChangeListener.playStatusPicChange(musicData.getMusicPicRes());
        }

    }

    private void notifyPlayStatusChange(ChangePlayStatus status) {

        if (playStatusChangeListener != null) {
            playStatusChangeListener.playStatusChange(status);
        }
    }

    private void notifyMusicInfoChanged(int position) {
        if (playStatusChangeListener != null) {
            MusicData musicData = musicDataList.get(position);
            playStatusChangeListener.playStatusInfoChange(musicData.getMusicName(), musicData.getMusicAuthor());
        }
    }

    public void setMusicDataList(List<MusicData> musicDataList) {
        if (musicDataList == null || musicDataList.isEmpty()) {
            return;
        }
        this.musicDataList.clear();
        this.discLayoutList.clear();
        this.discAnimationList.clear();
        this.musicDataList.addAll(musicDataList);
        //遍历musicDataList,根据list中的picresId给disclayoutList中添加数据，根据list的个数。给每个Animation集合中添加对应的控件
        for (int i = 0; i<this.musicDataList.size(); i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.disc_image, discViewPager, false);
            ImageView disc_image = (ImageView)view.findViewById(R.id.disc_image);
            disc_image.setImageDrawable(getDiscImageDrawable(musicDataList.get(i).getMusicPicRes()));
            discLayoutList.add(disc_image);
            discAnimationList.add(getDiscObjectAnimator(disc_image, i));
        }
    }

    private ObjectAnimator getDiscObjectAnimator(ImageView disc_image, int i) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(disc_image,View.ROTATION,0,360);
        objectAnimator.setDuration(20*1000);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setInterpolator(new LinearInterpolator());
        return objectAnimator;
    }

    private Drawable getDiscImageDrawable(int musicPicRes) {
        int discSize = (int)(screenWidth * DisplayUtils.SCALE_DISC_SIZE);
        int picSize = (int) (screenWidth * DisplayUtils.SCALE_MUSIC_PIC_SIZE);
        //控件先放胶片，后在胶片上贴图片
        Bitmap discBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_disc);
        Bitmap discBit = Bitmap.createScaledBitmap(discBitmap, discSize, discSize, false);
        Bitmap musicPicBitmap = getMusicPicBitmap(musicPicRes, picSize);
        //圆行话drawable
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),musicPicBitmap);
        BitmapDrawable discBitmapDrawable = new BitmapDrawable(discBit);
        //抗锯齿
        discBitmapDrawable.setAntiAlias(true);
        roundedBitmapDrawable.setAntiAlias(true);
        Drawable[] drawables= new Drawable[2];
        drawables[0] = discBitmapDrawable;
        drawables[1] = roundedBitmapDrawable;
        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        //adjust discPic position, align in center in pulgin
        int discLayoutMargin = (int)((DisplayUtils.SCALE_DISC_SIZE - DisplayUtils.SCALE_MUSIC_PIC_SIZE) * screenWidth/2);
        layerDrawable.setLayerInset(1, discLayoutMargin,discLayoutMargin, discLayoutMargin,discLayoutMargin);
        return layerDrawable;

    }

    private Bitmap getMusicPicBitmap(int musicPicRes, int picSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(),musicPicRes,options);
        int simple = options.outWidth / picSize;
        int scaleSample = 1;
        if (simple > scaleSample) {
            simple = scaleSample;
        }
        options.inSampleSize = simple;
        options.inJustDecodeBounds = false;
        //set pic decode rate
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),musicPicRes,options),picSize,picSize,true);
    }

    private void initNeedleAnimation() {
        // needle angle from -30 to 0
        needleAnimator = ObjectAnimator.ofFloat(needle, View.ROTATION, DisplayUtils.ROTATION_INIT_NEEDLE, 0);
        needleAnimator.setDuration(DURATION_NEEDLE_ANIAMTOR);
        needleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        needleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //动画结束，唱针由动态变成静止状态 move --> still，判断条件是move
                if (needleAnimationStatus == NeedleAnimationStatus.MOVE_FAR_END) {
                    //move faraway disc
                    needleAnimationStatus = NeedleAnimationStatus.STILL_FAR_END;
                    //stop disc rotate animation if need
                    if (playStatus == MusicPlayStatus.STOP) {
                        ifNeedToStopNeedleAnimation = true;
                    }
                }

                //move close disc
                else if (needleAnimationStatus == NeedleAnimationStatus.MOVE_NEAR_END) {
                    needleAnimationStatus = NeedleAnimationStatus.STILL_NEAR_END;
                    //begin to rotate current disc
                    //get current disc position from viewpager
                    int currentItem = discViewPager.getCurrentItem();
                    startDiscAnimation(currentItem);
                    //switch music play status to playing
                    playStatus = MusicPlayStatus.PLAY;
                }

                //switch music animation end we need to restart play animation for next music
                if (ifNeedToStopNeedleAnimation) {
                    ifNeedToStopNeedleAnimation = false;
                    //only viewpager not offset then start play animation
                    if (!viewPagerIsOffset) {
                        DiscView.this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                playAnimator();
                            }
                        }, 50);
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                //动画开始时，唱针是静止状态，转变成动态 still---> move，判断条件是still
                //begin at faraway disc position , so move to near disc
                if (needleAnimationStatus == NeedleAnimationStatus.STILL_FAR_END) {
                    needleAnimationStatus = NeedleAnimationStatus.MOVE_NEAR_END;
                }
                //begin at close disc so faraway disc
                else if(needleAnimationStatus == NeedleAnimationStatus.STILL_NEAR_END) {
                    needleAnimationStatus = NeedleAnimationStatus.MOVE_FAR_END;
                }
            }
        });
    }

    private void startDiscAnimation(int currentItem) {
        if (discAnimationList != null) {
            ObjectAnimator objectAnimator = discAnimationList.get(currentItem);
            if (objectAnimator.isPaused()) {
                objectAnimator.resume();
            } else {
                objectAnimator.start();
            }
        }
    }

    private void initNeedleView() {
        needle = (ImageView) findViewById(R.id.ivNeedle);
        int needleWidth = (int)(DisplayUtils.SCALE_NEEDLE_WIDTH * screenWidth);
        int needleHeight = (int)(DisplayUtils.SCALE_NEEDLE_HEIGHT * screenHeight);

        int needleMarginTop = (int)(DisplayUtils.SCALE_NEEDLE_MARGIN_TOP * screenHeight) *-1;
        int needleMarginLeft = (int)(DisplayUtils.SCALE_NEEDLE_MARGIN_LEFT * screenWidth);
        Bitmap needleBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_needle), needleWidth, needleHeight, false);
        //get imageview params
        LayoutParams layoutParams = (LayoutParams)needle.getLayoutParams();
        layoutParams.setMargins(needleMarginLeft,needleMarginTop,0,0);
        float needlePivotX = (DisplayUtils.SCALE_NEEDLE_PIVOT_X * screenWidth);
        float needlePivotY = (DisplayUtils.SCALE_NEEDLE_PIVOT_Y * screenHeight);
        //手动设置imageview的中心点
        needle.setPivotX(needlePivotX);
        needle.setPivotY(needlePivotY);
        //imageview rotate to -30
        needle.setRotation(DisplayUtils.ROTATION_INIT_NEEDLE);
        needle.setImageBitmap(needleBitmap);
        needle.setLayoutParams(layoutParams);

    }

    private void initDiscBlankBackground() {
        discBlackgound = (ImageView) findViewById(R.id.ivDiscBlackgound);
        System.out.println("discblackGround:"+discBlackgound);
        discBlackgound.setImageDrawable(getdiscBackgroundDrawable());
        int discMarginTop = (int)(DisplayUtils.SCALE_DISC_MARGIN_TOP * screenHeight);
        LayoutParams params = (LayoutParams)discBlackgound.getLayoutParams();
        params.setMargins(0,discMarginTop,0,0);
        discBlackgound.setLayoutParams(params);
    }

    private Drawable getdiscBackgroundDrawable() {
        int discSize = (int)(screenWidth * DisplayUtils.SCALE_DISC_SIZE);
        Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_disc_blackground), discSize, discSize, false);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(),bitmapDisc);
        return roundedBitmapDrawable;
    }
}
