package anden.examples.testing;

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
import org.anddev.andengine.engine.handler.collision.ICollisionCallback;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.IEntity.IEntityMatcher;
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
			+ CATEGORYBIT_PLAYER+CATEGORYBIT_ENEMY;
	public static final short MASKBITS_ENEMY = CATEGORYBIT_WALL
			+ CATEGORYBIT_BULLET + CATEGORYBIT_ENEMY+CATEGORYBIT_PLAYER;
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
	private TiledTextureRegion mHealthRegion;

	
	private AnimatedSprite mEnemySprite;
	
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
	private AnimatedSprite mBulletSprite;
	private long[] player_duration,health_duration;

	private static float mImpulseY = 14f;
	private static float mLinearVelocityX = 8.0f;

	private static String mapName = "tmx/map_1.tmx";
	private static String mapBG = "tmx/scn_sunny.png";
	private String fix1_name = "", fix2_name = "";
	private static int mapOffset = 100;
	private boolean deleteEnemy = false;
	private boolean deleteBullet = false;
	private int playerDir;
	private boolean desBull=false;
	private boolean desEnemy=false;
	private boolean bulletPresent = false;
	private boolean reduceHealth=false;
	private float refrainImpulse=5.0f;

	public float Player_Max_Health=100.0f;

	public float Player_Health_Reduce=20.0f;
	
	private AnimatedSprite mHealthSprite;
	private ContactListener collisionListener;
	private IUpdateHandler gameUpdater;
	private static boolean enableMusic;
    private static  boolean enableSounds;
    private static float mVolume=1.0f;
	//private Body collBody1,collBody2;
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

		player_duration = new long[7];
		for (int i = 0; i <= 6; i++) {
			player_duration[i] = 50;
		}

		MusicFactory.setAssetBasePath("mfx/");
		SoundFactory.setAssetBasePath("mfx/");

		loadCharacters();
		loadControls();
		loadSounds();
		loadMap();
	}

	Fixture fix1;
	ArrayList<IEntity> mEntityList;
	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());
		createEnemyWalkTimeHandler();
		mScene = new Scene();

		mScene.setBackground(this.mRepeatingSpriteBackground);
