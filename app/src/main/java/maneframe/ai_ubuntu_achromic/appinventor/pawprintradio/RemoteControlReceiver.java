package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Sonic on 8/30/2016.
 */
public class RemoteControlReceiver extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
