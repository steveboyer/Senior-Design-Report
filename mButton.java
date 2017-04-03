package us.steveboyer.sdremote;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by steve on 4/9/15.
 */
public class mButton extends Button {
    private boolean enabled = false;

    public mButton(Context context){
        super(context);
    }

    public mButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public mButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean mIsEnabled(){
        return enabled;
    }

    public void mSetEnabled(Boolean enabled){
        this.enabled = enabled;
        if(enabled){
            this.setClickable(true);
            this.setAlpha((float)1.0);
        } else {
            this.setClickable(false);
            this.setAlpha((float)0.25);
        }
    }

}
