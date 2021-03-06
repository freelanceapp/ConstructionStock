package com.creativeshare.constructionstock.activities_fragments.activity_splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.creativeshare.constructionstock.R;
import com.creativeshare.constructionstock.activities_fragments.home_activity.activities.Home_Activity;
import com.creativeshare.constructionstock.activities_fragments.sign_in_sign_up_activity.activity.Login_Activity;
import com.creativeshare.constructionstock.language.Language_Helper;
import com.creativeshare.constructionstock.preferences.Preferences;
import com.creativeshare.constructionstock.tags.Tags;


public class Splash_Activity extends AppCompatActivity {
   private FrameLayout fl;
   private Preferences preferences;
   private String session;
   private Animation animation;
   private boolean isHasNotificationData = false;


    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(Language_Helper.updateResources(base, Preferences.getInstance().getLanguage(base)));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        getDataFromIntent();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent.getExtras()!=null){
            isHasNotificationData = true;
        }
    }

    private void initView() {
        preferences=Preferences.getInstance();
        session=preferences.getSession(this);
        fl=findViewById(R.id.fl);

        animation= AnimationUtils.loadAnimation(getBaseContext(),R.anim.lanuch);

        fl.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(session.equals(Tags.session_login)){
                    Intent intent = new Intent(Splash_Activity.this, Home_Activity.class);
                    intent.putExtra("hasDataNotification",isHasNotificationData);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(Splash_Activity.this, Login_Activity.class);
                    startActivity(intent);
                }
                finish();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
