package botwars.main;

import anden.examples.testing.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsMenu extends Activity {

	final BotWars bw = new BotWars();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsmenu);

		final MediaPlayer test = MediaPlayer.create(this, R.drawable.explosion);
		Typeface tf = Typeface.createFromAsset(getAssets(), "ANKLEPAN.TTF");

		TextView txv_music = (TextView) findViewById(R.id.txv_music);
		TextView txv_sound = (TextView) findViewById(R.id.txv_sound);
		TextView txv_impulse = (TextView) findViewById(R.id.txv_impluse);
		TextView txv_veloc = (TextView) findViewById(R.id.txv_velocity);
		TextView txv_volume = (TextView) findViewById(R.id.txv_volume);
		final TextView txv_volume_value = (TextView) findViewById(R.id.txv_volume_value);
		final TextView txv_velocity_value = (TextView) findViewById(R.id.txv_velocity_value);
		final TextView txv_impulse_value = (TextView) findViewById(R.id.txv_impulse_value);

		txv_music.setTypeface(tf);
		txv_sound.setTypeface(tf);
		txv_impulse.setTypeface(tf);
		txv_veloc.setTypeface(tf);
		txv_volume.setTypeface(tf);

		SeekBar seekbar_volume = (SeekBar) findViewById(R.id.seekbar_volume); // make
																				// seekbar
																				// object
		seekbar_volume.setProgress(100);
		txv_volume_value.setText(100 + "%");

		seekbar_volume
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekbar_volume,
							int progress, boolean arg2) {
						// TODO Auto-generated method stub
						float volume = (float) progress / 100;
						BotWars.setMusicVolume(volume);
						txv_volume_value.setText(progress + "%");
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekbar_volume) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekbar_volume) {
						// TODO Auto-generated method stub

						test.start();

					}

				});

		SeekBar seekbar_velocity = (SeekBar) findViewById(R.id.seekbar_velocity); // make
																					// seekbar
																					// object
		seekbar_velocity.setProgress(8);
		txv_velocity_value.setText("8.0");
		BotWars.setVelocity(8.0f);
		seekbar_velocity
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekbar_velocity,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub
						float veloc = (float) (progress / 4);
						txv_velocity_value.setText("Velocity is" + veloc);
						BotWars.setVelocity(veloc);
						// Toast.makeText(SettingsMenu.this,txv_velocity_value.getText(),
						// Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekbar_velocity) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekbar_velocity) {
						// TODO Auto-generated method stub

					}

				});

		SeekBar seekbar_impluse = (SeekBar) findViewById(R.id.seekbar_impulse); // make
																				// seekbar object
				seekbar_impluse.setProgress(14);
				txv_impulse_value.setText("14.0");
				BotWars.setImpulse(14.0f);
		seekbar_impluse
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekbar_impulse,
							int progress, boolean fromUser) {
						// TODO Auto-generated method stub

						float impul = (float) (progress/4);
						txv_impulse_value.setText("Impluse is:" + impul);
						BotWars.setImpulse(impul);
						// Toast.makeText(SettingsMenu.this,txv_impulse_value.getText(),
						// Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekbar_impulse) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStopTrackingTouch(SeekBar seekbar_impulse) {
						// TODO Auto-generated method stub

					}

				});

		Button btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				StartMenu.settingsChanged();
				Intent openStartMenu = new Intent(SettingsMenu.this,StartMenu.class);
				startActivity(openStartMenu);
				
				Toast.makeText(SettingsMenu.this, "Settings saved",
						Toast.LENGTH_SHORT).show();
				
				//startGame(view);
				finish();
			}

		});

		final ToggleButton toggleSound = (ToggleButton) findViewById(R.id.togglebtn_sound);
		toggleSound.toggle();
		BotWars.enableSounds(true);
		toggleSound.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks

				if (toggleSound.isChecked()) {
					Toast.makeText(SettingsMenu.this, "Sounds ON",
							Toast.LENGTH_SHORT).show();
					BotWars.enableSounds(true);
				} else {
					Toast.makeText(SettingsMenu.this, "Sounds OFF",
							Toast.LENGTH_SHORT).show();
					BotWars.enableSounds(false);
				}
			}
		});

		final ToggleButton toggleMusic = (ToggleButton) findViewById(R.id.togglebtn_music);
		toggleMusic.toggle();
		BotWars.enableMusic(true);
		toggleMusic.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks

				if (toggleMusic.isChecked()) {
					Toast.makeText(SettingsMenu.this, "Music ON",
							Toast.LENGTH_SHORT).show();
					BotWars.enableMusic(true);
				} else {
					Toast.makeText(SettingsMenu.this, "Music OFF",
							Toast.LENGTH_SHORT).show();
					BotWars.enableMusic(false);
				}
			}
		});

	}

	public void startGame(View v) {

		Intent StartIntent = new Intent(SettingsMenu.this, BotWars.class);
		startActivity(StartIntent);

		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
StartMenu.settingsChanged();
			Intent openStartMenu = new Intent(SettingsMenu.this,StartMenu.class);
			startActivity(openStartMenu);

	    	finish();
	       // do something on back.
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}

}
