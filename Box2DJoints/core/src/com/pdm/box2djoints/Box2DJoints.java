package com.pdm.box2djoints;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;

public class Box2DJoints extends ApplicationAdapter {

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

		world = new World(new Vector2(0, -10), true);
		debugRenderer = new Box2DDebugRenderer();

		createScene();

		Box2DInputProcessor inputProcessor = new Box2DInputProcessor(sueloBody, camera, world);
		Gdx.input.setInputProcessor(inputProcessor);

		AssetManager manager = new AssetManager();
		manager.load("Img/player.png", Texture.class);
		manager.finishLoading();

		batch = new SpriteBatch();

		player = new Avatar(world, manager.get("Img/player.png", Texture.class), 7, new Vector2(40,3), 0, 1, 0.5f, 0.5f);
	}

	@Override
	public void render () {

		/*
		if( Gdx.input.justTouched() ) {
			for(Body b: pesos)
				b.applyLinearImpulse(30, -30, b.getPosition().x, b.getPosition().y, true);
		}

		if( Gdx.input.isTouched() ) {
			//rueda1.applyForce(new Vector2(5, 0), rueda1.getPosition(), true);
			//rueda2.applyForce(new Vector2(5, 0), rueda2.getPosition(), true);
			revolute1.enableMotor(true);
			revolute2.enableMotor(true);
		}else
		{
			revolute1.enableMotor(false);
			revolute2.enableMotor(false);
		}*/

		world.step(1 / 60f, 6, 2);

		player.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		debugRenderer.render(world, camera.combined);

		batch.begin();

			player.render(batch);

		batch.end();
	}

	@Override
	public void dispose () {

		world.destroyJoint(revolute1);
		world.destroyJoint(revolute2);

		world.destroyBody(rueda1);
		world.destroyBody(rueda2);
	}
	//endregion

	//region Metodos privados
	/**
	 * Crea la escena de prueba
	 */
	private Body createResorte(float aX, float aY, float bX, float bY, float fq, float damp)
	{
		Body pivote = createBox(aX, aY, 3, BodyDef.BodyType.StaticBody);
		peso = createNave(bX, bY, 3, BodyDef.BodyType.DynamicBody);
		DistanceJointDef distDef = new DistanceJointDef();
		distDef.frequencyHz = fq;
		distDef.dampingRatio = damp;
		distDef.initialize(pivote, peso, pivote.getPosition(), peso.getPosition().add(0,1.5f));
		distance = (DistanceJoint)world.createJoint(distDef);
		Body peso2 = createNave(bX, bY-5, 3, BodyDef.BodyType.DynamicBody);
		distDef.initialize(peso, peso2, peso.getPosition().sub(0,1.5f), peso2.getPosition().add(0,1.5f));
		distance = (DistanceJoint)world.createJoint(distDef);
		return peso2;
	}
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
		sueloBody = world.createBody(bodyDef);

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
		Fixture fixture = sueloBody.createFixture(fixtureDef);

		// liberamos la forma que creamos (ya no la necesitamos)
		suelo.dispose();

		Body box = createBox(5, topFloorY+1.5f, 3, BodyDef.BodyType.DynamicBody);
		rueda1 = createBall(box.getPosition().x-1.5f, box.getPosition().y-1.5f, 1);
		rueda2 = createBall(box.getPosition().x+1.5f, box.getPosition().y-1.5f, 1);

		RevoluteJointDef revDef = new RevoluteJointDef();
		revDef.initialize(box, rueda1, box.getPosition().add(-1.5f, -1.5f));
		revolute1 = (RevoluteJoint)world.createJoint(revDef);
		revolute1.setMotorSpeed(-4);
		revolute1.setMaxMotorTorque(10);

		revDef.initialize(box, rueda2, box.getPosition().add(1.5f, -1.5f));
		revolute2 = (RevoluteJoint)world.createJoint(revDef);
		revolute2.setMotorSpeed(-4);
		revolute2.setMaxMotorTorque(10);

		pesos = new Array<Body>();
		for(int r=0; r < 5 ; ++r) {
			float dx = (w/6);
			pesos.add(createResorte(dx+r*dx, h * 0.75f, dx+r*dx, h * 0.55f, 0.5f+r*2.0f, 0.1f + ((5-r) / 5.0f) * 0.9f) );
		}

	}

	/**
	 * Crea una caja cuadrada
	 * @param x La coordenada x inicial
	 * @param y La coordenada y inicial
	 * @param lado Dimension de cada lado de la caja
	 * @return El cuerpo creado
	 */
	private Body createBox(float x, float y, float lado, BodyDef.BodyType tipo)
	{
		// creamos definicion del cuerpo
		BodyDef bodyDef = new BodyDef();
		// sera de tipo dinamico
		bodyDef.type = tipo;
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
	private Body createNave(float x, float y, float ancho, BodyDef.BodyType tipo)
	{
		// creamos definicion del cuerpo
		BodyDef bodyDef = new BodyDef();
		// sera de tipo dinamico
		bodyDef.type = tipo;
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
	 * Rueda izquierda del auto
	 */
	private Body rueda1;

	/**
	 * Rueda derecha del auto
	 */
	private Body rueda2;

	/**
	 * Revolute joint izquierda
	 */
	private RevoluteJoint revolute1;

	/**
	 * Revolute joint derecha
	 */
	private RevoluteJoint revolute2;

	/**
	 * Distance joint
	 */
	private DistanceJoint distance;

	/**
	 * Peso del resorte
	 */
	Body peso;

	Array<Body> pesos;

	private Body sueloBody;

	Avatar player;

	SpriteBatch batch;

	//endregion
}