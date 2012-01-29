package anden.examples.testing;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class StartMenu extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startmenu);
	
	    }

		
		
		
	//});
	//}
	public void startGame(View v)
	{
    Intent StartIntent = new Intent(StartMenu.this,BotWars.class);
    startActivity(StartIntent);
    
    finish();
	}
		public void exit(View v)
		{
			finish();
			System.exit(0);
		}
		
		
}
