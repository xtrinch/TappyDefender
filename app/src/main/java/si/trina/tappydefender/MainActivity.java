package si.trina.tappydefender;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load fastest time
        SharedPreferences prefs;
        prefs = getSharedPreferences("HighScores", MODE_PRIVATE);

        // reference to button
        final Button buttonPlay =
                (Button)findViewById(R.id.buttonPlay);

        // reference to text view
        final TextView textFastestTime = (TextView)findViewById(R.id.textHighScore);

        buttonPlay.setOnClickListener(this);

        // load fastest time
        long fastestTime = prefs.getLong("fastestTime", 1000000);
        textFastestTime.setText(String.format("Fastest: %.2f s", fastestTime/1000.0));
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
        // end main activity
        finish();
    }

    // If the player hits the back button, quit the app
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false;
    }
}
