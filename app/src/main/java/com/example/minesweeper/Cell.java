package com.example.minesweeper;

public abstract class Cell {
    private boolean isOpen;
    private boolean isFlagged;

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

    public abstract boolean isMine();

    public int getMinesAround() {
        return 0;
    }
}