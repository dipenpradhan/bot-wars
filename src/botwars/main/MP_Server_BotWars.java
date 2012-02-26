package botwars.main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.util.Debug;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MP_Server_BotWars extends BotWars {

	private Socket mSocket;
	private ServerSocket mServerSocket;
	//private IUpdateHandler locationUpdater;
	private OutputStream mOutputStream;
	private PrintWriter mPrintWriterOUT;
	
	@Override
	public Scene onLoadScene() {
		
		try {
			mServerSocket = new ServerSocket(50000);

		} catch (IOException e) {

			Debug.d("Error creating ServerSocket");
		}
		//createMPLocationSendHandler();
		
		super.onLoadScene();
		//super.mScene.registerUpdateHandler(locationUpdater);
		
		return super.mScene;
		
		//return super.onLoadScene();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 4, 0, "Start ServerSocket");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void spawnBullet() {
		  
         
		super.spawnBullet();
		mPrintWriterOUT.println("jump,0,0");
		mPrintWriterOUT.flush();
		//sendMessage("jump,0,0");
	}
public void sendMessage(String pMessage)
{
	OutputStream pOutputStream;
	try {
		pOutputStream = mSocket.getOutputStream();
		PrintWriter pPrintWriterOUT = new PrintWriter(pOutputStream);
          pPrintWriterOUT.println(pMessage);
          //out.println("location: x is "+getPlayerLocX()+"y is "+getPlayerLocY());
          pPrintWriterOUT.flush();
          Debug.d("SENT MESSAGE   "+pMessage);
	} catch (IOException e) {
		Debug.d("Error writing to opstream");
	}

}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 4) {

			try {
				mSocket=mServerSocket.accept();
				mOutputStream=mSocket.getOutputStream();
				mPrintWriterOUT=new PrintWriter(mOutputStream);
			} catch (IOException e) {
				Debug.d("Error accepting ServerSocket");
			}
/*
			(new Thread() {

				public void run() {

				}

			}

			).start();*/
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	private boolean isStopped=true;
	

	@Override
	public void movePlayerRight() {
		mPrintWriterOUT.println("right,0,0");
		mPrintWriterOUT.flush();
		super.movePlayerRight();
		isStopped=false;
	}

	@Override
	public void movePlayerLeft() {
		mPrintWriterOUT.println("left,0,0");
		mPrintWriterOUT.flush();
		super.movePlayerLeft();
		isStopped=false;
	}

	@Override
	public void doNotMovePlayer() {
		
		if(mPrintWriterOUT!=null){if(!isStopped){mPrintWriterOUT.println("stop,0,0");
		mPrintWriterOUT.flush();isStopped=true;}
		super.doNotMovePlayer();
		}
		
	}

	
	
	//
	//
	//
	//
	//
	//
	//
	//
	//
	
	
	
	/*
	public void createMPLocationSendHandler() {
		
		locationUpdater = new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				OutputStream os;
				if(mSocket!=null){try {
					os = mSocket.getOutputStream();
					PrintWriter out = new PrintWriter(os);
			          out.println("move,"+getPlayerLocX()+","+getPlayerLocY());
			          //out.println("location: x is "+getPlayerLocX()+"y is "+getPlayerLocY());
			          out.flush();
			          Debug.d("moooooooooooooove");
				} catch (IOException e) {
					Debug.d("Error writing to opstream");
				}
				}
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			};
			}
	*/
	
	
	
	
		
	
}