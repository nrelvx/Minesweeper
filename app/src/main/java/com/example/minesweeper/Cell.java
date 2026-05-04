package com.example.minesweeper;

/**
 * Абстрактний клас, що представляє одну клітинку на полі "Сапера".
 * Визначає спільну поведінку для всіх типів клітинок (з міною та без).
 */
public abstract class Cell {
    private boolean isOpen;     // Стан клітинки: відкрита (true) чи закрита (false)
    private boolean isFlagged;  // Прапорець: чи позначена клітинка як підозріла на міну

    /**
     * Відкриває клітинку, якщо на ній немає прапорця.
     * Якщо прапорець є - клітинка не відкривається.
     */
    public void open() {
        if (!isFlagged) {
            isOpen = true;
        }
    }

    /**
     * Змінює стан прапорця на клітинці (ставити/знімати прапорець).
     * Прапорець можна ставити/знімати ТІЛЬКИ на закритій клітинці.
     */
    public void toggleFlag() {
        if (!isOpen) {
            isFlagged = !isFlagged;
        }
    }

    /**
     * Перевіряє, чи клітинка вже відкрита.
     * @return true - якщо відкрита, false - якщо закрита
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Перевіряє, чи стоїть на клітинці прапорець.
     * @return true - якщо прапорець є, false - якщо немає
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Абстрактний метод, який визначає, чи є в цій клітинці міна.
     * Різні підкласи (MineCell, EmptyCell) реалізують цей метод по-своєму.
     * Це приклад поліморфізму.
     * @return true - якщо клітинка з міною, false - якщо без міни
     */
    public abstract boolean isMine();

    /**
     * Повертає кількість мін навколо клітинки.
     * У базовому класі за замовчуванням повертає 0.
     * @return кількість мін у сусідніх клітинках (від 0 до 8)
     */
    public int getMinesAround() {
        return 0;
    }
}