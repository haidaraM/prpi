package com.prpi.network.communication;


public class HeartBeat {

    private int line;

    private int column;

    private String fileName;


    private CharSequence oldFragment;

    private CharSequence newFragment;


    public HeartBeat(int line, int column, String fileName, CharSequence oldFragment, CharSequence newFragment) {
        this.line = line;
        this.column = column;
        this.fileName = fileName;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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


}
