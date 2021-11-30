package hitboxing.concept.data;

import hitboxing.concept.geometry.Direction;

//Game entity that has a shape of circle.
public class SquareEntity {
    public float x, y, side, rotation, speed;
    public Direction direction;

    public SquareEntity(float x, float y, float side, float rotation, float speed) {
        this.x = x;
        this.y = y;
        this.side = side;
        this.rotation = rotation;
        this.speed = speed;
        this.direction = Direction.E;
    }
}