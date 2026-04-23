package com.example.minesweeper;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

public class MainActivity extends AppCompatActivity {

    private static final int COLOR_MINE = 0xFFFF0000;
    private static final int COLOR_OPEN_EMPTY = 0xFFAAAAAA;
    private static final int COLOR_OPEN_NUMBER = 0xFFFFFFFF;
    private static final int COLOR_CLOSED = 0xFF808080;
    private static final int COLOR_FLAGGED = 0xFFFFA500;

    private Game game;
    private GridLayout gridLayout;
    private Button[][] buttons;
    private TextView txtTimer, txtMines;
    private ImageButton btnRestart;
    private Button btnRules;

    private int rows = 9;
    private int cols = 9;
    private int mines = 12;
    private int flagsLeft;
    private int seconds = 0;
    private Handler timerHandler = new Handler();
    private boolean gameActive = true;
    private Runnable timerRunnable;

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
                    .setPositiveButton("Нова гра", (dialog, which) -> startNewGame())
                    .setNegativeButton("Вийти", (dialog, which) -> finish())
                    .show();
        } else {
            checkWin();
        }
    }

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

    private int getNumberColor(int num) {
        switch (num) {
            case 1: return 0xFF0000FF;
            case 2: return 0xFF008000;
            case 3: return 0xFFFF0000;
            case 4: return 0xFF000080;
            case 5: return 0xFF8B4513;
            default: return 0xFF000000;
        }
    }

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
                    .setMessage("Вітаємо!\n\nЧас: " + seconds + " секунд\nВикористано прапорців: " + (mines - flagsLeft) + "/" + mines)
                    .setPositiveButton("Грати знову", (dialog, which) -> startNewGame())
                    .setNegativeButton("Вийти", (dialog, which) -> finish())
                    .show();
        }
    }

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

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}