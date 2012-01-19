package anden.examples.testing;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.BoundCamera;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.constants.Constants;

//import com.badlogic.gdx.physics.box2d.Body;
//import com.org.GameComponents.TiledSpriteObject;

import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 13:58:48 - 19.07.2010
 */
public class BotWarsMainActivity extends BaseGameActivity implements IOnSceneTouchListener {
	private static final int CAMERA_WIDTH = 360;
	private static final int CAMERA_HEIGHT = 240;


	float newX,newY,oldX,oldY,offsetX=0,offsetY=0,scrollX,scrollY;
	BoundCamera gameCam;
	BitmapTextureAtlas mTextureAtlas;
	TextureRegion mPlayer;
	private TMXTiledMap mTMXTiledMap;
	TiledSprite face;
    DigitalOnScreenControl control;
	public boolean onCreateOptionsMenu(Menu m){

		super.onCreateOptionsMenu(m);

		m.add(0,0,0,"Pause");
		m.add(0,1,0,"Resume");

		Toast.makeText(this,"Options", 100).show();


		return true;

	}




	public Engine onLoadEngine() {
		gameCam=new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 0, CAMERA_WIDTH, 0,CAMERA_HEIGHT);
		return new Engine (new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH,CAMERA_HEIGHT), this.gameCam));
	}




	public void onLoadResources() {

		mTextureAtlas= new BitmapTextureAtlas(64,64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		mPlayer=BitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "andou_diagmore03.png",0,0);
		
	//	control = new DigitalOnScreenControl(pX, pY, pCamera, pControlBaseTextureRegion, pControlKnobTextureRegion, pTimeBetweenUpdates, pOnScreenControlListener)
		this.mEngine.getTextureManager().loadTexture(mTextureAtlas);
	}




	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene mScene=new Scene();
        

		/* Calculate the coordinates for the face, so its centered on the camera. */
		final int centerX = (200 - this.mTextureAtlas.getWidth()) / 2;
		final int centerY = (100 - this.mTextureAtlas.getHeight()) / 2;

		/* Create the face and add it to the scene. */
	/////////////	face = new TiledSprite(centerX, centerY,64,64, this.mPlayer);

		

		try {
			final TMXLoader tmxLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, null);/*new ITMXTilePropertiesListener() {
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
					/* We are going to count the tiles that have the property "cactus=true" set. */
				//}
		//	}
				
			this.mTMXTiledMap = tmxLoader.loadFromAsset(this, "gfx/desert.tmx");

			Toast.makeText(this, "Well,atleast the TMX loads... " ,Toast.LENGTH_LONG).show();
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}
		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);

		//this.gameCam.setBounds(0, tmxLayer.getWidth(), 0, tmxLayer.getHeight());


		gameCam.setBoundsEnabled(true);
		
		
		
		mScene.attachChild(tmxLayer);


		
		
		 this.gameCam.setBounds(0, tmxLayer.getWidth(), 0, tmxLayer.getHeight());
         this.gameCam.setBoundsEnabled(true);

        
         mScene.attachChild(face);
        
         this.gameCam.setChaseEntity(face);

         mScene.setOnSceneTouchListener(this);
		return mScene;

	}




	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}




	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.getAction()== MotionEvent.ACTION_DOWN )
		{
			newX=pSceneTouchEvent.getX();
			newY=pSceneTouchEvent.getY();
			
			if(pSceneTouchEvent.getAction()==MotionEvent.ACTION_MOVE)
			{
				oldX=this.face.getX();
				oldY=this.face.getY();
				//newX=touchX;
				//newY=touchY;
				/*offsetX=newX-oldX;
				offsetY=newY-oldY;
				scrollX=this.gameCam.getCenterX()+offsetX;
				scrollY=this.gameCam.getCenterY()+offsetY;*/
			//////////	this.face.setX(newX);
			/////////	this.face.setY(newY);
				    //  Body b = new Body(null, 0);                                                        
				}
		}
		return false;
	}

	
	// ===========================================================
}	 