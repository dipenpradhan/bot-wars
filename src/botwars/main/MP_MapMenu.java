package botwars.main;

import botwars.main.MapMenu.ImageAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MP_MapMenu extends MapMenu{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		 GridView gridView = (GridView) findViewById(R.id.gridview);
	        gridView.setAdapter(new ImageAdapter(this));
	        gridView.setOnItemClickListener(new OnItemClickListener()
	        {
	        public void onItemClick(AdapterView<?> parent,
	        View v, int position, long id)
	        {if(position<3)
	       {
	        BotWars.setMap(position);
	        makeModeDialog();
	        /*Intent StartIntent = new Intent(MP_MapMenu.this, BotWars.class);
			startActivity(StartIntent);
			finish();
	        */
	       }
	        else Toast.makeText(getBaseContext(),
	               "Map Not Available",
	                Toast.LENGTH_SHORT).show();
				
	        }
	        });
	}

	private void makeModeDialog() {

		AlertDialog.Builder modeDialog = new AlertDialog.Builder(this);

		modeDialog.setMessage("Select Mode");

		modeDialog.setPositiveButton("Server", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent openMP_Server_BotWars = new Intent(MP_MapMenu.this,MP_Server_BotWars.class);
				startActivity(openMP_Server_BotWars);
				finish();
			}
		});

		modeDialog.setNegativeButton("Client", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent openMP_Client_BotWars = new Intent(MP_MapMenu.this,MP_Client_BotWars.class);
				startActivity(openMP_Client_BotWars);
				finish();
			}
		});

		modeDialog.show();
	}
	
}
