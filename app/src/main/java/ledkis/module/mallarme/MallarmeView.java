package ledkis.module.mallarme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ledkis.module.mallarme.animation.AnimationFactory;
import ledkis.module.mallarme.animation.AnimationListener;

/**
 * Created by ledkis on 15/02/2016.
 */
public class MallarmeView extends RelativeLayout {

    /**
     * Mask color
     */
    private int maskColor;

    /**
     * MallarmeView will start
     * showing after delayMillis seconds
     * passed
     */
    private long delayMillis;

    /**
     * We don't draw MallarmeView
     * until isReady field set to true
     */
    private boolean isReady;

    /**
     * Show/Dismiss MallarmeView
     * with fade in/out animation if
     * this is enabled.
     */
    private boolean isFadeAnimationEnabled;

    /**
     * Animation duration
     */
    private long fadeInAnimationDuration;

    private long fadeOutAnimationDuration;

    /**
     * Handler will be used to
     * delay MallarmeView
     */
    private Handler handler;

    /**
     * All views will be drawn to
     * this bitmap and canvas then
     * bitmap will be drawn to canvas
     */
    private Bitmap bitmap;
    private Canvas canvas;

    /**
     * Layout width/height
     */
    private int width;
    private int height;

    /**
     * Dismiss on touch any position
     */
    private boolean dismissOnTouch;

    /**
     * Info dialog view
     */
    private View poemeVerseView;

    /**
     * Info Dialog Text
     */
    private TextView poemeVerseTextView;

    /**
     * Info dialog text color
     */
    private int colorTextViewInfo;


    /**
     * When layout completed, we set this true
     * Otherwise onGlobalLayoutListener stuck on loop.
     */
    private boolean isLayoutCompleted;

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

        /**
         * set default values
         */
        maskColor = Constants.DEFAULT_MASK_COLOR;
        delayMillis = Constants.DEFAULT_DELAY_MILLIS;
        fadeInAnimationDuration = Constants.DEFAULT_FADE_DURATION;
        fadeOutAnimationDuration = Constants.DEFAULT_FADE_DURATION;
        colorTextViewInfo = Constants.DEFAULT_COLOR_TEXTVIEW_INFO;
        isReady = false;
        isFadeAnimationEnabled = true;
        dismissOnTouch = false;
        isLayoutCompleted = false;

        /**
         * initialize objects
         */
        handler = new Handler();


        View layoutInfo = LayoutInflater.from(getContext()).inflate(R.layout.poeme_verse, null);

        poemeVerseView = layoutInfo.findViewById(R.id.info_layout);
        poemeVerseTextView = (TextView) layoutInfo.findViewById(R.id.poemeVerseTextView);
        poemeVerseTextView.setTextColor(colorTextViewInfo);


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

        /**
         * Draw mask
         */
        this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.canvas.drawColor(maskColor);

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    /**
     * Shows material view with fade in
     * animation
     *
     * @param activity
     */
    private void show(Activity activity) {

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setReady(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFadeAnimationEnabled)
                    AnimationFactory.animateFadeIn(MallarmeView.this, fadeInAnimationDuration, new AnimationListener.OnAnimationStartListener() {
                        @Override
                        public void onAnimationStart() {
                            setVisibility(VISIBLE);
                        }
                    });
                else
                    setVisibility(VISIBLE);
            }
        }, delayMillis);

    }

    /**
     * Dismiss Material Intro View
     */
    private void dismiss() {
        AnimationFactory.animateFadeOut(this, fadeOutAnimationDuration, new AnimationListener.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(GONE);
                removeMaterialView();

                bitmap.recycle();

            }
        });
    }

    private void removeMaterialView() {
        if (getParent() != null)
            ((ViewGroup) getParent()).removeView(this);
    }

    /**
     * locate info card view above/below the
     * circle. If circle's Y coordiante is bigger than
     * Y coordinate of root view, then locate cardview
     * above the circle. Otherwise locate below.
     */
    private void setInfoLayout() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                isLayoutCompleted = true;

