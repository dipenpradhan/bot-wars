package anden.examples.testing;

import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


public class mapViewActivity  extends Activity{

	//---the images to display---
	Integer[] imageIDs = {
	R.drawable.day,
	R.drawable.night,
	R.drawable.ic_launcher,
	R.drawable.ic_launcher,
	R.drawable.ic_launcher,
	R.drawable.ic_launcher,
	R.drawable.ic_launcher
	};
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid);
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this));
        gridView.setOnItemClickListener(new OnItemClickListener()
        {
        public void onItemClick(AdapterView<?> parent,
        View v, int position, long id)
        {
        BotWars.setMap(position);
        BotWars.setVelocity(14.0f);
        BotWars.setMusicVolume(1.0f);
        BotWars.enableMusic(true);
        BotWars.enableSounds(true);
        
        Intent StartIntent = new Intent(mapViewActivity.this, BotWars.class);
		startActivity(StartIntent);

		finish();
        	Toast.makeText(getBaseContext(),
        "pic" + (position + 1) + " selected",
        Toast.LENGTH_SHORT).show();
        }
        });
    }
    
    public class ImageAdapter extends BaseAdapter
    {
    private Context context;
    public ImageAdapter(Context c)
    {
    context = c;
    }
    //---returns the number of images---
    public int getCount() {
    return imageIDs.length;
    }
    //---returns the ID of an item---
    public Object getItem(int position) {
    return position;
    }
    //---returns the ID of an item---
    public long getItemId(int position) {
    return position;
    }
    public View getView(int position, View convertView, ViewGroup parent)
    {
	    ImageView imageView;
	    if (convertView == null)
	    {
	    	imageView = new ImageView(context);
	        imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
	        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	//    imageView.setPadding(5, 5, 5, 5);
    } 
    else
    {
    	imageView = (ImageView) convertView;
    }
    
    imageView.setImageResource(imageIDs[position]);
    return imageView;
    }
    }
	
	
	
	
	
}