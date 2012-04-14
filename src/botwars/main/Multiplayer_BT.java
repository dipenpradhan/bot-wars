package botwars.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.util.Debug;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import com.badlogic.gdx.physics.box2d.Body;

public class Multiplayer_BT extends BotWars_MultiPlayer {

	private DatagramSocket mDatagramSocket;
	private AnimatedSprite player_mp_sprite;
	private Body player_mp_body;
	private static String ipAdd;
	private OutputStream mOutputStream;
	private PrintWriter mPrintWriterOUT;
	private BufferedReader mBufferedReader; 
	
	private static BluetoothSocket mSocket;
	private boolean isRunning=false;

	
	private void initBT()
	{
	
		
		try {

				mBufferedReader=new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
				mOutputStream=mSocket.getOutputStream();
				mPrintWriterOUT=new PrintWriter(mOutputStream);
			} catch (UnknownHostException e) {
				
				Toast.makeText(Multiplayer_BT.this, "UNKNOWN SERVER ADDRESS", 100).show();
			} catch (IOException e) {
				
				Toast.makeText(Multiplayer_BT.this, "ERROR CREATING SOCKET", 100).show();
			}
		
		
		 
	}
	
	
	
	@Override
	public String receiveMessage()
	{
		

		String receivedMessage;
		try {
			receivedMessage = new String(mBufferedReader.readLine()+ "\n");
			receivedMessage.trim();
			return receivedMessage;
		} catch (IOException e) {
			Debug.d("error reading stream");
		}

		return null;
		
	}
	
	
	@Override
	public Scene onLoadScene() {


		initBT();
		
		super.onLoadScene();

		
		
		
		return super.mScene;
		


	}

	@Override
	public void sendMessage(String str) {

		mPrintWriterOUT.println(str);
		mPrintWriterOUT.flush();
	}

	
	public static void setBluetoothSocket(BluetoothSocket _BTSocket)
	{
		mSocket=_BTSocket;
	}


	
	

}