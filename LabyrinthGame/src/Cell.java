/* Data Class - Cell
 *
 *    Holds a 2D position on the GameBoard.
 *    The x & y fields are public as they are final - this is to emulate a struct, a feature lacking in Java.
 */
public final class Cell {
    // x, y: 2D position of this Cell
    public final int x, y;

    // Constructor(): Creates a new Cell at (0, 0).
    public Cell() {
        this.x = 0;
        this.y = 0;
    }

    // Constructor(int, int): Creates a new Cell at (x, y).
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Constructor(Cell): Creates a new Cell with the same coordinates of another Cell.
    public Cell(Cell other) {
        this.x = other.x;
        this.y = other.y;
    }


    // distance(Cell): Computes the actual distance between two Cells.
    public double distance(Cell other) {
        return Math.sqrt(Math.pow((this.y - other.y), 2) + Math.pow((this.x - other.x), 2));
    }

    // squareDistance(Cell): Computes the square distance between two Cells (maximum of x distance or y distance).
    public int squareDistance(Cell other) {
        return Math.max(Math.abs(this.x - other.x), Math.abs(this.y - other.y));
    }

    // equal(Cell): Determines if this Cell and another share the same position on the GameBoard.
    public boolean equals(Cell other) {
        return (this.x == other.x && this.y == other.y);
    }
}
