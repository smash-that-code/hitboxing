package hitboxing.concept;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ShortArray;
import hitboxing.concept.data.CircleEntity;
import hitboxing.concept.data.InputState;
import hitboxing.concept.geometry.Direction;
import hitboxing.concept.geometry.Line;

import java.util.*;

import static hitboxing.concept.input.Keyboard.*;

public class Hitboxing extends ApplicationAdapter {
	SpriteBatch batch;
	Texture playerTexture;
	Sprite playerSprite;
	TextureRegion inactivePolyTextureRegion;
	List<TextureRegion> activePolyTextureRegions = new ArrayList<>();
	private static final EarClippingTriangulator triangulator = new EarClippingTriangulator();
	List<PolygonSprite> inactiveTrapeziums = new ArrayList<>();
	List<PolygonSprite> activeTrapeziums = new ArrayList<>();
	PolygonSpriteBatch polyBatch;

	public static class GameState {
		public int screenMaxWidth;
		public int screenMaxHeight;

		public List<float[]> trapeziumVertices = new ArrayList<>();
		public List<Boolean> trapeziumActivity = new ArrayList<>();

		public GameState(int screenMaxWidth, int screenMaxHeight) {
			this.screenMaxWidth = screenMaxWidth;
			this.screenMaxHeight = screenMaxHeight;
		}

		public CircleEntity player = new CircleEntity(150, 150, 100, 0, 550);
	}

	GameState state;
	InputState inputState;

	public static void setupTrapeziums(GameState state,
									   List<PolygonSprite> activeTrapeziums, List<PolygonSprite> inactiveTrapeziums,
									   List<TextureRegion> activePolyTextureRegions, TextureRegion inactivePolyTextureRegion) {
		final int TRAPEZIUM_COUNT = 1;

		activeTrapeziums.clear();
		inactiveTrapeziums.clear();
		state.trapeziumVertices.clear();

		for (int i = 0; i < TRAPEZIUM_COUNT; i++) {
			Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
			pix.setColor(0, 0.5f, 0, 0.75f);
			pix.fill();
			Texture solidColorTexture = new Texture(pix); //these textures NOT DISPOSED!!! Hack!
			TextureRegion coloredTextureRegion = new TextureRegion(solidColorTexture);
			activePolyTextureRegions.add(coloredTextureRegion);

			float[] vertices = new float[] {400,400, 500,400, 500,500, 400,500};
			PolygonSprite newInactiveSprite = getPolySprite(inactivePolyTextureRegion, vertices);
			PolygonSprite newActiveSprite = getPolySprite(coloredTextureRegion, vertices);

			inactiveTrapeziums.add(newInactiveSprite);
			activeTrapeziums.add(newActiveSprite);
			state.trapeziumVertices.add(vertices);
			state.trapeziumActivity.add(false);
		}
	}

