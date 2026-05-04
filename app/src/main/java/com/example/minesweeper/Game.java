package com.example.minesweeper;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Головний ігровий клас, що керує логікою гри "Сапер".
 * Відповідає за створення поля, розміщення мін, підрахунок чисел,
 * відкриття клітинок та рекурсивне відкриття порожніх областей.
 * Демонструє роботу з двовимірними масивами, BFS-алгоритмом,
 * випадковою генерацією та взаємодією з об'єктами Cell.
 */
public class Game {
    private Cell[][] board;  // Ігрове поле (матриця клітинок)
    private int rows;        // Кількість рядків на полі
    private int cols;        // Кількість стовпців на полі
    private int mines;       // Загальна кількість мін на полі

    /**
     * Конструктор гри. Ініціалізує поле, розміщує міни та обчислює цифри навколо.
     * @param rows  кількість рядків на ігровому полі
     * @param cols  кількість стовпців на ігровому полі
     * @param mines кількість мін, які потрібно розмістити
     */
    public Game(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;

        board = new Cell[rows][cols];

        // Заповнення поля числовими клітинками з нулями (тимчасово)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new NumberCell(0);
            }
        }

        placeMines();
        calculateMinesAround();
    }

    /**
     * Розміщує задану кількість мін на полі у випадкових позиціях.
     * Використовує Random для генерації координат.
     * Міни не розміщуються на вже зайнятих позиціях.
     */
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

    /**
     * Обчислює для кожної не-мінової клітинки кількість мін навколо неї.
     * Перебирає всі 8 сусідніх напрямків та підраховує міни.
     * Результат зберігається у NumberCell через setMinesAround().
     */
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

    /**
     * Повертає клітинку за вказаними координатами.
     * @param row рядок клітинки
     * @param col стовпець клітинки
     * @return об'єкт Cell у вказаній позиції
     */
    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    /**
     * Відкриває клітинку за координатами.
     * Якщо клітинка вже відкрита або має прапорець - нічого не робить.
     * Якщо відкрита клітинка без мін і має 0 мін навколо -
     * автоматично відкриває всі сусідні клітинки через BFS.
     * @param row рядок клітинки для відкриття
     * @param col стовпець клітинки для відкриття
     */
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

    /**
     * Алгоритм BFS (пошук в ширину) для відкриття сусідніх порожніх клітинок.
     * Використовує чергу для обробки клітинок з 0 мін навколо.
     * Відкриває всі сусідні клітинки в 8 напрямках.
     * Демонструє роботу з чергою та алгоритмом пошуку.
     * @param row початковий рядок для відкриття сусідів
     * @param col початковий стовпець для відкриття сусідів
     */
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

    /**
     * Перевіряє, чи знаходяться координати в межах ігрового поля.
     * @param row рядок для перевірки
     * @param col стовпець для перевірки
     * @return true - якщо координати в межах поля, false - якщо за межами
     */
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
}