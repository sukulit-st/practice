import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Happy extends Application {
	// to add numbers
    private static final int GRID_SIZE = 3;
    private final int[][] gridState = new int[GRID_SIZE][GRID_SIZE];
    private final Button[][] boardButtons = new Button[GRID_SIZE][GRID_SIZE];
    private final List<GameStateRecord> moveHistory = new ArrayList<>();
    // add numbers and grid
    private int emptyRow = 2;
    private int emptyCol = 2;
    private int moveCount = 0;
    private int maxAllowedMoves = 20;
    private boolean isGameOver = false;

    private final Label statusLabel = new Label("Ready");
    private final Label moveCounterLabel = new Label();
    private final TextField maxMovesInput = new TextField("20");

    private static class GameStateRecord {
        final int[][] snapshot = new int[GRID_SIZE][GRID_SIZE];
        final int blankR, blankC;

        GameStateRecord(int[][] currentGrid, int r, int c) {
            for (int i = 0; i < GRID_SIZE; i++) {
                System.arraycopy(currentGrid[i], 0, snapshot[i], 0, GRID_SIZE);
            }
            this.blankR = r;
            this.blankC = c;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Number SLider");

        Button startBtn = new Button("New Game");
        Button resetBtn = new Button("Reset");
        Button undoBtn = new Button("Undo");
        Button applyLimitBtn = new Button("Set Limit");

        maxMovesInput.setPrefWidth(50);
        maxMovesInput.setAlignment(Pos.CENTER);

        HBox controlBar = new HBox(8, startBtn, resetBtn, undoBtn, new Label("Cap:"), maxMovesInput, applyLimitBtn);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setPadding(new Insets(12));

        GridPane puzzleGrid = new GridPane();
        puzzleGrid.setAlignment(Pos.CENTER);
        puzzleGrid.setHgap(8);
        puzzleGrid.setVgap(8);
        puzzleGrid.setPadding(new Insets(10));

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Button tile = new Button();
                tile.setPrefSize(80, 80);
                tile.setFont(Font.font(20));

                final int targetRow = r;
                final int targetCol = c;
                tile.setOnAction(e -> handleTileClick(targetRow, targetCol));

                boardButtons[r][c] = tile;
                puzzleGrid.add(tile, c, r);
            }
        }

        statusLabel.setFont(Font.font(14));
        moveCounterLabel.setFont(Font.font(12));
        VBox infoBar = new VBox(6, statusLabel, moveCounterLabel);
        infoBar.setAlignment(Pos.CENTER);
        infoBar.setPadding(new Insets(10));

        startBtn.setOnAction(e -> startNewGame());
        resetBtn.setOnAction(e -> resetToDefault());
        undoBtn.setOnAction(e -> performUndo());
        applyLimitBtn.setOnAction(e -> updateMoveLimit());

        resetToDefault();

        BorderPane root = new BorderPane();
        root.setTop(controlBar);
        root.setCenter(puzzleGrid);
        root.setBottom(infoBar);

        primaryStage.setScene(new Scene(root, 360, 440));
        primaryStage.show();
    }

    private void handleTileClick(int r, int c) {
        if (isGameOver) return;

        // Verify adjacency to empty spot (Manhattan distance = 1)
        if (Math.abs(r - emptyRow) + Math.abs(c - emptyCol) == 1) {
            moveHistory.add(new GameStateRecord(gridState, emptyRow, emptyCol));

            // Swap empty cell with clicked tile
            gridState[emptyRow][emptyCol] = gridState[r][c];
            gridState[r][c] = 0;
            emptyRow = r;
            emptyCol = c;

            moveCount++;
            checkGameState();
            renderBoard();
        }
    }

    private void startNewGame() {
        List<Integer> tilePool = new ArrayList<>();
        for (int i = 0; i < 9; i++) tilePool.add(i);

        do {
            Collections.shuffle(tilePool);
        } while (!isSolvable(tilePool) || isAlreadySolved(tilePool));

        int index = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int value = tilePool.get(index++);
                gridState[r][c] = value;
                if (value == 0) {
                    emptyRow = r;
                    emptyCol = c;
                }
            }
        }

        moveCount = 0;
        moveHistory.clear();
        isGameOver = false;
        statusLabel.setText("Game in progress...");
        renderBoard();
    }

    private void resetToDefault() {
        int initialVal = 1;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (r == 2 && c == 2) {
                    gridState[r][c] = 0;
                } else {
                    gridState[r][c] = initialVal++;
                }
            }
        }
        emptyRow = 2;
        emptyCol = 2;
        moveCount = 0;
        moveHistory.clear();
        isGameOver = false;
        statusLabel.setText("Board Reset");
        renderBoard();
    }

    private void performUndo() {
        if (moveHistory.isEmpty()) return;

        GameStateRecord lastState = moveHistory.remove(moveHistory.size() - 1);
        for (int r = 0; r < GRID_SIZE; r++) {
            System.arraycopy(lastState.snapshot[r], 0, gridState[r], 0, GRID_SIZE);
        }

        emptyRow = lastState.blankR;
        emptyCol = lastState.blankC;
        moveCount--;
        isGameOver = false; 

        statusLabel.setText("Undo Applied");
        renderBoard();
    }

    private void updateMoveLimit() {
        try {
            int newCap = Integer.parseInt(maxMovesInput.getText().trim());
            if (newCap > 0) {
                maxAllowedMoves = newCap;
                renderBoard();
            }
        } catch (NumberFormatException ignored) {
            maxMovesInput.setText(String.valueOf(maxAllowedMoves));
        }
    }

    private void checkGameState() {
        boolean solved = true;
        int expectedVal = 1;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (r == GRID_SIZE - 1 && c == GRID_SIZE - 1) {
                    if (gridState[r][c] != 0) solved = false;
                } else {
                    if (gridState[r][c] != expectedVal++) solved = false;
                }
            }
        }

        if (solved && moveCount <= maxAllowedMoves) {
            statusLabel.setText("Congrats, You Win!");
            isGameOver = true;
        } else if (moveCount >= maxAllowedMoves) {
            statusLabel.setText("Sorry Out of moves! Game Over.");
            isGameOver = true;
        }
    }

    private void renderBoard() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int val = gridState[r][c];
                Button b = boardButtons[r][c];
                if (val == 0) {
                    b.setText("");
                    b.setDisable(true);
                } else {
                    b.setText(String.valueOf(val));
                    b.setDisable(false);
                }
            }
        }
        moveCounterLabel.setText("Moves: " + moveCount + " / " + maxAllowedMoves);
    }

    private boolean isSolvable(List<Integer> pool) {
        int inversions = 0;
        List<Integer> nums = new ArrayList<>(pool);
        nums.remove(Integer.valueOf(0));
        for (int i = 0; i < nums.size(); i++) {
            for (int j = i + 1; j < nums.size(); j++) {
                if (nums.get(i) > nums.get(j)) inversions++;
            }
        }
        return inversions % 2 == 0;
    }

    private boolean isAlreadySolved(List<Integer> pool) {
        return pool.equals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 0));
    }
}
