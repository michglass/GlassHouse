package com.michglass.glasshouse.glasshouse;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver
 * Date 3/21/14.
 */
public class DrawingLogic {

    // Debug
    private static final String TAG = "Drawing Logic";

    // Game View Variables
    private Canvas mCanvas; // Background on which Game field and objects are drawn
    private SurfaceHolder mSurfaceHolder; // manages Canvas
    private TTTGameSurface mGameSurface;

    // Thread Var
    private DrawGameThread mGameThread;
    private boolean mKeepUpdatingGame; // indicates if game keeps updating
    private AIThread mAIThread;
    private FillCellThread mFillCellThread;

    // Store values for AI/Player symbol coordinates
    private List<Pair<Integer, Integer>> aiValues = new ArrayList<Pair<Integer, Integer>>();
    private List<Pair<Integer, Integer>> playerValues = new ArrayList<Pair<Integer, Integer>>();
    private boolean GAME_OVER;

    // Distance between 2 cells
    private final int XGap = 213;
    private final int YGap = 120;

    // Game Matrix for storing moves and checking winning condition
    private char[][] mGameMatrix = new char[3][3];
    private final char PLAYER = 'P';
    private final char AI = 'A';
    private List<Pair<Integer, Integer>> mOpenCells;

    // X/Y coordinates for indicating what cell currently is highlighted
    private int currX;
    private int currY;

    // Field Dimensions (Dimensions of a Card)
    private final int FIELD_WIDTH = 640;
    private final int FIELD_HEIGHT = 360;

    // Handler for indicating when game is over
    private final Handler handler;


    /**
     * Constructor
     */

    /**
     * Drawing Logic
     * Set up the Game Surface and Holder
     * Initialize the Open Cells
     * @param gameSurface Game Surface
     * @param gameHandler Handler for contacting Activity when game is over
     */
    public DrawingLogic(TTTGameSurface gameSurface, Handler gameHandler) {
        Log.v(TAG, "Constructor");

        handler = gameHandler;
        mGameSurface = gameSurface;
        mSurfaceHolder = gameSurface.getHolder();

        // set up open cells
        initOpenCells();
        // Debug
        printOpenCells();
    }

    /**
     * Methods that Handle the Threads
     * Start and Stop Threads
     */

    /**
     * Start Game Thread
     * Start Drawing the playing field
     */
    private void startGameThread() {
        Log.v(TAG, "Start Game Thread");

        mKeepUpdatingGame = true;
        mGameThread = new DrawGameThread();
        mGameThread.start();
        sendMessageToActivity(TTTGameSurface.ENABLE_INPUT);
    }
    /**
     * Resume Game Thread
     * Start Drawing Playing field and move the rectangle again
     */
    private void resumeGameThread() {
        Log.v(TAG, "Resume Game Thread");
        mKeepUpdatingGame = true;
        mGameThread = new DrawGameThread();
        mGameThread.start();
        sendMessageToActivity(TTTGameSurface.ENABLE_INPUT);
    }
    /**
     * Stop Game Thread
     * Stop drawing the playing field
     */
    private void stopGameThread() {
        Log.v(TAG, "Pause Game Thread");

        mKeepUpdatingGame = false;
        mGameThread = null;
        sendMessageToActivity(TTTGameSurface.DISABLE_INPUT);
    }

    /**
     * Fill Cell
     * Starts the Fill Cell Thread
     * @param id Indicates if Player or AI are making the move
     * @param x X Coord of Cell
     * @param y Y Coord of Cell
     */
    private void fillCell(int id, int x, int y) {
        Log.v(TAG, "Fill Cell");
        if(id == TTTGameSurface.PLAYER_ID) {
            stopGameThread();
        }
        mFillCellThread = new FillCellThread(id, x, y);
        mFillCellThread.start();
    }
    /**
     * Stop Fill Cell Thread
     */
    private void stopFillCellThread() {
        if(mFillCellThread != null) {
            Log.v(TAG, "Stop Fill Cell");
            mFillCellThread.setFillThreadBool(false);
            mFillCellThread = null;
        }
    }

    /**
     * AI Move
     * Start the AI Thread
     */
    private void aiMove() {
        mAIThread = new AIThread();
        mAIThread.start();
    }
    /**
     * Stop AI Thread
     */
    private void stopAIThread() {
        if(mAIThread != null) {
            mAIThread.setRunning(false);
            mAIThread.setThisRunning(false);
            mAIThread = null;
        }
    }

