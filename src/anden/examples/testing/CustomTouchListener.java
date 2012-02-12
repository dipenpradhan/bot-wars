package anden.examples.testing;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class CustomTouchListener implements View.OnTouchListener 
{     
    public boolean onTouch(View view, MotionEvent motionEvent) {
    
	    switch(motionEvent.getAction()){            
            case MotionEvent.ACTION_DOWN:
             ((TextView) view).setTextColor(0xFF6A5ceD); 
                break;          
            case MotionEvent.ACTION_CANCEL:             
            case MotionEvent.ACTION_UP:
            ((TextView) view).setTextColor(0xF9f9f9f9);
                break;
	    } 
     
        return false;   
    } 
}
