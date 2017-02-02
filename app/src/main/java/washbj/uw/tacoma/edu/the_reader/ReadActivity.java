package washbj.uw.tacoma.edu.the_reader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ReadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        this.findViewById(R.id.activity_read).setOnTouchListener(new OnSwipeTouchListener( this.findViewById(R.id.activity_read).getContext()) {
            @Override
            public void onSwipeLeft() {
                // Use to go to next page
                Log.w("SWIPE", "Swiped Left");
            }

            @Override
            public void onSwipeRight() {
                // Use to go to next page
                Log.w("SWIPE", "Swiped Right");
            }
        });



    }
}