    // Start updating the Game field
    public void updateGame() {
        Log.v(TAG, "Update Game");
        startGameThread();
    }
    // Pause the Game
    public void pauseGame() {
        Log.v(TAG, "Pause Game");
        stopGameThread();
    }
    // make a move
    public void makeMove(int id) {
        Log.v(TAG, "Make Move");
        sendMessageToActivity(TTTGameSurface.DISABLE_INPUT);
        fillCell(id,getCurrX(),getCurrY());

        aiMove();
    }

    /**
     * AI Thread
     * Thread that makes a move after player made a move
     */
    private class AIThread extends Thread {

        // Debug
        private final String TAG = "AI Thread";

        // coord
        private int xCoord;
        private int yCoord;

        // bool
        private boolean mKeepRunning;
        private boolean keepThisRunning;

        /**
         * Constructor
         */
        public AIThread() {
            xCoord = 107;
            yCoord = 60;
            mKeepRunning = true;
            keepThisRunning = true;
        }

        @Override
        public void run() {
            Log.v(TAG, "Run");

            if(keepThisRunning) {
                try {
                    sleep(1000);
                } catch (InterruptedException intE) {
                    Log.e(TAG, "Thread Interrupted");
                }
                if(mOpenCells.size() != 0) {
                    xCoord += mOpenCells.get(0).getXVal();
                    yCoord += mOpenCells.get(0).getYVal();
                    Log.v(TAG, "X: " + xCoord + ",Y: " + yCoord);
                    fillCell(TTTGameSurface.AI_ID, xCoord + 106, yCoord + 60);
                    try {
                        sleep(2000);
                    } catch (InterruptedException intE) {
                        Log.e(TAG, "AI Thread Interrupted", intE);
                    }
                    if (mKeepRunning) {
                        printGameMatrix();
                        resumeGameThread();
                    }
                } else {
                    Log.e(TAG, "Draw after User Input");
                    sendMessageToActivity(TTTGameSurface.GAME_OVER);
                }
            }
            Log.v(TAG, "Run Return");
        }
        public void setRunning(boolean b) {
            this.mKeepRunning = b;
        }
        public void setThisRunning(boolean b) {
            this.keepThisRunning = b;
        }
    }
    /**
     * Fill Cell Thread
     * When player/AI makes a move this thread
     * fills a cell with the appropriate symbol
     */
    private class FillCellThread extends Thread {

        // Debug
        private static final String TAG = "Fill Cell Thread";

        // ID that determines who made the move
        private int mID;
        private boolean mKeepRunning;

        // coord for symbol
        private int xCent;
        private int yCent;

        /**
         * Constructor
         */
        public FillCellThread(int id, int x, int y) {
            xCent = x - 106;
            yCent = y - 60;
            mID = id;
            mKeepRunning = true;
        }

