package botwars.main;

import java.io.IOException;

import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.ui.activity.BaseSplashActivity;
import org.anddev.andengine.util.Debug;

import android.app.Activity;

public class SplashScreen extends BaseSplashActivity {
	
	@Override
	protected ScreenOrientation getScreenOrientation() {
		// TODO Auto-generated method stub
		return ScreenOrientation.LANDSCAPE;
	}

	@Override
	protected IBitmapTextureAtlasSource onGetSplashTextureAtlasSource() {
		// TODO Auto-generated method stub
		return new AssetBitmapTextureAtlasSource(this, "gfx/splash.png");
	}

	@Override
	protected float getSplashDuration() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	protected Class<? extends Activity> getFollowUpActivity() {
		// TODO Auto-generated method stub
		return StartMenu.class;
	}

	@Override
    protected float getSplashScaleFrom() {
            return 5f;
    }   
	
}
