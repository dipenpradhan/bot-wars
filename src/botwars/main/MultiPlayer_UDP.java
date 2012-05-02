package botwars.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.util.Debug;

import android.view.KeyEvent;
import android.widget.Toast;

/**********************************************************************************
 * 
 * Inherit from superclass BotWars_MultiPlayer and add network specific implementation using UDP packets
 * 
 **********************************************************************************/

public class MultiPlayer_UDP extends BotWars_MultiPlayer {

	private DatagramSocket mDatagramSocket;
	private static String ipAdd;


	

	/**********************************************************************************
	 * 
	 * Create a datagram socket on port 5000
	 * 
	 **********************************************************************************/
	private void initUDP()
	{

		try {
			mDatagramSocket = new DatagramSocket(5000);
		} catch (SocketException e) {
			Toast.makeText(MultiPlayer_UDP.this, "Error Creating Socket", 100).show();
			Debug.d("Error Creating Socket");
		}
	}
	
	
	/**********************************************************************************
	 * 
	 * Override receiveMessage method from superclass BotWars_MultiPlayer 
	 * Add mechanism to receive UDP packets and obtain the String contained in them
	 * 
	 **********************************************************************************/
	
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



		String receivedMessage = new String(mDatagramPacket.getData());

		receivedMessage.trim();
		return receivedMessage;
	}
	
	/**********************************************************************************
	 * 
	 * initialize datagram socket on port 5000
	 * 
	 **********************************************************************************/
	
	@Override
	public Scene onLoadScene() {

		initUDP();
		
		super.onLoadScene();
	
		return super.mScene;

	}
	/**********************************************************************************
	 * 
	 * Override sendMessage method from superclass BotWars_MultiPlayer 
	 * Add mechanism to create a UDP packet containing given string and send it to given IP address on port 50000
	 * 
	 **********************************************************************************/
	
	@Override
	public void sendMessage(String str) {

		DatagramPacket mDatagramPacket;
		try {

			mDatagramPacket = new DatagramPacket(str.trim().getBytes(), 0, str.trim().getBytes().length, InetAddress.getByName(ipAdd), 5000);

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

	/**********************************************************************************
	 * 
	 * Method for other classes to set IP address of player 2
	 * 
	 **********************************************************************************/
	
	public static void setIPAdd(String ip)
	{
		ipAdd=ip;
	}

	/**********************************************************************************
	 * 
	 * Override endGame method from superclass BotWars 
	 * Add mechanism to close datagram socket
	 * 
	 **********************************************************************************/
	@Override
	public void endGame(int action)
	{
		
		super.endGame(action);
		mDatagramSocket.close();
	}
	

	/**********************************************************************************
	 * 
	 * Define behaviour of hardware keys
	 * Close socket if back key or home key is pressed
	 * 
	 **********************************************************************************/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK ||keyCode == KeyEvent.KEYCODE_HOME) && event.getRepeatCount() == 0) {
		//	mDatagramSocket.close();
			
			super.onKeyDown(keyCode, event);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}