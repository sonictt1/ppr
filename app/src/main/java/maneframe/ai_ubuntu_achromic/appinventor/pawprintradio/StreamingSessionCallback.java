package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Sonic on 8/30/2016.
 */
public class StreamingSessionCallback extends MediaSessionCompat.Callback {

    ComponentName mediaButtonReceiver;
    MediaSessionCompat mediaSession;
    MediaPlayer player;
    Context mediaContext;

    public StreamingSessionCallback(Context context) {
        super();
        mediaContext = context;
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
        mediaButtonReceiver = new ComponentName(mediaContext, RemoteControlReceiver.class);
        mediaSession = new MediaSessionCompat(mediaContext, "stream", mediaButtonReceiver, null);
        player = new MediaPlayer();
        try {
            player.setDataSource("https://radio.pawprintradio.com/stream");
        } catch (IOException e) {
            Log.e("Streaming Error", "IOException", e);
            return;
        }
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(this);

        MediaMetadataCompat meta = new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(mediaContext.getResources(), R.drawable.ic_play_arrow_white_24dp))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "PPR")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "PPR Album").build();

        mediaSession.setMetadata(meta);

        player.prepareAsync();


    }

    @Override
    public void onPlay() {
        super.onPlay();
        if(!mediaSession.isActive()) mediaSession.setActive(true);
        player.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        player.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mediaSession.isActive()) mediaSession.setActive(false);
        player.stop();
    }

    public PlaybackStateCompat getPlayState()
    {
        return new PlaybackStateCompat.Builder()
                .build();
    }
}
