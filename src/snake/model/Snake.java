package snake.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Snake entity containing segmented body positions, direction buffering, and growth state.
 */
public class Snake {
    private final LinkedList<Point> body;
    private Direction currentDirection;
    private Direction nextDirection;
    private boolean isAlive;

    public Snake(int startX, int startY, int initialLength, Direction initialDir) {
        this.body = new LinkedList<>();
        this.currentDirection = initialDir;
        this.nextDirection = initialDir;
        this.isAlive = true;
        reset(startX, startY, initialLength, initialDir);
    }

    public void reset(int startX, int startY, int initialLength, Direction initialDir) {
        body.clear();
        this.currentDirection = initialDir;
        this.nextDirection = initialDir;
        this.isAlive = true;

        for (int i = 0; i < initialLength; i++) {
            // Place segments trailing behind initial direction
            body.add(new Point(startX - i * initialDir.getDx(), startY - i * initialDir.getDy()));
        }
    }

    public Point getHead() {
        return body.getFirst();
    }

    public List<Point> getBody() {
        return Collections.unmodifiableList(body);
    }

    public int getLength() {
        return body.size();
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public Direction getNextDirection() {
        return nextDirection;
    }

    public void setNextDirection(Direction nextDirection) {
        // Prevent 180-degree instant reversal into own neck
        if (nextDirection != null && !nextDirection.isOpposite(currentDirection)) {
            this.nextDirection = nextDirection;
        }
    }

    public Point advance(boolean grow) {
        currentDirection = nextDirection;
        Point newHead = getHead().translate(currentDirection);
        body.addFirst(newHead);

        if (!grow) {
            body.removeLast();
        }
        return newHead;
    }

    public boolean occupies(Point p) {
        return body.contains(p);
    }

    public boolean occupiesExceptHead(Point p) {
        for (int i = 1; i < body.size(); i++) {
            if (body.get(i).equals(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkSelfCollision() {
        Point head = getHead();
        return occupiesExceptHead(head);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}
