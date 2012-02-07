package anden.examples.testing;

import java.io.IOException;

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
import org.anddev.andengine.engine.handler.collision.ICollisionCallback;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXObject;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXObjectGroup;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnectorManager;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.MathUtils;

import android.content.Intent;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga
 * 
 * @author Nicolas Gramlich
 * @since 00:06:23 - 11.07.2010
 */
public class BotWars extends BaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800; // Camera Parameters
	private static final int CAMERA_HEIGHT = 480;

	public static final short CATEGORYBIT_WALL = 1; // Body Categories
	public static final short CATEGORYBIT_PLAYER = 2;
	public static final short CATEGORYBIT_ENEMY = 4;
	public static final short CATEGORYBIT_BULLET = 8;

	// Masks
	// Put category bits of bodies which are supposed to collide in respective
	// mask bits
	public static final short MASKBITS_WALL = CATEGORYBIT_WALL
			+ CATEGORYBIT_PLAYER + CATEGORYBIT_ENEMY + CATEGORYBIT_BULLET;
	public static final short MASKBITS_PLAYER = CATEGORYBIT_WALL
			+ CATEGORYBIT_PLAYER;
	public static final short MASKBITS_ENEMY = CATEGORYBIT_WALL
			+ CATEGORYBIT_BULLET + CATEGORYBIT_ENEMY;
	public static final short MASKBITS_BULLET = CATEGORYBIT_WALL
			+ CATEGORYBIT_ENEMY + CATEGORYBIT_BULLET;
	// ===========================================================
	// Fields
	// ===========================================================
	private int bulletCount = 0;
	private SmoothCamera mCamera;

	private BitmapTextureAtlas mBitmapTextureAtlas; // atlas for player and
													// enemy textures
	private TiledTextureRegion mPlayerTextureRegion;
	private TiledTextureRegion mEnemyTextureRegion;

	private BitmapTextureAtlas mBulletTextureAtlas;
	private TiledTextureRegion mBulletTextureRegion;

	private BitmapTextureAtlas mOnScreenControlTexture; // atlas for
														// onScreenControl
														// textures
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;
	private DigitalOnScreenControl mDigitalOnScreenControl;

	private BitmapTextureAtlas mHUDTextureAtlas; // atlas for HUD textures
	private TextureRegion mJumpTextureRegion;
	private TextureRegion mShootTextureRegion;

	private TMXTiledMap mTMXTiledMap;
	private Music mMusic;
	private Sound mSound;
	private boolean isLanded = false;
	private Body mPlayerBody;
	private RepeatingSpriteBackground mRepeatingSpriteBackground;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	private FixtureDef boxFixtureDef;
	private AnimatedSprite mPlayerSprite;
	private long[] duration;

	private static float mImpulseY = 14f;
	private static float mLinearVelocityX = 8.0f;

	private static String mapName = "tmx/map_1.tmx";
	private static String mapBG = "tmx/scn_sunny.png";
	private String fix1_name = "", fix2_name = "";
	private static int mapOffset = 100;
	private boolean deleteEnemy = false;
	private boolean deleteBullet = false;
	private int playerDir;
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

		this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 200,
				200, 1.0f);

		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
		engineOptions.setNeedsMusic(true).setNeedsSound(true);
		return new Engine(engineOptions);

	}

	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		duration = new long[7];
		for (int i = 0; i <= 6; i++) {
			duration[i] = 50;
		}

		MusicFactory.setAssetBasePath("mfx/");
		SoundFactory.setAssetBasePath("mfx/");

		loadCharacters();
		loadControls();
		loadSounds();
		loadMap();
	}

	AnimatedSprite mEnemySprite;

	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());
		createEnemyWalkTimeHandler();
		mScene = new Scene();

		mScene.setBackground(this.mRepeatingSpriteBackground);

		if (mMusic.isPlaying()) {
			mMusic.pause();
		} else {// mMusic.setVolume(0.1f);
			mMusic.play();
		}

		final TMXLayer mTMXLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_EARTH), false);

		initCharacters();
		
		/*
		 * mPlayerSprite = new AnimatedSprite(mapOffset, 0,
		 * this.mPlayerTextureRegion);
		 * 
		 * mEnemySprite = new AnimatedSprite(mapOffset + 400, 0,
		 * this.mEnemyTextureRegion); mEnemySprite.animate(100);
		 * 
		 * 
		 * 
		 * this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
		 * SensorManager.GRAVITY_EARTH), false);
		 * 
		 * final FixtureDef mPlayerFixtureDef =
		 * PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_PLAYER,
		 * MASKBITS_PLAYER, (short) 0);
		 * 
		 * mPlayerBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
		 * mPlayerSprite, BodyType.DynamicBody, mPlayerFixtureDef);
		 * mPlayerBody.setUserData("player");
		 * this.mPhysicsWorld.registerPhysicsConnector(new
		 * PhysicsConnector(mPlayerSprite, mPlayerBody, true, false));
		 * 
		 * final FixtureDef mEnemyFixtureDef =
		 * PhysicsFactory.createFixtureDef(0, 0f, 0f, false, CATEGORYBIT_ENEMY,
		 * MASKBITS_ENEMY, (short) 0); final Body mEnemyBody =
		 * PhysicsFactory.createBoxBody( this.mPhysicsWorld, mEnemySprite,
		 * BodyType.DynamicBody, mEnemyFixtureDef);
		 * mEnemyBody.setUserData("enemy");
		 * 
		 * this.mPhysicsWorld.registerPhysicsConnector(new
		 * PhysicsConnector(mEnemySprite, mEnemyBody, true, false));
		 */
		mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mPhysicsWorld.setContactListener(new ContactListener() {

			public void beginContact(Contact contact) {

				Fixture fix1 = contact.getFixtureA();
				Fixture fix2 = contact.getFixtureB();
				
				
				if (fix1.getBody().getUserData() != null
						&& fix2.getBody().getUserData() != null) {

					fix1_name = fix1.getBody().getUserData().toString();
					fix2_name = fix2.getBody().getUserData().toString();

				} else {
					fix1_name = "";
					fix2_name = "";
				}

				isLanded = true;
				Debug.d("BeginContact");

			}

			public void endContact(Contact contact) {

				isLanded = false;

			}

			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}

		});
		mScene.registerUpdateHandler(new IUpdateHandler() {

			public void onUpdate(float pSecondsElapsed) {

				/*final Body mEnemyBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
						mEnemySprite);
				if (mEnemyBody!= null){if(mEnemyBody.getPosition().x-mPlayerBody.getPosition().x<=100){mEnemyBody.applyLinearImpulse(10, 10, mEnemyBody.getPosition().x, mEnemyBody.getPosition().y);}}
*/
				
				
				if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
						mBulletSprite) != null)
					// apply force to nullify gravity on bullet

					mPhysicsWorld
							.getPhysicsConnectorManager()
							.findBodyByShape(mBulletSprite)
							.applyForce(
									new Vector2(0, -SensorManager.GRAVITY_EARTH),
									mPhysicsWorld.getPhysicsConnectorManager()
											.findBodyByShape(mBulletSprite)
											.getWorldCenter());

				if (mBulletSprite != null
						&& mEnemySprite != null
						&& mPhysicsWorld.getPhysicsConnectorManager()
								.findPhysicsConnectorByShape(mEnemySprite) != null) {
					if ((fix1_name.equalsIgnoreCase("bullet") && fix2_name
							.equalsIgnoreCase("enemy"))
							
							|| (fix2_name.equalsIgnoreCase("bullet") && fix1_name
									.equalsIgnoreCase("enemy"))) {
									
						destroyEnemy();
						destroyBullet();

					}
				}

				if (mBulletSprite != null
						&& mPhysicsWorld.getPhysicsConnectorManager()
								.findPhysicsConnectorByShape(mBulletSprite) != null
						&& mPhysicsWorld.getPhysicsConnectorManager()
								.findBodyByShape(mBulletSprite) != null) {
					if ((fix1_name.equalsIgnoreCase("bullet") && fix2_name
							.equalsIgnoreCase("wall"))
							|| (fix2_name.equalsIgnoreCase("bullet") && fix1_name
									.equalsIgnoreCase("wall"))) {
						// destroyBullet();
					}
				}
				mCamera.setCenter(mPlayerSprite.getX(), mPlayerSprite.getY());

			}

			public void reset() {
				// TODO Auto-generated method stub

			}
		});

		initControls();
		mScene.attachChild(mTMXLayer);
		//mScene.attachChild(mEnemySprite);

		mScene.attachChild(mPlayerSprite);
		createUnwalkableObjects(mTMXTiledMap);

		mScene.setChildScene(this.mDigitalOnScreenControl);

		return mScene;
	}

	@Override
	public void onLoadComplete() {
		// this.showDialog(DIALOG_ALLOWDIAGONAL_ID);

	}

	/*
	 * @Override protected Dialog onCreateDialog(final int pID) { switch(pID) {
	 * case DIALOG_ALLOWDIAGONAL_ID: return new AlertDialog.Builder(this)
	 * .setTitle("Setup...") .setMessage(
	 * "Do you wish to allow diagonal directions on the OnScreenControl?")
	 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(final DialogInterface pDialog, final int
	 * pWhich) { BotWars.this.mDigitalOnScreenControl.setAllowDiagonal(true); }
	 * }) .setNegativeButton("No", new DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(final DialogInterface pDialog, final int
	 * pWhich) { BotWars.this.mDigitalOnScreenControl.setAllowDiagonal(false); }
	 * }) .create(); } return super.onCreateDialog(pID); }
	 */

	// ===========================================================
	// Methods
	// ===========================================================
	Rectangle rect;

	private void createUnwalkableObjects(TMXTiledMap map) {
		// Loop through the object groups

		for (final TMXObjectGroup group : map.getTMXObjectGroups()) {

			// if(group.getTMXObjectGroupProperties().containsTMXProperty("Zeme",
			// "true")){
			// This is our "wall" layer. Create the boxes from it

			for (final TMXObject object : group.getTMXObjects()) {

				rect = new Rectangle(object.getX(), object.getY(),
						object.getWidth(), object.getHeight());

				boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f,
						false, CATEGORYBIT_WALL, MASKBITS_WALL, (short) 0);

				PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect,
						BodyType.StaticBody, boxFixtureDef).setUserData("wall");

				rect.setVisible(false);
				mScene.attachChild(rect);
			}

		}

	}

	private void initControls() {

		HUD mHUD = new HUD();

		Sprite jump = new Sprite(CAMERA_WIDTH - 120, CAMERA_HEIGHT - 175,
				mJumpTextureRegion) {

			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {
				if (pEvent.isActionDown() && isLanded) {
					mPlayerBody.applyLinearImpulse(0, -mImpulseY,
							mPlayerBody.getPosition().x, // /////JUMP
							mPlayerBody.getPosition().y);
					mCamera.setZoomFactor(0.80f);

				}
				if (pEvent.isActionUp())
					mCamera.setZoomFactor(1.0f);
				
				
				return false;

			}

		};
		jump.setScale(0.70f);
		Sprite shoot = new Sprite(CAMERA_WIDTH - 200, CAMERA_HEIGHT - 100,
				mShootTextureRegion) {
			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {

				if (pEvent.isActionDown()) {
					if (bulletPresent) {
						destroyBullet();
						bulletPresent = false;
					}
					spawnBullet();

				}
				if (pEvent.isActionUp())
					mCamera.setZoomFactor(1.0f);
				return false;

			}

		};
		shoot.setScale(0.60f);
		mHUD.registerTouchArea(shoot);
		mHUD.attachChild(shoot);

		mHUD.registerTouchArea(jump);
		mHUD.attachChild(jump);

		mCamera.setHUD(mHUD);

		// /////////////////////////////////////////////////////////////////////////////////////

		this.mDigitalOnScreenControl = new DigitalOnScreenControl(0,
				CAMERA_HEIGHT
						- this.mOnScreenControlBaseTextureRegion.getHeight(),
				this.mCamera, this.mOnScreenControlBaseTextureRegion,
				this.mOnScreenControlKnobTextureRegion, 0.1f,

				new IOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {

						// physicsHandler.setVelocity(pValueX * 200, pValueY *
						// 200);

						if (pValueX > 0 && !mPlayerSprite.isAnimationRunning()) {
							mPlayerSprite.getTextureRegion()
									.setFlippedHorizontal(false);
							mPlayerBody.setLinearVelocity(mLinearVelocityX,
									mPlayerBody.getLinearVelocity().y);

							mPlayerSprite.animate(duration, 0, 6, false);
							// (30, false);
							mSound.play();
							playerDir=1;
						}

						else if (pValueX < 0
								&& !mPlayerSprite.isAnimationRunning()) {
							mPlayerSprite.getTextureRegion()
									.setFlippedHorizontal(true);
							mPlayerBody.setLinearVelocity(-mLinearVelocityX,
									mPlayerBody.getLinearVelocity().y);
							mPlayerSprite.animate(duration, 0, 6, false);
							// mPlayerSprite.animate(30, false);
							mSound.play();
							playerDir=-1;
						} else if (pValueX == 0) {
							mPlayerBody.setLinearVelocity(0f,
									mPlayerBody.getLinearVelocity().y);

						}
						// else mPlayerSprite.stopAnimation();
						// mCamera.setCenter(mPlayerSprite.getX(),
						// mPlayerSprite.getY());

					}

				});
		this.mDigitalOnScreenControl.getControlBase().setBlendFunction(
				GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
		this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		this.mDigitalOnScreenControl.getControlBase().setScale(1.25f);
		this.mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
		this.mDigitalOnScreenControl.getControlKnob().setAlpha(0.7f);
		this.mDigitalOnScreenControl.refreshControlKnobPosition();
		this.mDigitalOnScreenControl.setAllowDiagonal(false);

	}

	public void loadMap() {
		this.mRepeatingSpriteBackground = new RepeatingSpriteBackground(
				CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(),
				new AssetBitmapTextureAtlasSource(this, mapBG), 1.0f);
		try {
			final TMXLoader mTMXLoader = new TMXLoader(this,
					this.mEngine.getTextureManager(),
					TextureOptions.BILINEAR_PREMULTIPLYALPHA, null);

			this.mTMXTiledMap = mTMXLoader.loadFromAsset(this, mapName);

		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}

	}

	private void loadCharacters() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"andrun256.png", 0, 0, 8, 1);

		this.mEnemyTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"banana_tiled.png", 0, 64, 4, 2);

		this.mBulletTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"bullet.png", 0, 128, 1, 1);

	}

	private void loadControls() {

		this.mHUDTextureAtlas = new BitmapTextureAtlas(256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mJumpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mHUDTextureAtlas, this, "jump.png", 0, 0);

		this.mShootTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mHUDTextureAtlas, this, "shoot.png", 128, 0);

		this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_base.png", 0, 0);

		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_knob.png", 128, 0);

		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas,
				this.mOnScreenControlTexture, this.mHUDTextureAtlas);

	}

	private void loadSounds() {
		try {
			mMusic = MusicFactory.createMusicFromAsset(
					this.mEngine.getMusicManager(), this, "bg_music.mid");

			mMusic.setLooping(true);

			mSound = SoundFactory.createSoundFromAsset(
					this.mEngine.getSoundManager(), this, "fire_alarm4.wav");

		} catch (final IOException e) {
			Debug.e(e);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, R.string.menu_resume);
		menu.add(0, 1, 0, R.string.menu_pause);
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

			else if (item.getItemId() == 0) {// resume
				mEngine.start();
				Toast.makeText(this, "Resume", 100).show();

			} else if (item.getItemId() == 1) {// pause
				mEngine.stop();
				Toast.makeText(this, "Pause", 100).show();

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

	AnimatedSprite mBulletSprite;
	boolean bulletPresent = false;

	public void spawnBullet() {
		bulletPresent = true;
		mBulletSprite = new AnimatedSprite(mPlayerSprite.getX()
				+ playerDir*(mPlayerSprite.getWidth() + mPlayerSprite.getWidth() / 4),
				mPlayerSprite.getY() + mPlayerSprite.getHeight() / 4,
				mBulletTextureRegion);

		FixtureDef mBulletFixtureDef = PhysicsFactory.createFixtureDef(0, 0f,
				0f, false, CATEGORYBIT_BULLET, MASKBITS_BULLET, (short) 0);

		Body mBulletBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
				mBulletSprite, BodyType.DynamicBody, mBulletFixtureDef);
		mBulletBody.setUserData("bullet");

		mBulletBody.setBullet(true);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				mBulletSprite, mBulletBody, true, true));

		mBulletBody.setLinearVelocity(playerDir*20, 0);


		mScene.attachChild(mBulletSprite);
		mCamera.setZoomFactor(0.80f);
		bulletPresent = true;

	}


	public static void setImpulse(String str) {
		mImpulseY = Float.parseFloat(str);
	}

	public static void setVelocity(String str) {
		mLinearVelocityX = Float.parseFloat(str);
	}

	public static void setMap(int mapID) {
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
			mapBG = "tmx/scn_island.png";
		}
		if (sceneID == 3) {
			mapBG = "tmx/scn_underground.png";
		}
		if (sceneID == 4) {
			mapBG = "tmx/scn_sunset.png";
		}
	}

	private void destroyEnemy() {
		if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
				mEnemySprite) != null) {
			mPhysicsWorld
					.destroyBody(mPhysicsWorld.getPhysicsConnectorManager()
							.findBodyByShape(mEnemySprite));
			mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld
					.getPhysicsConnectorManager().findPhysicsConnectorByShape(
							mEnemySprite));
			mScene.detachChild(mEnemySprite);
		}
	}

	public void destroyBullet() {
		if (mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
				mBulletSprite) != null) {
			mPhysicsWorld.destroyBody(mPhysicsWorld
					.getPhysicsConnectorManager()
					.findBodyByShape(mBulletSprite));
			mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld
					.getPhysicsConnectorManager().findPhysicsConnectorByShape(
							mBulletSprite));
			mScene.detachChild(mBulletSprite);
		}
	}
