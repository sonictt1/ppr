package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Sonic on 8/30/2016.
 */
public class StreamingSessionCallback extends MediaSessionCompat.Callback {
    MediaPlayer player;

    @Override
    public void onPrepare() {
        super.onPrepare();
        player = new MediaPlayer();
        try {
            player.setDataSource("https://radio.pawprintradio.com/stream");
        } catch (IOException e) {
            Log.e("Streaming Error", "IOException", e);
            return;
        }

        player.prepareAsync();
    }

    @Override
    public void onPlay() {
        super.onPlay();
        player.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        player.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        player.stop();
    }
}
