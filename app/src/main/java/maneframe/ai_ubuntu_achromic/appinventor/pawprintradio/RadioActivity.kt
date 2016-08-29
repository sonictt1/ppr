package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.ShareCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.NotificationCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem

class RadioActivity : AppCompatActivity() {


    var mediaPlayer: MediaPlayer = MediaPlayer()
    var mediaSession: MediaSessionCompat? = null
    var isStarting: Boolean = true
    var fab: FloatingActionButton? = null
    var notifyId = 5234
    var mediaNotification: Notification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radio)

        mediaSession = MediaSessionCompat(applicationContext, "Paw Print Radio stream")
        mediaNotification = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setContentTitle("TrackTitle")
                .setContentText("Paw Print Radio")
                .setStyle(NotificationCompat.MediaStyle().setMediaSession(mediaSession!!.sessionToken as MediaSessionCompat.Token))
                .addAction(R.drawable.ic_pause_white_24dp, "Pause", getPendingPlayPauseIntent())
                .build()

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        fab = findViewById(R.id.fab) as FloatingActionButton?
        fab!!.setOnClickListener {
            togglePlayback("https://radio.pawprintradio.com/stream")
        }
        if(mediaPlayer.isPlaying) {
            fab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white_24dp, theme))
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_radio, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun togglePlayback(url: String) {
            if(!mediaPlayer.isPlaying){
                if(isStarting){
                    mediaPlayer.setDataSource(url)
                    mediaPlayer.prepareAsync()
                    mediaPlayer.setOnPreparedListener {
                        mediaPlayer.start()
                        fab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white_24dp, theme))
                        displayNotification()
                    }
                    isStarting = false
                } else {
                    mediaPlayer.start()
                    displayNotification()
                    fab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white_24dp, theme))
                }
            } else {
                mediaPlayer.pause()
                closeNotification()
                fab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_white_24dp, theme))
            }
    }

    fun displayNotification() {
        var notifyManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.notify(notifyId, mediaNotification)
    }

    fun closeNotification() {
        var notifyManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.cancel(notifyId)
    }

    fun editNotification(name: String, content: String) {
        if(mediaSession == null) {
            mediaSession = MediaSessionCompat(applicationContext, "Paw Print Radio stream")
        }
        mediaNotification = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setContentTitle("TrackTitle")
                .setContentText("Paw Print Radio")
                .setStyle(NotificationCompat.MediaStyle().setMediaSession(mediaSession!!.sessionToken as MediaSessionCompat.Token))
                .addAction(R.drawable.ic_pause_white_24dp, "Pause", getPendingPlayPauseIntent())
                .build()
    }

    fun getPendingPlayPauseIntent(): PendingIntent {
        return PendingIntent.getBroadcast(applicationContext, resources.getInteger(R.integer.notification_request_id), getPlayPauseIntent(), PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun getPlayPauseIntent(): Intent {
        var playPauseIntent: Intent = Intent(applicationContext, PlayReceiver::class.java)
        var extras: Bundle = Bundle()
        //extras.putSerializable(resources.getString(R.string.media_player_key), mediaPlayer)

        return playPauseIntent
    }
}
