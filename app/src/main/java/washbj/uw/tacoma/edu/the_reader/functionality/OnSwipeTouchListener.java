package washbj.uw.tacoma.edu.the_reader.functionality;
/**
 * @Author Justin Washburn on 2/2/2017.
 * Utilizes code samples from Edward Brey on StackOverflow and the Android API
 */

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects left and right swipes across a view
 */
public class OnSwipeTouchListener implements View.OnTouchListener {
    /**
     * Detects Swiping gestures
     */
    private final GestureDetector gestureDetector;

    /**
     * A default constructor
     * @param context the view
     */
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    /**
     * Method to be overriden for gestures
     */
    public void onSwipeLeft() {}

    /**
     * Method to be overriden for gestures
     */
    public void onSwipeRight() {}


    /**
     * A default method to register touches
     *
     * @param v the view
     * @param event the event of being touched
     * @return a boolean signigying being touched
     */
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * The logic class for swipe/fling motions
     */
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * An int that represents how far the finger must slide across the screen
         */
        private static final int SWIPE_DISTANCE_THRESHOLD = 150;

        /**
         * An int that represents how fast the finger must slide across the screen
         */
        private static final int SWIPE_VELOCITY_THRESHOLD = 150;

        /**
         * A method that returns true since downward movement is irrelevant for page flipping
         * @param e the touch event
         * @return always true
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * The logic method to determine if a swipe has occured
         * @param e1 The start of the touch
         * @param e2 The end of the touch
         * @param velocityX The velocity of horizontal movement
         * @param velocityY The velocity of vertical movement
         * @return true if the action qualified as a swipe
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }
    }
}