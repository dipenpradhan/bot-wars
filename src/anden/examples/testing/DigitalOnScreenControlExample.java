package anden.examples.testing;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.handler.collision.CollisionHandler;
import org.anddev.andengine.engine.handler.collision.ICollisionCallback;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.IShape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.util.FPSLogger;

import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 00:06:23 - 11.07.2010
 */
public class DigitalOnScreenControlExample extends BaseGameActivity implements ICollisionCallback {
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

	private TMXTiledMap mTMXTiledMap;
	private Music mMusic;
	private Sound mSound;
	private boolean flag=false;
private float fX=800,fY=480;
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
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera).setNeedsMusic(true).setNeedsSound(true));
	}

	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		MusicFactory.setAssetBasePath("mfx/");
		SoundFactory.setAssetBasePath("mfx/");
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "snapdragon_tiled.png", 0, 0, 4, 3);
		this.mBananaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "banana_tiled.png", 0, 180, 4, 2);
		
		this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);

		this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas, this.mOnScreenControlTexture);
		
		
		try {
			mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "wagner_the_ride_of_the_valkyries.ogg");
			mMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e(e);
		}
		
		try {
			mSound=SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this , "explosion.ogg");
		} catch (IllegalStateException e) {
	
			Debug.e(e);
		} catch (IOException e) {
			
			Debug.e(e);
		}
		
		
	}

	@Override
	public Scene onLoadScene(){
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		
		if(mMusic.isPlaying()) {
			mMusic.pause();
		} else {//mMusic.setVolume(0.1f);
			mMusic.play();
		}
		
		try {
			final TMXLoader mTMXLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, null);
				
			this.mTMXTiledMap = mTMXLoader.loadFromAsset(this, "gfx/desert.tmx");

			//Toast.makeText(this, "Well,atleast the TMX loads... " ,Toast.LENGTH_LONG).show();
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}
		final TMXLayer mTMXLayer = this.mTMXTiledMap.getTMXLayers().get(0);
		mCamera.setCenter(mTMXLayer.getWidth()/2, mTMXLayer.getHeight()/2);
		
		//final int centerX = (CAMERA_WIDTH - this.mFaceTextureRegion.getWidth()) / 2;
		//final int centerY = (CAMERA_HEIGHT - this.mFaceTextureRegion.getHeight()) / 2;
		final AnimatedSprite face = new AnimatedSprite(mTMXLayer.getWidth()/2,mTMXLayer.getHeight()/2, this.mFaceTextureRegion);
		final AnimatedSprite banana = new AnimatedSprite(mTMXLayer.getWidth()/2+100,mTMXLayer.getHeight()/2, this.mBananaTextureRegion);
		banana.animate(100);
		//face.animate(100);
		
		final PhysicsHandler physicsHandler = new PhysicsHandler(face);
		face.registerUpdateHandler(physicsHandler);
		
		final CollisionHandler collisionHandler=new CollisionHandler(this, face, banana);
		//face.registerUpdateHandler(collisionHandler);
		//banana.registerUpdateHandler(collisionHandler);
		this.mDigitalOnScreenControl = new DigitalOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				
				physicsHandler.setVelocity(pValueX * 200, pValueY * 200);
			if(pValueX!=0&&!face.isAnimationRunning()){face.animate(30,false);mSound.play();//onCollision(face, banana);
		
			}
				//else face.stopAnimation();
				mCamera.setCenter(face.getX(),face.getY());
				
			}

			
		});	
		//////////////////////////
		
		
	
		
		scene.attachChild(mTMXLayer);
		scene.attachChild(banana);
		scene.attachChild(face);
		
		/////////////////////////
		this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
		this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		this.mDigitalOnScreenControl.getControlBase().setScale(1.25f);
		this.mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
		this.mDigitalOnScreenControl.getControlKnob().setAlpha(0.7f);
		this.mDigitalOnScreenControl.refreshControlKnobPosition();

		scene.setChildScene(this.mDigitalOnScreenControl);

		return scene;
	}
/*	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if (pSceneTouchEvent.getAction()==MotionEvent.ACTION_DOWN)
		{
			 mCamera.setZoomFactor(2.0f);
			 mCamera.setCenter(fX, fY);
		}
		else if(pSceneTouchEvent.getAction()==MotionEvent.ACTION_UP){
			mCamera.setZoomFactor(1.0f);
		}
		return true;
	}*/
	@Override
	public void onLoadComplete() {
		this.showDialog(DIALOG_ALLOWDIAGONAL_ID);
	}

	@Override
	protected Dialog onCreateDialog(final int pID) {
		switch(pID) {
			case DIALOG_ALLOWDIAGONAL_ID:
				return new AlertDialog.Builder(this)
				.setTitle("Setup...")
				.setMessage("Do you wish to allow diagonal directions on the OnScreenControl?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface pDialog, final int pWhich) {
						DigitalOnScreenControlExample.this.mDigitalOnScreenControl.setAllowDiagonal(true);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface pDialog, final int pWhich) {
						DigitalOnScreenControlExample.this.mDigitalOnScreenControl.setAllowDiagonal(false);
					}
				})
				.create();
		}
		return super.onCreateDialog(pID);
	}

	@Override
	public boolean onCollision(IShape pCheckShape, IShape pTargetShape) {
		Toast.makeText(this, "BOOM " ,Toast.LENGTH_LONG).show();
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
