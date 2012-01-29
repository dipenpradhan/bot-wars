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
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
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

import android.hardware.SensorManager;
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

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga
 * 
 * @author Nicolas Gramlich
 * @since 00:06:23 - 11.07.2010
 */
public class BotWars extends BaseGameActivity implements ICollisionCallback {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	private static final int DIALOG_ALLOWDIAGONAL_ID = 0;

	// ===========================================================
	// Fields
	// ===========================================================

	private SmoothCamera mCamera;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mFaceTextureRegion;
	private TiledTextureRegion mBananaTextureRegion;

	private BitmapTextureAtlas mOnScreenControlTexture;
	private TextureRegion mOnScreenControlBaseTextureRegion;
	private TextureRegion mOnScreenControlKnobTextureRegion;
	private DigitalOnScreenControl mDigitalOnScreenControl;

	private BitmapTextureAtlas mJumpTextureAtlas;
	private TextureRegion mJumpTextureRegion;

	private TMXTiledMap mTMXTiledMap;
	private Music mMusic;
	private Sound mSound;
	private boolean isLanded = false;
	private float fX = 800, fY = 480;
	private Body mBody;
	private RepeatingSpriteBackground mRepeatingSpriteBackground;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;
	private FixtureDef boxFixtureDef;
	private AnimatedSprite face;
	private long[] duration;
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
		
		duration=new long[7];
		for(int i=0;i<=6;i++){duration[i]=50;}
		
