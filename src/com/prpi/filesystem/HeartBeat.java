package com.prpi.filesystem;


public class HeartBeat {

    /**
     * Line which was edited
     */
    private int line;

    /**
     * Column which was edited
     */
    private int column;

    /**
     * Absolute path
     */
    private String filePath;


    /**
     * Represents the old deleted character if the user delete a character. Must be empty if it's a insert heart beart
     */
    private CharSequence oldFragment;

    /**
     * Represents the new inserted character if the user insert a character. Must be empty if it's a delete heart beat
     */
    private CharSequence newFragment;


    public HeartBeat(int line, int column, String filePath, CharSequence oldFragment, CharSequence newFragment) {
        this.line = line;
        this.column = column;
        this.filePath = filePath;
        this.oldFragment = oldFragment;
        this.newFragment = newFragment;
    }


    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public CharSequence getOldFragment() {
        return oldFragment;
    }

    public void setOldFragment(CharSequence oldFragment) {
        this.oldFragment = oldFragment;
    }

    public CharSequence getNewFragment() {
        return newFragment;
    }

    public void setNewFragment(CharSequence newFragment) {
        this.newFragment = newFragment;
    }

    /**
     * Check if this heart beat is a insert heart beat
     * @return true if insert, false otherwise.
     */
    public boolean isInsertHeartBeat() {
        return newFragment.length() != 0;
    }
}
