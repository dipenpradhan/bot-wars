package botwars.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class StartMenu extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	public static boolean settingsChanged = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (!settingsChanged) { // BotWars.setMap(1);
			BotWars.setVelocity(8.0f);
			BotWars.setImpulse(14.0f);
			BotWars.setMusicVolume(1.0f);
			BotWars.enableMusic(true);
			BotWars.enableSounds(true);
		}
		// settingsChanged=false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startmenu);

		Typeface tf = Typeface.createFromAsset(getAssets(), "ANKLEPAN.TTF");
		Typeface tf1 = Typeface.createFromAsset(getAssets(), "AltamonteNF.ttf");

		TextView txv_start = (TextView) findViewById(R.id.txv_start);
		TextView txv_quick = (TextView) findViewById(R.id.txv_quick);
		TextView txv_settings = (TextView) findViewById(R.id.txv_settings);
		TextView txv_about = (TextView) findViewById(R.id.txv_about);
		TextView txv_exit = (TextView) findViewById(R.id.txv_exit);
		TextView txv_title = (TextView) findViewById(R.id.txv_title);

		txv_start.setTypeface(tf);
		txv_quick.setTypeface(tf);
		txv_settings.setTypeface(tf);
		txv_about.setTypeface(tf);
		txv_exit.setTypeface(tf);
		txv_title.setTypeface(tf1);

		txv_start.setOnTouchListener(new MenuTextTouchListener());
		txv_quick.setOnTouchListener(new MenuTextTouchListener());
		txv_settings.setOnTouchListener(new MenuTextTouchListener());
		txv_about.setOnTouchListener(new MenuTextTouchListener());
		txv_exit.setOnTouchListener(new MenuTextTouchListener());

		txv_start.setOnClickListener(this);
		txv_quick.setOnClickListener(this);
		txv_settings.setOnClickListener(this);
		txv_about.setOnClickListener(this);
		txv_exit.setOnClickListener(this);

		// Toast.makeText(this, "Press Menu to Start Game",
		// Toast.LENGTH_LONG).show();

	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.txv_start:
			Intent openMapsMenu = new Intent(this, MapMenu.class);
			startActivity(openMapsMenu);
			finish();
			break;
		case R.id.txv_quick:
			// Toast.makeText(getBaseContext(), "Feature under construction",
			// Toast.LENGTH_LONG).show();
			Intent openMP_MapsMenu = new Intent(this, MP_MapMenu_UDP.class);
			startActivity(openMP_MapsMenu);
			finish();
		
			
			//finish();
			break;
		case R.id.txv_settings:
			Intent openSettingsMenu = new Intent(this, SettingsMenu.class);
			startActivity(openSettingsMenu);
			finish();
			break;
		case R.id.txv_about:
			makeAboutDialog();
			break;
		case R.id.txv_exit:
			finish();
			break;
		}

	}

	private void makeAboutDialog() {

		AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);

		aboutDialog.setMessage("This Game has been made by Dipen,Gaurav & Mayuresh");

		aboutDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				// Toast.makeText(getBaseContext(), "BUY IT",
				// Toast.LENGTH_LONG).show();
			}
		});

		aboutDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});

		aboutDialog.show();
	}

	public static void settingsChanged() {
		settingsChanged = true;
	}
	// ImageView imageView= (ImageView)findViewById(R.id.imageview);
	// Animation fadeInAnimation = AnimationUtils.loadAnimation(this,
	// R.layout.anim);
	// Now Set your animation
	// imageView.startAnimation(fadeInAnimation );

}
