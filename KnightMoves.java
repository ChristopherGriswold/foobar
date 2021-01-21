public class KnightMoves {
    public static void main(String[] args) {
        System.out.println(solution(1,11));
    }
    public static final Square[][] chessboard = new Square[8][8];
    public static final Move[] legalMoves = new Move[8];
    public static boolean isDone;

    // Gets the minimum number of moves required for a knight chess piece
    // to get from any one square on a chessboard to another.
    public static int solution(int src, int dest) {
        // Input is outside of the bounds of the chessboard.
        if (src < 0 || src > 63 || dest < 0 || dest > 63) {
            return -1;
        }
        if (src == dest) {
            return 0;
        }
        setChessboard();
        setLegalMoves();

        // Depth of recursion for searchLayer method.
        int layer = 1;
        while (!isDone) {
            chessboard[src / 8][src % 8].searchLayer(dest, layer);
            layer++;
        }
        return layer - 1;
    }

    // Y and X offsets representing each legal move that a knight may perform.
    private static void setLegalMoves() {
        legalMoves[0] = new Move(2, 1);
        legalMoves[1] = new Move(2, -1);
        legalMoves[2] = new Move(-2, 1);
        legalMoves[3] = new Move(-2, -1);
        legalMoves[4] = new Move(1, 2);
        legalMoves[5] = new Move(1, -2);
        legalMoves[6] = new Move(-1, 2);
        legalMoves[7] = new Move(-1, -2);
    }

    // Creates an 8 x 8 grid of squares to represent the play surface.
    private static void setChessboard() {
        for (int i = 0; i < 64; i++) {
            chessboard[i / 8][i % 8] = new Square(((i / 8)  * 8) + (i % 8));
        }
    }

    static class Square {
        private int dist;
        private final int location;
        private Square parent;
        private final Square[] children;

        public Square(int location) {
            this.location = location;
            this.dist = 0;
            this.children = new Square[8];
        }

        // Performs a BFS. Recursively searching for valid squares layer by layer. A layer represents the total
        //  number of moves from the starting point so each square that is N moves away is checked
        //  before any squares that are n + 1 moves away.
        public void searchLayer(int dest, int layerNum) {
            if (isDone) {
                return;
            }
            if (layerNum == 1) {
                getChildren();
                if (checkChildren(children, dest)) {
                    isDone = true;
                }
            } else {
                for (Square child : children) {
                    if (child != null) {
                        child.searchLayer(dest, layerNum - 1);
                    }
                }
            }
        }

        // Attempts every legal move from this Square's location and stores all
        //  valid outcomes in the Square's children[] array. If the location that
        //  would result from the move has already been claimed by another Square
        //  it will be ignored.
        private void getChildren() {
            for (Move legalMove : legalMoves) {
                int nextLocation = legalMove.tryMove(location);
                if (nextLocation != -1) {
                    Square moveToAdd = chessboard[nextLocation / 8][nextLocation % 8];
                    if (moveToAdd.parent == null) {
                        for (int j = 0; j < children.length; j++) {
                            if (children[j] == null) {
                                children[j] = moveToAdd;
                                moveToAdd.parent = this;
                                moveToAdd.dist = dist + 1;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // See if any square at the current depth is the destination square.
        private boolean checkChildren(Square[] children, int dest) {
            for (Square child : children) {
                if (child != null && child.location == dest) {
                    return true;
                }
            }
            return false;
        }
    }

    static class Move {
        private final int vertical;
        private final int horizontal;

        public Move(int vertical, int horizontal) {
            this.vertical = vertical;
            this.horizontal = horizontal;
        }

        // Makes sure that a move from this location is within the bounds of the chessboard.
        public int tryMove(int location) {
            int x = location % 8;
            int y = location / 8;
            if ((x + horizontal < 0) || (x + horizontal > 7)){
                return -1;
            } else if ((y + vertical < 0) || (y + vertical > 7)) {
                return -1;
            }
            else {
                return ((x + horizontal) + 8 * (y + vertical));
            }
        }
    }
}

