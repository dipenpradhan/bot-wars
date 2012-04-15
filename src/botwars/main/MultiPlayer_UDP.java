package botwars.main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.badlogic.gdx.physics.box2d.Body;

public class MultiPlayer_UDP extends BotWars_MultiPlayer {

	private DatagramSocket mDatagramSocket;
	private AnimatedSprite player_mp_sprite;
	private Body player_mp_body;
	private static String ipAdd;
	private OutputStream mOutputStream;
	private PrintWriter mPrintWriterOUT;
	

	

	
	private void initUDP()
	{

		try {
			mDatagramSocket = new DatagramSocket(50000);
		} catch (SocketException e) {
			Toast.makeText(MultiPlayer_UDP.this, "Error Creating Socket", 100).show();
			Debug.d("Error Creating Socket");
		}
	}
	
	
	
	@Override
	public String receiveMessage()
	{
		byte buf[] = new byte[128];

		DatagramPacket mDatagramPacket = new DatagramPacket(buf, 0, buf.length);
		try {
			mDatagramSocket.receive(mDatagramPacket);
		} catch (IOException e) {
			Debug.d("IOException while receiving packet");
			
			isRunning=false;
			mDatagramSocket.close();
			
		}

		String msg;

		String receivedMessage = new String(mDatagramPacket.getData());

		receivedMessage.trim();
		return receivedMessage;
	}
	
	
	@Override
	public Scene onLoadScene() {


		initUDP();
		
		super.onLoadScene();

		
		
		
		return super.mScene;
		


	}

	@Override
	public void sendMessage(String str) {

		DatagramPacket mDatagramPacket;
		try {

			mDatagramPacket = new DatagramPacket(str.trim().getBytes(), 0, str.trim().getBytes().length, InetAddress.getByName(ipAdd), 50000);

			mDatagramSocket.send(mDatagramPacket);
		} catch (UnknownHostException e) {
			Debug.d("Unknown Host");
			
			isRunning=false;
			mDatagramSocket.close();
			
		}

		catch (IOException e) {
			Debug.d("IOException while sending packet");
			
			isRunning=false;
			mDatagramSocket.close();
			
		}
	}

	
	public static void setIPAdd(String ip)
	{
		ipAdd=ip;
	}


	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			mDatagramSocket.close();
			super.onKeyDown(keyCode, event);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}