if(enableMusic){
		if (mMusic.isPlaying()) {
			mMusic.pause();
		} else {mMusic.setVolume(mVolume);
			mMusic.play();
		}
}
		final TMXLayer mTMXLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_EARTH), false);

		initCharacters();
		
		
		mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		createCollisionListener();
		this.mPhysicsWorld.setContactListener(collisionListener);
			
		createGameUpdateHandler();
		mScene.registerUpdateHandler(gameUpdater);
				
		
		initControls();
		mScene.attachChild(mTMXLayer);
		//mScene.attachChild(mEnemySprite);

		mScene.attachChild(mPlayerSprite);
		createUnwalkableObjects(mTMXTiledMap);

		mScene.setChildScene(this.mDigitalOnScreenControl);


		mEntityList=new ArrayList<IEntity>(mScene.getChildCount());
		
		for(int i=0;i<mScene.getChildCount();i++)mEntityList.add(mScene.getChild(i));
		
		
		return mScene;
	}

	private void createCollisionListener()
	{
		collisionListener=new ContactListener() {

			public void beginContact(Contact contact) {
				
				fix1 = contact.getFixtureA();
				Fixture fix2 = contact.getFixtureB();
				
				Body collBody1=fix1.getBody();
				Body collBody2=fix2.getBody();

				final Body pBulletBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
						mBulletSprite);
				final Body pEnemyBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
						mEnemySprite);
				final Body pWallBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(rect);
			
				
				if (fix1.getBody().getUserData() != null
						&& fix2.getBody().getUserData() != null) {

					fix1_name = fix1.getBody().getUserData().toString();
					fix2_name = fix2.getBody().getUserData().toString();

				} else {
					fix1_name = "";
					fix2_name = "";
				}

				isLanded = true;
				//Debug.d("BeginContact");

				
				if (mBulletSprite != null
						&& mPhysicsWorld.getPhysicsConnectorManager()
								.findPhysicsConnectorByShape(mBulletSprite) != null
						&&pBulletBody != null) 
				{
					if ((fix1_name.equalsIgnoreCase("bullet") && fix2_name
							.equalsIgnoreCase("wall"))
							|| (fix2_name.equalsIgnoreCase("bullet") && fix1_name
								.equalsIgnoreCase("wall"))) {
						//if(desBull){
					
					desBull=true;
					
					}
					
					
					
				}
				
				if ((fix1_name.equalsIgnoreCase("player") && fix2_name.contains("enemy"))
						|| (fix2_name.equalsIgnoreCase("player") && fix1_name.contains("enemy"))) {Debug.d("player hits enemy");reduceHealth=true;}
					else reduceHealth=false;
				
				
				if (mBulletSprite != null
						&& mEnemySprite != null){
						//&& pEnemyBody!=null) {
					if ((fix1_name.equalsIgnoreCase("bullet") && fix2_name
							.contains("enemy"))
							
							|| (fix2_name.equalsIgnoreCase("bullet") && fix1_name
									.contains("enemy"))) {
									
						desEnemy=true;
						desBull=true;

					}
					
					
					
				//if(collBody1==pEnemyBody||collBody2==pEnemyBody)Debug.d("enemy body hit");
				//if(fix1_name=="enemy"||fix2_name=="enemy")Debug.d("enemy hit hit hit");
					
				}
				
				
				
				
			}

			public void endContact(Contact contact) {
				fix1_name = "";
				fix2_name = "";	
				desEnemy=false;
				desBull=false;
				reduceHealth=false;
				isLanded = false;

			}

			public void preSolve(Contact contact, Manifold oldManifold) {
				// TODO Auto-generated method stub

			}

			public void postSolve(Contact contact, ContactImpulse impulse) {
				// TODO Auto-generated method stub

			}

		};
		
		
	}
	
	private void createGameUpdateHandler()
	{gameUpdater=new IUpdateHandler() {

		public void onUpdate(float pSecondsElapsed) {

			
			final Body pBulletBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
					mBulletSprite);
			
			final Body pEnemyBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
					mEnemySprite);
			
			final Body pPlayerBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(
					mPlayerSprite);
			
			if (pBulletBody != null)
				// apply force to nullify gravity on bullet
			
				pBulletBody
						.applyForce(
								new Vector2(0, -SensorManager.GRAVITY_EARTH),
								pBulletBody.getWorldCenter());
/*
			if (mBulletSprite != null
					&& mEnemySprite != null
					&& pEnemyBody!=null) {
				if ((fix1_name.equalsIgnoreCase("bullet") && fix2_name
						.equalsIgnoreCase("enemy"))
						
						|| (fix2_name.equalsIgnoreCase("bullet") && fix1_name
								.equalsIgnoreCase("enemy"))) {
								
					destroyEnemy();
					destroyBullet();

				}
			}*/

		/*	if (mBulletSprite != null
					&& mPhysicsWorld.getPhysicsConnectorManager()
							.findPhysicsConnectorByShape(mBulletSprite) != null
					&&pBulletBody != null) {
				if ((fix1_name.equalsIgnoreCase("bullet") && fix2_name
						.equalsIgnoreCase("wall"))
						|| (fix2_name.equalsIgnoreCase("bullet") && fix1_name
							.equalsIgnoreCase("wall"))) {
				//if(desBull){
					Debug.d("BULLHIT");
				destroyBullet();
				}
			}*//////////////
			if(desEnemy){destroyEnemy(fix1_name,fix2_name);desEnemy=false;}
			if(desBull){destroyBullet();desBull=false;}
			
			////////////////////////////////////////////////////////////////////////////////////////
			
			if(reduceHealth&&mHealthSprite!=null&&mPlayerSprite!=null){
					Debug.d("Player_Max_Health_Here_Man");
			
				
				pPlayerBody.applyLinearImpulse((float) (refrainImpulse*1.5),-refrainImpulse,
				pPlayerBody.getPosition().x, 
				pPlayerBody.getPosition().y);
				
				Player_Max_Health-=Player_Health_Reduce;
				
				
				health_duration = new long[2];
				health_duration[0]=50;
				health_duration[1]=50;
				
				switch((int)Player_Max_Health)
				{
				  case 100: break;
				  case 80: mHealthSprite.animate(health_duration,0,1,false); break;
				  case 60: mHealthSprite.animate(health_duration,1,2,false); break;
				  case 40: mHealthSprite.animate(health_duration,2,3,false);break;
				  case 20: mHealthSprite.animate(health_duration,3,4,false);break;
				  default : break;
				
				}
						
			if(Player_Max_Health<=0)
				{
			
          
					mPhysicsWorld.destroyBody(pPlayerBody);
					mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mPlayerSprite));
					mScene.detachChild(mPlayerSprite);
				}
			reduceHealth=false;}	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
	
			mCamera.setCenter(mPlayerSprite.getX(), mPlayerSprite.getY());
		}
		
		
		
		
		
		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}
		
	
	};
	
	}
	
	@Override
	public void onLoadComplete() {
		// this.showDialog(DIALOG_ALLOWDIAGONAL_ID);

	}

	
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
						
					}
					spawnBullet();

				}
				if (pEvent.isActionUp())
					mCamera.setZoomFactor(1.0f);
				return false;

			}

		};
		shoot.setScale(0.60f);
		mHealthSprite=new AnimatedSprite(CAMERA_WIDTH-256, 10,mHealthRegion );
		mHealthSprite.setScale(0.5f);

		mHUD.registerTouchArea(shoot);
		mHUD.attachChild(shoot);

		mHUD.registerTouchArea(jump);
		mHUD.attachChild(jump);
		mHUD.attachChild(mHealthSprite);
		mCamera.setHUD(mHUD);



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

						

						if (pValueX > 0 && !mPlayerSprite.isAnimationRunning()) {
							mPlayerSprite.getTextureRegion()
									.setFlippedHorizontal(false);
							mPlayerBody.setLinearVelocity(mLinearVelocityX,
									mPlayerBody.getLinearVelocity().y);

							mPlayerSprite.animate(player_duration, 0, 6, false);
						
							if(enableSounds)mSound.play();
							playerDir=1;
						}

						else if (pValueX < 0
								&& !mPlayerSprite.isAnimationRunning()) {
							mPlayerSprite.getTextureRegion()
									.setFlippedHorizontal(true);
							mPlayerBody.setLinearVelocity(-mLinearVelocityX,
									mPlayerBody.getLinearVelocity().y);
							mPlayerSprite.animate(player_duration, 0, 6, false);
						
							mSound.play();
							playerDir=-1;
						} else if (pValueX == 0) {
							mPlayerBody.setLinearVelocity(0f,
									mPlayerBody.getLinearVelocity().y);

						}

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


		this.mHUDTextureAtlas = new BitmapTextureAtlas(256, 1024,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		this.mJumpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mHUDTextureAtlas, this, "jump.png", 0, 128);

		this.mShootTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(mHUDTextureAtlas, this, "shoot.png", 128, 0);

	this.mHealthRegion= BitmapTextureAtlasTextureRegionFactory
		        .createTiledFromAsset(this.mHUDTextureAtlas, this,
				"health_bar.png",0,256,1,8);
		
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

				Intent StartIntent = new Intent(BotWars.this, GameMenuActivity.class);
				startActivity(StartIntent);

				finish();
				Toast.makeText(this, "Main Menu", 100).show();

			}

		}
		return true;
	}

	
	public void spawnBullet() {
		
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

	public static void setVelocity(float str) {
		mLinearVelocityX =str; //Float.parseFloat(str);
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

	private void destroyEnemy(String bodyName1,String bodyName2) {
		final String name1=bodyName1;
		final String name2=bodyName2;
		Debug.d(bodyName1+"  "+bodyName2);
	/*	final Body pEnemyBody=mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(mEnemySprite);
		//if (pEnemyBody != null&&((pEnemyBody.getUserData()==bodyName1)||(pEnemyBody.getUserData()==bodyName2))) {
			if(pEnemyBody.getUserData()=="enemy1"){	Debug.d("yoyoyo");
			mPhysicsWorld
					.destroyBody(pEnemyBody);
			mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld
					.getPhysicsConnectorManager().findPhysicsConnectorByShape(
							mEnemySprite));
			
			mScene.detachChild(mEnemySprite);
			Debug.d("enemy killed"+bodyName1+bodyName2);}
	*/
		//if(fix1!=null)mPhysicsWorld.destroyBody(fix1.getBody());
		//PhysicsConnector pEnemyPhysConn=mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(mEnemySprite);
		
		//if(pEnemyPhysConn.getBody()==fix1.getBody())mPhysicsWorld.unregisterPhysicsConnector(pEnemyPhysConn);
	
		
		for(IEntity pEnemy:mEntityList){if(pEnemy.getUserData()!=null){
			if(pEnemy.getUserData().toString().equalsIgnoreCase(bodyName1)||pEnemy.getUserData().toString().equalsIgnoreCase(bodyName2))
				{mScene.detachChild(pEnemy);
				mPhysicsWorld.destroyBody(mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape((IShape)pEnemy));
				mPhysicsWorld.unregisterPhysicsConnector(mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape((IShape)pEnemy));}
			}
		}
			//Debug.d("see"+ie.getUserData().toString());}}//
		//if(phycon.getShape()==fix1.getShape())

	
//IEntity xyz=fix1.;

//if(imat.matches(mEnemySprite)){Debug.d("zooopu");}

//mScene.findChild(imat);
//mScene.detachChild(mEnemySprite);
				
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
			bulletPresent = false;
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
private int enemyCount=0;
public void spawnEnemy(int xLoc) {
	enemyCount++;
	mEnemySprite = new AnimatedSprite(mapOffset+100*xLoc,0,mEnemyTextureRegion);

	FixtureDef mEnemyFixtureDef = PhysicsFactory.createFixtureDef(0, 0f,
			0f, false, CATEGORYBIT_ENEMY, MASKBITS_ENEMY, (short) 0);

	Body mEnemyBody =PhysicsFactory.createBoxBody(this.mPhysicsWorld,
			mEnemySprite, BodyType.DynamicBody, mEnemyFixtureDef);
	mEnemyBody.setUserData("enemy"+enemyCount);
	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
			mEnemySprite, mEnemyBody, true, true));
	mEnemySprite.setUserData("enemy"+enemyCount);
	
	
	mScene.attachChild(mEnemySprite);
	mEnemySprite.animate(100);
}
	private void initCharacters() {
for(int i=1;i<=10;i++){spawnEnemy(i);}
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
								for(IEntity pEnemy:mEntityList){if(pEnemy.getUserData()!=null){if(pEnemy.getUserData().toString().contains("enemy")){
								Body EnemyBody = mPhysicsWorld
										.getPhysicsConnectorManager()
										.findBodyByShape((IShape)pEnemy);
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
								}
								dir++;
								}}}
								// Random Position Generator
								// final float xPos = MathUtils.random(30.0f,
								// (CAMERA_WIDTH - 30.0f));
								// final float yPos = MathUtils.random(30.0f,
								// (CAMERA_HEIGHT - 30.0f));

								// createSprite(xPos, yPos);
							}
						}));
	}
	
    public  static void enableMusic(boolean m )
    {enableMusic=m;}
    
    public static void enableSounds(boolean s )
    {enableSounds=s;}
	public static void setMusicVolume(float vol)
	{
		mVolume=vol;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
