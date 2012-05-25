package botwars.main;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXObject;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXObjectGroup;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;


public class BotWars extends BaseGameActivity implements IPinchZoomDetectorListener, IOnSceneTouchListener, IScrollDetectorListener {

	// ===========================================================
	// Constants
	// ===========================================================

	// Camera Parameters
	private static final short CAMERA_WIDTH = 800;
	private static final short CAMERA_HEIGHT = 480;

	// Player Direction
	public static final short PLAYER_DIRECTION_RIGHT = 1;
	public static final short PLAYER_DIRECTION_LEFT = -1;

	// OptionsMenu Action
	private static final short MENU_RETURN = 0;
	private static final short MENU_EXIT = 1;

	// EndGame Action
	private static final short SHOW_START_MENU_ACTIVITY = 0;
	private static final short SHOW_GAME_OVER_ACTIVITY = 1;
	private static final short SHOW_LEVEL_COMPLETE_ACTIVITY = 2;

	// Category bits for creating FixtureDefs
	private static final short CATEGORYBIT_WALL = 1;
	private static final short CATEGORYBIT_PLAYER = 2;
	private static final short CATEGORYBIT_ENEMY = 4;
	private static final short CATEGORYBIT_BULLET = 8;

	// Mask bits for creating FixtureDefs
	// Put category bits of bodies which are supposed to collide in respective
	// mask bits
	private static final short MASKBITS_WALL = CATEGORYBIT_WALL + CATEGORYBIT_PLAYER + CATEGORYBIT_ENEMY + CATEGORYBIT_BULLET;
	private static final short MASKBITS_PLAYER = CATEGORYBIT_WALL + CATEGORYBIT_PLAYER + CATEGORYBIT_ENEMY;
	private static final short MASKBITS_ENEMY = CATEGORYBIT_WALL + CATEGORYBIT_BULLET + CATEGORYBIT_ENEMY + CATEGORYBIT_PLAYER;
	private static final short MASKBITS_BULLET = CATEGORYBIT_WALL + CATEGORYBIT_ENEMY;

	// ===========================================================
	// Fields
	// ===========================================================
	private int bulletCount = 0;

	private SmoothCamera mCamera;
	public HUD mHUD;
	public Scene mScene;
	public PhysicsWorld mPhysicsWorld;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mOnScreenControlTexture;
	private BitmapTextureAtlas mHUDTextureAtlas;
	private BitmapTextureAtlas mScoreTextureAtlas;

	public TiledTextureRegion mPlayer_MPTextureRegion;
	private TiledTextureRegion mPlayerTextureRegion;
	private TiledTextureRegion mEnemyTextureRegion;
	private TiledTextureRegion mBulletTextureRegion;
	private TiledTextureRegion mHealthTextureRegion;

	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;
	private TextureRegion mJumpTextureRegion;
	private TextureRegion mShootTextureRegion;

	private AnimatedSprite mHealthSprite;

	private DigitalOnScreenControl mDigitalOnScreenControl;

	public Font mScoreFont;
	private ChangeableText mScoreChangeableText;
	private ChangeableText mTimerChangeableText;
	private ChangeableText mRemainingEnemiesChangeableText;

	private TMXTiledMap mTMXTiledMap;

	private RepeatingSpriteBackground mRepeatingSpriteBackground;

	private Music mMusic;
	private Sound mWalkSound, mShootSound;

	private static boolean enableMusic;
	private static boolean enableSounds;
	public boolean isPlayerMoving = false;
	private boolean isLanded = false;
	private boolean enemyLanded = false;
	private boolean desBull = false;
	private boolean desEnemy = false;
	private boolean reduceHealth = false;
	private boolean enemyShot = false;
	private boolean isButtonAreaTouched = false;
	private boolean machineGun = false;
	private boolean bulletPresent=false;
	private FixtureDef boxFixtureDef;

	private static String mapName = "tmx/map_1.tmx";
	private static String mapBG = "tmx/scn_sunny.png";
	private String fix1_name = "", fix2_name = "";

	boolean[] enemyLandedArr;

	private static float mImpulseY = 10f;
	private static float mLinearVelocityX = 8.0f;
	private static float mVolume = 1.0f;

	private float refrainImpulse = 5.0f;
	private float Player_Max_Health = 100.0f;
	private float Player_Health_Reduce = 10.0f;

	private static int mapOffset = 100;
	private static int mapID;
	private int mScore = 0;
	private int jumpDir = 0;
	private int timer = 30;

	public int enemyCount = 0;
	public int remainingEnemies;
	public int playerDir = 1;
	private int playerCount=0;
	// mEntityList stores a list of all entities on the scene
	public ArrayList<IEntity> mEntityList;
	

	private Rectangle rect;

	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private SurfaceScrollDetector mScrollDetector;

	private static AnimatedSprite player_self_sprite;
	private static Body player_self_body;
	
	private long totalTime;
	private CountDownTimer mCountDownTimer;
	
	
	/**********************************************************************************
	 * 
	 * Override onLoadEngine method of AndEngine
	 * Create the engine and camera, check multitouch support and set engine options here
	 * 
	 **********************************************************************************/
	@Override
	public Engine onLoadEngine() {

		this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 200, 200, 1.0f);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				mCamera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		engineOptions.setNeedsMusic(true).setNeedsSound(true);

		final Engine engine = new Engine(engineOptions);

		try {
			if (MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
				if (MultiTouch.isSupportedDistinct(this)) {

				} else {
					Toast.makeText(this, "(MultiTouch detected, but your device might have problems to distinguish between separate fingers.)",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)", Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)", Toast.LENGTH_LONG).show();
		}

		return engine;
	}


