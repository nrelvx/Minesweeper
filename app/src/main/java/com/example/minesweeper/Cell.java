package com.example.minesweeper;

public class Cell {
    protected boolean isOpen;
    protected boolean isFlagged;

    public void open() {
        if (!isFlagged) {
            isOpen = true;
        }
    }

    public void toggleFlag() {
        if (!isOpen) {
            isFlagged = !isFlagged;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public boolean isMine() {
        return false;
    }
}