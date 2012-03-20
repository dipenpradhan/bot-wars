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
import org.anddev.andengine.entity.primitive.Line;
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
import android.graphics.Rect;
import android.hardware.SensorManager;
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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga
 * 
 * @author Nicolas Gramlich
 * @since 00:06:23 - 11.07.2010
 */
public class BotWars extends BaseGameActivity implements IPinchZoomDetectorListener, IOnSceneTouchListener, IScrollDetectorListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800; // Camera Parameters
	private static final int CAMERA_HEIGHT = 480;

	public static final int PLAYER_DIRECTION_RIGHT = 1;
	public static final int PLAYER_DIRECTION_LEFT = -1;

	private static final short CATEGORYBIT_WALL = 1; // Body Categories
	private static final short CATEGORYBIT_PLAYER = 2;
	private static final short CATEGORYBIT_ENEMY = 4;
	private static final short CATEGORYBIT_BULLET = 8;

	// Masks
	// Put category bits of bodies which are supposed to collide in respective
	// mask bits
	private static final short MASKBITS_WALL = CATEGORYBIT_WALL + CATEGORYBIT_PLAYER + CATEGORYBIT_ENEMY + CATEGORYBIT_BULLET;
	private static final short MASKBITS_PLAYER = CATEGORYBIT_WALL + CATEGORYBIT_PLAYER + CATEGORYBIT_ENEMY;
	private static final short MASKBITS_ENEMY = CATEGORYBIT_WALL + CATEGORYBIT_BULLET + CATEGORYBIT_ENEMY + CATEGORYBIT_PLAYER;
	private static final short MASKBITS_BULLET = CATEGORYBIT_WALL + CATEGORYBIT_ENEMY;// +
																						// CATEGORYBIT_BULLET;
	// ===========================================================
	// Fields
	// ===========================================================
	private int bulletCount = 0;
	private SmoothCamera mCamera;

	private BitmapTextureAtlas mBitmapTextureAtlas; // atlas for player and
													// enemy textures
	private TiledTextureRegion mPlayerTextureRegion;
	public TiledTextureRegion mPlayer_MPTextureRegion;
	private TiledTextureRegion mEnemyTextureRegion;

	private BitmapTextureAtlas mBulletTextureAtlas;
	private TiledTextureRegion mBulletTextureRegion;

	private BitmapTextureAtlas mOnScreenControlTexture; // atlas for
														// onScreenControl
														// textures
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;
	public DigitalOnScreenControl mDigitalOnScreenControl;

	private BitmapTextureAtlas mHUDTextureAtlas; // atlas for HUD textures
	private TextureRegion mJumpTextureRegion;
	private TextureRegion mShootTextureRegion;
	private TiledTextureRegion mHealthRegion;

	private AnimatedSprite mEnemySprite;

	private TMXTiledMap mTMXTiledMap;
	private Music mMusic;
	private Sound mWalkSound,mShootSound;
	private boolean isLanded = false;
	boolean enemyLanded = false;
	private Body mPlayerBody;
	private RepeatingSpriteBackground mRepeatingSpriteBackground;
	public Scene mScene;
	public PhysicsWorld mPhysicsWorld;
	private FixtureDef boxFixtureDef;
	private AnimatedSprite mPlayerSprite;
	private AnimatedSprite mBulletSprite;

	private static float mImpulseY = 14f;
	private static float mLinearVelocityX = 8.0f;

	private static String mapName = "tmx/map_1.tmx";
	private static String mapBG = "tmx/scn_sunny.png";
	private String fix1_name = "", fix2_name = "";
	private static int mapOffset = 100;
	private boolean deleteEnemy = false;
	private boolean deleteBullet = false;
	public int playerDir = 1;
	private boolean desBull = false;
	private boolean desEnemy = false;
	private boolean bulletPresent = false;
	private boolean reduceHealth = false;
	private boolean enemyShot = false;
	private boolean isButtonAreaTouched=false;
	
	private float refrainImpulse = 5.0f;

	public float Player_Max_Health = 100.0f;

	public float Player_Health_Reduce = 5.0f;

	private AnimatedSprite mHealthSprite;
	private ContactListener collisionListener;
	private IUpdateHandler gameUpdater;
	private static boolean enableMusic;
	private static boolean enableSounds;
	private static float mVolume = 1.0f;
	private int mScore = 0;

	private int jumpDir = 0;

	public ArrayList<IEntity> mEntityList;

	private Rectangle rect;
	private int enemyCount = 0;

	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private SurfaceScrollDetector mScrollDetector;

	private static AnimatedSprite player_self_sprite;

	private static Body player_self_body;

	private ChangeableText mScoreChangeableText;
	private Font mScoreFont;
	private BitmapTextureAtlas mScoreTextureAtlas;

	private int[][] wallYLine;

	// private Body collBody1,collBody2;
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public Engine onLoadEngine() {

		this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 200, 200, 1.0f);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		engineOptions.setNeedsMusic(true).setNeedsSound(true);

		final Engine engine = new Engine(engineOptions);

		try {
			if (MultiTouch.isSupported(this)) {
				engine.setTouchController(new MultiTouchController());
				if (MultiTouch.isSupportedDistinct(this)) {

				} else {
					Toast.makeText(this, "(MultiTouch detected, but your device might have problems to distinguish between separate fingers.)", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)", Toast.LENGTH_LONG).show();
			}
		} catch (final MultiTouchException e) {
			Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)", Toast.LENGTH_LONG).show();
		}

		return engine;
	}

	/*
	 * @Override public Engine onLoadEngine() {
	 * 
	 * this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 200,
	 * 200, 1.0f);
	 * 
	 * final EngineOptions engineOptions = new EngineOptions(true,
	 * ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH,
	 * CAMERA_HEIGHT), mCamera);
	 * engineOptions.getTouchOptions().setRunOnUpdateThread(true);
	 * engineOptions.setNeedsMusic(true).setNeedsSound(true);
	 * 
	 * 
	 * return new Engine(engineOptions);
	 * 
	 * }
	 */

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

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		mScene.registerUpdateHandler(this.mPhysicsWorld);

		createCollisionListener();
		this.mPhysicsWorld.setContactListener(collisionListener);

		createGameUpdateHandler();
		mScene.registerUpdateHandler(gameUpdater);

		initControls();
		mScene.attachChild(mTMXLayer);
		// mScene.attachChild(mEnemySprite);

		// mScene.attachChild(mPlayerSprite);
		createUnwalkableObjects(mTMXTiledMap);

		mScene.setChildScene(this.mDigitalOnScreenControl);

		mEntityList = new ArrayList<IEntity>(mScene.getChildCount());

		for (int i = 0; i < mScene.getChildCount(); i++)
			mEntityList.add(mScene.getChild(i));
		initCharacters();
		this.mScrollDetector = new SurfaceScrollDetector(this);

		this.mScrollDetector.setEnabled(false);
		createPinchZoomDetector();
		player_self_sprite = (AnimatedSprite) findShape("player_self");
		player_self_body = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player_self_sprite);

		enemyLandedArr = new boolean[enemyCount];

		//Line l = new Line(0, 0, 100, 0);
		//l.setUserData("line");

		//mScene.attachChild(l);
		//mEntityList.add(l);
		return mScene;
	}

	private void createCollisionListener() {
		collisionListener = new ContactListener() {

			public void beginContact(Contact contact) {

				Fixture fix1 = contact.getFixtureA();
				Fixture fix2 = contact.getFixtureB();
				Body collBody1 = fix1.getBody();
				Body collBody2 = fix2.getBody();

				final Body pBulletBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(mBulletSprite);
				final Body pEnemyBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(mEnemySprite);
				final Body pWallBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(rect);

				if (fix1.getBody().getUserData() != null && fix2.getBody().getUserData() != null) {

					fix1_name = fix1.getBody().getUserData().toString();
					fix2_name = fix2.getBody().getUserData().toString();

				} else {
					fix1_name = "";
					fix2_name = "";
				}

				if ((fix1_name.contains("player_self") && fix2_name.contains("wall")) || (fix2_name.contains("player_self") && fix1_name.contains("wall"))) {
					isLanded = true;
				}

				if ((fix1_name.contains("player_self") && fix2_name.contains("enemy")) || (fix2_name.contains("player_self") && fix1_name.contains("enemy")))
					isLanded = true;

				if ((fix1_name.contains("enemy") && fix2_name.contains("wall")) || (fix2_name.contains("enemy") && fix1_name.contains("wall"))) {
					enemyLanded = true;

				}
				// Debug.d("BeginContact");

				if (mBulletSprite != null && mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mBulletSprite) != null && pBulletBody != null) {
					if ((fix1_name.contains("bullet") && fix2_name.equalsIgnoreCase("wall")) || (fix2_name.contains("bullet") && fix1_name.equalsIgnoreCase("wall"))) {

						desBull = true;

					}

				}

				if ((fix1_name.contains("player_self") && fix2_name.contains("enemy")) || (fix2_name.contains("player_self") && fix1_name.contains("enemy"))) {
					Debug.d("player hits enemy");
					reduceHealth = true;
				} else
					reduceHealth = false;

				if (mBulletSprite != null && mEnemySprite != null) {
					// && pEnemyBody!=null) {
					if ((fix1_name.contains("bullet") && fix2_name.contains("enemy"))

					|| (fix2_name.contains("bullet") && fix1_name.contains("enemy"))) {
						enemyShot = true;
						desEnemy = true;
						desBull = true;

					}

					// if(collBody1==pEnemyBody||collBody2==pEnemyBody)Debug.d("enemy body hit");
					// if(fix1_name=="enemy"||fix2_name=="enemy")Debug.d("enemy hit hit hit");

				}
			}

			public void endContact(Contact contact) {

				desEnemy = false;
				desBull = false;
				reduceHealth = false;
				// isLanded = false;

			}

			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub
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

		};

	}

	int test = 1, test2 = 0;
	boolean[] enemyLandedArr;
	boolean testboo;

	public void createGameUpdateHandler() {

		gameUpdater = new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {

				// make bullets defy gravity
				for (IEntity pEntity : mEntityList) {
					if (pEntity.getUserData() != null) {
						if (pEntity.getUserData().toString().contains("bullet")) {

							final Body pBulletBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape((IShape) pEntity);
							if (pBulletBody != null)
								pBulletBody.applyForce(new Vector2(0, -SensorManager.GRAVITY_EARTH), pBulletBody.getWorldCenter());

						}
					}
				}
				if (enemyShot) {
					mScore += 10;
					mScoreChangeableText.setText("Score: " + mScore);
					enemyShot = false;
				}
				// jump enemy if enemy is landed and within 5.0m
				for (int i = 0; i < enemyCount; i++) {
					IShape temp_enemy_shape = findShape("enemy" + i);
					Body temp_enemy_body = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(temp_enemy_shape);

					if (temp_enemy_body != null) {

						if (getDistance(player_self_body, temp_enemy_body) < 10.0f && enemyLandedArr[i]) {
							
							
							
							temp_enemy_body.applyLinearImpulse(0, -5.0f, temp_enemy_body.getPosition().x, temp_enemy_body.getPosition().y);
							if (playerDir == PLAYER_DIRECTION_RIGHT)
								temp_enemy_body.setLinearVelocity(-mLinearVelocityX, temp_enemy_body.getLinearVelocity().y);

							if (playerDir == PLAYER_DIRECTION_LEFT)
								temp_enemy_body.setLinearVelocity(mLinearVelocityX, temp_enemy_body.getLinearVelocity().y);

							enemyLandedArr[i] = false;
							// testboo=false;
							Debug.d("enemylanded at " + i + " true");

							/*
							 * for(int j=0;j<wallCount;j++) { for(int
							 * k=wallYLine[0][i];k<wallYLine[1][i];k++) {
							 * for(int l=(int)player_self_sprite.getX();l<(int)
							 * temp_enemy_shape.getX();l++) {
							 * 
							 * 
							 * if(k==l){ testboo=true; }
							 * 
							 * } for(int l=(int)temp_enemy_shape.getX();l<(int)
							 * player_self_sprite.getX();l++) {
							 * 
							 * 
							 * if(k==l){ testboo=true; }
							 * 
							 * } } }
							 */
							/*
							 * findShape("line").setPosition(temp_enemy_shape.getX
							 * (), temp_enemy_shape.getY());
							 * 
							 * Debug.d("wall count"+wallCount);
							 * 
							 * 
							 * for(int j=0;j<wallCount;j++) {
							 * Debug.d("checking  "
							 * +findShape("wallLine"+j).getUserData
							 * ().toString());
							 * if(findShape("wallLine"+j).collidesWith
							 * (findShape("line"))){
							 * 
							 * testboo=true;
							 * Debug.d("WALL "+j+" DETECTED by"+temp_enemy_shape
							 * .getUserData().toString()); }
							 * 
							 * }
							 */

						}

						else if (enemyLandedArr[i]) {
							temp_enemy_body.setLinearVelocity(0, 0);
						}

					}

				}

				// destroy enemy

				if (desEnemy) {
					if (fix1_name.contains("enemy"))
						destroyEnemy(fix1_name);
					if (fix2_name.contains("enemy"))
						destroyEnemy(fix2_name);
					desEnemy = false;
				}

				// destroy bullet

				if (desBull) {

					if (fix1_name.contains("bullet"))
						destroyBullet(fix1_name);
					if (fix2_name.contains("bullet"))
						destroyBullet(fix2_name);
					desBull = false;
				}

				// reduce health by value of Player_Health_Reduce,
				// reduce health bar by 1/5 portions every 20 health points
				// and make player bounce off enemy

				if (machineGun && test % 4 == 0) {
					test = 1;
					spawnBullet(player_self_sprite, playerDir);
				} else if (machineGun && test % 4 != 0)
					test++;

				if (reduceHealth && mHealthSprite != null && mPlayerSprite != null) {

					player_self_body.applyLinearImpulse((float) (refrainImpulse * 1.5), -refrainImpulse, player_self_body.getPosition().x, player_self_body.getPosition().y);

					Player_Max_Health -= Player_Health_Reduce;

					switch ((int) Player_Max_Health) {
					case 100:
						break;
					case 80:
						mHealthSprite.animate(new long[] { 50, 50 }, 0, 1, false);
						break;
					case 60:
						mHealthSprite.animate(new long[] { 50, 50 }, 1, 2, false);
						break;
					case 40:
						mHealthSprite.animate(new long[] { 50, 50 }, 2, 3, false);
						break;
					case 20:
						mHealthSprite.animate(new long[] { 50, 50 }, 3, 4, false);
						break;
					default:
						break;

					}

					// kill player if health becomes 0
					if (Player_Max_Health <= 0) {

						mPhysicsWorld.destroyBody(player_self_body);
						mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(player_self_sprite));
						mScene.detachChild(player_self_sprite);

						Intent StartIntent = new Intent(BotWars.this, StartMenu.class);
						startActivity(StartIntent);

						finish();

					}
					reduceHealth = false;
				}

				if (!mScrollDetector.isEnabled())
					mCamera.setCenter(player_self_sprite.getX(), player_self_sprite.getY());
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

		};

	}

	String enemyName = "";
	private boolean machineGun = false;

	@Override
	public void onLoadComplete() {

	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void createUnwalkableObjects(TMXTiledMap map) {
		// Loop through the object groups

		for (final TMXObjectGroup group : map.getTMXObjectGroups()) {

			if (group.getName().equals("wall")) {
				makeRectanglesFromObjects(group, "wall");
			}
		}

	}

	int wallCount = 0;

	private void makeRectanglesFromObjects(TMXObjectGroup _group, String _userData) {

		for (final TMXObject object : _group.getTMXObjects()) {

			/*
			 * Line mLine=new
			 * Line(object.getX(),object.getY(),object.getX(),object
			 * .getY()+object.getHeight());
			 * mLine.setUserData("wallLine"+wallCount);
			 * 
			 * mScene.attachChild(mLine); wallCount++;
			 */
			rect = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight());
			boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f, false, CATEGORYBIT_WALL, MASKBITS_WALL, (short) 0);

			PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.StaticBody, boxFixtureDef).setUserData(_userData);
			// rect.setUserData(_userData+wallCount);

			rect.setVisible(false);
			mScene.attachChild(rect);
		}
	}

	public void initControls() {

		HUD mHUD = new HUD();

		/* The ScoreText showing how many points the player scored. */
		mScoreChangeableText = new ChangeableText(5, 5, mScoreFont, "Score: 0", "Score: XXXX".length());
		mScoreChangeableText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mScoreChangeableText.setAlpha(0.5f);
		mHUD.attachChild(mScoreChangeableText);

		Sprite jump = new Sprite(CAMERA_WIDTH - 120, CAMERA_HEIGHT - 175, mJumpTextureRegion) {

			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {
				if (pEvent.isActionDown() && isLanded) {
					// mPlayerBody.applyLinearImpulse(0, -mImpulseY,
					// mPlayerBody.getPosition().x,
					// mPlayerBody.getPosition().y);
					jumpPlayer(player_self_body);
					mCamera.setZoomFactor(0.80f);
					isLanded = false;
					isButtonAreaTouched=true;
				}
				if (pEvent.isActionUp())
					mCamera.setZoomFactor(1.0f);
					isButtonAreaTouched=false;
				return false;

			}

		};
		jump.setScale(0.70f);
		Sprite shoot = new Sprite(CAMERA_WIDTH - 200, CAMERA_HEIGHT - 100, mShootTextureRegion) {
			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {

				if (pEvent.isActionDown()) {
					/*
					 * if (bulletPresent) { destroyBullet();
					 * 
					 * }
					 */
					// spawnBullet(player_self_sprite, playerDir);
					machineGun = true;
					mCamera.setZoomFactor(0.80f);
					isButtonAreaTouched=true;
				}
				if (pEvent.isActionUp()) {
					mCamera.setZoomFactor(1.0f);
					machineGun = false;
					isButtonAreaTouched=false;
				}
				return false;

			}

		};
		shoot.setScale(0.60f);
		mHealthSprite = new AnimatedSprite(CAMERA_WIDTH - 256, 10, mHealthRegion);
		mHealthSprite.setScale(0.5f);

		mHUD.registerTouchArea(shoot);
		mHUD.attachChild(shoot);

		mHUD.registerTouchArea(jump);
		mHUD.attachChild(jump);
		mHUD.attachChild(mHealthSprite);
		mCamera.setHUD(mHUD);

		this.mDigitalOnScreenControl = new DigitalOnScreenControl(10, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight()-5, this.mCamera,
				this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f,

				new IOnScreenControlListener() {
					@Override
					public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
						
						
						if (pValueX > 0 && !player_self_sprite.isAnimationRunning()) {
							movePlayerRight(player_self_sprite, player_self_body);
							isButtonAreaTouched=true;
						}

						else if (pValueX < 0 && !player_self_sprite.isAnimationRunning()) {
							movePlayerLeft(player_self_sprite, player_self_body);
							isButtonAreaTouched=true;
						} 
						else if (pValueX == 0) {
							if (isPlayerMoving) {
								stopPlayer(player_self_sprite, player_self_body);
								isPlayerMoving = false;
								isButtonAreaTouched=false;
							}

						}

					}

				});

		this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mDigitalOnScreenControl.getControlBase().setAlpha(0.9f);
		this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		this.mDigitalOnScreenControl.getControlBase().setScale(1.50f);
		this.mDigitalOnScreenControl.getControlKnob().setScale(1.50f);
		this.mDigitalOnScreenControl.getControlKnob().setAlpha(0.4f);
		this.mDigitalOnScreenControl.refreshControlKnobPosition();
		this.mDigitalOnScreenControl.setAllowDiagonal(false);

	}

	private void loadMap() {
		this.mRepeatingSpriteBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this,
				mapBG), 1.0f);
		try {
			final TMXLoader mTMXLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, null);

			this.mTMXTiledMap = mTMXLoader.loadFromAsset(this, mapName);

		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}

	}

	private void loadScore() {
		/* Load the font we are going to use. */

		this.mScoreTextureAtlas = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mScoreFont = FontFactory.createFromAsset(this.mScoreTextureAtlas, this, "UnrealTournament.ttf", 32, true, Color.WHITE);

		this.mEngine.getTextureManager().loadTexture(this.mScoreTextureAtlas);
		this.mEngine.getFontManager().loadFont(this.mScoreFont);
	}

	private void loadCharacters() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "andrun256.png", 0, 0, 8, 1);
		this.mPlayer_MPTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "andrun256_red.png", 0, 64, 8, 1);
		this.mEnemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bananaHQ_tiled.png", 0, 128, 4, 2);

		this.mBulletTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bullet.png", 0, 256, 1, 1);

	}

	private void loadControls() {

		this.mHUDTextureAtlas = new BitmapTextureAtlas(256, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mJumpTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHUDTextureAtlas, this, "jump.png", 0, 128);

		this.mShootTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mHUDTextureAtlas, this, "shoot.png", 128, 0);

		this.mHealthRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mHUDTextureAtlas, this, "health_bar.png", 0, 256, 1, 8);

		this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);

		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas, this.mOnScreenControlTexture, this.mHUDTextureAtlas);

	}

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		menu.add(0, 2, 0, R.string.menu_return);
		menu.add(0, 3, 0, R.string.menu_exit);

		Toast.makeText(this, "Options", 100).show();
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		{

			if (item.getItemId() == 3) {// exit

				Toast.makeText(this, "Exit", 100).show();
				System.exit(0);

			}

			else if (item.getItemId() == 2) {// return to main menu
				// onDestroy();

				Intent StartIntent = new Intent(BotWars.this, StartMenu.class);
				startActivity(StartIntent);

				finish();
				Toast.makeText(this, "Main Menu", 100).show();

			}

		}
		return true;
	}

	public static void setImpulse(float impul) {
		mImpulseY = impul;
	}

	public static void setVelocity(float str) {
		mLinearVelocityX = str; // Float.parseFloat(str);
	}

	public static void setMap(int mapID) {

		setScene(mapID);
		if (mapID == 0) {
			mapName = "tmx/map_1.tmx";
			mapOffset = 20;
		}
		if (mapID == 1) {
			mapName = "tmx/map_2.tmx";
			mapOffset = 20;
		}
		if (mapID == 2) {
			mapName = "tmx/map_3.tmx";
			mapOffset = 100;
		}
	}

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

	private void destroyEnemy(String enemyName) {

		/*
		 * for (IEntity pEntity : mEntityList) { if (pEntity.getUserData() !=
		 * null) { if
		 * (pEntity.getUserData().toString().equalsIgnoreCase(bodyName)){
		 * mScene.detachChild(pEntity);
		 * mPhysicsWorld.destroyBody(mPhysicsWorld.getPhysicsConnectorManager
		 * ().findBodyByShape((IShape) pEntity));
		 * mPhysicsWorld.unregisterPhysicsConnector
		 * (mPhysicsWorld.getPhysicsConnectorManager
		 * ().findPhysicsConnectorByShape((IShape) pEntity)); } } }
		 */

		mScene.detachChild(findShape(enemyName));
		mPhysicsWorld.destroyBody(mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(enemyName)));
		mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(findShape(enemyName)));

	}

	public void destroyBullet(String bulletName) {
		/*
		 * for (IEntity pEntity : mEntityList) { if (pEntity.getUserData() !=
		 * null) { if
		 * (pEntity.getUserData().toString().equalsIgnoreCase(bodyName)){
		 * mScene.detachChild(pEntity);
		 * 
		 * mPhysicsWorld.destroyBody(mPhysicsWorld.getPhysicsConnectorManager().
		 * findBodyByShape((IShape) pEntity));
		 * mPhysicsWorld.unregisterPhysicsConnector
		 * (mPhysicsWorld.getPhysicsConnectorManager
		 * ().findPhysicsConnectorByShape((IShape) pEntity));
		 * 
		 * } } }
		 */
		mScene.detachChild(findShape(bulletName));
		mPhysicsWorld.destroyBody(mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(findShape(bulletName)));
		mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(findShape(bulletName)));
	}

	public IShape findShape(String shapeName) {
		IShape pShape = null;
		for (IEntity pEntity : mEntityList) {
			if (pEntity.getUserData() != null) {
				if (pEntity.getUserData().toString().equalsIgnoreCase(shapeName)) {
					pShape = (IShape) pEntity;
				}
			}
		}
		return pShape;
	}

	public void spawnBullet(AnimatedSprite _playerSprite, int _playerDir) {
		bulletCount++;
		mBulletSprite = new AnimatedSprite(_playerSprite.getX() + _playerDir, _playerSprite.getY() + _playerSprite.getHeight() / 4, mBulletTextureRegion);

		FixtureDef mBulletFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_BULLET, MASKBITS_BULLET, (short) 0);

		Body mBulletBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, mBulletSprite, BodyType.DynamicBody, mBulletFixtureDef);
		mBulletBody.setUserData("bullet" + bulletCount);

		mBulletBody.setBullet(true);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mBulletSprite, mBulletBody, true, true));
		mBulletSprite.setUserData("bullet" + bulletCount);
		mBulletBody.setLinearVelocity(_playerDir * 20, 0);

		mScene.attachChild(mBulletSprite);

		bulletPresent = true;
		mEntityList.add(mBulletSprite);
		mShootSound.play();
	}

	private int playerCount = 0;

	public void spawnPlayer(String playerName, TiledTextureRegion playerTexture) {

		playerCount++;
		mPlayerSprite = new AnimatedSprite(mapOffset, 0, playerTexture);

		final FixtureDef mPlayerFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_PLAYER, MASKBITS_PLAYER, (short) 0);

		mPlayerBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, mPlayerSprite, BodyType.DynamicBody, mPlayerFixtureDef);
		mPlayerSprite.setUserData(playerName);
		mPlayerBody.setUserData(playerName);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPlayerSprite, mPlayerBody, true, false));

		mScene.attachChild(mPlayerSprite);
		mEntityList.add(mPlayerSprite);
	}

	public void spawnEnemy(int xLoc) {

		mEnemySprite = new AnimatedSprite(mapOffset + 150 * xLoc, 0 + enemyCount * 10, mEnemyTextureRegion);
		mEnemySprite.setScale(0.7f);
		FixtureDef mEnemyFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_ENEMY, MASKBITS_ENEMY, (short) 0);

		Body mEnemyBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, mEnemySprite, BodyType.DynamicBody, mEnemyFixtureDef);
		mEnemyBody.setUserData("enemy" + enemyCount);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mEnemySprite, mEnemyBody, true, true));
		mEnemySprite.setUserData("enemy" + enemyCount);
		enemyCount++;
		mScene.attachChild(mEnemySprite);
		mEnemySprite.animate(100);
		mEntityList.add(mEnemySprite);
	}

	private void initCharacters() {
		for (int i = 1; i <= 20; i++) {
			spawnEnemy(i);
		}
		// for(int i=1;i<=10;i++)
		// {spawnMultiBullet(i);}
		// spawnPlayer("player_MP", mPlayer_MPTextureRegion);
		spawnPlayer("player_self", mPlayerTextureRegion);

		// spawnPlayer("player_MP", mPlayer_MPTextureRegion);
	}

	private void createEnemyWalkTimeHandler() {
		final TimerHandler enemyWalkTimerHandler;

		this.getEngine().registerUpdateHandler(enemyWalkTimerHandler = new TimerHandler(2, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				jumpDir++;
				for (IEntity pEnemy : mEntityList) {
					if (pEnemy.getUserData() != null) {
						if (pEnemy.getUserData().toString().contains("enemy")) {
							Body EnemyBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape((IShape) pEnemy);
							if (EnemyBody != null) {
								if (jumpDir % 2 == 0) {

									EnemyBody.applyLinearImpulse(0, -7, EnemyBody.getPosition().x, EnemyBody.getPosition().y);
									EnemyBody.applyLinearImpulse(7, 0, EnemyBody.getPosition().x, EnemyBody.getPosition().y);

									// EnemyBody.setLinearVelocity(
									// mLinearVelocityX, 0);
									// Debug.d("right jump");

								}
								if (jumpDir % 2 != 0) {
									EnemyBody.applyLinearImpulse(0, -7, EnemyBody.getPosition().x, EnemyBody.getPosition().y);
									EnemyBody.applyLinearImpulse(-7, 0, EnemyBody.getPosition().x, EnemyBody.getPosition().y);

									//
									// EnemyBody.setLinearVelocity(
									// -mLinearVelocityX, 0);
									// Debug.d("left jump");

								}

							}

						}
					}
				}
				// Random Position Generator
				// final float xPos = MathUtils.random(30.0f,
				// (CAMERA_WIDTH - 30.0f));
				// final float yPos = MathUtils.random(30.0f,
				// (CAMERA_HEIGHT - 30.0f));

				// createSprite(xPos, yPos);
			}
		}));
	}

	public static void enableMusic(boolean m) {
		enableMusic = m;
	}

	public static void enableSounds(boolean s) {
		enableSounds = s;
	}

	public static void setMusicVolume(float vol) {
		mVolume = vol;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			Intent openStartMenu = new Intent(BotWars.this, StartMenu.class);
			startActivity(openStartMenu);

			finish();
			// do something on back.
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void createPinchZoomDetector() {
		if (MultiTouch.isSupportedByAndroidVersion()) {
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

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
		Debug.d("zoomstart");
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		if(!isButtonAreaTouched)
		{
		this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
		Debug.d("zoom factor"+pZoomFactor);
		}
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);

	}

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

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mCamera.getZoomFactor();
		if(!isButtonAreaTouched)
	{this.mCamera.offsetCenter(-pDistanceX / zoomFactor * 10, -pDistanceY / zoomFactor * 10);}
	}

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
	public boolean isPlayerMoving = false;

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

	public void stopPlayer(AnimatedSprite _playerSprite, Body _playerBody) {
		// if (isPlayerMoving) {
		_playerBody.setLinearVelocity(0f, _playerBody.getLinearVelocity().y);
		// isPlayerMoving = false;}
	}

	public float getDistance(Body _player, Body _enemy) {
		float dist_x = (float) Math.pow(_player.getPosition().x - _enemy.getPosition().x, 2);

		float dist_y = (float) Math.pow(_player.getPosition().y - _enemy.getPosition().y, 2);

		// Debug.d(dist_x+" "+dist_y);
		return ((float) Math.sqrt(dist_x + dist_y));
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}