package com.michglass.glasshouse.glasshouse;

/**
 * Created by Oliver on 4/11/2014.
 */
public class Letter {

    // Debug
    private static final String TAG = "Letter";

    private int letterShuffX;
    private int letterShuffY;
    private int letterSpellX;
    private int letterSpellY;

    private int letterRectXs;
    private int letterRectXf;
    private int letterRectYs;
    private int letterRectYf;

    private String letter;

    public Letter(String l, int xs, int xf, int ys, int yf) {
        letter = l;
        letterRectXs = xs;
        letterRectXf = xf;
        letterRectYs = ys;
        letterRectYf = yf;
    }

    public int getLetterShuffX() {
        return letterShuffX;
    }

    public void setLetterShuffX(int letterX) {
        this.letterShuffX = letterX;
    }

    public int getLetterShuffY() {
        return letterShuffY;
    }

    public void setLetterShuffY(int letterY) {
        this.letterShuffY = letterY;
    }

    public int getLetterSpellX() { return letterSpellX; }

    public void setLetterSpellX(int lX) { this.letterSpellX = lX; }

    public int getLetterSpellY() { return letterSpellY; }

    public void setLetterSpellY(int lY) { this.letterSpellY = lY; }

    public int getLetterRectXs() {
        return letterRectXs;
    }

    public void setLetterRectXs(int letterRectXs) {
        this.letterRectXs = letterRectXs;
    }

    public int getLetterRectXf() {
        return letterRectXf;
    }

    public void setLetterRectXf(int letterRectXf) {
        this.letterRectXf = letterRectXf;
    }

    public int getLetterRectYs() {
        return letterRectYs;
    }

    public void setLetterRectYs(int letterRectYs) {
        this.letterRectYs = letterRectYs;
    }

    public int getLetterRectYf() {
        return letterRectYf;
    }

    public void setLetterRectYf(int letterRectYf) {
        this.letterRectYf = letterRectYf;
    }

    public String getLetter() {
        return letter;
    }
}
