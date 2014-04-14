package com.michglass.glasshouse.glasshouse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Oliver on 4/9/2014.
 */
public class SpellingGameSurface extends SurfaceView {

    // Debug
    private static final String TAG = "Game Surface";

    // Game Surface Variables
    private Rect mRectangle; // Rectangle that indicates which cell is currently in focus

    // Paint Objects
    private Paint mRectPaint; // Paint for setting styles for Rectangle
    private Paint mTextPaint;
    private Rect mShuffledContainer;
    private Rect mSpelledContainer;
    private Paint mShuffledContPaint;
    private Paint mSpelledContPaint;

    // Player Variables
    public static final int DISABLE_INPUT = 1;
    public static final int ENABLE_INPUT = 2;
    public static final int GAME_OVER = 3;

    // Text Size for word that is being displayed
    private static final float TEXT_SIZE = 60f;
    private static final int CONT_DIST = 150;

    // field dimensions == Dimensions of a Card
    private final int FIELD_WIDTH = 640;
    private final int FIELD_HEIGHT = 360;

    // word container coordinates
    private int CONTAINER_OFFSETX;
    private int CONTAINER_OFFSETY;

    /**
     * Constructor
     *
     */
    public SpellingGameSurface(Context context) {
        super(context);
        Log.v(TAG, "Constructor");

        // set up the game rectangle
        mRectangle = new Rect();

        // set up different paint objects
        mRectPaint = initPaint(Color.BLUE, Paint.Style.STROKE, 5);
        mShuffledContPaint = initPaint(Color.GREEN, Paint.Style.STROKE, 5);
        mSpelledContPaint = initPaint(Color.GREEN, Paint.Style.STROKE, 5);
        mTextPaint = initPaint(Color.RED, TEXT_SIZE);
        mShuffledContainer = new Rect();
        mSpelledContainer = new Rect();
    }

    /**
     * Utility Functions
     * Init Paint: Set Paint for different things (canvas, rectangle, circle)
     * Get Rectangle: Get a Rectangle
     */

    /**
     * Init Paint
     * Set up a Paint Object
     * @param color Set color of Paint
     * @param style Set style of Paint
     * @param strokeWidth stroke Width
     * @return Paint
     */
    private Paint initPaint(int color, Paint.Style style, int strokeWidth) {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(style);
        p.setStrokeWidth(strokeWidth);
        return p;
    }
    /**
     * Init Paint
     * Init a Paint Object with color and text size
     * @param color Color for paint
     * @param textsize Text size
     *
     */
    private Paint initPaint(int color, float textsize) {
        Paint p = new Paint();
        p.setColor(color);
        p.setTextSize(textsize);
        p.setTextAlign(Paint.Align.CENTER);
        return p;
    }
    /**
     * Init Paint
     * Initialize a Paint Object for a text
     */
    private Paint initPaint(Paint.Style s, int color, Paint.Align a, float textSize) {
        Paint p = new Paint();

        p.setStyle(s);
        p.setColor(color);
        p.setTextAlign(a);
        p.setTextSize(textSize);

        return p;
    }

    /**
     * Drawing Methods
     * Draw Field: Draws the TicTacToe Grid
     * Draw Rectangle: Highlight the current game cell with a rectangle
     * Draw Symbols: Draw the Game Symbols (green and black circles)
     * Draw Game Over: Draws a text indicating that the game is over
     */

    /**
     * Draw Field
     * Draws the Game Field
     * @param mCanvas Background on which game field is drawn
     */
    public void drawField(Canvas mCanvas) {

        if(mCanvas != null) {
            Log.v(TAG, "Draw Game Field");
            mCanvas.drawColor(Color.DKGRAY);
        }
    }

    /**
     * Draw Text on Canvas
     */
    public void drawText(ArrayList<Letter> word, Canvas canvas, int type) {
        if(word.size() > 0) {
            for (Letter l : word) {
                drawLetter(canvas, l, type);
            }
        }
    }
    private void drawLetter(Canvas canvas, Letter letter, int type) {
        if (canvas != null) {
            int x = 0;
            int y = 0;
            if(type == SpellLogic.SHUFFLE) {
                x = letter.getLetterShuffX();
                y = letter.getLetterShuffY();
            } else if(type == SpellLogic.SPELLED) {
                x = letter.getLetterSpellX();
                y = letter.getLetterSpellY();
            }
            canvas.drawText(
                    letter.getLetter(),
                    x,
                    y,
                    mTextPaint);
        }
    }