private void spawnPlayer(){

	mPlayerSprite = new AnimatedSprite(mapOffset, 0,
			this.mPlayerTextureRegion);
	
	final FixtureDef mPlayerFixtureDef = PhysicsFactory.createFixtureDef(0,
			0f, 0f, false, CATEGORYBIT_PLAYER, MASKBITS_PLAYER, (short) 0);

	mPlayerBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
			mPlayerSprite, BodyType.DynamicBody, mPlayerFixtureDef);
	mPlayerBody.setUserData("player");
	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
			mPlayerSprite, mPlayerBody, true, false));

}

public void spawnEnemy(int xLoc) {
	
	mEnemySprite = new AnimatedSprite(mapOffset+100*xLoc,0,mEnemyTextureRegion);

	FixtureDef mEnemyFixtureDef = PhysicsFactory.createFixtureDef(0, 0f,
			0f, false, CATEGORYBIT_ENEMY, MASKBITS_ENEMY, (short) 0);

	Body mEnemyBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld,
			mEnemySprite, BodyType.DynamicBody, mEnemyFixtureDef);
	mEnemyBody.setUserData("enemy"+xLoc);
	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
			mEnemySprite, mEnemyBody, true, true));
	mScene.attachChild(mEnemySprite);
}
	private void initCharacters() {
for(int i=1;i<=2;i++){spawnEnemy(i);}
		//for(int i=1;i<=10;i++)
	//{spawnMultiBullet(i);}
spawnPlayer();
	}

	private int dir = 0;

	private void createEnemyWalkTimeHandler() {
		TimerHandler enemyWalkTimerHandler;

		this.getEngine().registerUpdateHandler(
				enemyWalkTimerHandler = new TimerHandler(4, true,
						new ITimerCallback() {
							@Override
							public void onTimePassed(
									final TimerHandler pTimerHandler) {

								Body EnemyBody = mPhysicsWorld
										.getPhysicsConnectorManager()
										.findBodyByShape(mEnemySprite);
								if(EnemyBody!=null)
								{if (dir % 2 == 0 && isLanded) {
									EnemyBody.setLinearVelocity(
											mLinearVelocityX, 0);
									EnemyBody.applyLinearImpulse(0, -10,
											mPlayerBody.getPosition().x,
											mPlayerBody.getPosition().y);
								}

								if (dir % 2 != 0) {
									EnemyBody.applyLinearImpulse(0, -10,
											mPlayerBody.getPosition().x,
											mPlayerBody.getPosition().y);
									EnemyBody.setLinearVelocity(
											-mLinearVelocityX, 0);
								}

								dir++;
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
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
