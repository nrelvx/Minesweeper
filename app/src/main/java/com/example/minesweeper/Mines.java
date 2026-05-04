package com.example.minesweeper;

/**
 * Клас, що представляє клітинку з міною.
 * Успадковує базову поведінку від класу Cell.
 */
public class Mines extends Cell {

    /**
     * Вказує, що ця клітинка містить міну.
     * @return true - завжди повертає true, оскільки це клітинка з міною
     */
    @Override
    public boolean isMine() {
        return true;
    }
}