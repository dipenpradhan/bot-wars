package botwars.main;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.popup.TextPopupScene;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.text.ChangeableText;

import android.view.KeyEvent;

import com.badlogic.gdx.physics.box2d.Body;

/**********************************************************************************
 * 
 * Inherit from superclass BotWars and add multiplayer specific implementation
 * 
 **********************************************************************************/
public class BotWars_MultiPlayer extends BotWars {

	private AnimatedSprite player_mp_sprite;
	private Body player_mp_body;

	private boolean desPlayerMP = false;
	public boolean isRunning = false;

	private int mTeamScore;
	private ChangeableText mTeamScoreChangeableText;

	
	/**********************************************************************************
	 * 
	 * Create player 2 sprite and body
	 * Create and register updatehandler to add multiplayer movement and action handling to main game loop
	 * Create and register location correction timer
	 * Create team score display and attach to HUD
	 * 
	 **********************************************************************************/
	@Override
	public Scene onLoadScene() {

		super.onLoadScene();

		spawnPlayer("player_MP", super.mPlayer_MPTextureRegion);

		createLocationErrorCorrectionHandler();
		createMPHandler();

		// mScene.registerUpdateHandler(Bullet_MPHandler);

		player_mp_sprite = (AnimatedSprite) findShape("player_MP");
		player_mp_body = super.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_mp_sprite);
		startReceiverThread();

		mTeamScoreChangeableText = new ChangeableText(5, 35, mScoreFont, "Team: 0", "Team: XXXX".length());
		mTeamScoreChangeableText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mTeamScoreChangeableText.setAlpha(0.9f);
		mHUD.attachChild(mTeamScoreChangeableText);

