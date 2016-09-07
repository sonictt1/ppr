package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by Sonic on 8/30/2016.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_MEDIA_BUTTON.equals((intent.getAction())))
        {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if(KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode())
            {

            }
            else if(KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode())
            {

            }
            else if(KeyEvent.KEYCODE_MEDIA_STOP == event.getKeyCode())
            {

            }
        }
    }
}
