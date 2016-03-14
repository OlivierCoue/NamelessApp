package com.oliviercoue.nameless.components.start;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.oliviercoue.nameless.R;

/**
 * Created by Olivier on 14/03/2016.
 */
public class StartBackground {

    private ImageView startsImageView;
    private Context context;

    public StartBackground(Context context) {
        this.context = context;

        startsImageView = (ImageView) ((Activity)context).findViewById(R.id.stars_imageview);
        rotateStars();
    }

    private void rotateStars(){
        RotateAnimation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.3f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(180000);
        startsImageView.startAnimation(anim);

    }
}
