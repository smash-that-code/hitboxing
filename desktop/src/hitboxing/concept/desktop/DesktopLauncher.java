package hitboxing.concept.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import hitboxing.concept.Hitboxing;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.fullscreen = false;
		config.samples = 4; //antialiasing
		config.title = "Hitboxing At Its Finest";
		config.width = 1920;
		config.height = 1000;
		new LwjglApplication(new Hitboxing(), config);
	}
}
