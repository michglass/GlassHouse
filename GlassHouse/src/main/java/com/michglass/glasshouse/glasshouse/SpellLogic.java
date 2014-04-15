package com.michglass.glasshouse.glasshouse;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;

/**
 * Created by Oliver
 * Date: 4/9/2014.
 */
public class SpellLogic {

    // Debug
    private static final String TAG = "Spell Logic";

    // Game Surface vars
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private SpellingGameSurface mGameSurface;

    // Words for this game
    private ArrayList<String> mGameWords;
    private String gameWord;
    private String resultWord;

    // word width and height
    private final int SHUFFLED_CONT_WIDTH;
    private final int SHUFFLED_CONT_HEIGHT;

    // word container coord
    private int SHUFFLED_CONT_LEFT;
    private int SHUFFLED_CONT_RIGHT;
    private int SHUFFLED_CONT_TOP;
    private int SHUFFLED_CONT_BOTTOM;
    private int SPELLED_CONT_TOP;

    // letter rectangle width and height
    private int lRectWidth;
    private int lRectHeight;

    // list of shuffled, spelled letters
    private ArrayList<Letter> shuffledWord;
    private ArrayList<Letter> spelledWord;

    // current letter
    private int currentIndex;
    private boolean inShuffle;

    // types
    public static final int SHUFFLE = 1;
    public static final int SPELLED = 2;

    // Game thread
    private GameThread mGameThread;
    private SelectThread mSelectThread;

    // game handler
    private Handler mGameHandler;

    // Constructor
    public SpellLogic(SpellingGameSurface gameSurface, Handler gameHandler) {

        Log.v(TAG, "Constructor");
        mSurfaceHolder = gameSurface.getHolder();
        mGameSurface = gameSurface;

        // init words
        mGameWords = new ArrayList<String>();
        mGameWords.add("cat");
        mGameWords.add("dog");
        mGameWords.add("submarine");
        mGameWords.add("music");
        mGameWords.add("ab");
        mGameWords.add("nice");
        mGameWords.add("a");

        // get the current word
        if(SpellingGameActivity.GAME_NUMBER == mGameWords.size())
            SpellingGameActivity.GAME_NUMBER = 0;
        Log.v(TAG, "Game Number: " + SpellingGameActivity.GAME_NUMBER);
        gameWord = mGameWords.get(SpellingGameActivity.GAME_NUMBER);

        // set up game handler
        mGameHandler = gameHandler;

        int[] ary = new int[4];
        // get the width and height from a word
        mGameSurface.getWordDim(gameWord, ary);
        SHUFFLED_CONT_WIDTH = ary[0];
        SHUFFLED_CONT_HEIGHT = ary[1];
        Log.v(TAG, "Word W/H: " + SHUFFLED_CONT_WIDTH + "/" + SHUFFLED_CONT_HEIGHT);

        // get the dimensions for the container holding the scrambled word
        int[] contCoord = new int[6];
        mGameSurface.getContainerCoord(contCoord);
        SHUFFLED_CONT_LEFT = contCoord[0];
        SHUFFLED_CONT_RIGHT = contCoord[1];
        SHUFFLED_CONT_TOP = contCoord[2];
        SHUFFLED_CONT_BOTTOM = contCoord[3];
        SPELLED_CONT_TOP = contCoord[4];
        Log.v(TAG, "Container L/R: "+SHUFFLED_CONT_LEFT+"/"+SHUFFLED_CONT_RIGHT);
        Log.v(TAG, "Container B/T: "+SHUFFLED_CONT_BOTTOM+"/"+SHUFFLED_CONT_TOP);

        // initialize the dimensions of the rectangle that scrolls over the word
        initletterRectDim();

        // initialize the shuffled word
        initShuffledWord();
        spelledWord = new ArrayList<Letter>();

        // result word is empty
        resultWord = "";
    }

