package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Sonic on 8/30/2016.
 */
public class StreamingSession extends MediaSessionCompat.Callback {

    ComponentName mediaButtonReceiver;
    MediaSessionCompat mediaSession;
    MediaPlayer player;
    Context mediaContext;
    Notification mediaNotification;

    public StreamingSession(Context context) {
        super();
        mediaContext = context;
        mediaButtonReceiver = new ComponentName(mediaContext, RemoteControlReceiver.class);
        mediaSession = new MediaSessionCompat(mediaContext, "stream", mediaButtonReceiver, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(this);
        player = new MediaPlayer();


        try {
            player.setDataSource(mediaContext.getResources().getString(R.string.stream_url));
        } catch (IOException e) {
            Log.e("Streaming Error", "IOException", e);
            return;
        }


        MediaMetadataCompat meta = new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(mediaContext.getResources(), R.drawable.ic_play_arrow_white_24dp))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "PPR")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "PPR Album").build();

        mediaSession.setMetadata(meta);
        mediaSession.setPlaybackState(getConnectingPlayState());
        player.setOnPreparedListener(getOnPreparedListener());
        player.prepareAsync();
        mediaSession.setActive(true);
        mediaSession.setPlaybackState(getPausedPlayState());
    }

    public MediaSessionCompat getMediaSession(){
        return mediaSession;
    }

    @Override
    public void onPrepare() {
        super.onPrepare();

    }

    @Override
    public void onPlay() {
        super.onPlay();
        if(!mediaSession.isActive()) mediaSession.setActive(true);
        mediaSession.setPlaybackState(getPlayingPlayState());
        setUpNotification();
        player.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaSession.setPlaybackState(getPausedPlayState());
        player.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mediaSession.isActive()) mediaSession.setActive(false);
        if(player.isPlaying()) player.stop();
        mediaSession.setPlaybackState(getStoppedPlayState());
        player.release();
    }

    private PlaybackStateCompat getConnectingPlayState()
    {
        long PLAYBACK_ACTIONS = PlaybackStateCompat.ACTION_STOP;
        return new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_CONNECTING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .setActions(PLAYBACK_ACTIONS).build();
    }

    private PlaybackStateCompat getPlayingPlayState()
    {
        long PLAYBACK_ACTIONS = PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PAUSE;
        return new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .setActions(PLAYBACK_ACTIONS).build();
    }

    private PlaybackStateCompat getPausedPlayState()
    {
        long PLAYBACK_ACTIONS = PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY;
        return new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .setActions(PLAYBACK_ACTIONS).build();
    }

    private PlaybackStateCompat getStoppedPlayState()
    {
        return new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0).build();
    }

    private void resetMediaPlayer()
    {

    }

    public MediaPlayer getMediaPlayer() {
        return player;
    }

    private MediaPlayer.OnPreparedListener getOnPreparedListener() {
        return new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        };
    }

    private void setUpNotification() {
        mediaNotification = new NotificationCompat.Builder(mediaContext).setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setStyle(new NotificationCompat.MediaStyle().setMediaSession(getMediaSession().getSessionToken())).build();
        NotificationManager notifyManager = (NotificationManager) mediaContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(mediaContext.getResources().getInteger(R.integer.notification_request_id), mediaNotification);
    }
}
