package com.example.minesweeper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Game {
    private Cell[][] board;
    private int rows;
    private int cols;
    private int mines;

    public Game(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;

        board = new Cell[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new NumberCell(0);
            }
        }

        placeMines();
        calculateMinesAround();
    }

    private void placeMines() {
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < mines) {
            int row = rand.nextInt(rows);
            int col = rand.nextInt(cols);

            if (!board[row][col].isMine()) {
                board[row][col] = new Mines();
                placedMines++;
            }
        }
    }

    private void calculateMinesAround() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!board[i][j].isMine()) {
                    int count = 0;
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            int newRow = i + x;
                            int newCol = j + y;
                            if (isInBounds(newRow, newCol) && board[newRow][newCol].isMine()) {
                                count++;
                            }
                        }
                    }
                    ((NumberCell) board[i][j]).setMinesAround(count);
                }
            }
        }
    }

    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    public void openCell(int row, int col) {
        Cell cell = board[row][col];

        if (cell.isOpen() || cell.isFlagged()) {
            return;
        }

        cell.open();

        if (!cell.isMine() && cell.getMinesAround() == 0) {
            openNeighbors(row, col);
        }
    }

    private void openNeighbors(int row, int col) {
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{row, col});

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    int newRow = r + x;
                    int newCol = c + y;

                    if (isInBounds(newRow, newCol)) {
                        Cell neighbor = board[newRow][newCol];
                        if (!neighbor.isOpen() && !neighbor.isFlagged() && !neighbor.isMine()) {
                            neighbor.open();
                            if (neighbor.getMinesAround() == 0) {
                                queue.add(new int[]{newRow, newCol});
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
}