    // initiate the dimensions for the letter rectangle
    private void initletterRectDim() {
        lRectWidth = (SHUFFLED_CONT_RIGHT - SHUFFLED_CONT_LEFT)/gameWord.length();
        lRectHeight = SHUFFLED_CONT_BOTTOM - SHUFFLED_CONT_TOP;
        Log.v(TAG, "letterRect W/H: "+lRectWidth+"/"+lRectHeight);
    }
    // init the shuffled word list
    private void initShuffledWord() {
        int xs = SHUFFLED_CONT_LEFT;
        int ys = SHUFFLED_CONT_TOP;
        int yf = ys + lRectHeight;
        shuffledWord = new ArrayList<Letter>();
        for(int i=0; i<gameWord.length(); i++) {
            Letter l = new Letter(String.valueOf(gameWord.charAt(i)),
                    xs,xs+lRectWidth,ys,yf);
            l.setLetterShuffX(l.getLetterRectXf()-(lRectWidth/2));
            l.setLetterShuffY(l.getLetterRectYf()-(lRectHeight/4));
            Log.v(TAG, "Letter Rect W/2;H/2: "+lRectWidth/2+"/"+lRectHeight/2);
            Log.v(TAG, "Letter X/Y: "+l.getLetterShuffX()+"/"+l.getLetterShuffY());
            xs += lRectWidth;
            shuffledWord.add(i, l);
        }
    }
    // check win condition
    private boolean checkWinCondition() {
        for(Letter l : spelledWord)
            resultWord = resultWord.concat(l.getLetter());

        Log.v(TAG, "Spelled Word: " + resultWord);
        return gameWord.compareTo(resultWord) == 0;
    }
    // game over dialog
    private void gameOverDialog(boolean isGameWon) {
        boolean b = true;
        while(b) {
            if(!mSurfaceHolder.getSurface().isValid())
                continue;
            mCanvas = mSurfaceHolder.lockCanvas();
            Log.v(TAG, "Draw");
            mGameSurface.drawGameOver(resultWord, isGameWon, mCanvas);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            b = false;
        }
        stopGame();
    }
    // start game
    public void startGame() {
        mGameThread = new GameThread();
        mGameThread.start();
        sendMessage(SpellingGameSurface.ENABLE_INPUT);
    }
    // stop game
    public void stopGame() {
        if(mGameThread != null) {
            mGameThread.stopGameThread();
            mGameThread = null;
        }
    }
    // draw all elements of the game
    private void drawGame() {
        if(mCanvas != null) {
            mGameSurface.drawField(mCanvas);
            //mGameSurface.drawShuffledContainer(mCanvas);
            mGameSurface.drawSpelledContainer(mCanvas);
            mGameSurface.drawText(shuffledWord, mCanvas, SHUFFLE);
            mGameSurface.drawText(spelledWord, mCanvas, SPELLED);
            //mGameSurface.drawTextBounds(shuffledWord, mCanvas);
        }
    }
    // update coordinates of a letter
    private void updateCoord(Letter l, ArrayList<Letter> newList, int type) {
        if(newList.size() == 0) {
            l.setLetterRectXs(SHUFFLED_CONT_LEFT);
            l.setLetterRectXf(l.getLetterRectXs()+lRectWidth);
            if(type == SHUFFLE) {
                l.setLetterRectYs(SHUFFLED_CONT_TOP);
                l.setLetterRectYf(l.getLetterRectYs()+lRectHeight);
            }
            else if(type == SPELLED) {
                l.setLetterRectYs(SPELLED_CONT_TOP);
                l.setLetterRectYf(l.getLetterRectYs()+lRectHeight);
                l.setLetterSpellX(l.getLetterRectXf() - (lRectWidth / 2));
                l.setLetterSpellY(l.getLetterRectYf() - (lRectHeight / 4));
            }
        } else {

            if(type == SPELLED) {
                Letter prevL = newList.get(newList.size() - 1);
                l.setLetterRectXs(prevL.getLetterRectXf());
                l.setLetterRectXf(l.getLetterRectXs() + lRectWidth);
                l.setLetterRectYs(prevL.getLetterRectYs());
                l.setLetterRectYf(prevL.getLetterRectYf());

                l.setLetterSpellX(l.getLetterRectXf() - (lRectWidth / 2));
                l.setLetterSpellY(l.getLetterRectYf() - (lRectHeight / 4));
            } else if(type == SHUFFLE) {
                l.setLetterRectXs(l.getLetterShuffX()-(lRectWidth/2));
                l.setLetterRectXf(l.getLetterRectXs() + lRectWidth);
                l.setLetterRectYs(l.getLetterShuffY() - (lRectHeight*3/4));
                l.setLetterRectYf(l.getLetterRectYs() + lRectHeight);
            }
        }
    }
    // shift letters on the right of l to the left
    private void shiftAdjLetters(Letter selectedLetter, ArrayList<Letter> letters, int index) {

        Letter previousL = selectedLetter;
        for(int i = index; i<letters.size(); i++) {
            previousL = moveLetter(previousL, letters.get(i));
        }
    }
    // set the coord of moveL to the coord of prevL
    private Letter moveLetter(Letter prevL, Letter moveL) {

        Letter l = new Letter(moveL.getLetter(),moveL.getLetterRectXs(),moveL.getLetterRectXf(),
                moveL.getLetterRectYs(),moveL.getLetterRectYf());
        l.setLetterSpellX(moveL.getLetterSpellX());
        l.setLetterSpellY(moveL.getLetterSpellY());
        l.setLetterShuffX(moveL.getLetterShuffX());
        l.setLetterShuffY(moveL.getLetterShuffY());

        moveL.setLetterSpellX(prevL.getLetterSpellX());
        moveL.setLetterSpellY(prevL.getLetterSpellY());

        moveL.setLetterRectXs(prevL.getLetterRectXs());
        moveL.setLetterRectXf(prevL.getLetterRectXf());
        moveL.setLetterRectYs(prevL.getLetterRectYs());
        moveL.setLetterRectYf(prevL.getLetterRectYf());

        return l;
    }
    public void userSelectLetter() {
        sendMessage(SpellingGameSurface.DISABLE_INPUT);
        stopGame();
        mSelectThread = new SelectThread();
        mSelectThread.start();
    }
    private void sendMessage(int m) {
        Message msg = new Message();
        msg.what = m;
        mGameHandler.sendMessage(msg);
    }

