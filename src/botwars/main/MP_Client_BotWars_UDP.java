package botwars.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.util.Debug;

import com.badlogic.gdx.physics.box2d.Body;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MP_Client_BotWars_UDP extends BotWars {
private DatagramSocket mDatagramSocket;
private BufferedReader mBufferedReader; 
AnimatedSprite player_mp_sprite;
Body player_mp_body;
//http://www.andengine.org/forums/development/body-settransform-t1150.html
	@Override
	public Scene onLoadScene() {
		 
		try {
			mDatagramSocket=new DatagramSocket(50000);
		} catch (SocketException e) {
			
		}
			 
		 super.onLoadScene();
		 spawnPlayer("player_MP", super.mPlayer_MPTextureRegion);
		 
		 
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
	                if(msgArray[0].equalsIgnoreCase("stop")){stopPlayer();}
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
	
	int playerDir_MP;
	boolean spawnBullet_MP=false;
	

private IUpdateHandler Bullet_MPHandler;

private void createBullet_MPHandler(){
	Bullet_MPHandler=new IUpdateHandler(){

		@Override
		public void onUpdate(float pSecondsElapsed) {
			if(spawnBullet_MP)
			{
				spawnBullet(player_mp_sprite, playerDir_MP);
				spawnBullet_MP=false;
			}
	}

	@Override
	public void reset() {

		
	}
	
};
}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 4) {// resume
		
			
	    	createBullet_MPHandler();
	    	mScene.registerUpdateHandler(Bullet_MPHandler);
			 
	    	player_mp_sprite=(AnimatedSprite)findShape("player_MP");
			player_mp_body=super.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_mp_sprite);	
			/*( new Thread() {

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
			              /*  if(msgArray[0].contains("bullet")){
			                	if(msgArray[1].equals(String.valueOf(PLAYER_DIRECTION_LEFT)))spawnBullet(player_mp_sprite, PLAYER_DIRECTION_LEFT);
			                	if(msgArray[1].equals(String.valueOf(PLAYER_DIRECTION_RIGHT)))spawnBullet(player_mp_sprite, PLAYER_DIRECTION_RIGHT);
			                }/////////
			                
			                if(msgArray[0].contains("bullet")){
			                	playerDir_MP=Integer.parseInt(msgArray[1]);
			                	spawnBullet_MP=true;
			                	
			                }
			                
			                
			                if(msgArray[0].contains("location")){
			                	player_mp_body.setTransform(Float.parseFloat(msgArray[1]), Float.parseFloat(msgArray[2]), 0);
			                }
			                if(msgArray[0].contains("jump")){jumpPlayer(player_mp_body);}//jumpImpulse();}	
			                if(msgArray[0].equalsIgnoreCase("right")){movePlayerRight(player_mp_sprite, player_mp_body);}//movePlayerRight("player_x");}
			                if(msgArray[0].equalsIgnoreCase("left")){movePlayerLeft(player_mp_sprite, player_mp_body);}//movePlayerLeft();}
			                if(msgArray[0].equalsIgnoreCase("stop")){stopPlayer(player_mp_sprite, player_mp_body);}//stopPlayer();}
						}
					} catch (IOException e) {
						Debug.d("error in bufferedreader");
					}
				}

				}

				).start();*/
			
			
			( new Thread() {

				public void run() {
				

					while(true)
						{
						byte buf[]=new byte[128];
					
						DatagramPacket mDatagramPacket=new DatagramPacket(buf,0,buf.length);
						try {
							mDatagramSocket.receive(mDatagramPacket);
						} catch (IOException e) {
							Debug.d("IOException while receiving packet");
						}
						
						
						//buf=mDatagramPacket.getData();
					
						String msg;
						
				
								msg = new String(mDatagramPacket.getData());
							
						
						msg.trim();
						
			               String[] msgArray=new String[2];
			               msgArray=msg.split(",",3);
			                Debug.d("look: " +msg);
			                
			                if(msgArray[0].contains("bullet")){
			                	playerDir_MP=Integer.parseInt(msgArray[1]);
			                	spawnBullet_MP=true;
			                	
			                }

			                
			                if(msgArray[0].contains("location")){
			                	player_mp_body.setTransform(Float.parseFloat(msgArray[1]), Float.parseFloat(msgArray[2]), 0);
			                }
			                if(msgArray[0].contains("jump")){jumpPlayer(player_mp_body);}//jumpImpulse();}	
			                if(msgArray[0].equalsIgnoreCase("right")){movePlayerRight(player_mp_sprite, player_mp_body);}//movePlayerRight("player_x");}
			                if(msgArray[0].equalsIgnoreCase("left")){movePlayerLeft(player_mp_sprite, player_mp_body);}//movePlayerLeft();}
			                if(msgArray[0].equalsIgnoreCase("stop")){stopPlayer(player_mp_sprite, player_mp_body);}//stopPlayer();}
							}



				}}).start();
		}
		return super.onOptionsItemSelected(item);
	}

/*


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
                if(strarr[0].contains("jump")){}//jumpPlayer();}	
                if(strarr[0].equalsIgnoreCase("right")){}//movePlayerRight("player_x");}
                if(strarr[0].equalsIgnoreCase("left")){}//movePlayerLeft("player_x");}
                if(strarr[0].equalsIgnoreCase("stop")){}//stopPlayer();}
                
               }
        } catch (Exception ex) { 
Debug.d("lost connection to server");
        	//Toast.makeText(BotWars.this, "LOST CONNECTION TO SERVER", Toast.LENGTH_LONG).show();
        }}
	
}

*/


	
}
