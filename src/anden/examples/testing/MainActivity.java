package anden.examples.testing;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.IUpdateHandler;
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
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.modifier.IModifier;

import android.content.SharedPreferences;
import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class MainActivity extends BaseGameActivity implements IOnSceneTouchListener{
    // Kameros plotis:
	private static final int CAMERA_WIDTH = (int) (720 / 1.5f);
	// Kameros aukstis:
    private static final int CAMERA_HEIGHT = (int) (480 / 1.5f) ;
    private static final float CHAR_MOVING_SPEED = 4f;
    //PARAMETRAMS:
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	//---------------
	private PhysicsWorld mPhysicsWorld;
	// Nustatom kamera:
    SmoothCamera camera = null;
    // Musu pagrindine zaidimo scena
    private Scene mScene;
    // SPRITAI
    Sprite charactersprite;
    // Atlasai ir Regionai-----
    private RepeatingSpriteBackground Background;
    private BitmapTextureAtlas charactera;
    private BitmapTextureAtlas forwarda;
    private TextureRegion forwardr;
    private BitmapTextureAtlas backwarda;
    private TextureRegion backwardr;
    private BitmapTextureAtlas jumpa;
    private TextureRegion jumpr;
   //--------------
    // MAPAI
    private TMXTiledMap mTMXTiledMap;
    private TextureRegion character;

    //----------
    //Kiti var'ai
    private boolean isLanded = false;
    private FixtureDef boxFixtureDef;
	public Engine onLoadEngine() {
		// Musu kamera:
		 camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 60 * 3,60 * 3, 30);
		 // Nustatymai:------
	        final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE , new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	        engineOptions.getTouchOptions().setRunOnUpdateThread(true);
	        engineOptions.setNeedsMusic(true);
	        return new Engine(engineOptions);
	}

	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		// Background:----------------------------------------------
        this.Background = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(), new AssetBitmapTextureAtlasSource(this, "gfx/background_air.png"),2);

         // --------=---------=----------=--------------=-----------
        this.charactera = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        this.character = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.charactera, this, "Chor.png", 0, 0);
        this.forwarda = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        this.forwardr = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.forwarda, this, "forward.png", 0, 0);

        this.backwarda = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        this.backwardr = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backwarda, this, "backward.png", 0, 0);

        this.jumpa = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        this.jumpr = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.jumpa, this, "jump.png", 0, 0);

        this.mEngine.getTextureManager().loadTextures(this.charactera, this.forwarda, this.backwarda, this.jumpa);

	}

	public Scene onLoadScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());

        this.mScene = new Scene();
        this.mScene.setBackground(this.Background);
// MAPPPPPPPASSSSS
        try {
                final TMXLoader tmxLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA);
                this.mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/lvl1.tmx");

                
        } catch (final TMXLoadException tmxle) {
                Debug.e(tmxle);
        }

        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        mScene.attachChild(tmxLayer);
        
        final TMXLayer tmxLayer1 = this.mTMXTiledMap.getTMXLayers().get(1);
        mScene.attachChild(tmxLayer1);
        
        final TMXLayer tmxLayer2 = this.mTMXTiledMap.getTMXLayers().get(2);
        mScene.attachChild(tmxLayer2);
        this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,SensorManager.GRAVITY_EARTH), false);
        // BAIGIASI MAPAS
        
        // Characteris:
        charactersprite = new Sprite(40, 0, this.character);
        charactersprite.setScaleX(0.65f);

        this.mScene.setOnSceneTouchListener( this);
        
        // FIZIKA
        final FixtureDef characterfictur = PhysicsFactory.createFixtureDef(0, 0f,0f);
        
        
        this.mScene.registerUpdateHandler(this.mPhysicsWorld);
        
        final Body body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, charactersprite, BodyType.DynamicBody, characterfictur);

        this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(charactersprite, body, true, false));
        mScene.attachChild(charactersprite);
        
        createUnwalkableObjects(mTMXTiledMap);
        final PhysicsHandler physicsHandler = new PhysicsHandler(charactersprite);
        charactersprite.registerUpdateHandler(physicsHandler);

        this.mPhysicsWorld.setContactListener(new ContactListener(){

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
        // HUD
        HUD my = new HUD();
        Sprite forward = new Sprite( 50, CAMERA_HEIGHT - 170, forwardr){
            @Override
            public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY){
          
            		charactersprite.getTextureRegion().setFlippedHorizontal(false);
            if(!pEvent.isActionUp()){
            		body.setLinearVelocity(CHAR_MOVING_SPEED, body.getLinearVelocity().y);
            	//body.applyLinearImpulse(new Vector2(2,0), body.getPosition());
            }else{
            	body.setLinearVelocity(0, body.getLinearVelocity().y);
            }
         
            	return false;
            	
                           }
        };
        
        forward.setScale(0.3f);
        my.registerTouchArea(forward);
        my.attachChild(forward);
        
        Sprite backward = new Sprite( -50, CAMERA_HEIGHT - 170, backwardr){
            @Override
            public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY){
            
            	charactersprite.getTextureRegion().setFlippedHorizontal(true);
            	if(!pEvent.isActionUp()){
            	
            	body.setLinearVelocity(-CHAR_MOVING_SPEED, body.getLinearVelocity().y);
            }else{
            	body.setLinearVelocity(0, body.getLinearVelocity().y);
            }
            	return false;
            	
                           }
    
        };
        
        backward.setScale(0.3f);
        my.registerTouchArea(backward);
        my.attachChild(backward);
        Sprite jump = new Sprite(300, CAMERA_HEIGHT - 170, jumpr){
            @Override
            public boolean onAreaTouched(TouchEvent pEvent, float pX, float pY){
            	if(pEvent.isActionDown() && isLanded){
            	
            	//body.setLinearVelocity(new Vector2(body.getLinearVelocity().x,body.getLinearVelocity().y + CHAR_MOVING_SPEED)); // Don't look at there
            	body.applyLinearImpulse(0, -7, body.getPosition().x, body.getPosition().y);
            	}
            	return false;
            	
                           }
    
        };
        
        jump.setScale(0.3f);
        my.registerTouchArea(jump);
        my.attachChild(jump);
        camera.setHUD(my);
        mScene.registerUpdateHandler(new IUpdateHandler(){

			public void onUpdate(float pSecondsElapsed) {
				camera.setCenter(charactersprite.getX(), charactersprite.getY());
				
			}

			public void reset() {
				// TODO Auto-generated method stub
				
			}
        	
        });
        return this.mScene;
	}

	public void onLoadComplete() {
		
		
	}
    /** Called when the activity is first created. */

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		return false;
	}
    private void createUnwalkableObjects(TMXTiledMap map){
        // Loop through the object groups
    	
         for(final TMXObjectGroup group: map.getTMXObjectGroups()) {
        	
                 //if(group.getTMXObjectGroupProperties().containsTMXProperty("Zeme", "true")){
                         // This is our "wall" layer. Create the boxes from it
                	 
                         for(final TMXObject object : group.getTMXObjects()) {
 
                                final Rectangle rect = new Rectangle(object.getX(), object.getY(),object.getWidth(), object.getHeight());
                                boxFixtureDef = PhysicsFactory.createFixtureDef(0, 0,1f);
                                PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.StaticBody, boxFixtureDef);
                                rect.setVisible(false);
                                mScene.attachChild(rect);
                         }
                 //}
         }
         
}
}