    /**
     * Draw Rectangle
     * Highlight the current cell with a rectangle
     * @param mCanvas View Background
     * @param xS X Start coord
     * @param yS Y Start coord
     * @param xF X Stop coord
     * @param yF Y Stop coord
     */
    public void drawRectangle(Canvas mCanvas, int xS, int yS, int xF, int yF) {
        mRectangle.set(xS, yS, xF, yF);
        if(mCanvas != null) {
            mCanvas.drawRect(mRectangle, mRectPaint);
        }
    }
    // draw rectangle that encapsulates the whole word
    public void drawShuffledContainer (Canvas canvas) {

        if(canvas != null) {
            canvas.drawRect(mShuffledContainer, mShuffledContPaint);
        }
        Log.v(TAG, "Container L/T: " + mShuffledContainer.left + "/"+mShuffledContainer.top);
    }
    // draw rectangle that encapsulates the whole word
    public void drawSpelledContainer (Canvas canvas) {

        if(canvas != null) {
            canvas.drawRect(mSpelledContainer, mSpelledContPaint);
        }
        Log.v(TAG, "Container L/T: " + mSpelledContainer.left + "/"+mSpelledContainer.top);
    }
    // draw word bounds
    public void drawTextBounds(ArrayList<Letter> word, Canvas canvas) {

        for(Letter l : word) {
            int xs = l.getLetterShuffX();
            int ys = l.getLetterShuffY();
            canvas.drawLine(xs,0,xs,FIELD_HEIGHT,new Paint());
            canvas.drawLine(0,ys,FIELD_WIDTH,ys,new Paint());
        }
    }

    /**
     * Get the width and height of a word
     */
    public void getWordDim(String word, int[] ary) {
        mTextPaint.getTextBounds(word, 0, word.length(), mShuffledContainer);
        mTextPaint.getTextBounds(word,0,word.length(),mSpelledContainer);
        scaleContainer(mShuffledContainer);
        scaleContainer(mSpelledContainer);
        CONTAINER_OFFSETX = (FIELD_WIDTH/2 - (mShuffledContainer.width()/2));
        CONTAINER_OFFSETY = ((FIELD_HEIGHT/2 + (mShuffledContainer.height()/2)));
        CONTAINER_OFFSETY = CONTAINER_OFFSETY/2;
        ary[0] = mShuffledContainer.width();
        ary[1] = mShuffledContainer.height();
        ary[2] = mSpelledContainer.width();
        ary[3] = mSpelledContainer.height();
        mShuffledContainer.offset(CONTAINER_OFFSETX, CONTAINER_OFFSETY);
        mSpelledContainer.offset(CONTAINER_OFFSETX, CONTAINER_OFFSETY + CONT_DIST);
        Log.v(TAG, "Container dX/dY: "+CONTAINER_OFFSETX+"/"+CONTAINER_OFFSETY);
    }

    /**
     * Get Dimensions of word container
     */
    public void getContainerCoord(int[] ary) {
        ary[0] = mShuffledContainer.left;
        ary[1] = mShuffledContainer.right;
        ary[2] = mShuffledContainer.top;
        ary[3] = mShuffledContainer.bottom;
        ary[4] = mSpelledContainer.top;
        ary[5] = mSpelledContainer.bottom;
        Log.v(TAG, "Bott-Bott:" + (FIELD_HEIGHT-mSpelledContainer.bottom));
    }
    /**
     * Scale the container size
     */
    private void scaleContainer(Rect container) {
        container.set(container.left-40,container.top-20,container.right+40,container.bottom+20);
    }
    // draw game over
    public void drawGameOver(String spelledWord, boolean isWon, Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        int color;
        if(isWon)
            color = Color.GREEN;
        else
            color = Color.RED;
        Paint p = initPaint(Paint.Style.FILL, color, Paint.Align.CENTER, 100f);
        canvas.drawText(spelledWord, canvas.getWidth()/2,
                (canvas.getHeight() / 2) - ((p.descent() + p.ascent()) / 2),p);
    }
}