//                if (poemeVerseView.getParent() != null)
//                    ((ViewGroup) poemeVerseView.getParent()).removeView(poemeVerseView);
//
//                RelativeLayout.LayoutParams infoDialogParams = new RelativeLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT);
//
//                if (circleShape.getPoint().y < height / 2) {
//                    ((RelativeLayout) poemeVerseView).setGravity(Gravity.TOP);
//                    infoDialogParams.setMargins(
//                            0,
//                            circleShape.getPoint().y + circleShape.getRadius(),
//                            0,
//                            0);
//                } else {
//                    ((RelativeLayout) poemeVerseView).setGravity(Gravity.BOTTOM);
//                    infoDialogParams.setMargins(
//                            0,
//                            0,
//                            0,
//                            height - (circleShape.getPoint().y + circleShape.getRadius()) + 2 * circleShape.getRadius());
//                }
//
//                poemeVerseView.setLayoutParams(infoDialogParams);
//                poemeVerseView.postInvalidate();
//
//                addView(poemeVerseView);
//
//                poemeVerseView.setVisibility(VISIBLE);
//
//                poemeVerseView.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dismiss();
//                    }
//                });

            }
        });
    }


    /**
     * SETTERS
     */

    private void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    private void setDelay(int delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void setFadeInAnimationDuration(long fadeInAnimationDuration) {
        this.fadeInAnimationDuration = fadeInAnimationDuration;
    }

    public void setFadeOutAnimationDuration(long fadeOutAnimationDuration) {
        this.fadeOutAnimationDuration = fadeOutAnimationDuration;
    }

    private void enableFadeAnimation(boolean isFadeAnimationEnabled) {
        this.isFadeAnimationEnabled = isFadeAnimationEnabled;
    }

    private void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    private void setDismissOnTouch(boolean dismissOnTouch) {
        this.dismissOnTouch = dismissOnTouch;
    }

    private void setColorTextViewInfo(int colorTextViewInfo) {
        this.colorTextViewInfo = colorTextViewInfo;
        poemeVerseTextView.setTextColor(this.colorTextViewInfo);
    }

    private void setPoemeVerseTextView(String poemeVerseTextView) {
        this.poemeVerseTextView.setText(poemeVerseTextView);
    }

    private void setTextViewInfoSize(int textViewInfoSize) {
        this.poemeVerseTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textViewInfoSize);
    }


    /**
     * Builder Class
     */
    public static class Builder {

        private MallarmeView mallarmeView;

        private Activity activity;


        public Builder(Activity activity) {
            this.activity = activity;
            mallarmeView = new MallarmeView(activity);
        }

        public Builder setMaskColor(int maskColor) {
            mallarmeView.setMaskColor(maskColor);
            return this;
        }

        public Builder setDelayMillis(int delayMillis) {
            mallarmeView.setDelay(delayMillis);
            return this;
        }

        public Builder setFadeInAnimationDuration(long fadeInAnimationDuration) {
            mallarmeView.setFadeInAnimationDuration(fadeInAnimationDuration);
            return this;
        }

        public Builder setFadeOutAnimationDuration(long fadeOutAnimationDuration) {
            mallarmeView.setFadeOutAnimationDuration(fadeOutAnimationDuration);
            return this;
        }

        public Builder enableFadeAnimation(boolean isFadeAnimationEnabled) {
            mallarmeView.enableFadeAnimation(isFadeAnimationEnabled);
            return this;
        }


        public Builder setTextColor(int textColor) {
            mallarmeView.setColorTextViewInfo(textColor);
            return this;
        }

        public Builder setInfoText(String poemeVerse) {
            mallarmeView.setPoemeVerseTextView(poemeVerse);
            return this;
        }

        public Builder setInfoTextSize(int textSize) {
            mallarmeView.setTextViewInfoSize(textSize);
            return this;
        }

        public Builder dismissOnTouch(boolean dismissOnTouch) {
            mallarmeView.setDismissOnTouch(dismissOnTouch);
            return this;
        }


        public MallarmeView build() {
            return mallarmeView;
        }

        public MallarmeView show() {
            build().show(activity);
            return mallarmeView;
        }

    }


}