		MusicFactory.setAssetBasePath("mfx/");
		SoundFactory.setAssetBasePath("mfx/");
		
		
		loadCharacters();
		loadControls();
		loadSounds();
		loadMap();
	}

	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());

		mScene = new Scene();
		
		 mScene.setBackground(this.mRepeatingSpriteBackground);
		//mScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

		if (mMusic.isPlaying()) {
			mMusic.pause();
		} else {// mMusic.setVolume(0.1f);
			mMusic.play();
		}
		
		final TMXLayer mTMXLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		// final TMXLayer mTMXLayer1 = this.mTMXTiledMap.getTMXLayers().get(1);

		// mCamera.setCenter(mTMXLayer.getWidth() / 2, mTMXLayer.getHeight() /
		// 2);

		// final int centerX = (CAMERA_WIDTH -
		// this.mFaceTextureRegion.getWidth()) / 2;
		// final int centerY = (CAMERA_HEIGHT -
		// this.mFaceTextureRegion.getHeight()) / 2;
		face = new AnimatedSprite(0, 20,
		// mTMXLayer.getWidth() / 2, mTMXLayer.getHeight() / 2,
				this.mFaceTextureRegion);

		final AnimatedSprite banana = new AnimatedSprite(
				//mTMXLayer.getWidth() / 2 + 100, mTMXLayer.getHeight() / 2,
				2000,0,
				this.mBananaTextureRegion);
		banana.animate(100);
		// face.animate(100);

		// final PhysicsHandler physicsHandler = new PhysicsHandler(face);
		// face.registerUpdateHandler(physicsHandler);

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_EARTH), false);

		final FixtureDef mFaceFixtureDef = PhysicsFactory.createFixtureDef(0,
				0f, 0f);
		mBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face,
				BodyType.DynamicBody, mFaceFixtureDef);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face,
				mBody, true, false));
		
		final FixtureDef mBananaFixtureDef = PhysicsFactory.createFixtureDef(0,
				0f, 0f);
		final Body mBananaBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, banana,
				BodyType.DynamicBody, mBananaFixtureDef);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(banana,
				mBananaBody, true, false));

		mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mPhysicsWorld.setContactListener(new ContactListener() {

			public void beginContact(Contact contact) {

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
				if(face.collidesWith(banana)){
					mScene.detachChild(banana);
				}
				mCamera.setCenter(face.getX(), face.getY());

			}

			public void reset() {
				// TODO Auto-generated method stub

			}
		});

		// final CollisionHandler collisionHandler = new CollisionHandler(this,
		// face, banana);
		// face.registerUpdateHandler(collisionHandler);
		// banana.registerUpdateHandler(collisionHandler);

		// ////////////////////////
		initControls();
		mScene.attachChild(mTMXLayer);
		mScene.attachChild(banana);
		mScene.attachChild(face);
		createUnwalkableObjects(mTMXTiledMap);
		// ///////////////////////

		mScene.setChildScene(this.mDigitalOnScreenControl);

		return mScene;
	}

	/*
	 * @Override public boolean onSceneTouchEvent(Scene pScene, TouchEvent
	 * pSceneTouchEvent) { if
	 * (pSceneTouchEvent.getAction()==MotionEvent.ACTION_DOWN) {
	 * mCamera.setZoomFactor(2.0f); mCamera.setCenter(fX, fY); } else
	 * if(pSceneTouchEvent.getAction()==MotionEvent.ACTION_UP){
	 * mCamera.setZoomFactor(1.0f); } return true; }
	 */
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
	@Override
	public boolean onCollision(IShape pCheckShape, IShape pTargetShape) {
		Toast.makeText(this, "BOOM ", Toast.LENGTH_LONG).show();
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void createUnwalkableObjects(TMXTiledMap map) {
		// Loop through the object groups

		for (final TMXObjectGroup group : map.getTMXObjectGroups()) {

			// if(group.getTMXObjectGroupProperties().containsTMXProperty("Zeme",
			// "true")){
			// This is our "wall" layer. Create the boxes from it

			for (final TMXObject object : group.getTMXObjects()) {

				final Rectangle rect = new Rectangle(object.getX(),
						object.getY(), object.getWidth(), object.getHeight());
				Debug.d("aaaaaaaaaaaaaaaaaaa" + rect);
				boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 1f);
				PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect,
						BodyType.StaticBody, boxFixtureDef);
				rect.setVisible(false);
				mScene.attachChild(rect);
			}
		
		}

	}

	private void initControls() {

		HUD mHUD = new HUD();

		Sprite jump = new Sprite(CAMERA_WIDTH - 128, CAMERA_HEIGHT - 128,
				mJumpTextureRegion) {
			@Override
			public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY) {
				if (pEvent.isActionDown() && isLanded) {
					mBody.applyLinearImpulse(0, -14, mBody.getPosition().x,		///////JUMP
							mBody.getPosition().y);								
					mCamera.setZoomFactor(0.80f);
				}
				if (pEvent.isActionUp())
					mCamera.setZoomFactor(1.0f);
				return false;

			}

		};

		// jump.setScale(0.3f);
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

						if (pValueX > 0 && !face.isAnimationRunning()
								) {
							face.getTextureRegion().setFlippedHorizontal(false);
							mBody.setLinearVelocity(8f,
									mBody.getLinearVelocity().y);
							
							face.animate(duration, 0, 6, false);
							//(30, false);
							mSound.play();
						}
						
						else if (pValueX < 0 && !face.isAnimationRunning()
								) {
							face.getTextureRegion().setFlippedHorizontal(true);
							mBody.setLinearVelocity(-8f,
									mBody.getLinearVelocity().y);
							face.animate(duration, 0, 6, false);
							//face.animate(30, false);
							mSound.play();
						}
						else if(pValueX==0){
							mBody.setLinearVelocity(0f,
									mBody.getLinearVelocity().y);
						}
						// else face.stopAnimation();
						// mCamera.setCenter(face.getX(), face.getY());

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
		this.mRepeatingSpriteBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_island.png"),1.0f);
		try {
			final TMXLoader mTMXLoader = new TMXLoader(this,
					this.mEngine.getTextureManager(),
					TextureOptions.BILINEAR_PREMULTIPLYALPHA, null);

			this.mTMXTiledMap = mTMXLoader.loadFromAsset(this,
					"tmx/map.tmx");

			// Toast.makeText(this, "Well,atleast the TMX loads... "
			// ,Toast.LENGTH_LONG).show();
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}

	}

	private void loadCharacters() {
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

	//	this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
	//			.createTiledFromAsset(this.mBitmapTextureAtlas, this,
	//					"snapdragon_tiled32.png", 0, 0, 4, 3);

		
		this.mFaceTextureRegion= BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "andrun256.png",0,0, 8, 1);
		
		this.mBananaTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"banana_tiled.png", 0, 64, 4, 2);
	}

	private void loadControls() {

		this.mJumpTextureAtlas = new BitmapTextureAtlas(128, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mJumpTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mJumpTextureAtlas, this, "jump1.png", 0,
						0);

		this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_base.png", 0, 0);

		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_knob.png", 128, 0);

		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas,
				this.mOnScreenControlTexture, this.mJumpTextureAtlas);

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
		menu.add(0, 0, 0,"Exit");
		//menu.add(0, 1, 0, R.string.menu_resume);
//		menu.add(0, 2, 0, R.string.menu_return);
	//	menu.add(0, 3, 0, R.string.menu_exit);
		Toast.makeText(this, "Options", 100).show();
		return true;

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		{

			if (item.getItemId() == 0) {// exit
				
				Toast.makeText(this, "Exit", 100).show();
				System.exit(0);
				

			} 
		}return true;
	}
		

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
