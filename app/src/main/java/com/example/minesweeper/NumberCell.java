package com.example.minesweeper;

public class NumberCell extends Cell {

    private int minesAround;

    public NumberCell(int minesAround) {
        this.minesAround = minesAround;
    }

    public int getMinesAround() {
        return minesAround;
    }

    public void setMinesAround(int minesAround) {
        this.minesAround = minesAround;
    }
}