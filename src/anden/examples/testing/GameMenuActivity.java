package anden.examples.testing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameMenuActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startmenu);
   

    Typeface tf = Typeface.createFromAsset(getAssets(),"ANKLEPAN.TTF");
    Typeface tf1=Typeface.createFromAsset(getAssets(),"AltamonteNF.ttf");
    
    TextView tv = (TextView) findViewById(R.id.start);
    TextView tv1 = (TextView) findViewById(R.id.quick);
    TextView tv2 = (TextView) findViewById(R.id.settings);
    TextView tv3 = (TextView) findViewById(R.id.about);
    TextView tv4 = (TextView) findViewById(R.id.exit);
    TextView tv5 = (TextView) findViewById(R.id.heading);
    
     tv.setTypeface(tf);
    tv1.setTypeface(tf);
      tv2.setTypeface(tf);
     tv3.setTypeface(tf);
    tv4.setTypeface(tf);
    tv5.setTypeface(tf1);
    
    tv.setOnTouchListener(new CustomTouchListener());
     tv1.setOnTouchListener(new CustomTouchListener());
      tv2.setOnTouchListener(new CustomTouchListener());
     tv3.setOnTouchListener(new CustomTouchListener());
    tv4.setOnTouchListener(new CustomTouchListener());
    
    tv.setOnClickListener(this);
     tv1.setOnClickListener(this);
     tv2.setOnClickListener(this);
    tv3.setOnClickListener(this);
    tv4.setOnClickListener(this);        
    
    Toast.makeText(this, "Press Menu to Start Game", Toast.LENGTH_LONG).show();
	   
}
  
public void onClick(View v) {
	
	switch(v.getId()){
		case R.id.start:
			Intent i=new Intent(this,mapViewActivity.class);
			startActivity(i);
		
			break;
		case R.id.quick:
			
			break;
		case R.id.settings:
			Intent setting = new Intent(this, SettingActivity.class);
			startActivity(setting);
			break;
		case R.id.about:
			makeDialog();
			break;	
		case R.id.exit:
			finish();
			break;
	}
	
}

private void makeDialog() {		
	
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);	    
    
    dialog.setMessage("This is a demo");

    dialog.setPositiveButton("Buy the full version", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {
        	Toast.makeText(getBaseContext(), "BUY IT", Toast.LENGTH_LONG).show();
        }
    });

    dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface arg0, int arg1) {}
    });

    dialog.show();
}
	

    
   // ImageView imageView= (ImageView)findViewById(R.id.imageview);
   // Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.layout.anim);
    //Now Set your animation
   // imageView.startAnimation(fadeInAnimation );
    
    
    
    }











