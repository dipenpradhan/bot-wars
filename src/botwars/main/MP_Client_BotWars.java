package botwars.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.util.Debug;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MP_Client_BotWars extends BotWars {
private Socket mSocket;
private BufferedReader mBufferedReader; 


	@Override
	public Scene onLoadScene() {
		 try {
				mSocket=new Socket(InetAddress.getByName("192.168.1.130"), 50000);
				mBufferedReader=new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			} catch (UnknownHostException e) {
				
				Toast.makeText(MP_Client_BotWars.this, "UNKNOWN SERVER ADDRESS", 100).show();
			} catch (IOException e) {
				
				Toast.makeText(MP_Client_BotWars.this, "ERROR CREATING SOCKET", 100).show();
			}
		 super.onLoadScene();
	//	 createMsgRecHandler();
	//	super.mScene.registerUpdateHandler(msgRecHandler);
		return super.mScene;
	}

	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 4, 0, "Start Receiver Thread");
		return super.onCreateOptionsMenu(menu);
	}
/*
private IUpdateHandler msgRecHandler;

private void createMsgRecHandler(){
	msgRecHandler=new IUpdateHandler(){

		@Override
		public void onUpdate(float pSecondsElapsed) {
			try {
				//Debug.d("LOOK:  "+ mBufferedReader.readLine());
				 String msg = mBufferedReader.readLine() + "\n";
	               String[] msgArray=new String[2];
	               msgArray=msg.split(",",3);
	                Debug.d("look: " +msg);
	                //if(str.contains("jump")){jumpImpulse();} 
	                if(msgArray[0].contains("jump")){jumpImpulse();}	
	                if(msgArray[0].equalsIgnoreCase("right")){Debug.d("MOVING RIGHT/////////////////////");movePlayerRight();}
	                if(msgArray[0].equalsIgnoreCase("left")){movePlayerLeft();}
	                if(msgArray[0].equalsIgnoreCase("stop")){doNotMovePlayer();}
			} catch (IOException e) {
				Debug.d("error in bufferedreader");
			}
			//recMessage();
		}

		@Override
		public void reset() {

			
		}
		
	};
}
*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 4) {// resume
		
			
	    	
			
			( new Thread() {

				public void run() {

				//recMessage();
					try {
						//Debug.d("LOOK:  "+ mBufferedReader.readLine());
						while(true)
						{ String msg = mBufferedReader.readLine() + "\n";
			               String[] msgArray=new String[2];
			               msgArray=msg.split(",",3);
			                Debug.d("look: " +msg);
			                //if(str.contains("jump")){jumpImpulse();} 
			                if(msgArray[0].contains("jump")){jumpImpulse();}	
			                if(msgArray[0].equalsIgnoreCase("right")){movePlayerRight();}
			                if(msgArray[0].equalsIgnoreCase("left")){movePlayerLeft();}
			                if(msgArray[0].equalsIgnoreCase("stop")){doNotMovePlayer();}}
					} catch (IOException e) {
						Debug.d("error in bufferedreader");
					}
				}

				}

				).start();
		}
		return super.onOptionsItemSelected(item);
	}




	public void recMessage() {

        InputStream is = null;
        if(mSocket!=null)
        	{try {
            is = mSocket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            
               while(true){ String str = br.readLine() + "\n";
               String[] strarr=new String[2];
               strarr=str.split(",",3);
                Debug.d("look: " +str);
                //if(str.contains("jump")){jumpImpulse();} 
                if(strarr[0].contains("jump")){jumpImpulse();}	
                if(strarr[0].equalsIgnoreCase("right")){Debug.d("MOVING RIGHT/////////////////////");movePlayerRight();}
                if(strarr[0].equalsIgnoreCase("left")){movePlayerLeft();}
                if(strarr[0].equalsIgnoreCase("stop")){doNotMovePlayer();}
                
               }
        } catch (Exception ex) { 
Debug.d("lost connection to server");
        	//Toast.makeText(BotWars.this, "LOST CONNECTION TO SERVER", Toast.LENGTH_LONG).show();
        }}
	
}




	
}
