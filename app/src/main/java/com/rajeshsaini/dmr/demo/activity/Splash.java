package com.rajeshsaini.dmr.demo.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.rajeshsaini.dmr.demo.R;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        zoom_in();
        runSplash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        runSplash();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runSplash();
    }

    private void runSplash(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(Splash.this,LoginActivity.class);
                Splash.this.startActivity(intent);
                finish();
            }
        },1000);
    }

    public void zoom_in() {
        ImageView image = (ImageView) findViewById(R.id.logo);
        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        image.startAnimation(animation1);
    }
}
