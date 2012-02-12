package anden.examples.testing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingActivity extends Activity {

	final BotWars bw = new BotWars();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
           
		
		final MediaPlayer test=MediaPlayer.create(this,R.drawable.explosion);
		Typeface tf = Typeface.createFromAsset(getAssets(),"ANKLEPAN.TTF");
		
		TextView tv = (TextView) findViewById(R.id.txtUserName);
	    TextView tv1 = (TextView) findViewById(R.id.txtPassword);
	    TextView tv2 = (TextView) findViewById(R.id.impluse);
	    TextView tv3 = (TextView) findViewById(R.id.txv_veloc);
	    TextView tv4 = (TextView) findViewById(R.id.volume);
	    final TextView tv5 = (TextView) findViewById(R.id.volume_show);
	    final TextView tv6 = (TextView) findViewById(R.id.velocity_show);
	    final TextView tv7 = (TextView) findViewById(R.id.implusetext);
		    
	    
	     tv.setTypeface(tf);
	    tv1.setTypeface(tf);
	      tv2.setTypeface(tf);
	     tv3.setTypeface(tf);
	    tv4.setTypeface(tf);
	    
		
	    SeekBar volumeBar = (SeekBar)findViewById(R.id.seekBar1); // make seekbar object
	    volumeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
           
		

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress, boolean arg2) {
				// TODO Auto-generated method stub
		        float volume=(float)progress/100;		        
		        BotWars.setMusicVolume(volume);
		        
		        
				tv5.setText("The level is "+progress+"%");
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekbar) {
				// TODO Auto-generated method stub
				
			  test.start();
				
			}
	    	 
	    	
			
	    	
	    });
	    
	    
	    SeekBar velocity = (SeekBar)findViewById(R.id.velocity); // make seekbar object
	    velocity.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar1, int progress3,
					boolean fromUser) {
				// TODO Auto-generated method stub
				float p = (float) (progress3/4);
				tv6.setText("Velocity is"+p);
				BotWars.setVelocity(p);
				Toast.makeText(SettingActivity.this,tv6.getText(),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar1) {
				// TODO Auto-generated method stub
				
			}
		
		});
	

	    
	    
	 /*  SeekBar impluse = (SeekBar)findViewById(R.id.impluse); // make seekbar object
	    impluse.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar2, int progress2,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				float p2= (float) progress2;
				tv7.setText("The Impluse is:"+progress2);
				BotWars.setImpulse(p2);
				Toast.makeText(SettingActivity.this,tv7.getText(),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar2) {
				// TODO Auto-generated method stub
				
			}
		
}); */
	   
	Button save=(Button) findViewById(R.id.save);
	save.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			
		Toast.makeText(SettingActivity.this,"Settings saved",
					Toast.LENGTH_SHORT).show();
			
			startGame(view);
			finish();
		}
		
});
	
    final ToggleButton toggleSound = (ToggleButton) findViewById(R.id.sound);
	toggleSound.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
	        // Perform action on clicks
	        
	        
	        if (toggleSound.isChecked()) {
	            Toast.makeText(SettingActivity.this, "Sound ON", Toast.LENGTH_SHORT).show();
	            BotWars.enableSounds(true);
	        } else {
	            Toast.makeText(SettingActivity.this, "Sound Off", Toast.LENGTH_SHORT).show();
	            BotWars.enableSounds(false);
	        }
	    }
	});	
	
	
	
	
	}
	    
	    public void startGame(View v) {

			Intent StartIntent = new Intent(SettingActivity.this, BotWars.class);
			startActivity(StartIntent);

			finish();
		}	    
	    
	    
	    
	    
	    
	    
	    
	
		}















