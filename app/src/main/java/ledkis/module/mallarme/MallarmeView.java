package ledkis.module.mallarme;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;


/**
 * Created by ledkis on 15/02/2016.
 */
public class MallarmeView extends RelativeLayout {

    public static final String TAG = "MallarmeView";


    public interface AnimationListener {

        /**
         * We need to make MaterialIntroView visible
         * before fade in animation starts
         */
        interface OnAnimationStartListener {
            void onAnimationStart();
        }

        /**
         * We need to make MaterialIntroView invisible
         * after fade out animation ends.
         */
        interface OnAnimationEndListener {
            void onAnimationEnd();
        }

    }

    private int maskColor;
    private long delayMillis;
    private boolean isReady;
    private boolean isFadeAnimationEnabled;
    private long fadeInAnimationDuration;
    private long fadeOutAnimationDuration;

    private Handler handler;

    private Bitmap bitmap;
    private Canvas canvas;

    private int width;
    private int height;

    private int textWidth;
    private int textHeight;

    private float textLeftMargin;
    private float textLeftMarginProgress;
    private float lastTextLeftMargin;

    private boolean dismissOnTouch;

    private MallarmeTextManager mallarmeTextManager;

    private View poemeVerseView;

    private TextView poemeVerseTextView;

    private Random random = new Random();

    private int verseCount;

    private boolean isLayoutCompleted;

    private ValueAnimator textAnimator;
    private ValueAnimator fadeAnimator;

    private float currentAlpha;

    public MallarmeView(Context context) {
        super(context);
        init(context);
    }

    public MallarmeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MallarmeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MallarmeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);

        maskColor = Constants.DEFAULT_MASK_COLOR;
        delayMillis = Constants.DEFAULT_DELAY_MILLIS;
        fadeInAnimationDuration = Constants.DEFAULT_FADE_DURATION;
        fadeOutAnimationDuration = Constants.DEFAULT_FADE_DURATION;
        isReady = false;
        isFadeAnimationEnabled = true;
        dismissOnTouch = false;
        isLayoutCompleted = false;

        verseCount = 0;

        handler = new Handler();

        mallarmeTextManager = new MallarmeTextManager(context, "mallarme.txt", false);

        View poemeVerseLayout = LayoutInflater.from(getContext()).inflate(R.layout.poeme_verse_layout, null);
        poemeVerseView = poemeVerseLayout.findViewById(R.id.poemeVerseView);
        poemeVerseTextView = (TextView) poemeVerseLayout.findViewById(R.id.poemeVerseTextView);

        updateText();

        currentAlpha = 0f;

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // in the case showcaseViewCase y can be 0 : the targetView can be centered
                if (!isLayoutCompleted) {
                    setInfoLayout();

                    removeOnGlobalLayoutListener(MallarmeView.this, this);
                }
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady) return;

        if (bitmap == null || canvas == null) {
            if (bitmap != null) bitmap.recycle();

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(bitmap);
        }

//        this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.canvas.drawColor(maskColor);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public void add(Activity activity){
        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setReady(true);
    }


    public void dismiss() {
        setVisibility(GONE);
        removeMaterialView();

//        bitmap.recycle();
    }

    private void removeMaterialView() {
        if (getParent() != null)
            ((ViewGroup) getParent()).removeView(this);
    }

    private void setInfoLayout() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                isLayoutCompleted = true;

                if (poemeVerseView.getParent() != null)
                    ((ViewGroup) poemeVerseView.getParent()).removeView(poemeVerseView);

                FrameLayout.LayoutParams poemeVerseViewParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                poemeVerseViewParams.setMargins(
                        width / 2,
                        height / 2,
                        0,
                        0);
//
                poemeVerseTextView.setLayoutParams(poemeVerseViewParams);
                poemeVerseTextView.invalidate();

                addView(poemeVerseView);

                poemeVerseView.setVisibility(VISIBLE);

            }
        });
    }


    private void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void startTextAnimation() {

        if(null != textAnimator)
            textAnimator.cancel();


        textAnimator = ObjectAnimator.ofFloat(0f, 1f);
        textAnimator.setRepeatCount(ValueAnimator.INFINITE);
        textAnimator.setDuration(3000);

        textAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float progress = (Float) animation.getAnimatedValue();

                float alpha;
                if (progress < 0.5f) {
                    alpha = progress;
                } else {
                    alpha = 1f - progress;
                }

                poemeVerseView.setAlpha(alpha);

                translateText(progress);

            }
        });

        textAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                poemeVerseView.setY(height / 3);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

                updateText();
                updateTextPos();

            }
        });

        textAnimator.start();

    }

    private void updateText(){
        verseCount +=1;
        poemeVerseTextView.setText(mallarmeTextManager.getVerse(verseCount));
        poemeVerseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mallarmeTextManager.getVerseSize(verseCount));
    }

    private void updateTextPos() {

        float widthRatio = 2 * Utils.map(random.nextFloat(), 0f, 1f, 0.1f, 0.9f);
        float heightRatio = 2 * Utils.map(random.nextFloat(), 0f, 1f, 0.1f, 0.9f);

        FrameLayout.LayoutParams poemeVerseViewParams = (FrameLayout.LayoutParams) poemeVerseTextView.getLayoutParams();

        textLeftMargin = widthRatio * width / 2;
        textLeftMarginProgress = lastTextLeftMargin;
        lastTextLeftMargin = 0;

        poemeVerseViewParams.leftMargin = (int) textLeftMargin;
        poemeVerseViewParams.topMargin = (int) (heightRatio * height / 2);
        poemeVerseTextView.setLayoutParams(poemeVerseViewParams);
    }

    private void translateText(float progress) {
        FrameLayout.LayoutParams poemeVerseViewParams = (FrameLayout.LayoutParams) poemeVerseTextView.getLayoutParams();

        textLeftMarginProgress += (0.1f * progress * width) - lastTextLeftMargin;
        lastTextLeftMargin = textLeftMarginProgress;

        poemeVerseViewParams.leftMargin = (int) (textLeftMargin + textLeftMarginProgress);
        poemeVerseTextView.setLayoutParams(poemeVerseViewParams);
    }


    public void fadeIn() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != fadeAnimator)
                    fadeAnimator.cancel();


                fadeAnimator = ValueAnimator.ofFloat(currentAlpha, 1f);
                fadeAnimator.setDuration(fadeInAnimationDuration);

                fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        currentAlpha = (Float) animation.getAnimatedValue();

                        setAlpha(currentAlpha);
                    }
                });

                fadeAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setVisibility(VISIBLE);
                        startTextAnimation();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if(null != textAnimator)
                            textAnimator.cancel();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                fadeAnimator.start();
            }
        });


    }

    public void fadeOut() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (null != fadeAnimator)
                    fadeAnimator.cancel();


                fadeAnimator = ValueAnimator.ofFloat(currentAlpha, 0f);
                fadeAnimator.setDuration(fadeOutAnimationDuration);

                fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        currentAlpha = (Float) animation.getAnimatedValue();

                        setAlpha(currentAlpha);
                    }
                });

                fadeAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setVisibility(GONE);
                        if(null != textAnimator)
                            textAnimator.cancel();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if(null != textAnimator)
                            textAnimator.cancel();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                fadeAnimator.start();
            }
        });

    }


}