        @Override
        public void run() {
            Log.v(TAG, "Run");

            if(!GAME_OVER) {
                while (mKeepRunning) {

                    if (!mSurfaceHolder.getSurface().isValid())
                        continue;

                    mCanvas = mSurfaceHolder.lockCanvas();
                    mGameSurface.drawField(mCanvas);
                    Log.v(TAG, "XCent: " + xCent + ", YCent: " + yCent);
                    if (mKeepRunning) {
                        if (mID == TTTGameSurface.PLAYER_ID) {
                            playerValues.add(new Pair<Integer, Integer>(xCent, yCent));
                            mGameSurface.drawSymbols(aiValues, playerValues, mCanvas);
                            addToMatrix(xCent - 107, yCent - 60, TTTGameSurface.PLAYER_ID);
                            deleteFromOpenCells(xCent - 107, yCent - 60);
                            printOpenCells();

                            if (checkWin(TTTGameSurface.PLAYER_ID)) {
                                GAME_OVER = true;
                                Log.v(TAG, "Player wins");
                                Paint p = new Paint();
                                p.setTextSize(100f);
                                p.setColor(Color.BLUE);
                                p.setTextAlign(Paint.Align.CENTER);
                                mCanvas.drawText("You Win!", FIELD_WIDTH/2,
                                        (FIELD_HEIGHT / 2) - ((p.descent() + p.ascent()) / 2), p);
                                stopGameThread();
                                stopAIThread();
                                stopFillCellThread();
                                sendMessageToActivity(TTTGameSurface.GAME_OVER);
                            }
                        }
                    }
                    if (mKeepRunning) {
                        if (mID == TTTGameSurface.AI_ID) {
                            aiValues.add(new Pair<Integer, Integer>(xCent, yCent));
                            mGameSurface.drawSymbols(aiValues, playerValues, mCanvas);
                            addToMatrix(xCent - 107, yCent - 60, TTTGameSurface.AI_ID);
                            deleteFromOpenCells(xCent - 107, yCent - 60);
                            printOpenCells();

                            if (checkWin(TTTGameSurface.AI_ID)) {
                                GAME_OVER = true;
                                Log.v(TAG, "AI Wins");
                                Paint p = new Paint();
                                p.setTextSize(100f);
                                p.setColor(Color.BLUE);
                                p.setTextAlign(Paint.Align.CENTER);
                                mCanvas.drawText("Glass Wins!", FIELD_WIDTH/2,
                                        (FIELD_HEIGHT / 2) - ((p.descent() + p.ascent()) / 2), p);
                                stopGameThread();
                                stopAIThread();
                                stopFillCellThread();
                                sendMessageToActivity(TTTGameSurface.GAME_OVER);
                            }
                        }
                    }
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);

                    mKeepRunning = false;
                }
            }
            Log.v(TAG, "Run Return");
        }
        public void setFillThreadBool(boolean b) {
            this.mKeepRunning = b;
        }
    }
    /**
     * Draw Game Thread
     * Updates the game field every n - seconds
     * Moves the Rectangle one Cell further
     */
    private class DrawGameThread extends Thread {

        // Debug
        private static final String TAG = "Draw Game Thread";

        /**
         * Constructor
         */
        public DrawGameThread() {
            GAME_OVER = false;
            Log.v(TAG, "Constructor");
        }

        @Override
        public void run() {
            Log.v(TAG, "Run");
            int x;
            int y;
            int count = 0;
            while (mKeepUpdatingGame) {

                if(!mSurfaceHolder.getSurface().isValid())
                    continue;

                mCanvas = mSurfaceHolder.lockCanvas();

                if(mKeepUpdatingGame) {
                    if(mOpenCells.size() == 0) {
                        stopAIThread();
                        stopFillCellThread();
                        startGameThread();
                        Log.e(TAG, "Draw after AI moves");
                        sendMessageToActivity(TTTGameSurface.GAME_OVER);
                    } else {
                        x = mOpenCells.get(count).getXVal();
                        y = mOpenCells.get(count).getYVal();
                        currX = x + XGap;
                        currY = y + YGap;
                        mGameSurface.drawField(mCanvas);
                        mGameSurface.drawSymbols(aiValues, playerValues, mCanvas);
                        mGameSurface.drawRectangle(mCanvas, x, y, currX, currY);
                    }
                } else {break;}
                if(mCanvas != null && mKeepUpdatingGame) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                    try {
                        Log.v(TAG, "Delay Game");
                        sleep(2000);
                    } catch (InterruptedException intE) {
                        Log.e(TAG, "Game Thread interrupted");
                    }
                } else {break;}

                if(count == mOpenCells.size()-1)
                    count = 0;
                else
                    count++;
            }
            Log.v(TAG, "Run Return");
        }
    }

    /**
     * Utility Methods
     * Open Cells methods: init all open cells, delete from open cells, print open cells
     * Game Matrix methods: add to game matrix, check winning condition, print
     * Get Coord methods: Get the X/Y values for the currently highlighted cell
     * Pair Class: Class for storing coordinate pairs
     */

    /**
     * Init Open Cells
     * Initialize the open cells of the game matrix
     */
    private void initOpenCells() {
        mOpenCells = new ArrayList<Pair<Integer, Integer>>();
        for(int j=0; j<=240; j+=YGap) {
            for(int i=0; i<=426; i+=XGap) {
                Pair<Integer,Integer> pair = new Pair<Integer, Integer>(i,j);
                mOpenCells.add(pair);
            }
        }
    }
    /**
     * Delete From Open Cells
     * If someone makes a move, delete cell that got filled from open cells
     * @param x X Coord
     * @param y Y Coord
     */
    private void deleteFromOpenCells(int x, int y) {
        Log.v(TAG, "Delete From Open Cells");
        for(int i=0; i<mOpenCells.size(); i++) {
            int xCurr = mOpenCells.get(i).getXVal();
            int yCurr = mOpenCells.get(i).getYVal();
            if(x == xCurr && y == yCurr)
                mOpenCells.remove(i);
        }
    }
    /**
     * Print Open Cells (for Debugging)
     */
    private void printOpenCells() {
        for(Pair p : mOpenCells) {
            Log.v(TAG, ""+p.getXVal()+","+p.getYVal()+"/");
        }
    }

    /**
     * Add To Matrix
     * If Player or AI makes a move, store it in the Game Matrix
     * @param xStart X Coord (in Card Coordinates)
     * @param yStart Y Coord (in Card Coordinates)
     * @param id Add 'P' for Player or 'A' for AI
     */
    private void addToMatrix(int xStart, int yStart, int id) {
        int row = 0;
        int column = 0;

        switch (xStart) {
            case 0:
                column = 0;
                break;
            case 213:
                column = 1;
                break;
            case 426:
                column = 2;
                break;
        }
        switch (yStart) {
            case 0:
                row = 0;
                break;
            case 120:
                row = 1;
                break;
            case 240:
                row = 2;
                break;
        }
        if(id == TTTGameSurface.PLAYER_ID)
            mGameMatrix[row][column] = PLAYER;
        if(id == TTTGameSurface.AI_ID)
            mGameMatrix[row][column] = AI;
    }
    /**
     * Check Win
     * Checks winning condition in the Game Matrix
     * @param id Check Win for Player or AI
     * @return Boolean if 3 symbols in a row, column or diag
     */
    private boolean checkWin(int id) {

        for(int i=0; i<3; i++)
            if(checkRowWin(id, i))
                return true;
        for(int j=0; j<3; j++)
            if(checkColumnWin(id, j))
                return true;
        for(int c = 0; c < 2; c++)
            if(checkDiagWin(id, c))
                return true;
        return false;
    }
    private boolean checkRowWin(int id, int row) {
        char c;
        if (id == TTTGameSurface.PLAYER_ID) c = PLAYER;
        else c = AI;

        for (int j = 0; j < 3; j++) {
            if (c != mGameMatrix[row][j])
                return false;
        }
        return true;
    }
    private boolean checkColumnWin(int id, int col) {
        char c;
        if (id == TTTGameSurface.PLAYER_ID) c = PLAYER;
        else c = AI;

        for (int i = 0; i < 3; i++) {
            if (c != mGameMatrix[i][col])
                return false;
        }
        return true;
    }
    private boolean checkDiagWin(int id, int diag) {
        char c;
        if (id == TTTGameSurface.PLAYER_ID) c = PLAYER;
        else c = AI;
        int row;
        int column;
        if(diag == 1) {
            row = 0;
            column = 0;
        } else {
            row = 0;
            column = 2;
        }

        for(int count=0; count<3; count++) {
            if(mGameMatrix[row][column] != c)
                return false;
            else {
                if(diag == 1) {
                    row++;
                    column++;
                } else {
                    row++;
                    column--;
                }
            }
        }
        return true;
    }
    /**
     * Print the Game Matrix (for Debugging)
     */
    private void printGameMatrix() {
        for(int i=0; i<3; i++) {
            Log.v(TAG, mGameMatrix[i][0] + ","+mGameMatrix[i][1]+","+mGameMatrix[i][2]);
        }
    }
    /**
     * Get X
     * Get the x-value for the currently highlighted cell
     * @return X Coord
     */
    private int getCurrX() {
        Log.v(TAG, "Get X");
        return currX;
    }
    /**
     * Get Y
     * Get the y-value for the currently highlighted cell
     * @return Y Coord
     */
    private  int getCurrY() {
        Log.v(TAG, "Get Y");
        return currY;
    }
    /**
     * Pair
     * Class that holds X,Y Coordinates
     * Used to draw field, rectangle, game symbols
     * @param <XVal> X Coord
     * @param <YVal> Y Coord
     */
    public class Pair<XVal,YVal> {
        private XVal l;
        private YVal r;
        public Pair(XVal l, YVal r){
            this.l = l;
            this.r = r;
        }
        public XVal getXVal(){ return l; }
        public YVal getYVal(){ return r; }
        public void setXVal(XVal l){ this.l = l; }
        public void setYVal(YVal r){ this.r = r; }
    }
    /**
     * Send Message to Activity indicating that the game is over
     * @param m Message (game over message)
     */
    private void sendMessageToActivity(int m) {
        Message msg = new Message();
        msg.what = m;
        handler.sendMessage(msg);
    }
}

