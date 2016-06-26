package si.trina.tappydefender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by trinch on 6/21/2016.
 */
public class EnemyShip {
    private Bitmap bitmap;
    private int x, y;
    private int speed = 1;

    private int maxX;
    private int maxY;
    private int minX;
    private int minY;
    private Rect hitbox;

    // getter methods so that the draw method knows where and what to draw
    public int getX() {
        return x;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getY() {
        return y;
    }
    public Rect getHitbox() {
        return hitbox;
    }

    // setters
    public void setX(int x) {
        this.x = x;
    }

    // constructors

    public EnemyShip(Context context, Point screenSize) {
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.enemy);
        minX = 0;
        minY = 0;
        maxX = screenSize.x;
        maxY = screenSize.y - bitmap.getHeight();

        // random speed
        Random generator = new Random();
        speed = generator.nextInt(6) + 10;

        // random y position
        x = screenSize.x;
        y = generator.nextInt(maxY);
        hitbox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    //

    public void update(int playerSpeed) {
        // move left
        x -= playerSpeed;
        x -= speed;

        // respawn when off screen
        if (x < minX - bitmap.getWidth()) {
            Random generator = new Random();
            speed = generator.nextInt(10) + 10;
            x = maxX;
            y = generator.nextInt(maxY) - bitmap.getHeight();
        }

        hitbox.left = x;
        hitbox.right = x + bitmap.getWidth();
        hitbox.top = y;
        hitbox.bottom = y + bitmap.getHeight();
    }
}
