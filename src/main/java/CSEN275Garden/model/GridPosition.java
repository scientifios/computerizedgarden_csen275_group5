package CSEN275Garden.model;

import java.util.Objects;

/**
 * Immutable value object representing a coordinate in the garden grid.
 */
public final class GridPosition {
    private final int row;
    private final int column;

    /**
    * Creates a position with zero-based row and column indices.
     *
    * @throws IllegalArgumentException if either coordinate is negative
    */
    public GridPosition(int row, int column) {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException(
                "Position coordinates must be non-negative: (" + row + ", " + column + ")");
        }
        this.row = row;
        this.column = column;
    }

    public int row() {
        return row;
    }

    public int column() {
        return column;
    }

    /**
    * Returns true if the two positions are adjacent (including diagonals).
    */
    public boolean isAdjacentTo(GridPosition other) {
        int rowDiff = Math.abs(this.row - other.row);
        int colDiff = Math.abs(this.column - other.column);
        return (rowDiff <= 1 && colDiff <= 1) && !(rowDiff == 0 && colDiff == 0);
    }

    /**
    * Returns the Manhattan distance between this position and another.
    */
    public int distanceTo(GridPosition other) {
        return Math.abs(this.row - other.row) + Math.abs(this.column - other.column);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GridPosition position = (GridPosition) obj;
        return row == position.row && column == position.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return "(" + row + "," + column + ")";
    }
}
