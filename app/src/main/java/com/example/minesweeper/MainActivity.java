package com.example.minesweeper;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

/**
 * Головна Activity гри "Сапер" для Android.
 * Відповідає за графічний інтерфейс, взаємодію з гравцем,
 * обробку натискань, таймер, прапорці та перевірку перемоги/поразки.
 * Демонструє роботу з Android компонентами, GridLayout,
 * обробниками подій, діалоговими вікнами та інтеграцію з ігровою логікою.
 */
public class MainActivity extends AppCompatActivity {

    // Кольори для різних станів клітинок (формат ARGB)
    private static final int COLOR_MINE = 0xFFFF0000;          // Червоний для міни
    private static final int COLOR_OPEN_EMPTY = 0xFFAAAAAA;    // Сірий для пустої клітинки
    private static final int COLOR_OPEN_NUMBER = 0xFFFFFFFF;   // Білий для числової клітинки
    private static final int COLOR_CLOSED = 0xFF808080;        // Темно-сірий для закритої
    private static final int COLOR_FLAGGED = 0xFFFFA500;       // Помаранчевий для прапорця

    private Game game;                 // Об'єкт ігрової логіки
    private GridLayout gridLayout;     // Контейнер для клітинок (сітка)
    private Button[][] buttons;        // Двовимірний масив кнопок (клітинок)
    private TextView txtTimer, txtMines;  // Текстові поля для таймера та лічильника мін
    private ImageButton btnRestart;    // Кнопка перезапуску гри
    private Button btnRules;           // Кнопка правил гри

    private int rows = 9;      // Кількість рядків на полі
    private int cols = 9;      // Кількість стовпців на полі
    private int mines = 12;    // Кількість мін на полі
    private int flagsLeft;     // Скільки прапорців залишилось (максимум = mines)
    private int seconds = 0;   // Лічильник секунд (таймер)
    private Handler timerHandler = new Handler();  // Для оновлення таймера
    private boolean gameActive = true;             // Чи активна гра (не завершена)
    private Runnable timerRunnable;                // Завдання для таймера