		return super.mScene;

	}

	// =====================================================================================================================//
	// =====================================================================================================================//
	
	/**********************************************************************************
	 * 
	 * Override player movement methods from superclass BotWars 
	 * Add message sending mechanism to all player actions like jumping,moving and shooting
	 * 
	 **********************************************************************************/

	@Override
	public void jumpPlayer(Body _playerBody) {
		if (_playerBody.getUserData().toString().contains("player_self"))
			sendMessage("jump,0,0");
		// isStopped=false;
		super.jumpPlayer(_playerBody);
	}

	@Override
	public void movePlayerRight(AnimatedSprite _playerSprite, Body _playerBody) {

		if (_playerBody.getUserData().toString().contains("player_self")) {
			sendMessage("right,0,0");
			isStopped = false;
		}
		super.movePlayerRight(_playerSprite, _playerBody);
	}

	@Override
	public void movePlayerLeft(AnimatedSprite _playerSprite, Body _playerBody) {
		if (_playerBody.getUserData().toString().contains("player_self")) {
			sendMessage("left,0,0");
			isStopped = false;
		}
		super.movePlayerLeft(_playerSprite, _playerBody);
	}

	@Override
	public void stopPlayer(AnimatedSprite _playerSprite, Body _playerBody) {
		if (_playerBody.getUserData().toString().contains("player_self")) {
			if (!isStopped) {
				sendMessage("stop,0,0");

				isStopped = true;
				sendMessage("location," + mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().x + ","
						+ mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().y);
			}
		}
		super.stopPlayer(_playerSprite, _playerBody);
	}

	@Override
	public void spawnBullet(AnimatedSprite _playerSprite, int _playerDir, String bulletName) {

		super.spawnBullet(_playerSprite, _playerDir, bulletName);
		if (_playerSprite.getUserData().toString().contains("player_self"))
			sendMessage("bullet," + _playerDir + ",0");

	}

	// =====================================================================================================================//
	// =====================================================================================================================//

	
	
	/**********************************************************************************
	 * 
	 * sendMessage stub, add network specific implementation in subclasses
	 * 
	 **********************************************************************************/
	public void sendMessage(String str) {
		// sendMessage stub
	}

	/**********************************************************************************
	 * 
	 * receiveMessage stub, add network specific implementation in subclasses
	 * 
	 **********************************************************************************/
	public String receiveMessage() {
		// receiveMessage stub
		return null;
	}

	private boolean isStopped = true;

	/**********************************************************************************
	 * 
	 * send location of player and enemies every five seconds, to correct difference in location on two devices
	 * 
	 **********************************************************************************/
	private void createLocationErrorCorrectionHandler() {
		// final TimerHandler locationErrorCorrectionHandler;

		this.getEngine().registerUpdateHandler(new TimerHandler(5, true, new ITimerCallback() {

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")) != null) {
					sendMessage("location," + mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().x + ","

					+ mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("player_self")).getPosition().y);
				}

				for (int i = 0; i < enemyLandedArr.length; i++) {
					if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("enemy" + i)) != null)// &&enemyLandedArr[i])
					{
						sendMessage("enemy" + i + "," + mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("enemy" + i)).getPosition().x
								+ "," + mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape("enemy" + i)).getPosition().y);

					}
				}

			}
		}));
	}

	int playerDir_MP;
	boolean spawnBullet_MP = false;

	// private IUpdateHandler Bullet_MPHandler;
	private String removeEnemyName;
	private boolean removeEnemy;
	/**********************************************************************************
	 * 
	 * Create UpdateHandler to add multiplayer specific actions in the game loop
	 * spawning bullets, killing MP_player calculating AI actions and displaying team score is done here
	 * 
	 **********************************************************************************/
	private void createMPHandler() {
		this.getEngine().registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (spawnBullet_MP) {
					spawnBullet(player_mp_sprite, playerDir_MP, "bullet_MP");
					spawnBullet_MP = false;
				}
				if (desPlayerMP) {
					destroyGameObject("player_MP");
					TextPopupScene mTextPopupScene = new TextPopupScene(BotWars_MultiPlayer.this.getEngine().getCamera(), mScene.getChildScene(), mScoreFont,
							"PLAYER 2 HAS DIED", 5.0f);
					// mScene.setChildScene(mDigitalOnScreenControl);
					desPlayerMP = false;
				}
				doAICalculations(player_mp_body);

				// if(remainingEnemies==0)endGame();
				if (removeEnemy) {
					destroyGameObject("remove_" + removeEnemyName);
				}
				mTeamScoreChangeableText.setText("Team: " + mTeamScore);
			}

			@Override
			public void reset() {

			}

		});

	}
	/**********************************************************************************
	 * 
	 * Create thread to receive and handle messages
	 * 
	 **********************************************************************************/
	private void startReceiverThread() {
		isRunning = true;
		(new Thread() {

			public void run() {

				while (isRunning) {

					String[] msgArray = new String[2];
					msgArray = receiveMessage().split(",", 3);

					// Debug.d("incoming: " + msg);

					handleReceivedMessage(msgArray);

				}

			}
		}).start();

	}
	
	/**********************************************************************************
	 * 
	 * Handle incoming messages
	 * Define what to do when a particular message is received 
	 * 
	 **********************************************************************************/
	private void handleReceivedMessage(String[] msgArray) {
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
		if (msgArray[0].equalsIgnoreCase("remove")) {
			desPlayerMP = true;
		}

		if (msgArray[0].contains("enemy")) {
			if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(msgArray[0])) != null)
				mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(msgArray[0]))
						.setTransform(Float.parseFloat(msgArray[1]), Float.parseFloat(msgArray[2]), 0);
		}
		if (msgArray[0].equalsIgnoreCase("removeEnemy")) {
			if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(msgArray[1])) != null) {
				removeEnemy = true;
				removeEnemyName = msgArray[1];

			}
		}

	}
	/**********************************************************************************
	 * 
	 * Override endGame method from superclass BotWars 
	 * Add mechanism to send message to remove player 2 and stop receiver thread
	 * 
	 **********************************************************************************/
	@Override
	public void endGame(int action) {
		sendMessage("remove,0,0");
		isRunning = false;
		super.endGame(action);
	}

	/**********************************************************************************
	 * 
	 * Define behaviour of hardware keys
	 * stop receiver thread when back key or home key is pressed
	 * 
	 **********************************************************************************/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) && event.getRepeatCount() == 0) {

			isRunning = false;
			// Intent openStartMenu = new Intent(BotWars_MultiPlayer.this,
			// StartMenu.class);
			// startActivity(openStartMenu);

			// finish();
			super.onKeyDown(keyCode, event);
			// do something on back.
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	
	/**********************************************************************************
	 * 
	 * Override destroyGameObject method from superclass BotWars
	 * Add mechanism to send message for confirming removal of enemy on other device
	 * 
	 **********************************************************************************/
	
	@Override
	public void destroyGameObject(String name) {

		if (name.contains("remove_")) {
			name = name.substring(name.indexOf("_") + 1, name.length());

		}

		else if (name.contains("enemy")) {
			sendMessage("removeEnemy," + name + ",0");
		}

		super.destroyGameObject(name);
	}

	/**********************************************************************************
	 * 
	 * Override reduceRemainingEnemies method from superclass BotWars 
	 * Add mechanism to update Team Score
	 * 
	 **********************************************************************************/	
	@Override
	public void reduceRemainingEnemies() {
		mTeamScore = (enemyCount - remainingEnemies) * 10;
		super.reduceRemainingEnemies();
	}
}