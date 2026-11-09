import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberSliderAppV3 extends Application {
    private int[][] matrix = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
    private int zRow = 2, zCol = 2;
    private int count = 0, cap = 20;
    private boolean ended = false;
    private List<Record> history = new ArrayList<>();
    private Button[][] tiles = new Button[3][3];
    private Label msg = new Label("Ready");
    private Label counter = new Label("Moves: 0 / 20");
    private TextField inputCap = new TextField("20");

    private static class Record {
        int[][] grid = new int[3][3];
        int r, c;
        Record(int[][] g, int row, int col) {
            for(int i=0; i<3; i++) System.arraycopy(g[i], 0, grid[i], 0, 3);
            r = row; c = col;
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Slider Game V3");
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10));
        Button play = new Button("Play");
        Button reset = new Button("Reset");
        Button undo = new Button("Undo");
        Button set = new Button("Limit");
        inputCap.setPrefWidth(45);
        top.getChildren().addAll(play, reset, undo, inputCap, set);

        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(5);
        board.setVgap(5);
        for(int r=0; r<3; r++) {
            for(int c=0; c<3; c++) {
                Button b = new Button();
                b.setPrefSize(75, 75);
                final int tr = r, tc = c;
                b.setOnAction(e -> processClick(tr, tc));
                tiles[r][c] = b;
                board.add(b, c, r);
            }
        }

        VBox bottom = new VBox(5);
        bottom.setAlignment(Pos.CENTER);
        bottom.getChildren().addAll(msg, counter);

        play.setOnAction(e -> {
            List<Integer> pool = new ArrayList<>();
            for(int i=0; i<=8; i++) pool.add(i);
            do {
                Collections.shuffle(pool);
            } while(!checkInv(pool) || checkMatch(pool));
            int idx = 0;
            for(int i=0; i<3; i++) {
                for(int j=0; j<3; j++) {
                    matrix[i][j] = pool.get(idx++);
                    if(matrix[i][j] == 0) { zRow = i; zCol = j; }
                }
            }
            count = 0; history.clear(); ended = false;
            msg.setText("In Progress");
            sync();
        });

        reset.setOnAction(e -> {
            int[][] def = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
            for(int i=0; i<3; i++) System.arraycopy(def[i], 0, matrix[i], 0, 3);
            zRow = 2; zCol = 2; count = 0; history.clear(); ended = false;
            msg.setText("Reset");
            sync();
        });

        undo.setOnAction(e -> {
            if(ended || history.isEmpty()) return;
            Record last = history.remove(history.size() - 1);
            for(int i=0; i<3; i++) System.arraycopy(last.grid[i], 0, matrix[i], 0, 3);
            zRow = last.r; zCol = last.c; count--;
            msg.setText("Undo Performed");
            sync();
        });

        set.setOnAction(e -> {
            try {
                int val = Integer.parseInt(inputCap.getText().trim());
                if(val > 0) { cap = val; sync(); }
            } catch(Exception ignored) {}
        });

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(board);
        root.setBottom(bottom);
        sync();
        stage.setScene(new Scene(root, 330, 400));
        stage.show();
    }

    private void processClick(int r, int c) {
        if(ended) return;
        if(Math.abs(r - zRow) + Math.abs(c - zCol) == 1) {
            history.add(new Record(matrix, zRow, zCol));
            matrix[zRow][zCol] = matrix[r][c];
            matrix[r][c] = 0;
            zRow = r; zCol = c;
            count++;
            evaluate();
            sync();
        }
    }

    private boolean checkInv(List<Integer> list) {
        int inv = 0;
        List<Integer> sub = new ArrayList<>(list);
        sub.remove(Integer.valueOf(0));
        for(int i=0; i<sub.size(); i++) {
            for(int j=i+1; j<sub.size(); j++) {
                if(sub.get(i) > sub.get(j)) inv++;
            }
        }
        return inv % 2 == 0;
    }

    private boolean checkMatch(List<Integer> list) {
        return list.equals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 0));
    }

    private void evaluate() {
        boolean win = true;
        int target = 1;
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                if(i == 2 && j == 2) { if(matrix[i][j] != 0) win = false; }
                else { if(matrix[i][j] != target++) win = false; }
            }
        }
        if(win && count < cap) { msg.setText("Congrats, WINNER!!!"); ended = true; }
        else if(count >= cap) { msg.setText("You FAILED!"); ended = true; }
    }

    private void sync() {
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++) {
                if(matrix[i][j] == 0) tiles[i][j].setText("");
                else tiles[i][j].setText(String.valueOf(matrix[i][j]));
            }
        }
        counter.setText("Moves: " + count + " / " + cap);
    }
}
