package si.trina.tappydefender;

import android.content.Context;
import android.graphics.Point;

import java.util.Random;

/**
 * Created by trinch on 6/21/2016.
 */
public class SpaceDust {
    private int x;
    private int y;
    private int speed;

    // bind the dust to the screen
    private int maxX;
    private int maxY;
    private int minX;
    private int minY;

    // constructor
    public SpaceDust(Context context, Point screenSize) {
        maxX = screenSize.x;
        maxY = screenSize.y;
        minX = 0;
        minY = 0;

        // speed between 0 and  9
        Random generator = new Random();
        speed = generator.nextInt(10);

        x = generator.nextInt(maxX);
        y = generator.nextInt(maxY);
    }

    // update method
    public void update(int playerSpeed) {
        x -= playerSpeed;
        x -= speed;

        // respawn when off screen
        if (x < 0) {
            x = maxX;
            Random generator = new Random();
            speed = generator.nextInt(15);
            y = generator.nextInt(maxY);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
