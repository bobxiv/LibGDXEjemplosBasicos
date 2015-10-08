package com.pdm.box2dhelloworld;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Box2DHelloWorld extends ApplicationAdapter {

	//region Metodos publicos
	@Override
	public void create () {
		// antes creabamos la escena con escala en pixeles
		//camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera = new OrthographicCamera();
		float invRatio = Gdx.graphics.getHeight()/(float)Gdx.graphics.getWidth();
		w = 50;
		h = 50*invRatio;
		camera.setToOrtho(false, w, h);
		time = 0;

		world = new World(new Vector2(0, -10), true);
		debugRenderer = new Box2DDebugRenderer();

		world.setContactListener(new GameContactListener());

		createScene();
	}

	@Override
	public void render () {

		if( Gdx.input.justTouched() ) {
			Vector2 applyPoint = bala.getPosition().sub(new Vector2(-1,1));
			bala.applyLinearImpulse(new Vector2(3, 3), applyPoint, true);
		}

		if( Gdx.input.isTouched() ) {
			for (Body b : primeraPila)
				b.applyForce(new Vector2(100, 0), b.getPosition(), true);
		}

		time += Gdx.graphics.getDeltaTime();
		float x = MathUtils.lerp(5,w-5, Math.abs(MathUtils.sin(time)));
		float y = h / 2.0f;
		float ang = time*5;
		nave.setTransform(x, y, ang);

		world.step(1 / 60f, 6, 2);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		debugRenderer.render(world, camera.combined);
	}

	@Override
	public void dispose () {

		world.destroyBody(bala);
		world.destroyBody(nave);

		for(Body b: pelotas)
			world.destroyBody(b);

		for(Body b: segundaPila)
			world.destroyBody(b);

		for(Body b: primeraPila)
			world.destroyBody(b);
	}
	//endregion

	//region Metodos privados
	/**
	 * Crea la escena de prueba
	 */
	private void createScene()
	{
		float floorHeigth = 1.0f;
		float floorY = 0.75f;
		float topFloorY = floorY+floorHeigth/2;

		// creamos definicion del cuerpo
		BodyDef bodyDef = new BodyDef();
		// sera de tipo estatico
		bodyDef.type = BodyDef.BodyType.StaticBody;
		// establecemos la posicion
		bodyDef.position.set(w/2.0f, floorY);

		// creamos un cuerpo con esta definicion en el mundo
		Body body = world.createBody(bodyDef);

		// creamos el adorno para el cuerpo anterior
		FixtureDef fixtureDef = new FixtureDef();
		// creaamos una forma circular de radio 6
		PolygonShape suelo = new  PolygonShape();
		suelo.setAsBox(w/2.0f-1, floorHeigth/2);
		fixtureDef.shape = suelo;
		// establecemos propiedades fisicas
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f;

		// agregamos el adorno al cuerpo
		Fixture fixture = body.createFixture(fixtureDef);

		// liberamos la forma que creamos (ya no la necesitamos)
		suelo.dispose();

		nave = createNave(w/2.0f, h/2.0f, 2);

		primeraPila = new Array<Body>();
		for (int i=0; i < 12 ; ++i)
			primeraPila.add(createBox(35, topFloorY+1.1f*i, 1));

		segundaPila = new Array<Body>();
		for (int i=0; i < 7 ; ++i)
			segundaPila.add(createBox(45, topFloorY+1.1f*i, 1));

		pelotas = new Array<Body>();
		for (int i=0; i < 10 ; ++i)
			pelotas.add(createBall(MathUtils.random(4, w) - 2, MathUtils.random(h * 0.75f, h), MathUtils.random(0.5f, 2.0f)));

		bala = createBox(10/*x*/, topFloorY/*y*/, 2/*lado*/);
	}

	/**
	 * Crea una caja cuadrada
	 * @param x La coordenada x inicial
	 * @param y La coordenada y inicial
     * @param lado Dimension de cada lado de la caja
	 * @return El cuerpo creado
	 */
	private Body createBox(float x, float y, float lado)
	{
		// creamos definicion del cuerpo
		BodyDef bodyDef = new BodyDef();
		// sera de tipo dinamico
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		// establecemos la posicion
		bodyDef.position.set(x, y);

		// creamos un cuerpo con esta definicion en el mundo
		Body body = world.createBody(bodyDef);

		// creamos el adorno para el cuerpo anterior
		FixtureDef fixtureDef = new FixtureDef();
		// creaamos una forma circular de radio 6
		PolygonShape caja = new  PolygonShape();
		caja.setAsBox(lado/2, lado/2);
		fixtureDef.shape = caja;
		// establecemos propiedades fisicas
		fixtureDef.density = 0.1f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f;

		// agregamos el adorno al cuerpo
		Fixture fixture = body.createFixture(fixtureDef);

		// liberamos la forma que creamos (ya no la necesitamos)
		caja.dispose();

		return body;
	}

	/**
	 * Crea un circulo
	 * @param x La coordenada x inicial
	 * @param y La coordenada y inicial
	 * @param radio El radio del circulo
	 * @return El cuerpo creado
	 */
	private Body createBall(float x, float y, float radio)
	{
		// creamos definicion del cuerpo
		BodyDef bodyDef = new BodyDef();
		// sera de tipo dinamico
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		// establecemos la posicion
		bodyDef.position.set(x, y);

		// creamos un cuerpo con esta definicion en el mundo
		Body body = world.createBody(bodyDef);

		// creamos el adorno para el cuerpo anterior
		FixtureDef fixtureDef = new FixtureDef();
		// creaamos una forma circular de radio 6
		CircleShape circle = new CircleShape();
		circle.setRadius(radio);
		fixtureDef.shape = circle;
		// establecemos propiedades fisicas
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f;

		// agregamos el adorno al cuerpo
		Fixture fixture = body.createFixture(fixtureDef);

		// liberamos la forma que creamos (ya no la necesitamos)
		circle.dispose();
		return body;
	}

	/**
	 * Crea una nave, con forma triangular
	 * @param x La coordenada x inicial
	 * @param y La coordenada y inicial
	 * @param ancho El ancho de la nave
	 * @return El cuerpo creado
	 */
	private Body createNave(float x, float y, float ancho)
	{
		// creamos definicion del cuerpo
		BodyDef bodyDef = new BodyDef();
		// sera de tipo dinamico
		bodyDef.type = BodyDef.BodyType.KinematicBody;
		// establecemos la posicion
		bodyDef.position.set(x, y);
		// creamos un cuerpo con esta definicion en el mundo
		Body body = world.createBody(bodyDef);

		// creamos el adorno para el cuerpo anterior
		FixtureDef fixtureDef = new FixtureDef();
		// creaamos una forma circular de radio 6
		PolygonShape triangulo = new  PolygonShape();
		Vector2[] vert = new Vector2[3];
		vert[0] = new Vector2(-ancho/2.0f, -ancho/2.0f);
		vert[1] = new Vector2(ancho/2.0f, -ancho/2.0f);
		vert[2] = new Vector2(0.0f, ancho/2.0f);
		triangulo.set(vert);
		fixtureDef.shape = triangulo;
		// establecemos propiedades fisicas
		fixtureDef.density = 0.5f;
		fixtureDef.friction = 0.4f;
		fixtureDef.restitution = 0.6f;

		// agregamos el adorno al cuerpo
		Fixture fixture = body.createFixture(fixtureDef);

		// liberamos la forma que creamos (ya no la necesitamos)
		triangulo.dispose();
		return body;
	}
	//endregion

	//region Propiedades privadas
	/**
	 * El mundo de Box2D
	 */
	private World world;

	/**
	 * Ancho del mundo (en metros)
	 */
	private float w;

	/**
	 * Alto del mundo (en metros)
	 */
	private float h;

	/**
	 * La camara de la escena
	 */
	private OrthographicCamera camera;

	/**
	 * El objeto de renderizado para depuracion de Box2D
	 */
	private Box2DDebugRenderer debugRenderer;

	/**
	 * Pila de cajas mas hacia la izquierda
	 */
	private Array<Body> primeraPila;

	/**
	 * Pila de cajas mas hacia la derecha
	 */
	private Array<Body> segundaPila;

	/**
	 * Las pelotas
	 */
	private Array<Body> pelotas;

	/**
	 * Triangulo que se mueve en la escena
	 */
	private Body nave;

	/**
	 * Variable para animar la nave
	 */
	private float time;

	/**
	 * Bala de la escena que puede ser disparada
	 */
	private Body bala;
	//endregion
}
