package com.example.minesweeper;

/**
 * Клас, що представляє числову клітинку без міни.
 * Показує кількість мін у сусідніх клітинках (від 0 до 8).
 * Демонструє роботу з даними та перевизначення методів абстрактного класу.
 */
public class NumberCell extends Cell {
    private int minesAround;  // Зберігає кількість мін навколо клітинки

    /**
     * Конструктор числової клітинки.
     * @param minesAround кількість мін у сусідніх клітинках (0 - пуста, 1-8 - цифра)
     */
    public NumberCell(int minesAround) {
        this.minesAround = minesAround;
    }

    /**
     * Вказує, що ця клітинка НЕ містить міну.
     * @return false - завжди повертає false
     */
    @Override
    public boolean isMine() {
        return false;
    }

    /**
     * Повертає кількість мін навколо клітинки.
     * @return кількість мін у сусідніх клітинках (число від 0 до 8)
     */
    @Override
    public int getMinesAround() {
        return minesAround;
    }

    /**
     * Встановлює кількість мін навколо клітинки.
     * Може використовуватися для оновлення значення після створення об'єкта.
     * @param minesAround нова кількість мін навколо
     */
    public void setMinesAround(int minesAround) {
        this.minesAround = minesAround;
    }
}