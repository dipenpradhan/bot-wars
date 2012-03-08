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

public class MP_Server_BotWars_UDP extends BotWars {

	private DatagramSocket mDatagramSocket;
	private AnimatedSprite player_mp_sprite;
	private Body player_mp_body;
	private static String ipAdd;
	private OutputStream mOutputStream;
	private PrintWriter mPrintWriterOUT;
	

	private boolean isRunning=false;

	@Override
	public Scene onLoadScene() {


		super.onLoadScene();

		try {
			mDatagramSocket = new DatagramSocket(50000);
		} catch (SocketException e) {
			Toast.makeText(MP_Server_BotWars_UDP.this, "Error Creating Socket", 100).show();
			Debug.d("Error Creating Socket");
		}
		spawnPlayer("player_MP", super.mPlayer_MPTextureRegion);
		
		
		createLocationErrorCorrectionHandler();
		createBullet_MPHandler();
		
		//mScene.registerUpdateHandler(Bullet_MPHandler);

		player_mp_sprite = (AnimatedSprite) findShape("player_MP");
		player_mp_body = super.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_mp_sprite);
		startReceiverThread();
	
		
		
		return super.mScene;
		


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, 4, 0, "Start Game");
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void jumpPlayer(Body _playerBody) {
		if(_playerBody.getUserData().toString().contains("player_self"))sendMessage("jump,0,0");
		// isStopped=false;
		super.jumpPlayer(_playerBody);
	}

	@Override
	public void spawnBullet(AnimatedSprite _playerSprite, int _playerDir) {

		super.spawnBullet(_playerSprite, _playerDir);
		if(_playerSprite.getUserData().toString().contains("player_self"))
		sendMessage("bullet," + _playerDir + ",0");

	}

	public void sendMessage(String str) {

		DatagramPacket mDatagramPacket;
		try {

			mDatagramPacket = new DatagramPacket(str.trim().getBytes(), 0, str.trim().getBytes().length, InetAddress.getByName(ipAdd), 50000);

			mDatagramSocket.send(mDatagramPacket);
		} catch (UnknownHostException e) {
			Debug.d("Unknown Host");
		}

		catch (IOException e) {
			Debug.d("IOException while sending packet");

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 4) {

			createLocationErrorCorrectionHandler();

			createBullet_MPHandler();
			//mScene.registerUpdateHandler(Bullet_MPHandler);

			player_mp_sprite = (AnimatedSprite) findShape("player_MP");
			player_mp_body = super.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_mp_sprite);
startReceiverThread();
			
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean isStopped = true;

	@Override
	public void movePlayerRight(AnimatedSprite _playerSprite, Body _playerBody) {

		if(_playerBody.getUserData().toString().contains("player_self"))
		{sendMessage("right,0,0");
		isStopped = false;}
		super.movePlayerRight(_playerSprite, _playerBody);
	}

	@Override
	public void movePlayerLeft(AnimatedSprite _playerSprite, Body _playerBody) {
		if(_playerBody.getUserData().toString().contains("player_self")){
		sendMessage("left,0,0");
		isStopped = false;
		}
		
		super.movePlayerLeft(_playerSprite, _playerBody);
		
	}

	@Override
	public void stopPlayer(AnimatedSprite _playerSprite, Body _playerBody) {

		if(_playerBody.getUserData().toString().contains("player_self")){
				if (!isStopped) {
			sendMessage("stop,0,0");

			isStopped = true;
			sendMessage("location," + mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().x + ","
					+ mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().y);

		}}

		super.stopPlayer(_playerSprite, _playerBody);
	}

	private void createLocationErrorCorrectionHandler() {
		//final TimerHandler locationErrorCorrectionHandler;

		this.getEngine().registerUpdateHandler(new TimerHandler(5, true, new ITimerCallback() {

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				
				sendMessage("location," + mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().x + ","
						+ mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().y);

			}
		}));
	}

	int playerDir_MP;
	boolean spawnBullet_MP = false;

	//private IUpdateHandler Bullet_MPHandler;

	private void createBullet_MPHandler() {
		this.getEngine().registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (spawnBullet_MP) {
					spawnBullet(player_mp_sprite, playerDir_MP);
					spawnBullet_MP = false;
				}
			}

			@Override
			public void reset() {

			}

		});
		
		
	}
	
	
	private void startReceiverThread()
	{isRunning=true;
		(new Thread() {

			public void run() {

				while (isRunning) {
					byte buf[] = new byte[128];

					DatagramPacket mDatagramPacket = new DatagramPacket(buf, 0, buf.length);
					try {
						mDatagramSocket.receive(mDatagramPacket);
					} catch (IOException e) {
						Debug.d("IOException while receiving packet");
					}

					String msg;

					msg = new String(mDatagramPacket.getData());

					msg.trim();

					String[] msgArray = new String[2];
					msgArray = msg.split(",", 3);
					Debug.d("incoming: " + msg);

					if (msgArray[0].contains("bullet")) {
						playerDir_MP = Integer.parseInt(msgArray[1]);
						spawnBullet_MP = true;

					}

					if (msgArray[0].contains("location")) {
						player_mp_body.setTransform(Float.parseFloat(msgArray[1]), Float.parseFloat(msgArray[2]), 0);
					}
					if (msgArray[0].contains("jump")) {
						jumpPlayer(player_mp_body);
					}
					if (msgArray[0].equalsIgnoreCase("right")) {
						movePlayerRight(player_mp_sprite, player_mp_body);
					}
					if (msgArray[0].equalsIgnoreCase("left")) {
						movePlayerLeft(player_mp_sprite, player_mp_body);
					}
					if (msgArray[0].equalsIgnoreCase("stop")) {
						stopPlayer(player_mp_sprite, player_mp_body);
					}
				}

			}
		}).start();

	}
	
	
	public static void setIPAdd(String ip)
	{
		ipAdd=ip;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			isRunning=false;
			Intent openStartMenu = new Intent(MP_Server_BotWars_UDP.this, StartMenu.class);
			startActivity(openStartMenu);
			
			finish();
			// do something on back.
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	

}