package com.example.minesweeper;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

public class MainActivity extends AppCompatActivity {
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

        // Initialize views
        gridLayout = findViewById(R.id.gridLayout);
        txtTimer = findViewById(R.id.txt_timer);
        txtMines = findViewById(R.id.txt_mines);
        btnRestart = findViewById(R.id.btn_restart);
        btnRules = findViewById(R.id.btn_rules);

        // Start new game
        startNewGame();

        // Restart button click listener
        btnRestart.setOnClickListener(v -> startNewGame());

        // Rules button click listener
        btnRules.setOnClickListener(v -> showRulesDialog());
    }

    private void showRulesDialog() {
        new AlertDialog.Builder(this)
                .setTitle("📖 Правила гри")
                .setMessage("🎯 **Мета гри:**\nВідкрити всі клітинки, які не містять мін.\n\n" +
                        "🖱️ **Керування:**\n" +
                        "• Коротке натискання = Відкрити клітинку\n" +
                        "• Довге натискання = Поставити/прибрати прапорець 🚩\n\n" +
                        "🔢 **Цифри:**\n" +
                        "Цифри показують, скільки мін знаходиться поряд з цією клітинкою.\n\n" +
                        "💣 **Програш:**\n" +
                        "Якщо ви відкриєте міну, ви програєте!\n\n" +
                        "🏆 **Перемога:**\n" +
                        "Відкрийте всі безпечні клітинки, щоб перемогти!\n\n" +
                        "💡 **Порада:**\n" +
                        "Використовуйте прапорці, щоб відмічати підозрілі місця!\n\n" +
                        "🔍 **Підказка:**\n" +
                        "Якщо навколо відкритої клітинки з цифрою стоїть стільки ж прапорців,\n" +
                        "то решту клітинок навколо можна безпечно відкрити.")
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

        // Calculate cell size to fit screen
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Calculate available space
        int availableWidth = screenWidth - 100;
        int availableHeight = screenHeight - 500;

        int cellWidth = availableWidth / cols;
        int cellHeight = availableHeight / rows;
        int cellSize = Math.min(cellWidth, cellHeight);

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

                // Short click - open cell
                btn.setOnClickListener(v -> {
                    if (gameActive && !game.getCell(row, col).isFlagged()) {
                        if (seconds == 0) {
                            startTimer();
                        }

                        game.openCell(row, col);
                        updateBoardUI();

                        // Check if player lost
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
                        }

                        checkWin();
                    }
                });

                // Long click - place/remove flag
                btn.setOnLongClickListener(v -> {
                    if (gameActive) {
                        Cell cell = game.getCell(row, col);
                        if (!cell.isOpen()) {
                            cell.toggleFlag();
                            if (cell.isFlagged()) {
                                flagsLeft--;
                            } else {
                                flagsLeft++;
                            }
                            txtMines.setText("🚩 " + flagsLeft);
                            updateBoardUI();
                            checkWin();
                        }
                        return true;
                    }
                    return false;
                });

                gridLayout.addView(btn);
                buttons[i][j] = btn;
            }
        }
        updateBoardUI();
    }

    private void updateBoardUI() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Button btn = buttons[i][j];
                Cell cell = game.getCell(i, j);

                if (cell.isOpen()) {
                    if (cell.isMine()) {
                        btn.setText("💣");
                        btn.setBackgroundColor(0xFFFF0000);
                    } else if (cell instanceof NumberCell) {
                        int num = ((NumberCell) cell).getMinesAround();
                        if (num == 0) {
                            btn.setText("");
                            btn.setBackgroundColor(0xFFAAAAAA);
                        } else {
                            btn.setText(String.valueOf(num));
                            btn.setBackgroundColor(0xFFFFFFFF);
                            // Set different colors for different numbers
                            if (num == 1) btn.setTextColor(0xFF0000FF);
                            else if (num == 2) btn.setTextColor(0xFF008000);
                            else if (num == 3) btn.setTextColor(0xFFFF0000);
                            else if (num == 4) btn.setTextColor(0xFF000080);
                            else if (num == 5) btn.setTextColor(0xFF8B4513);
                            else btn.setTextColor(0xFF000000);
                        }
                    }
                } else {
                    if (cell.isFlagged()) {
                        btn.setText("🚩");
                        btn.setBackgroundColor(0xFFFFA500);
                    } else {
                        btn.setText("");
                        btn.setBackgroundColor(0xFF808080);
                    }
                }
            }
        }
    }

    private void showAllMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (game.getCell(i, j).isMine()) {
                    buttons[i][j].setText("💣");
                    buttons[i][j].setBackgroundColor(0xFFFF0000);
                }
            }
        }
    }

    private void checkWin() {
        int unopenedSafe = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = game.getCell(i, j);
                if (!cell.isMine() && !cell.isOpen()) {
                    unopenedSafe++;
                }
            }
        }

        if (unopenedSafe == 0 && gameActive) {
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