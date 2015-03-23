package com.rudedroid.rudesoundboard;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.rudedroid.rudesoundboard.data.SoundboardManager;

public class SplashActivity extends Activity {
	private ImageView rude;
	private ImageView placa;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		placa= (ImageView)findViewById(R.id.imgPlaca);
		final MediaPlayer sonidoPlaca= MediaPlayer.create(SplashActivity.this,R.raw.sonidorude);

		Animation animplaca=AnimationUtils.loadAnimation(this, R.anim.fadein);
		final Animation animrude=AnimationUtils.loadAnimation(this, R.anim.reducir); 
		final Animation animfadeout=AnimationUtils.loadAnimation(this, R.anim.fadeout);

		placa.setAnimation(animplaca);
		animplaca.setAnimationListener(new AnimationListener(){


			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				sonidoPlaca.start();
				rude = (ImageView) findViewById(R.id.imgRude);
				rude.setVisibility(View.VISIBLE);
				rude.setAnimation(animrude);
			}
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}
		});

		animrude.setAnimationListener(new AnimationListener(){


			public void onAnimationEnd(Animation arg0) {
				new SoundboardManager(SplashActivity.this);
				placa.setAnimation(animfadeout);
				rude.setAnimation(animfadeout);
			}
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}

		});

		animfadeout.setAnimationListener(new AnimationListener(){


			public void onAnimationEnd(Animation arg0) {
				rude.setAlpha(0);
				placa.setAlpha(0);
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();
			}
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub


			}
		});
	}

}