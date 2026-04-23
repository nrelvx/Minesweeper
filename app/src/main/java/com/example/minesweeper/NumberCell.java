package com.example.minesweeper;

public class NumberCell extends Cell {
    private int minesAround;

    public NumberCell(int minesAround) {
        this.minesAround = minesAround;
    }

    @Override
    public boolean isMine() {
        return false;
    }

    @Override
    public int getMinesAround() {
        return minesAround;
    }

    public void setMinesAround(int minesAround) {
        this.minesAround = minesAround;
    }
}