class BoardCore {
    constructor() {
        this.data = [[1, 2, 3], [4, 5, 6], [7, 8, 0]];
        this.blankX = 2;
        this.blankY = 2;
    }

    refresh() {
        this.data = [[1, 2, 3], [4, 5, 6], [7, 8, 0]];
        this.blankX = 2;
        this.blankY = 2;
    }

    shuffleMatrix() {
        let pool = [1, 2, 3, 4, 5, 6, 7, 8, 0];
        do {
            for (let i = pool.length - 1; i > 0; i--) {
                let j = Math.floor(Math.random() * (i + 1));
                [pool[i], pool[j]] = [pool[j], pool[i]];
            }
        } while (!this.validateSolvability(pool));

        for (let x = 0; x < 3; x++) {
            for (let y = 0; y < 3; y++) {
                this.data[x][y] = pool[x * 3 + y];
                if (this.data[x][y] === 0) {
                    this.blankX = x;
                    this.blankY = y;
                }
            }
        }
    }

    validateSolvability(arr) {
        let inv = 0;
        let pure = arr.filter(v => v !== 0);
        for (let i = 0; i < pure.length; i++) {
            for (let j = i + 1; j < pure.length; j++) {
                if (pure[i] > pure[j]) inv++;
            }
        }
        return inv % 2 === 0;
    }

    shift(x, y) {
        if (Math.abs(x - this.blankX) + Math.abs(y - this.blankY) !== 1) return false;
        this.data[this.blankX][this.blankY] = this.data[x][y];
        this.data[x][y] = 0;
        this.blankX = x;
        this.blankY = y;
        return true;
    }

    checkWin() {
        let target = 1;
        for (let x = 0; x < 3; x++) {
            for (let y = 0; y < 3; y++) {
                if (x === 2 && y === 2) return this.data[x][y] === 0;
                if (this.data[x][y] !== target++) return false;
            }
        }
        return true;
    }
}

class GameApp {
    constructor() {
        this.board = new BoardCore();
        this.counter = 0;
        this.limit = 20;
        this.snapshots = [];
        this.ended = false;
        this.statusText = "";
    }

    startNew() {
        this.board.shuffleMatrix();
        this.counter = 0;
        this.snapshots = [];
        this.ended = false;
        this.statusText = "";
    }

    resetGame() {
        this.board.refresh();
        this.counter = 0;
        this.snapshots = [];
        this.ended = false;
        this.statusText = "";
    }

    setLimit(val) {
        this.limit = val;
    }

    selectCell(x, y) {
        if (this.ended) return;
        let backup = this.board.data.map(row => [...row]);
        if (this.board.shift(x, y)) {
            this.snapshots.push({
                matrix: backup,
                bx: this.board.blankX,
                by: this.board.blankY
            });
            this.counter++;
            this.evaluate();
        }
    }

    undo() {
        if (this.ended || this.snapshots.length === 0) return;
        let prev = this.snapshots.pop();
        this.board.data = prev.matrix.map(row => [...row]);
        this.board.blankX = prev.bx;
        this.board.blankY = prev.by;
        this.counter--;
    }

    evaluate() {
        if (this.board.checkWin() && this.counter < this.limit) {
            this.statusText = "Congrats, WINNER!!!";
            this.ended = true;
        } else if (this.counter >= this.limit) {
            this.statusText = "You FAILED!";
            this.ended = true;
        }
    }
}
