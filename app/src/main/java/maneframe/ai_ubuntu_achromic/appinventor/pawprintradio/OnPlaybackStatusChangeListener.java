package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

/**
 * Created by tthomp on 9/10/16.
 */
public abstract class OnPlaybackStatusChangeListener {

    public abstract void onPrepareMedia();

    public abstract void onMediaPrepared();

    public abstract void onPlayMedia();

    public abstract void onPauseMedia();

    public abstract void onStopMedia();
}
