package botwars.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.util.Debug;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;


/**********************************************************************************
 * 
 * Inherit from superclass BotWars_Multiplayer and add Bluetooth specific implementation
 * 
 **********************************************************************************/

public class Multiplayer_BT extends BotWars_MultiPlayer {


	private OutputStream mOutputStream;
	private PrintWriter mPrintWriterOUT;
	private BufferedReader mBufferedReader; 
	
	private static BluetoothSocket mSocket;
	

	/**********************************************************************************
	 * 
	 * obtain input and output streams of BluetoothSocket
	 * 
	 **********************************************************************************/
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

	/**********************************************************************************
	 * 
	 * initialize bluetooth socket, streams, reader and writer in onLoadScene
	 * 
	 **********************************************************************************/
	
	@Override
	public Scene onLoadScene() {

		initBT();
		
		super.onLoadScene();
		return super.mScene;
		


	}

	/**********************************************************************************
	 * 
	 * Override endGame method from superclass BotWars
	 * Add mechanism to close socket and stop receiving thread 
	 * 
	 **********************************************************************************/
	@Override
public void endGame(int action)
{   super.endGame(action);
	//MapMenu_BT.stopBTThread();
	
}
	
	
	
	/**********************************************************************************
	 * 
	 * Override receiveMessage method from superclass BotWars_MultiPlayer 
	 * Add mechanism to read from inputStream of socket and return incoming message
	 * 
	 **********************************************************************************/
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
			isRunning=false;
			endGame(0);
		}

		return null;
		
	}
	/**********************************************************************************
	 * 
	 * Override sendMessage method from superclass BotWars_MultiPlayer 
	 * Add mechanism to write given String to outputStream
	 * 
	 **********************************************************************************/
	
	@Override
	public void sendMessage(String str) {

		mPrintWriterOUT.println(str);
		mPrintWriterOUT.flush();
	}

	/**********************************************************************************
	 * 
	 * Method for other classes to pass BluetoothSocket
	 * 
	 **********************************************************************************/
	
	public static void setBluetoothSocket(BluetoothSocket _BTSocket)
	{
		mSocket=_BTSocket;
	}


	
	

}