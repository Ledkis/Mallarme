package ledkis.module.mallarme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    View container;

    MallarmeView mallarmeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);

        mallarmeView = new MallarmeView(this);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Utils.setFullScreen(MainActivity.this, true);
                        mallarmeView.fadeIn();

                        return true;
                    case MotionEvent.ACTION_UP:
                        Utils.setFullScreen(MainActivity.this, false);
                        mallarmeView.fadeOut();

                        return true;
                    default:
                        break;
                }

                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mallarmeView.add(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mallarmeView.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