    private class GameThread extends Thread {

        // Debug
        private static final String TAG = "Game Thread";

        // keep thread running
        private boolean mKeepRunning;

        public GameThread() {
            if(mSelectThread != null) {
                mSelectThread.stopSelectThread();
                mSelectThread = null;
            }
            mKeepRunning = true;
        }

        @Override
        public void run() {
            Log.v(TAG, "Run");
            inShuffle = true;
            int index = 0;
            boolean isInShuffle = true;

            // check game over condition
            if(shuffledWord.size() == 0) {
                Log.v(TAG, "Game Over");
                gameOverDialog(checkWinCondition());
                sendMessage(SpellingGameSurface.GAME_OVER);
            }

            while(mKeepRunning) {

                if(!mSurfaceHolder.getSurface().isValid())
                    continue;

                mCanvas = mSurfaceHolder.lockCanvas();

                if(mKeepRunning) {
                    Log.v(TAG, "Draw");
                    drawGame();
                }

                if(isInShuffle && mKeepRunning) {
                    currentIndex = index;
                    mGameSurface.drawRectangle(mCanvas,
                            shuffledWord.get(index).getLetterRectXs(),
                            shuffledWord.get(index).getLetterRectYs(),
                            shuffledWord.get(index).getLetterRectXf(),
                            shuffledWord.get(index).getLetterRectYf());
                }
                else if(!isInShuffle && mKeepRunning) {
                    currentIndex = index;
                    mGameSurface.drawRectangle(mCanvas,
                            spelledWord.get(index).getLetterRectXs(),
                            spelledWord.get(index).getLetterRectYs(),
                            spelledWord.get(index).getLetterRectXf(),
                            spelledWord.get(index).getLetterRectYf());
                }
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);

                if(mKeepRunning) {
                    try {
                        sleep(2000);
                    } catch (InterruptedException intE) {
                        Log.e(TAG, "Interrupted", intE);
                    }
                    if(mKeepRunning) {
                        index++;
                        if (index == shuffledWord.size() && isInShuffle) {
                            if (spelledWord.size() != 0) {
                                isInShuffle = false;
                                inShuffle = false;
                            }
                            index = 0;
                        } else if (index == spelledWord.size() && !isInShuffle) {
                            isInShuffle = true;
                            inShuffle = true;
                            index = 0;
                        }
                    }
                }
            }
            Log.v(TAG, "Run Return");
        }
        public void stopGameThread() {
            mKeepRunning = false;
        }
    }

    /**
     * Select Thread
     * When User selects a letter put this letterin its new list
     */
    private class SelectThread extends Thread {

        // Debug
        private static final String TAG = "Select Thread";

        // Running var
        private boolean mKeepRunning;

        // Constr
        public SelectThread() { mKeepRunning = true; }

        @Override
        public void run() {
            Log.v(TAG, "Run");
            int i = -1;
            if (inShuffle) {
                i = currentIndex;
                Letter l = shuffledWord.get(i);
                Log.v(TAG, l.getLetter());
                shuffledWord.remove(i);
                updateCoord(l, spelledWord, SPELLED);
                spelledWord.add(l);
            } else if (!inShuffle) {
                i = currentIndex;
                Letter l = spelledWord.get(i);
                Log.v(TAG, l.getLetter());
                shiftAdjLetters(l, spelledWord, i);
                spelledWord.remove(i);
                updateCoord(l, shuffledWord, SHUFFLE);
                shuffledWord.add(l);
            }


            while(mKeepRunning) {

                if(!mSurfaceHolder.getSurface().isValid())
                    continue;

                mCanvas = mSurfaceHolder.lockCanvas();

                if(mKeepRunning) {
                    Log.v(TAG, "Draw");
                    drawGame();
                }

                mSurfaceHolder.unlockCanvasAndPost(mCanvas);

                if(mKeepRunning) {
                    try {
                        Log.v(TAG, "Sleep");
                        sleep(2000);
                    } catch (InterruptedException intE) {
                        Log.e(TAG, "Interrupted", intE);
                    }
                }
                startGame();
                stopSelectThread();
            }
            Log.v(TAG, "Run Return");
        }

        public void stopSelectThread() {
            mKeepRunning = false;
        }
    }
}
