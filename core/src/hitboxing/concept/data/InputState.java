package hitboxing.concept.data;

import java.util.HashMap;
import java.util.Map;

//Intermediate structure to keep active input received from the player.
public class InputState {
    public Map<String, Boolean> keyboardKeyState = new HashMap<>();

    public float directionAngle = 0;
}