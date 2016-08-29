package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log

/**
 * Created by Sonic on 8/10/2016.
 */
class PlayReceiver : BroadcastReceiver(){



    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null) {
            Log.e("PPR Broadcast Receiver", "Context null")
            return
        }

        if(intent == null) {
            Log.e("PPR Broadcast Receiver", "Intent null")
            return
        }

        var mediaPlayer: MediaPlayer = intent!!.getSerializableExtra(context.getString(R.string.media_player_key)) as MediaPlayer
        if(mediaPlayer.isPlaying) mediaPlayer.pause()
        else mediaPlayer.start()
    }

}