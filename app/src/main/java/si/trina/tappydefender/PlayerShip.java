package si.trina.tappydefender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by trinch on 6/18/2016.
 */
public class PlayerShip {
    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;
    private final int GRAVITY = -12;
    private final int NUM_SHIELDS = 10;

    private Bitmap bitmap;
    private boolean boosting;
    private int x, y;
    private int speed = 0;
    private int maxY;
    private int minY;
    private Rect hitbox;
    private int shieldStrength;

    public PlayerShip(Context context, Point screenSize) {
        x = 50;
        y = 50;
        bitmap = BitmapFactory.decodeResource
                (context.getResources(), R.drawable.ship);
        maxY = screenSize.y - bitmap.getHeight();
        minY = 0;

        speed = 1;
        boosting = false;
        hitbox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
        shieldStrength = NUM_SHIELDS;
    }

    public void update() {
        //y=100;

        if (boosting) {
            speed += 2;
        } else {
            speed -= 5;
        }
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }
        if (speed < MIN_SPEED) {
            speed = MIN_SPEED;
        }
        y -= speed + GRAVITY;

        if (y < minY) {
            y = minY;
        }
        if (y > maxY) {
            y = maxY;
        }

        hitbox.left = x;
        hitbox.right = x + bitmap.getWidth();
        hitbox.top = y;
        hitbox.bottom = y + bitmap.getHeight();
    }

    public void reduceShieldStrength() {
        shieldStrength--;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getSpeed() {
        return speed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setBoosting(boolean boosting) {
        this.boosting = boosting;
    }

    public Rect getHitbox() { return hitbox; }

    public int getShieldStrength() {
        return shieldStrength;
    }

}