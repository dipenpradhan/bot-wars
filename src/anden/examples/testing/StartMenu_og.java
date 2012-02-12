package anden.examples.testing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class StartMenu_og extends Activity {
	final BotWars bw = new BotWars();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startmenu);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		final EditText edtx_impul = (EditText) findViewById(R.id.edtx_impul);
		edtx_impul.setText("14.0f");
		edtx_impul.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press

					BotWars.setImpulse(edtx_impul.getText().toString());
					Toast.makeText(StartMenu_og.this, edtx_impul.getText(),
							Toast.LENGTH_SHORT).show();

					return true;
				}
				return false;
			}
		});

		final EditText edtx_veloc = (EditText) findViewById(R.id.edtx_veloc);
		edtx_veloc.setText("8.0f");
		edtx_veloc.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press

					/////////////BotWars.setVelocity(edtx_veloc.getText().toString());
					Toast.makeText(StartMenu_og.this, edtx_veloc.getText(),
							Toast.LENGTH_SHORT).show();

					return true;
				}
				return false;
			}
		});

		final Spinner spn_map = (Spinner) findViewById(R.id.spn_map);
		ArrayAdapter<CharSequence> maps_adapter = ArrayAdapter.createFromResource(
				this, R.array.maps_array, android.R.layout.simple_spinner_item);
		maps_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spn_map.setAdapter(maps_adapter);

		spn_map.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				//Toast.makeText(parent.getContext(),"The planet is "+ parent.getItemAtPosition(pos).toString(),Toast.LENGTH_LONG).show();
				BotWars.setMap(pos);
				//if(pos==0){BotWars.setMap(pos);}
				//if(pos==1){BotWars.setMap(pos);}
				//if(pos==2){BotWars.setMap(pos);}
				
			}

			public void onNothingSelected(AdapterView parent) {
				// Do nothing.
			}

		});

	
	final Spinner spn_scene = (Spinner) findViewById(R.id.spn_scene);
	ArrayAdapter<CharSequence> scenes_adapter = ArrayAdapter.createFromResource(
			this, R.array.scene_array, android.R.layout.simple_spinner_item);
	scenes_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	spn_scene.setAdapter(scenes_adapter);

	spn_scene.setOnItemSelectedListener(new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View view,
				int pos, long id) {
			//Toast.makeText(parent.getContext(),"The planet is "+ parent.getItemAtPosition(pos).toString(),Toast.LENGTH_LONG).show();
			//if(pos==0){BotWars.setScene(pos);}
			//if(pos==1){BotWars.setScene(pos);}
			//if(pos==2){BotWars.setScene(pos);}
			//if(pos==2){BotWars.setScene(pos);}
			BotWars.setScene(pos);
		}

		public void onNothingSelected(AdapterView parent) {
			// Do nothing.
		}

	});

}

	// });
	// }
	public void startGame(View v) {

		Intent StartIntent = new Intent(StartMenu_og.this, BotWars.class);
		startActivity(StartIntent);

		finish();
	}

	public void exit(View v) {
		finish();
		System.exit(0);
	}

}