    /**
     * Викликається при створенні Activity.
     * Ініціалізує інтерфейс, кнопки та запускає нову гру.
     * @param savedInstanceState збережений стан Activity (якщо є)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = findViewById(R.id.gridLayout);
        txtTimer = findViewById(R.id.txt_timer);
        txtMines = findViewById(R.id.txt_mines);
        btnRestart = findViewById(R.id.btn_restart);
        btnRules = findViewById(R.id.btn_rules);

        startNewGame();

        btnRestart.setOnClickListener(v -> startNewGame());
        btnRules.setOnClickListener(v -> showRulesDialog());
    }

    /**
     * Показує діалогове вікно з правилами гри.
     * Містить мету, керування та пояснення цифр.
     * Демонструє роботу з AlertDialog.
     */
    private void showRulesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("📖 Правила гри")
                .setMessage("🎯 Мета гри:\nВідкрити всі клітинки без мін.\n\n" +
                        "🖱️ Керування:\n" +
                        "• Коротке натискання = Відкрити клітинку\n" +
                        "• Довге натискання = Поставити/прибрати прапорець 🚩\n\n" +
                        "🔢 Цифри показують кількість мін поряд.\n\n" +
                        "💣 Програш: відкрили міну.\n\n" +
                        "🏆 Перемога: відкрили всі безпечні клітинки.")
                .setPositiveButton("Зрозуміло!", null)
                .setNeutralButton("Нова гра", (dialog, which) -> startNewGame())
                .show();
    }

    /**
     * Розпочинає нову гру: створює об'єкт Game, скидає таймер, прапорці та поле.
     */
    private void startNewGame() {
        game = new Game(rows, cols, mines);
        flagsLeft = mines;
        gameActive = true;
        seconds = 0;
        txtTimer.setText("⏱️ 0");
        txtMines.setText("🚩 " + mines);
        stopTimer();
        createBoard();
    }

    /**
     * Створює графічне ігрове поле з кнопок.
     * Обчислює розмір клітинки залежно від розміру екрану.
     * Додає обробники короткого та довгого натискань.
     */
    private void createBoard() {
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(cols);
        buttons = new Button[rows][cols];

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int cellSize = Math.min((screenWidth - 100) / cols, (screenHeight - 500) / rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Button btn = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(1, 1, 1, 1);
                btn.setLayoutParams(params);
                btn.setTextSize(Math.max(10, cellSize / 5));

                final int row = i;
                final int col = j;

                btn.setOnClickListener(v -> handleCellClick(row, col));
                btn.setOnLongClickListener(v -> handleCellLongClick(row, col));

                gridLayout.addView(btn);
                buttons[i][j] = btn;
            }
        }
        updateBoardUI();
    }

    /**
     * Обробляє коротке натискання на клітинку (спроба відкрити).
     * Якщо гра активна і немає прапорця - відкриває клітинку.
     * При першому відкритті запускає таймер.
     * При відкритті міни - показує діалог програшу.
     * @param row рядок натиснутої клітинки
     * @param col стовпець натиснутої клітинки
     */
    private void handleCellClick(int row, int col) {
        if (!gameActive || game.getCell(row, col).isFlagged()) return;

        if (seconds == 0) startTimer();

        game.openCell(row, col);
        updateBoardUI();

        if (game.getCell(row, col).isMine() && game.getCell(row, col).isOpen()) {
            gameActive = false;
            stopTimer();
            showAllMines();
            new AlertDialog.Builder(this)
                    .setTitle("💥 БАХ!")
                    .setMessage("Ви наступили на міну!\n\nЧас: " + seconds + " секунд")
                    .setPositiveButton("Нова гра", (dialog, which) ->
                            startNewGame())
                    .setNegativeButton("Вийти", (dialog, which) ->
                            finish())
                    .show();
        } else {
            checkWin();
        }
    }

    /**
     * Обробляє довге натискання на клітинку (поставити/зняти прапорець).
     * Прапорець можна ставити тільки на закритій клітинці.
     * Оновлює лічильник прапорців та перевіряє перемогу.
     * @param row рядок натиснутої клітинки
     * @param col стовпець натиснутої клітинки
     * @return true - подія оброблена
     */
    private boolean handleCellLongClick(int row, int col) {
        if (!gameActive) return false;

        Cell cell = game.getCell(row, col);
        if (!cell.isOpen()) {
            cell.toggleFlag();
            flagsLeft += cell.isFlagged() ? -1 : 1;
            txtMines.setText("🚩 " + flagsLeft);
            updateBoardUI();
            checkWin();
        }
        return true;
    }

    /**
     * Оновлює графічне відображення всіх клітинок на полі.
     * Відображає текст, кольори, прапорці, міни та числа.
     */
    private void updateBoardUI() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Button btn = buttons[i][j];
                Cell cell = game.getCell(i, j);

                if (cell.isOpen()) {
                    if (cell.isMine()) {
                        btn.setText("💣");
                        btn.setBackgroundColor(COLOR_MINE);
                    } else {
                        int num = cell.getMinesAround();
                        btn.setText(num == 0 ? "" : String.valueOf(num));
                        btn.setBackgroundColor(num == 0 ? COLOR_OPEN_EMPTY : COLOR_OPEN_NUMBER);
                        btn.setTextColor(getNumberColor(num));
                    }
                } else {
                    btn.setText(cell.isFlagged() ? "🚩" : "");
                    btn.setBackgroundColor(cell.isFlagged() ? COLOR_FLAGGED : COLOR_CLOSED);
                }
            }
        }
    }

    /**
     * Повертає колір для тексту числа залежно від його значення.
     * @param num число (1-8) для визначення кольору
     * @return колір у форматі ARGB
     */
    private int getNumberColor(int num) {
        switch (num) {
            case 1: return 0xFF0000FF;  // Синій
            case 2: return 0xFF008000;  // Зелений
            case 3: return 0xFFFF0000;  // Червоний
            case 4: return 0xFF000080;  // Темно-синій
            case 5: return 0xFF8B4513;  // Коричневий
            default: return 0xFF000000; // Чорний
        }
    }

    /**
     * Показує всі міни на полі (при програші).
     * Використовується для демонстрації гравцеві, де були міни.
     */
    private void showAllMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (game.getCell(i, j).isMine()) {
                    buttons[i][j].setText("💣");
                    buttons[i][j].setBackgroundColor(COLOR_MINE);
                }
            }
        }
    }

    /**
     * Перевіряє, чи гравець виграв (відкрив всі безпечні клітинки).
     * При перемозі зупиняє таймер та показує діалог з результатами.
     */
    private void checkWin() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = game.getCell(i, j);
                if (!cell.isMine() && !cell.isOpen()) return;
            }
        }

        if (gameActive) {
            gameActive = false;
            stopTimer();
            new AlertDialog.Builder(this)
                    .setTitle("🎉 ПЕРЕМОГА! 🎉")
                    .setMessage("Вітаємо!\n\nЧас: " + seconds + " секунд\nВикористано прапорців: "
                            + (mines - flagsLeft) + "/" + mines)
                    .setPositiveButton("Грати знову", (dialog, which) ->
                            startNewGame())
                    .setNegativeButton("Вийти", (dialog, which) ->
                            finish())
                    .show();
        }
    }

    /**
     * Запускає таймер, який збільшує лічильник секунд кожну секунду.
     * Оновлює текст на екрані.
     */
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameActive) {
                    seconds++;
                    txtTimer.setText("⏱️ " + seconds);
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    /**
     * Зупиняє таймер (видаляє заплановане завдання).
     */
    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}