	@Override
	public void create () {
		state = new GameState(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		inputState = new InputState();

		batch = new SpriteBatch();
		playerTexture = new Texture("player.png");
		playerTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		playerSprite = new Sprite(playerTexture);

		polyBatch = new PolygonSpriteBatch();

		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(.5f, 0.5f, .5f, 0.5f);
		pix.fill();
		Texture solidColorTexture = new Texture(pix);
		inactivePolyTextureRegion = new TextureRegion(solidColorTexture);


		setupTrapeziums(state,
						activeTrapeziums, inactiveTrapeziums,
						activePolyTextureRegions, inactivePolyTextureRegion);


		Gdx.input.setInputProcessor(new InputAdapter() {

			@Override
			public boolean keyDown (int keyCode) {
				if (keyCode == Input.Keys.ESCAPE) {
					inputState.keyboardKeyState.put(ESCAPE, Boolean.TRUE);
				}
				else if (keyCode == Input.Keys.SPACE) {
					inputState.keyboardKeyState.put(SPACE, Boolean.TRUE);
				}
				return true;
			}

			@Override
			public boolean keyUp(int keyCode) {
				if (keyCode == Input.Keys.SPACE) {
					inputState.keyboardKeyState.put(SPACE, Boolean.FALSE);
				}
				return true;
			}
		});
	}


	public static void handleKeyboardInput(Input input, InputState inputState) {
		inputState.keyboardKeyState.put(W, Boolean.FALSE);
		inputState.keyboardKeyState.put(A, Boolean.FALSE);
		inputState.keyboardKeyState.put(S, Boolean.FALSE);
		inputState.keyboardKeyState.put(D, Boolean.FALSE);
		inputState.keyboardKeyState.put(UP, Boolean.FALSE);
		inputState.keyboardKeyState.put(DOWN, Boolean.FALSE);
		inputState.keyboardKeyState.put(LEFT, Boolean.FALSE);
		inputState.keyboardKeyState.put(RIGHT, Boolean.FALSE);

		if (input.isKeyPressed(Input.Keys.W)) {
			inputState.keyboardKeyState.put(UP, Boolean.TRUE);
		}
		else if (input.isKeyPressed(Input.Keys.S)) {
			inputState.keyboardKeyState.put(DOWN, Boolean.TRUE);
		}

		if (input.isKeyPressed(Input.Keys.A)) {
			inputState.keyboardKeyState.put(LEFT, Boolean.TRUE);
		}
		else if (input.isKeyPressed(Input.Keys.D)) {
			inputState.keyboardKeyState.put(RIGHT, Boolean.TRUE);
		}

		if (input.isKeyPressed(Input.Keys.LEFT)) {
			inputState.keyboardKeyState.put(LEFT, Boolean.TRUE);
		}
		else if (input.isKeyPressed(Input.Keys.RIGHT)) {
			inputState.keyboardKeyState.put(RIGHT, Boolean.TRUE);
		}

		if (input.isKeyPressed(Input.Keys.UP)) {
			inputState.keyboardKeyState.put(UP, Boolean.TRUE);
		}
		else if (input.isKeyPressed(Input.Keys.DOWN)) {
			inputState.keyboardKeyState.put(DOWN, Boolean.TRUE);
		}
	}

	public static void checkExit(InputState inputState) {
		if (inputState.keyboardKeyState.get(ESCAPE) == Boolean.TRUE) {
			Gdx.app.exit();
			System.exit(0);
		}
	}

	public static void applyPlayerInput(InputState inputState, GameState state, float delta) {
		if (inputState.keyboardKeyState.get(LEFT) != Boolean.TRUE
			&& inputState.keyboardKeyState.get(RIGHT) != Boolean.TRUE
			&& inputState.keyboardKeyState.get(UP) != Boolean.TRUE
			&& inputState.keyboardKeyState.get(DOWN) != Boolean.TRUE) {
			//no input
			return;
		}

		float x = 0;
		if (inputState.keyboardKeyState.get(LEFT) == Boolean.TRUE) {
			x = -1;
		}
		else if (inputState.keyboardKeyState.get(RIGHT) == Boolean.TRUE) {
			x = 1;
		}

		float y = 0;
		if (inputState.keyboardKeyState.get(UP) == Boolean.TRUE) {
			y = 1;
		}
		else if (inputState.keyboardKeyState.get(DOWN) == Boolean.TRUE) {
			y = -1;
		}

		float rotation = Direction.getDirection(x, y).degreeAngle;


		float directionX = (float) Math.cos(Math.PI / 180 * rotation);
		float directionY = (float) Math.sin(Math.PI / 180 * rotation);

		float step = state.player.speed * delta;
		state.player.x += directionX * step;
		state.player.y += directionY * step;
		state.player.rotation = rotation;
	}

	//do not allow player move beyond screen
	public static void calculatePlayerCollisions(GameState state) {
		CircleEntity player = state.player;

		//against borders
		if (player.x > state.screenMaxWidth - state.player.radius) {
			player.x = state.screenMaxWidth - state.player.radius;
		} else if (player.x < player.radius) {
			player.x = player.radius;
		}

		if (player.y > state.screenMaxHeight - state.player.radius) {
			System.out.println(state.screenMaxHeight);
			player.y = state.screenMaxHeight - state.player.radius;
		} else if (player.y < player.radius) {
			player.y = player.radius;
		}
	}

	public static void calculateTrapeziumCollisions(GameState state) {
		CircleEntity player = state.player;
		float x = player.x;
		float y = player.y;
		float radius = player.radius;

		for (int i = 0; i < state.trapeziumVertices.size(); i++) {
			float[] vertices = state.trapeziumVertices.get(i);

			boolean playerCenterInPolygon = Intersector.isPointInPolygon(vertices, 0, vertices.length, x, y);

			boolean verticeInsidePlayerCircle = false;

			if (playerCenterInPolygon != true) {
				vertice_loop:
				for (int j = 0; j < vertices.length/2; j++) {
					float verticeX = vertices[j*2];
					float verticeY = vertices[j*2+1];

					float distance = Line.distance(x, y, verticeX, verticeY);

					if (distance < radius) {
						verticeInsidePlayerCircle = true;
						break vertice_loop;
					}
				}
			}

			boolean intersected = false;
			if (playerCenterInPolygon == false && verticeInsidePlayerCircle == false) {
				Vector2 center = new Vector2(x, y);
				float squaredRadius = radius*radius;

				intersection_loop:
				for (int j = 0; j < vertices.length/2; j++) {
					Vector2 start = new Vector2(vertices[j*2], vertices[j*2+1]);

					Vector2 end;

					//last vertex -> its edge end is very FIRST vertex;
					if (j == vertices.length/2-1) {
						end = new Vector2(vertices[0], vertices[1]);
					}
					else {
						end = new Vector2(vertices[j * 2 + 2], vertices[j * 2 + 3]);
					}

					boolean intersectionFound = Intersector.intersectSegmentCircle(start, end, center, squaredRadius);

					if (intersectionFound) {
						intersected = true;
						break intersection_loop;
					}
				}
			}

			if (playerCenterInPolygon || verticeInsidePlayerCircle || intersected) {
				state.trapeziumActivity.set(i, true);
			}
			else {
				state.trapeziumActivity.set(i, false);
			}
		}
	}

	public static PolygonSprite getPolySprite(TextureRegion polyTextureRegion, float[] vertices) {
		ShortArray triangleIndices = triangulator.computeTriangles(vertices);
		PolygonRegion polyReg = new PolygonRegion(polyTextureRegion, vertices, triangleIndices.toArray());

		return new PolygonSprite(polyReg);
	}

	public final int MAX_UPDATE_ITERATIONS = 3;
	public final float FIXED_TIMESTAMP = 1/60f;
	private float internalTimeTracker = 0;

	@Override
	public void render () {
		//input handling
		checkExit(inputState);
		handleKeyboardInput(Gdx.input, inputState);

		//fixed-timestamp logic handling
		float delta = Gdx.graphics.getDeltaTime();
		internalTimeTracker += delta;
		int iterations = 0;

		while(internalTimeTracker > FIXED_TIMESTAMP && iterations < MAX_UPDATE_ITERATIONS) {
			//apply input
			applyPlayerInput(inputState, state, FIXED_TIMESTAMP);

			//collision detection
			calculatePlayerCollisions(state);
			calculateTrapeziumCollisions(state);

			//time tracking logic
			internalTimeTracker -= FIXED_TIMESTAMP;
			iterations++;
		}

		//render
		Gdx.gl.glClearColor(0.25f, 1, 03.f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		//this is where we draw rectangles/trapeziums
		polyBatch.begin();

		for (int i = 0; i < state.trapeziumActivity.size(); i++) {
			boolean isActive = state.trapeziumActivity.get(i);
			if (isActive) {
				activeTrapeziums.get(i).draw(polyBatch);
			}
			else {
				inactiveTrapeziums.get(i).draw(polyBatch);
			}
		}

		polyBatch.end();


		//this is where we actually draw player image
		batch.begin();

		CircleEntity player = state.player;
		playerSprite.setBounds(player.x-player.radius, player.y-player.radius, player.radius *2, player.radius *2);
		playerSprite.setOriginCenter();
		playerSprite.setRotation(player.rotation);
		playerSprite.draw(batch);

		batch.end();
	}

	//it shouuld be called when we exit our program
	@Override
	public void dispose () {
		batch.dispose();
		playerTexture.dispose();
		polyBatch.dispose();
		inactivePolyTextureRegion.getTexture().dispose();
		for (TextureRegion region: activePolyTextureRegions) {
			region.getTexture().dispose();
		}
	}

}