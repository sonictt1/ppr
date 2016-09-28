package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;

/**
 * Created by Sonic on 8/30/2016.
 *
 * This class handles audio playback and Android citizenship.
 *
 * It will ask for focus before attempting to play audio and
 * relinquish focus when another app takes over.
 *
 * It will also lower audio volume during notification noises.
 *
 * It will grab input from accessories (in-line controls and bluetooth headphones).
 *
 * It displays a notification for media control. It also displays controls on Android Wear devices.
 */
public class StreamingSession extends MediaSessionCompat.Callback {

    private ComponentName mediaButtonReceiver;
    private MediaSessionCompat mediaSession;
    private MediaPlayer player;
    private Context mediaContext;
    private Notification mediaNotification;
    private AudioManager audioManager;
    private OnPlaybackStatusChangeListener listener;
    private static StreamingSession session;

    private BroadcastReceiver noisyReciever =
        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onPause();
            }
        };

    private StreamingSession(Context context, @Nullable OnPlaybackStatusChangeListener listener) {
        super();
        if(listener != null) {
            this.listener = listener;
            listener.onPrepareMedia();
        }
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(!requestAudioFocus()) {
            Log.e("StreamingSession", "Audio Focus not granted");
            return;
        }

        mediaContext = context;
        mediaButtonReceiver = new ComponentName(mediaContext, RemoteControlReceiver.class);
        mediaSession = new MediaSessionCompat(mediaContext, "stream", mediaButtonReceiver, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(this);
        player = new MediaPlayer();
        player.setOnPreparedListener(getMediaSessionOnPreparedListener());


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

    public static StreamingSession getInstance(Context context, @Nullable OnPlaybackStatusChangeListener listener) {
        if(session == null) return session = new StreamingSession(context, listener);
        else session.setOnPlaybackStatusChangeListener(listener);
        return session;
    }

    public static StreamingSession getInstance(Context context)
    {
        return getInstance(context, null);
    }

    public MediaSessionCompat getMediaSession(){
        return mediaSession;
    }

    private MediaPlayer.OnPreparedListener getMediaSessionOnPreparedListener() {
        return new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if(listener != null) listener.onMediaPrepared();
            }
        };
    }

    @Override
    public void onPlay() {
        super.onPlay();
        if(!mediaSession.isActive()) mediaSession.setActive(true);
        mediaSession.setPlaybackState(getPlayingPlayState());
        notifyUser_Play();
        registerNoisyReceiver();
        player.start();
        if(listener != null) listener.onPlayMedia();
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaSession.setPlaybackState(getPausedPlayState());
        notifyUser_Pause();
        player.pause();
        if(listener != null) listener.onPauseMedia();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mediaSession.isActive()) mediaSession.setActive(false);
        if(player.isPlaying()) player.stop();
        audioManager.abandonAudioFocus(getFocusChangeListener());
        mediaSession.setPlaybackState(getStoppedPlayState());
        unregisterNoisyReceiver();
        player.release();
        if(listener != null) listener.onStopMedia();
        dismissNotification();
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

    private boolean requestAudioFocus() {
        int requestResult = audioManager.requestAudioFocus(getFocusChangeListener(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private AudioManager.OnAudioFocusChangeListener getFocusChangeListener() {
        return new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                switch (i) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        onStop();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        onPause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        getMediaPlayer().setVolume(.5f, .5f);
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        getMediaPlayer().setVolume(1.0f, 1.0f);
                        break;
                }
            }
        };
    }



    private void registerNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mediaContext.registerReceiver(noisyReciever, filter);
    }

    private void unregisterNoisyReceiver() {
        mediaContext.unregisterReceiver(noisyReciever);
    }

    private void notifyUser_Play() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(mediaContext, mediaSession);
        builder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp).setColor(ContextCompat.getColor(mediaContext, R.color.colorPrimaryDark));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_white_24dp, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(mediaContext, PlaybackStateCompat.ACTION_PAUSE)));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_stop_white_24dp, "Stop", MediaButtonReceiver.buildMediaButtonPendingIntent(mediaContext, PlaybackStateCompat.ACTION_STOP)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSession.getSessionToken()));
        NotificationManager manager = (NotificationManager) mediaContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mediaNotification = builder.build();
        manager.notify(mediaContext.getResources().getInteger(R.integer.notification_request_id), mediaNotification);
    }

    private void notifyUser_Pause() {
        NotificationCompat.Builder builder = MediaStyleHelper.from(mediaContext, mediaSession);
        builder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp).setColor(ContextCompat.getColor(mediaContext, R.color.colorPrimaryDark));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_arrow_white_24dp, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(mediaContext, PlaybackStateCompat.ACTION_PLAY)));
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_stop_white_24dp, "Stop", MediaButtonReceiver.buildMediaButtonPendingIntent(mediaContext, PlaybackStateCompat.ACTION_STOP)));
        builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0).setMediaSession(mediaSession.getSessionToken()));
        NotificationManager manager = (NotificationManager) mediaContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mediaNotification = builder.build();
        manager.notify(mediaContext.getResources().getInteger(R.integer.notification_request_id), mediaNotification);
    }

    private void dismissNotification() {
        NotificationManager manager = (NotificationManager) mediaContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(mediaContext.getResources().getInteger(R.integer.notification_request_id));
    }

    public void setOnPlaybackStatusChangeListener(OnPlaybackStatusChangeListener listener) {
        this.listener = listener;
    }

    public static class RemoteControlReceiver extends BroadcastReceiver {

        public RemoteControlReceiver() {}
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals((intent.getAction()))) {
                KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    getInstance(context).onPlay();
                } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                    getInstance(context).onPause();
                } else if (KeyEvent.KEYCODE_MEDIA_STOP == event.getKeyCode()) {
                    getInstance(context).onStop();
                }
            }
        }
    }
}
