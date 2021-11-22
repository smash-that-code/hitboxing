package hitboxing.concept.data;

import hitboxing.concept.geometry.Direction;

//Game entity that has a shape of circle.
public class RectangleEntity {
    public float x, y, width, height, rotation, speed;
    public Direction direction;

    public RectangleEntity(float x, float y, float width, float height, float rotation, float speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.speed = speed;
        this.direction = Direction.E;
    }
}