package edu.scu.csen275.smartgarden.model;

import java.util.Objects;

/**
 * Represents a position in the garden grid.
 * Immutable value object for grid coordinates.
 */
public final class Position {
    private final int row;
    private final int column;
    
    /**
     * Creates a new Position.
     * 
     * @param row the row coordinate (0-based)
     * @param column the column coordinate (0-based)
     * @throws IllegalArgumentException if coordinates are negative
     */
    public Position(int row, int column) {
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
     * Checks if this position is adjacent to another position.
     */
    public boolean isAdjacentTo(Position other) {
        int rowDiff = Math.abs(this.row - other.row);
        int colDiff = Math.abs(this.column - other.column);
        return (rowDiff <= 1 && colDiff <= 1) && !(rowDiff == 0 && colDiff == 0);
    }
    
    /**
     * Calculates Manhattan distance to another position.
     */
    public int distanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.column - other.column);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
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

