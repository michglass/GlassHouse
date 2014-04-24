package com.michglass.glasshouse.glasshouse;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Oliver
 * Date 3/21/14.
 */
public class TTTGameSurface extends SurfaceView {

    // Debug
    private static final String TAG = "Game Surface";

    // Game Surface Variables
    private Rect mRectangle; // Rectangle that indicates which cell is currently in focus

    // Paint Objects
    private Paint mGridPaint; // Paint for setting styles for Grid
    private Paint mRectPaint; // Paint for setting styles for Rectangle
    private Paint mPlayerSymbPaint; // Paint for the Player Symbols
    private Paint mAISymbPaint; // Paint for AI Symbols

    // Player Variables
    public static final int PLAYER_ID = 1;
    public static final int AI_ID = 2;
    public static final int GAME_OVER = 3;
    public static final int DISABLE_INPUT = 4;
    public static final int ENABLE_INPUT = 5;

    /**
     * Constructor
     *
     */
    public TTTGameSurface(Context context) {
        super(context);
        Log.v(TAG, "Constructor");

        // set up the game rectangle
        mRectangle = new Rect();

        // set up different paint objects
        mGridPaint = initPaint(Color.YELLOW, Paint.Style.STROKE, 3);
        mRectPaint = initPaint(Color.BLUE, Paint.Style.STROKE, 3);
        mPlayerSymbPaint = initPaint(Color.WHITE, Paint.Style.STROKE, 5);
        mAISymbPaint = initPaint(Color.BLACK, Paint.Style.STROKE, 5);
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
     * Init a simple Paint just with color
     * @param color Color of Paint
     * @return Paint Object
     */
    private Paint initPaint(int color) {
        Paint p = new Paint();
        p.setColor(color);
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

            mCanvas.drawLine(mCanvas.getWidth() / 3, 0, mCanvas.getWidth() / 3,
                    mCanvas.getHeight(), mGridPaint);
            mCanvas.drawLine(mCanvas.getWidth() * 2 / 3, 0, mCanvas.getWidth() * 2 / 3,
                    mCanvas.getHeight(), mGridPaint);
            mCanvas.drawLine(0, mCanvas.getHeight() / 3, mCanvas.getWidth(),
                    mCanvas.getHeight() / 3, mGridPaint);
            mCanvas.drawLine(0, mCanvas.getHeight() * 2 / 3, mCanvas.getWidth(),
                    mCanvas.getHeight() * 2 / 3, mGridPaint);
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
            Log.v(TAG, "Draw Rectangle");
            Log.v(TAG, "xS:" + xS + ",xF:"+xF+"/yS:"+yS+",yF:"+yF);
            mCanvas.drawRect(mRectangle, mRectPaint);
        }
    }
    /**
     * Draw Symbols
     * Draws the Player and AI Symbols
     * @param aiCoord Coordinates of the AI Symbols
     * @param playerCoord Coordinates of the Player Symbols
     * @param mCanvas View Background
     */
    public void drawSymbols(List<DrawingLogic.Pair<Integer, Integer>> aiCoord,
                            List<DrawingLogic.Pair<Integer, Integer>> playerCoord, Canvas mCanvas) {

        drawPlayerSymbols(playerCoord, mCanvas);
        drawAISymbols(aiCoord, mCanvas);
    }
    /**
     * Draw Player Symbols
     * @param coord The coordinates of the Symbols
     * @param mCanvas View Background
     */
    private void drawPlayerSymbols(List<DrawingLogic.Pair<Integer, Integer>> coord, Canvas mCanvas) {
        Log.v(TAG, "Draw Circles");
        for(DrawingLogic.Pair<Integer,Integer> p : coord) {
            mCanvas.drawCircle(p.getXVal(),p.getYVal(),20,mPlayerSymbPaint);
        }
    }
    /**
     * Draw AI Symbols
     * @param coord The coordinates of the Symbols
     * @param mCanvas View Background
     */
    private void drawAISymbols(List<DrawingLogic.Pair<Integer, Integer>> coord, Canvas mCanvas){
        Log.v(TAG, "Draw Crosses");
        for(DrawingLogic.Pair<Integer,Integer> p : coord) {
            mCanvas.drawCircle(p.getXVal(),p.getYVal(),20,mAISymbPaint);
        }
    }
}