	/**********************************************************************************
	 * 
	 * Override onLoadResources method of AndEngine
	 * Load all textures, maps, sounds and fonts here
	 * 
	 **********************************************************************************/
	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		MusicFactory.setAssetBasePath("mfx/");
		SoundFactory.setAssetBasePath("mfx/");
		FontFactory.setAssetBasePath("font/");

		loadCharacters();
		loadControls();
		loadSounds();
		loadScore();
		loadMap();

	}
	/**********************************************************************************
	 *
	 * Override onLoadScene method of AndEngine
	 * Initialize everything here
	 * 
	 * Create a Scene and PhysicsWorld
	 * Attach all sprites, buttons and map to scene
	 * Start music
	 * Create and register game loop UpdateHandler 
	 * Create and register ContactListener
	 * Create and register scrollDetector and pinchZoomDetector
	 * Put all sprites into mEntityList  
	 * Set Camera bounds
	 * Start CountDownTimer
	 * 
	 **********************************************************************************/
	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());
		// createEnemyWalkTimeHandler();
		mScene = new Scene();

		mScene.setBackground(this.mRepeatingSpriteBackground);

		if (enableMusic) {
			if (mMusic.isPlaying()) {
				mMusic.pause();
			} else {
				mMusic.setVolume(mVolume);
				mMusic.play();
			}
		}
		final TMXLayer mTMXLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		mScene.attachChild(mTMXLayer);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		mScene.registerUpdateHandler(this.mPhysicsWorld);

		createCollisionListener();

		createGameUpdateHandler();

		initControls();
		mScene.setChildScene(this.mDigitalOnScreenControl);

		loadObjectsFromMap(mTMXTiledMap);

		mEntityList = new ArrayList<IEntity>(mScene.getChildCount());

		for (int i = 0; i < mScene.getChildCount(); i++)
			mEntityList.add(mScene.getChild(i));

		// initCharacters();
		spawnPlayer("player_self", mPlayerTextureRegion);

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mScrollDetector.setEnabled(false);

		createPinchZoomDetector();

		player_self_sprite = (AnimatedSprite) findShape("player_self");
		player_self_body = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_self_sprite);

		enemyLandedArr = new boolean[enemyCount];

		for (int i = 0; i < enemyCount; i++) {
			enemyLandedArr[i] = true;
		}
		remainingEnemies = enemyCount;
		mCamera.setBoundsEnabled(true);
		mCamera.setBounds(0, mTMXTiledMap.getTileColumns() * mTMXTiledMap.getTileWidth(), 0, mTMXTiledMap.getTileRows() * mTMXTiledMap.getTileHeight());
		mCamera.setChaseEntity(player_self_sprite);
		if (mapID == 0)
			totalTime = 120000;
		if (mapID == 1)
			totalTime = 60000;
		if (mapID == 2)
			totalTime = 240000;

		startCountDownTimer();
		return mScene;
	}
	
	@Override
	public void onLoadComplete() {

	}

	
	/**********************************************************************************
	 * 
	 * Create a ContactListner to detect collisions DO NOT create or destroy any
	 * Body inside ContactListener, use it ONLY to flip booleans For more info
	 * on ContactListener, please read Box2D documentation
	 * 
	 **********************************************************************************/
	private void createCollisionListener() {
		this.mPhysicsWorld.setContactListener(new ContactListener() {

			public void beginContact(Contact contact) {

				// get UserData of the two bodies between which collision is detected
				if (contact.getFixtureA().getBody().getUserData() != null && contact.getFixtureA().getBody().getUserData() != null) {

					fix1_name = contact.getFixtureA().getBody().getUserData().toString();
					fix2_name = contact.getFixtureB().getBody().getUserData().toString();

				} else {
					fix1_name = "";
					fix2_name = "";
				}

				// collision between player and wall 
				if ((fix1_name.contains("player_self") && fix2_name.contains("wall")) || (fix2_name.contains("player_self") && fix1_name.contains("wall"))) {
					isLanded = true;
				}	
				
				// collision between enemy and wall
				if ((fix1_name.contains("enemy") && fix2_name.contains("wall")) || (fix2_name.contains("enemy") && fix1_name.contains("wall"))) {
					enemyLanded = true;

				}

				// collision between bullet and wall
				if ((fix1_name.contains("bullet") && fix2_name.equalsIgnoreCase("wall"))
						|| (fix2_name.contains("bullet") && fix1_name.equalsIgnoreCase("wall"))) {

					desBull = true;
				}

				// collision between player and enemy
				if ((fix1_name.contains("player_self") && fix2_name.contains("enemy")) || (fix2_name.contains("player_self") && fix1_name.contains("enemy"))) {
					Debug.d("player hits enemy");
					reduceHealth = true;
					isLanded=true;
				} else
					reduceHealth = false;

				// collision between bullet fired by own player and enemy
				if ((fix1_name.contains("bullet_self") && fix2_name.contains("enemy"))

				|| (fix2_name.contains("bullet_self") && fix1_name.contains("enemy"))) {
					enemyShot = true;
				}

				// collision between bullet fired by any player and enemy
				if ((fix1_name.contains("bullet") && fix2_name.contains("enemy"))

				|| (fix2_name.contains("bullet") && fix1_name.contains("enemy"))) {
					// enemyShot = true;
					desEnemy = true;
					desBull = true;

				}
			}

			public void endContact(Contact contact) {

				//desEnemy = false;
				//desBull = false;
				//reduceHealth = false;
				// isLanded = false;

			}

			public void preSolve(Contact contact, Manifold oldManifold) {
				

			}

			public void postSolve(Contact contact, ContactImpulse impulse) {
				
				if (enemyLanded) {

					for (int i = 0; i < enemyCount; i++) {
						if (fix2_name.equals("enemy" + i)) {
							enemyLandedArr[i] = true;
							Debug.d("enemylanded at " + i + " true");
						}
					}

					enemyLanded = false;
				}

			}

		});

	}

	int test = 1, test2 = 0;

	boolean testboo;

	/**********************************************************************************
	 * 
	 * Create an IUpdateHandler for the main game loop. This is where all game
	 * logic is written
	 * 
	 **********************************************************************************/
	public void createGameUpdateHandler() {

		this.mScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {

				// ===========================THE GAME
				// LOOP===========================//

				makeBulletsDefyGravity();

				doAICalculations(player_self_body);

				mRemainingEnemiesChangeableText.setText(remainingEnemies + " Enemies Left");

				// desEnemy is true when collision is detected between bullet
				// and enemy
				if (desEnemy) {

					if (fix1_name.contains("enemy"))
						destroyGameObject(fix1_name);

					if (fix2_name.contains("enemy"))
						destroyGameObject(fix2_name);

					desEnemy = false;
				}

				// desBull is true when collision is detected between bullet and
				// wall or enemy
				if (desBull) {

					if (fix1_name.contains("bullet"))
						destroyGameObject(fix1_name);

					if (fix2_name.contains("bullet"))
						destroyGameObject(fix2_name);

					desBull = false;
				}

				// shoot rapidly with a machine gun effect if machineGun is true
				if (machineGun && test % 4 == 0) {
					test = 1;
					spawnBullet(player_self_sprite, playerDir, "bullet_self");
				} else if (machineGun && test % 4 != 0)
					test++;

				updateScore();

				updateHealthBar();

				// show game over screen if player dies
				if (Player_Max_Health <= 0) {
					endGame(SHOW_GAME_OVER_ACTIVITY);
				}

				// show level complete screen if all enemies are killed
				if (remainingEnemies == 0) {
					endGame(SHOW_LEVEL_COMPLETE_ACTIVITY);
				}

				if (!mScrollDetector.isEnabled()) {
					// mCamera.setCenter(player_self_sprite.getX(),
					// player_self_sprite.getY());
				}

			}

			@Override
			public void reset() {

			}

		});

	}


	// ===========================================================
	// Methods
	// ===========================================================

	private void loadObjectsFromMap(TMXTiledMap map) {

		// Loop through all the object groups in tmx map

		for (final TMXObjectGroup group : map.getTMXObjectGroups()) {

			if (group.getName().equals("wall")) {
				makeRectanglesFromObjects(group, "wall");
			}

			if (group.getName().equals("enemies")) {
				makeEnemiesFromObjects(group);
			}

		}

	}

	/**********************************************************************************
	 * 
	 * Create rectangle objects in tmx map along the walls and floors Read the
	 * rectangles from tmx map, make them invisible and attach them to the scene
	 * Create static bodies for each of the rectangles with boxFixtureDef and
	 * set UserData string to "wall"
	 * 
	 **********************************************************************************/
	private void makeRectanglesFromObjects(TMXObjectGroup _group, String _userData) {

		// loop through all objects in the group
		for (final TMXObject object : _group.getTMXObjects()) {

			// create rectangles for each object in tmx map
			rect = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight());

			// create Box FixtureDef
			boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f, false, CATEGORYBIT_WALL, MASKBITS_WALL, (short) 0);

			// create static body for each rectangle and set userData string to
			// "wall"+wallCount
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.StaticBody, boxFixtureDef).setUserData(_userData);

			// make rectangles invisible
			rect.setVisible(false);

			// attach rectangle to the scene
			mScene.attachChild(rect);
		}
	}

	/**********************************************************************************
	 * 
	 * Create small rectangle objects in tmx map to define enemy spawn points
	 * Use the locations of these rectangles to spawn enemies
	 * 
	 **********************************************************************************/
	private void makeEnemiesFromObjects(TMXObjectGroup _group) {

		// loop through all objects in the group
		for (final TMXObject object : _group.getTMXObjects()) {
			if (object.getX() > mapOffset + 320) {
				// use object locations to spawn enemies
				spawnEnemy(object.getX(), object.getY());
			}
		}
	}


	/**********************************************************************************
	 * 
	 * Create and start a CountDownTimer
	 * 
	 **********************************************************************************/
	private void startCountDownTimer() {
		mCountDownTimer = new CountDownTimer(totalTime, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				Debug.d("---------" + timer);
				// timer=(int)millisUntilFinished / 1000;
				mTimerChangeableText.setText("Time: " + millisUntilFinished / 1000);

				if (millisUntilFinished / 1000 < 10)
					mTimerChangeableText.setColor(255, 0, 0);
			}

			@Override
			public void onFinish() {
				endGame(SHOW_GAME_OVER_ACTIVITY);
			}
		}.start();

	}

	/**********************************************************************************
	 * 
	 * Create all on-screen controls and handle their behaviour
	 * 
	 **********************************************************************************/
	public void initControls() {

		mHUD = new HUD();

		// Create the Score text showing how many points the player scoredand
		// attach to HUD
		mScoreChangeableText = new ChangeableText(5, 5, mScoreFont, "Score: 0", "Score: XXXX".length());
		mScoreChangeableText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mScoreChangeableText.setAlpha(0.9f);
		mHUD.attachChild(mScoreChangeableText);

		// Create the Timer text showing the countdown timer and attach to HUD
		mTimerChangeableText = new ChangeableText(CAMERA_WIDTH - 180, 50, mScoreFont, "Time: x", "Time: xxx".length());
		mTimerChangeableText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mTimerChangeableText.setColor(0, 255, 0);
		mTimerChangeableText.setAlpha(0.9f);
		mHUD.attachChild(mTimerChangeableText);

		// Create the RemainingEnemies text showing remaining no. of enemies and
		// attach to HUD
		mRemainingEnemiesChangeableText = new ChangeableText(250, CAMERA_HEIGHT - 40, mScoreFont, "x Enemies Left", "xxxx Enemies Left".length());
		mRemainingEnemiesChangeableText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mRemainingEnemiesChangeableText.setAlpha(0.9f);
		mRemainingEnemiesChangeableText.setScale(0.8f);
		mHUD.attachChild(mRemainingEnemiesChangeableText);

		// Create the jump button and attach to HUD
		Sprite jump = new Sprite(CAMERA_WIDTH - 120, CAMERA_HEIGHT - 175, mJumpTextureRegion) {

			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {

				// when jump button is pressed and player is landed, make player
				// jump and zoom out camera
				if (pEvent.isActionDown() && isLanded) {

					jumpPlayer(player_self_body);

					mCamera.setZoomFactor(0.80f);

					isLanded = false;
					isButtonAreaTouched = true;
				}

				// when jump button is released, zoom in
				if (pEvent.isActionUp()) {

					mCamera.setZoomFactor(1.0f);

					isButtonAreaTouched = false;

				}

				return false;

			}

		};
		jump.setScale(0.70f);
		mHUD.registerTouchArea(jump);
		mHUD.attachChild(jump);

		// Create the shoot button and attach to HUD
		Sprite shoot = new Sprite(CAMERA_WIDTH - 200, CAMERA_HEIGHT - 110, mShootTextureRegion) {
			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {

				// when shoot button is pressed, spawn bullets in front of
				// player and zoom out
				if (pEvent.isActionDown()) {
					/*
					 * if (bulletPresent) { destroyGameObject();
					 * 
					 * }
					 */

					if (!machineGun)
						spawnBullet(player_self_sprite, playerDir, "bullet_self");

					// machineGun = true;

					mCamera.setZoomFactor(0.80f);
					isButtonAreaTouched = true;
				}

				// when shoot button is released zoom out
				if (pEvent.isActionUp()) {
					mCamera.setZoomFactor(1.0f);
					machineGun = false;
					isButtonAreaTouched = false;
				}
				return false;
			}
		};
		shoot.setScale(0.60f);
		mHUD.registerTouchArea(shoot);
		mHUD.attachChild(shoot);

		// Create health sprite and attach to HUD
		mHealthSprite = new AnimatedSprite(CAMERA_WIDTH - 256, 10, mHealthTextureRegion);
		mHealthSprite.setScale(0.5f);
		mHUD.attachChild(mHealthSprite);

		mCamera.setHUD(mHUD);

		// Create DigitalOnScreenControl to move player

		this.mDigitalOnScreenControl = new DigitalOnScreenControl(10, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight() - 5, this.mCamera,
				this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {

					@Override
					public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {

						// if controller is moved right and animation is not
						// running, move player to right and animate
						if (pValueX > 0 && !player_self_sprite.isAnimationRunning()) {
							movePlayerRight(player_self_sprite, player_self_body);
							isButtonAreaTouched = true;
						}

						// if controller is moved left and animation is not
						// running, move player to left and animate
						else if (pValueX < 0 && !player_self_sprite.isAnimationRunning()) {
							movePlayerLeft(player_self_sprite, player_self_body);
							isButtonAreaTouched = true;
						}

						// if controller is at centre, and player is moving,
						// stop player
						else if (pValueX == 0) {
							if (isPlayerMoving) {
								stopPlayer(player_self_sprite, player_self_body);
								isPlayerMoving = false;
								isButtonAreaTouched = false;
							}
						}
					}
				});

		this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mDigitalOnScreenControl.getControlBase().setAlpha(0.55f);
		this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		this.mDigitalOnScreenControl.getControlBase().setScale(1.5f);
		this.mDigitalOnScreenControl.getControlKnob().setAlpha(0.40f);
		this.mDigitalOnScreenControl.getControlKnob().setScale(0.7f);
		this.mDigitalOnScreenControl.refreshControlKnobPosition();
		this.mDigitalOnScreenControl.setAllowDiagonal(false);

	}

	/**********************************************************************************
	 * 
	 * Load TMX map and background image
	 * 
	 **********************************************************************************/

	private void loadMap() {
		this.mRepeatingSpriteBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(),
				new AssetBitmapTextureAtlasSource(this, mapBG), 1.0f);

		try {
			final TMXLoader mTMXLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, null);

			this.mTMXTiledMap = mTMXLoader.loadFromAsset(this, mapName);
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}
	}

	/**********************************************************************************
	 * 
	 * Load the fonts to be used for displaying score and other info on screen
	 * 
	 **********************************************************************************/

	private void loadScore() {
		/* Load the font we are going to use. */

		this.mScoreTextureAtlas = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mScoreFont = FontFactory.createFromAsset(this.mScoreTextureAtlas, this, "UnrealTournament.ttf", 32, true, Color.WHITE);

		this.mEngine.getTextureManager().loadTexture(this.mScoreTextureAtlas);
		this.mEngine.getFontManager().loadFont(this.mScoreFont);
	}

	/**********************************************************************************
	 * 
	 * Load the images for all character sprites
	 * 
	 **********************************************************************************/

	private void loadCharacters() {
		
		// create atlas of 512*512 
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		// load player,player_mp, enemy and bullet images onto atlas
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "andrun256.png", 0, 0, 8, 1);

		this.mPlayer_MPTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "andrun256_red.png", 0, 64,
				8, 1);

		this.mEnemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bananaHQ_tiled.png", 0, 128, 4,
				2);

		this.mBulletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bullet.png", 0, 256, 1, 1);

	}

	/**********************************************************************************
	 * 
	 * Load the images for all controls
	 * 
	 **********************************************************************************/

	private void loadControls() {

		// create atlas of 512*1024
		this.mHUDTextureAtlas = new BitmapTextureAtlas(512, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		//load button and healthbar images onto atlas
		this.mJumpTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHUDTextureAtlas, this, "jump.png", 0, 128);

		this.mShootTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mHUDTextureAtlas, this, "shoot.png", 128, 0);

		this.mHealthTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mHUDTextureAtlas, this, "health2.png", 0, 256, 1, 11);

		// create atlas of 256*128
		this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		// load direction controller images onto atlas
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this,
				"onscreen_control_base.png", 0, 0);

		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this,
				"onscreen_control_knob.png", 128, 0);

		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas, this.mOnScreenControlTexture, this.mHUDTextureAtlas);

	}

	/**********************************************************************************
	 * 
	 * Load all sounds to be used
	 * 
	 **********************************************************************************/

	private void loadSounds() {
		try {

			mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "mi.ogg");
			mMusic.setLooping(true);

			mWalkSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "fire_alarm4.wav");

			mShootSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "explosion.ogg");

		} catch (final IOException e) {
			Debug.e(e);
		}

	}

	/**********************************************************************************
	 * 
	 * Add optionsMenu that shows up when menu key is pressed\
	 * 
	 **********************************************************************************/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_RETURN, 0, R.string.menu_return);
		menu.add(0, MENU_EXIT, 0, R.string.menu_exit);

		Toast.makeText(this, "Options", 100).show();
		return true;

	}

	/**********************************************************************************
	 * 
	 * Defines what happens when particular item is selected from optionsMenu
	 * 
	 **********************************************************************************/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {

		case MENU_RETURN: {
			Intent StartIntent = new Intent(BotWars.this, StartMenu.class);
			startActivity(StartIntent);

			finish();
			Toast.makeText(this, "Main Menu", 100).show();
		}

		case MENU_EXIT: {
			Toast.makeText(this, "Exit", 100).show();
			System.exit(0);
		}

		}
		/*
		 * if (item.getItemId() == MENU_EXIT) {
		 * 
		 * Toast.makeText(this, "Exit", 100).show(); System.exit(0);
		 * 
		 * }
		 * 
		 * else if (item.getItemId() == MENU_RETURN) {// return to main menu
		 * 
		 * 
		 * Intent StartIntent = new Intent(BotWars.this, StartMenu.class);
		 * startActivity(StartIntent);
		 * 
		 * finish(); Toast.makeText(this, "Main Menu", 100).show();
		 * 
		 * }
		 */

		return true;
	}

	/**********************************************************************************
	 * 
	 * Method used by other classes to set value of jump impulse along y
	 * 
	 **********************************************************************************/

	public static void setImpulse(float impul) {
		mImpulseY = impul;
	}

	/**********************************************************************************
	 * 
	 * Method used by other classes to set value of velocity along X
	 * 
	 **********************************************************************************/

	public static void setVelocity(float str) {
		mLinearVelocityX = str; // Float.parseFloat(str);
	}

	/**********************************************************************************
	 * 
	 * Method used by other classes to set map and background image
	 * 
	 **********************************************************************************/

	public static void setMap(int _mapID) {
		mapID = _mapID;
		setScene(_mapID);

		if (_mapID == 0) {
			mapName = "tmx/map_1.tmx";
			mapOffset = 20;
		}

		if (_mapID == 1) {
			mapName = "tmx/map_2.tmx";
			mapOffset = 30;
		}

		if (_mapID == 2) {
			mapName = "tmx/map_3.tmx";
			mapOffset = 100;
		}
	}

	/**********************************************************************************
	 * 
	 * Method to set RepeatingSpriteBackground
	 * 
	 **********************************************************************************/

	public static void setScene(int sceneID) {
		if (sceneID == 0) {
			mapBG = "tmx/scn_sunny.png";
		}
		if (sceneID == 1) {
			mapBG = "tmx/scn_dark.png";
		}
		if (sceneID == 2) {
			mapBG = "tmx/scn_sunset.png";
		}
		if (sceneID == 3) {
			mapBG = "tmx/scn_underground.png";
		}
		if (sceneID == 4) {
			mapBG = "tmx/scn_sunset.png";
		}
	}

	/**********************************************************************************
	 * 
	 * Method to find Shape using UserData string (name)
	 * 
	 **********************************************************************************/

	public IShape findShape(String shapeName) {
		IShape pShape = null;

		// loop through all entities present and return the entity that has
		// given UserData string
		for (IEntity pEntity : mEntityList) {
			if (pEntity.getUserData() != null) {
				if (pEntity.getUserData().toString().equalsIgnoreCase(shapeName)) {
					pShape = (IShape) pEntity;
				}
			}
		}
		return pShape;
	}

	/**********************************************************************************
	 * 
	 * Method to destroy objects in the game safely using UserData string Detach
	 * sprite from scene-> destroy body associated with it-> unregister physics
	 * connector
	 * 
	 **********************************************************************************/

	public void destroyGameObject(String name) {

		// update remainingEnemies if the object being destroyed is an enemy
		if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(name)) != null) {
			if (name.contains("enemy")) {
				reduceRemainingEnemies();
				Debug.d("remaining enemies   " + remainingEnemies);
			}
		// remove sprite from screen and destroy body associated with it
			mScene.detachChild(findShape(name));
			mPhysicsWorld.destroyBody(mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(name)));
			mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(findShape(name)));
		}
	}

	/**********************************************************************************
	 * 
	 * Spawn bullet in front of given player and set UserData string (name) and play shoot sound
	 * 
	 **********************************************************************************/

	public void spawnBullet(AnimatedSprite _playerSprite, int _playerDir, String bulletName) {
		bulletCount++;
		
		AnimatedSprite mBulletSprite = new AnimatedSprite(_playerSprite.getX() + _playerDir, _playerSprite.getY() + _playerSprite.getHeight() / 4,
				mBulletTextureRegion);

		FixtureDef mBulletFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_BULLET, MASKBITS_BULLET, (short) 0);

		Body mBulletBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, mBulletSprite, BodyType.DynamicBody, mBulletFixtureDef);
		mBulletBody.setUserData(bulletName + bulletCount);

		mBulletBody.setBullet(true);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mBulletSprite, mBulletBody, true, true));
		mBulletSprite.setUserData(bulletName + bulletCount);
		mBulletBody.setLinearVelocity(_playerDir * 20, 0);

		mScene.attachChild(mBulletSprite);

		bulletPresent = true;
		mEntityList.add(mBulletSprite);
		mShootSound.play();
	}

	/**********************************************************************************
	 * 
	 * Spawn player with given UserData string and image
	 * 
	 **********************************************************************************/

	public void spawnPlayer(String playerName, TiledTextureRegion playerTexture) {

		playerCount++;
		AnimatedSprite mPlayerSprite = new AnimatedSprite(mapOffset, 0, playerTexture);

		final FixtureDef mPlayerFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_PLAYER, MASKBITS_PLAYER, (short) 0);

		Body mPlayerBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, mPlayerSprite, BodyType.DynamicBody, mPlayerFixtureDef);
		mPlayerSprite.setUserData(playerName);
		mPlayerBody.setUserData(playerName);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPlayerSprite, mPlayerBody, true, false));

		mScene.attachChild(mPlayerSprite);
		mEntityList.add(mPlayerSprite);
	}

	/**********************************************************************************
	 * 
	 * Spawn enemy at give x,y location and set UserData string to
	 * "enemy"+enemyCount
	 * 
	 **********************************************************************************/

	public void spawnEnemy(int xLoc, int yLoc) {

		AnimatedSprite mEnemySprite = new AnimatedSprite(xLoc, yLoc, mEnemyTextureRegion);
		mEnemySprite.setScale(0.7f);

		FixtureDef mEnemyFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_ENEMY, MASKBITS_ENEMY, (short) 0);

		Body mEnemyBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, mEnemySprite, BodyType.DynamicBody, mEnemyFixtureDef);
		mEnemyBody.setUserData("enemy" + enemyCount);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mEnemySprite, mEnemyBody, true, true));
		mEnemySprite.setUserData("enemy" + enemyCount);
		enemyCount++;

		mScene.attachChild(mEnemySprite);
		mEnemySprite.animate(100);

		mEnemySprite.setCullingEnabled(true);
	}


	/**********************************************************************************
	 * 
	 * Method used by other classes to enable/disable music
	 * 
	 **********************************************************************************/

	public static void enableMusic(boolean m) {
		enableMusic = m;
	}

	/**********************************************************************************
	 * 
	 * Method used by other classes to enable/disable sounds
	 * 
	 **********************************************************************************/

	public static void enableSounds(boolean s) {
		enableSounds = s;
	}

	/**********************************************************************************
	 * 
	 * Method used by other classes to set volume
	 * 
	 **********************************************************************************/

	public static void setMusicVolume(float vol) {
		mVolume = vol;
	}

	/**********************************************************************************
	 * 
	 * Define behaviour of hardware buttons
	 * 
	 **********************************************************************************/

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// if back button is pressed, end game and show start menu
		if (keyCode == KeyEvent.KEYCODE_BACK ){//&&  event.getRepeatCount() == 0) {
			
			endGame(SHOW_START_MENU_ACTIVITY);

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**********************************************************************************
	 * 
	 * Check if multitouch is supported and create pinch zoom detector
	 * 
	 **********************************************************************************/

	public void createPinchZoomDetector() {
		if (MultiTouch.isSupportedByAndroidVersion() && MultiTouch.isSupportedDistinct(this) && MultiTouch.isSupported(this)) {
			try {
				this.mPinchZoomDetector = new PinchZoomDetector(this);
				Debug.d("pincher");
			} catch (final MultiTouchException e) {
				this.mPinchZoomDetector = null;
			}
		} else {
			this.mPinchZoomDetector = null;
		}
		this.mScene.setOnSceneTouchListener(this);
		this.mScene.setTouchAreaBindingEnabled(true);

	}

	/**********************************************************************************
	 * 
	 * Define actions to perform when pinch is started
	 * 
	 **********************************************************************************/

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
		Debug.d("zoomstart");
	}

	/**********************************************************************************
	 * 
	 * Define actions to perform during pinch zooming (set camera zoom factor
	 * according to pinch)
	 * 
	 **********************************************************************************/

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		// if(!isButtonAreaTouched&&pTouchEvent.getX()<CAMERA_WIDTH-200-110&&pTouchEvent.getX()>CAMERA_WIDTH+200&&pTouchEvent.getY()<CAMERA_HEIGHT-100-185&&pTouchEvent.getY()>CAMERA_HEIGHT+200)
		if (!isButtonAreaTouched && pZoomFactor > 0.4f && mPinchZoomStartedCameraZoomFactor > 0.4f) {

			this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
			Debug.d("zoom factor" + pZoomFactor);
		}
	}

	/**********************************************************************************
	 * 
	 * Define actions to be performed after pinch is stopped (set camera zoom
	 * factor back to original zoom factor before pinch was started)
	 * 
	 **********************************************************************************/

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		
		if (!isButtonAreaTouched && pZoomFactor > 0.4f && mPinchZoomStartedCameraZoomFactor > 0.4f) {
			this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		}
	}

	/**********************************************************************************
	 * 
	 * Enable swipe scrolling when pinch zoom is not happening ///////////NOT
	 * USED///////
	 * 
	 **********************************************************************************/

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (this.mPinchZoomDetector != null) {
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

			if (this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if (pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);

				}

				if (pSceneTouchEvent.isActionUp()) {
					this.mScrollDetector.setEnabled(false);

				}

				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}
		return true;
	}

	/**********************************************************************************
	 * 
	 * Define behaviour of swipe scroll
	 * 
	 **********************************************************************************/

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mCamera.getZoomFactor();
		if (!isButtonAreaTouched) {
			this.mCamera.offsetCenter(-pDistanceX / zoomFactor * 10, -pDistanceY / zoomFactor * 10);
		}
	}

	/**********************************************************************************
	 * 
	 * Apply jump impulse on given body
	 * 
	 **********************************************************************************/

	public void jumpPlayer(Body _playerBody) {
		// playerDir = PLAYER_DIRECTION_RIGHT;

		_playerBody.applyLinearImpulse(0, -mImpulseY, _playerBody.getPosition().x, // /////JUMP
				_playerBody.getPosition().y);

	}

	/*
	 * public float getPlayerLocX() { return mPlayerBody.getPosition().x;
	 * 
	 * }
	 * 
	 * public float getPlayerLocY() { return mPlayerBody.getPosition().y;
	 * 
	 * }
	 * 
	 * public void setPlayerLoc(float x, float y) { mPlayerSprite.setPosition(x,
	 * y); }
	 */

	/*
	 * public void movePlayerRight() {
	 * 
	 * mPlayerSprite.getTextureRegion().setFlippedHorizontal(false);
	 * mPlayerBody.setLinearVelocity(mLinearVelocityX,
	 * mPlayerBody.getLinearVelocity().y);
	 * 
	 * mPlayerSprite.animate(new long[] { 50, 50, 50, 50, 50, 50, 50 }, 0, 6,
	 * false);
	 * 
	 * if (enableSounds) mWalkSound.play(); playerDir = PLAYER_DIRECTION_RIGHT;
	 * isPlayerMoving = true; }
	 */

	/**********************************************************************************
	 * 
	 * Move player with given sprite and body in right direction and play
	 * walking sound
	 * 
	 **********************************************************************************/

	public void movePlayerRight(AnimatedSprite _playerSprite, Body _playerBody) {

		// Body
		// pPlayerBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(playerName));
		// if(pPlayerBody==null)
		// Debug.d("NULLPOOPER");
		// pPlayerBody.setLinearVelocity(mLinearVelocityX,
		// pPlayerBody.getLinearVelocity().y);
		// final AnimatedSprite
		// pPlayerSprite=(AnimatedSprite)findShape(playerName);
		// final Body
		// pPlayerBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_self);
		// if(playerName.equalsIgnoreCase("player_self")){
		_playerSprite.getTextureRegion().setFlippedHorizontal(false);
		_playerBody.setLinearVelocity(mLinearVelocityX, _playerBody.getLinearVelocity().y);
		_playerSprite.animate(new long[] { 50, 50, 50, 50, 50, 50, 50 }, 0, 6, false);

		if (enableSounds)
			mWalkSound.play();
		if (_playerSprite.getUserData().toString().contains("player_self")) {
			playerDir = PLAYER_DIRECTION_RIGHT;
			isPlayerMoving = true;
		}
		// }

	}

	/**********************************************************************************
	 * 
	 * Move player with given sprite and body in left direction and play walking
	 * sound
	 * 
	 **********************************************************************************/

	public void movePlayerLeft(AnimatedSprite _playerSprite, Body _playerBody) {

		// final AnimatedSprite
		// pPlayerSprite=(AnimatedSprite)findShape(playerName);
		// final Body
		// pPlayerBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(pPlayerSprite);
		// if(playerName.equalsIgnoreCase("player_self")){
		_playerSprite.getTextureRegion().setFlippedHorizontal(true);
		_playerBody.setLinearVelocity(-mLinearVelocityX, _playerBody.getLinearVelocity().y);

		_playerSprite.animate(new long[] { 50, 50, 50, 50, 50, 50, 50 }, 0, 6, false);

		if (enableSounds)
			mWalkSound.play();
		if (_playerSprite.getUserData().toString().contains("player_self")) {
			playerDir = PLAYER_DIRECTION_LEFT;
			isPlayerMoving = true;
		}
		// }
	}

	/*
	 * public void movePlayerLeft() {
	 * mPlayerSprite.getTextureRegion().setFlippedHorizontal(true);
	 * mPlayerBody.setLinearVelocity(-mLinearVelocityX,
	 * mPlayerBody.getLinearVelocity().y); mPlayerSprite.animate(new long[] {
	 * 50, 50, 50, 50, 50, 50, 50 }, 0, 6, false);
	 * 
	 * if (enableSounds) mWalkSound.play(); playerDir = PLAYER_DIRECTION_LEFT;
	 * isPlayerMoving = true; }
	 */

	/**********************************************************************************
	 * 
	 * Stop player with given sprite and body
	 * 
	 **********************************************************************************/

	public void stopPlayer(AnimatedSprite _playerSprite, Body _playerBody) {
		// if (isPlayerMoving) {
		_playerBody.setLinearVelocity(0f, _playerBody.getLinearVelocity().y);
		// isPlayerMoving = false;}
	}

	/**********************************************************************************
	 * 
	 * Find distance between two bodies, player and enemy
	 * 
	 **********************************************************************************/

	public float getDistance(Body _player, Body _enemy) {
		float dist_x = (float) Math.pow(_player.getPosition().x - _enemy.getPosition().x, 2);

		float dist_y = (float) Math.pow(_player.getPosition().y - _enemy.getPosition().y, 2);

		// Debug.d(dist_x+" "+dist_y);
		return ((float) Math.sqrt(dist_x + dist_y));
	}

	/**********************************************************************************
	 * 
	 * Apply force on bullets to counter gravity and float, allowing to move
	 * horizontally
	 * 
	 **********************************************************************************/

	private void makeBulletsDefyGravity() {
		for (IEntity pEntity : mEntityList) {
			if (pEntity.getUserData() != null) {
				if (pEntity.getUserData().toString().contains("bullet")) {

					final Body pBulletBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape((IShape) pEntity);
					if (pBulletBody != null)
						pBulletBody.applyForce(new Vector2(0, -SensorManager.GRAVITY_EARTH), pBulletBody.getWorldCenter());

				}
			}
		}
	}

	/**********************************************************************************
	 * 
	 * Update the score display
	 * 
	 **********************************************************************************/

	public void updateScore() {
		if (enemyShot) {
			mScore += 10;
			mScoreChangeableText.setText("Score: " + mScore);
			enemyShot = false;
		}
	}

	/**********************************************************************************
	 * 
	 * Perform AI calculations on given body
	 * 
	 **********************************************************************************/

	public void doAICalculations(Body _playerBody) {
		for (int i = 0; i < enemyCount; i++) {
			IShape temp_enemy_shape = findShape("enemy" + i);
			Body temp_enemy_body = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(temp_enemy_shape);

			if (temp_enemy_body != null) {

				if (getDistance(_playerBody, temp_enemy_body) < 10.0f && enemyLandedArr[i]) {

					doAIActions(temp_enemy_body, playerDir, 0);

					enemyLandedArr[i] = false;
					// testboo=false;
					// Debug.d("enemylanded at " + i + " true");

				}

				else if (enemyLandedArr[i]) {
					temp_enemy_body.setLinearVelocity(0, 0);
				}

			}

		}
	}

	/**********************************************************************************
	 * 
	 * Perform AI actions on given body based on given direction of player and
	 * action to perform
	 * 
	 **********************************************************************************/

	public void doAIActions(Body temp_enemy_body, int _playerDir, int action) {
		temp_enemy_body.applyLinearImpulse(0, -5.0f, temp_enemy_body.getPosition().x, temp_enemy_body.getPosition().y);
		if (_playerDir == PLAYER_DIRECTION_RIGHT)
			temp_enemy_body.setLinearVelocity(-mLinearVelocityX, temp_enemy_body.getLinearVelocity().y);

		if (_playerDir == PLAYER_DIRECTION_LEFT)
			temp_enemy_body.setLinearVelocity(mLinearVelocityX, temp_enemy_body.getLinearVelocity().y);

	}

	/**********************************************************************************
	 * 
	 * Update health bar
	 * 
	 **********************************************************************************/

	private void updateHealthBar() {
		// reduce health by value of Player_Health_Reduce, reduce health bar by
		// 1/5 portions every 20 health points and make player bounce off enemy

		if (reduceHealth && mHealthSprite != null && player_self_body != null) {

			player_self_body.applyLinearImpulse((float) (refrainImpulse * 1.5), -refrainImpulse, player_self_body.getPosition().x,
					player_self_body.getPosition().y);

			Player_Max_Health -= Player_Health_Reduce;

			switch ((int) Player_Max_Health) {
			case 100:
				break;
			case 90:
				mHealthSprite.animate(new long[] { 50, 50 }, 0, 1, false);
				break;
			case 80:
				mHealthSprite.animate(new long[] { 50, 50 }, 1, 2, false);
				break;
			case 70:
				mHealthSprite.animate(new long[] { 50, 50 }, 2, 3, false);
				break;
			case 60:
				mHealthSprite.animate(new long[] { 50, 50 }, 3, 4, false);
				break;
			case 50:
				mHealthSprite.animate(new long[] { 50, 50 }, 4, 5, false);
				break;
			case 40:
				mHealthSprite.animate(new long[] { 50, 50 }, 5, 6, false);
				break;
			case 30:
				mHealthSprite.animate(new long[] { 50, 50 }, 6, 7, false);
				break;
			case 20:
				mHealthSprite.animate(new long[] { 50, 50 }, 7, 8, false);
				break;
			case 10:
				mHealthSprite.animate(new long[] { 50, 50 }, 8, 9, false);
				break;
			case 0:
				mHealthSprite.animate(new long[] { 50, 50 }, 9, 10, false);
				break;

			default:
				break;

			}

			// kill player if health becomes 0

			reduceHealth = false;
		}
	}

	/**********************************************************************************
	 * 
	 * Method to decrement remainingEnemies
	 * 
	 **********************************************************************************/
	public void reduceRemainingEnemies() {
		remainingEnemies--;
	}

	/**********************************************************************************
	 * 
	 * Method to end game and start next activity
	 * 
	 **********************************************************************************/
	public void endGame(int action) {
		mCountDownTimer.cancel();
		// mPhysicsWorld.destroyBody(player_self_body);
		// mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(player_self_sprite));
		// mScene.detachChild(player_self_sprite);

		if (action == SHOW_START_MENU_ACTIVITY) {
			Intent StartIntent = new Intent(BotWars.this, StartMenu.class);
			finish();
			startActivity(StartIntent);
		}
		if (action == SHOW_GAME_OVER_ACTIVITY) {
			Intent StartIntent = new Intent(BotWars.this, GameOver.class);
			finish();
			startActivity(StartIntent);
		}
		if (action == SHOW_LEVEL_COMPLETE_ACTIVITY) {
			Intent StartIntent = new Intent(BotWars.this, LevelComplete.class);
			finish();
			startActivity(StartIntent);
		}

	}
}