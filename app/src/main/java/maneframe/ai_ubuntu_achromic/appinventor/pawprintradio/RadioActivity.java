package maneframe.ai_ubuntu_achromic.appinventor.pawprintradio;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class RadioActivity extends AppCompatActivity {

    FloatingActionButton fab;
    int notifyId = 5234;
    Notification mediaNotification;
    StreamingSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(session == null)
                    session = new StreamingSession(getApplicationContext());
                if(session.getMediaPlayer().isPlaying()) {
                    session.onPause();
                    setFabIcon(R.drawable.ic_play_arrow_white_24dp);
                } else {
                    session.onPlay();
                    setFabIcon(R.drawable.ic_pause_white_24dp);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_radio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setFabIcon(int drawableId) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fab.setImageDrawable(getResources().getDrawable(drawableId, getTheme()));
        else
            fab.setImageDrawable(getResources().getDrawable(drawableId));
    }
}
