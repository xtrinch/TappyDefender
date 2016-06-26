package si.trina.tappydefender;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Space;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by trinch on 6/18/2016.
 */
public class TDView extends SurfaceView implements Runnable {

    private final int NUM_ENEMY_SHIPS = 5;
    private final int NUM_SPECS = 80;
    private final int MAX_DISTANCE_REMAINING = 1000; // 10km
    private final int REFRESH_RATE = 17;

    // screen size
    private Point screenSize;

    // volatile because it will be accessed from outside the thread and within
    volatile boolean playing;
    Thread gameThread = null;

    // Game objects
    private PlayerShip player;
    private ArrayList<EnemyShip> enemyShips;
    private ArrayList<SpaceDust> spaceDust;

    // For drawing
    private Paint paint;
    private Canvas canvas;
    // allows us to lock your Canvas object while we are manipulating it and unlock it when we are ready to draw the frame
    private SurfaceHolder ourHolder;

    // game stats
    private float distanceRemaining;
    private long timeTaken;
    private long timeStarted;
    private long fastestTime;

    private Context context;
    private boolean gameOver;

    // sound fx
    private SoundPool soundPool;
    int start = -1;
    int bump = -1;
    int destroyed = -1;
    int win = -1;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public TDView(Context context, Point screenSize) {
        super(context);
        this.context = context;

        // deprecated soundpool initializer
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor assetFileDescriptor;

            assetFileDescriptor = assetManager.openFd("start.ogg");
            start = soundPool.load(assetFileDescriptor, 0);

            assetFileDescriptor = assetManager.openFd("win.ogg");
            win = soundPool.load(assetFileDescriptor, 0);

            assetFileDescriptor = assetManager.openFd("hit.ogg");
            bump = soundPool.load(assetFileDescriptor, 0);

            assetFileDescriptor = assetManager.openFd("explosion.ogg");
            destroyed = soundPool.load(assetFileDescriptor, 0);
        } catch(IOException e) {
            Log.e("error", "Failed to load sound files with exception:" + e.toString());
        }

        // get a reference to high scores file, create if nonexistant
        prefs = context.getSharedPreferences("HighScores", context.MODE_PRIVATE);
        editor = prefs.edit();

        // load fastest time from file entry labeled "fastestTime"
        fastestTime = prefs.getLong("fastestTime", 1000000);

        this.screenSize = screenSize;

        // Initialize our drawing objects
        ourHolder = getHolder();
        paint = new Paint();

        startGame();
    }

    private void startGame() {
        gameOver = false;
        
        // reset time and distance
        distanceRemaining = MAX_DISTANCE_REMAINING;
        timeTaken = 0;

        // get start time
        timeStarted = System.currentTimeMillis();

        // play start game sound
        soundPool.play(start, 1, 1, 0, 0, 1);

        // Initialize our player ship
        player = new PlayerShip(context, screenSize);

        // Initialize enemies
        enemyShips = new ArrayList<EnemyShip>();
        for (int i=0; i<NUM_ENEMY_SHIPS; i++) {
            enemyShips.add(new EnemyShip(context, screenSize));
        }

        // Initialize space dust
        spaceDust = new ArrayList<SpaceDust>();
        for (int i=0; i<NUM_SPECS; i++) {
            spaceDust.add(new SpaceDust(context, screenSize));
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        player.update();

        // detect hits
        for (EnemyShip enemy:enemyShips) {
            // hit detected
            if (Rect.intersects(player.getHitbox(), enemy.getHitbox())) {
                // let enemy class update function respawn enemy
                enemy.setX(-1000);
                player.reduceShieldStrength();

                if (!gameOver) {
                    // play hit sound
                    soundPool.play(bump, 1, 1, 0, 0, 1);


                    if (player.getShieldStrength() < 0) {
                        // play destroyed sound
                        soundPool.play(destroyed, 1, 1, 0, 0, 1);

                        // game over
                        gameOver = true;
                    }
                }
            }

            enemy.update(player.getSpeed());
        }
        for (SpaceDust spec:spaceDust) {
            spec.update(player.getSpeed());
        }

        if (!gameOver) {
            // subtract distance to home planet
            distanceRemaining -= player.getSpeed();
            timeTaken = System.currentTimeMillis() - timeStarted;
        }

        // player has finished the game
        if (distanceRemaining < 0) {
            gameOver = true;
            soundPool.play(win, 1, 1, 0, 0, 1);

            if (timeTaken < fastestTime) {
                // save high scores
                editor.putLong("fastestTime", timeTaken);
                editor.apply();
                fastestTime = timeTaken;
            }

            distanceRemaining = 0;
        }
    }


    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.argb(255, 0, 0, 0));

            paint.setColor(Color.argb(255, 255, 255, 255));

            for (SpaceDust spec:spaceDust) {
                canvas.drawPoint(spec.getX(), spec.getY(), paint);
            }

            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);
            for (int i=0; i<enemyShips.size(); i++) {
                canvas.drawBitmap(
                        enemyShips.get(i).getBitmap(),
                        enemyShips.get(i).getX(),
                        enemyShips.get(i).getY(),
                        paint);
            }

            // draw HUD (heads up display
            if (!gameOver) {
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setColor(Color.WHITE);
                paint.setTextSize(50);

                // top of the screen
                canvas.drawText(String.format("Fastest: %.2f s", fastestTime/1000.0), 30, 60, paint);
                canvas.drawText(String.format("Time: %.2f s", timeTaken/1000.0), screenSize.x/2, 60, paint);

                // bottom of the screen
                canvas.drawText("Distance: " + distanceRemaining / 1000 + " km", screenSize.x/3, screenSize.y - 20, paint);
                canvas.drawText("Shields: " + player.getShieldStrength(), 30, screenSize.y - 20, paint);
                canvas.drawText("Speed: " + player.getSpeed() + " m/s", 2 * (screenSize.x / 3), screenSize.y - 20, paint);
            } else {
                paint.setTextSize(160);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("GAME OVER", screenSize.x / 2, 280, paint);
                paint.setTextSize(80);
                canvas.drawText(String.format("Fastest: %.2f s", fastestTime/1000.0), screenSize.x / 2, 380, paint);
                canvas.drawText(String.format("Time: %.2f s", timeTaken/1000.0), screenSize.x/2, 480, paint);
                paint.setTextSize(120);
                canvas.drawText("Tap to replay", screenSize.x/2, screenSize.y - 180, paint);
            }
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(REFRESH_RATE);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                // finger off the screen
                player.setBoosting(false);
                break;
            case MotionEvent.ACTION_DOWN:
                // finger on the screen
                player.setBoosting(true);
                // if the screen was tapped while game over
                if (gameOver) {
                    startGame();
                }
                break;
        }
        return true;
    }
}