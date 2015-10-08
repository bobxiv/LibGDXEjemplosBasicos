package com.pdm.box2djoints;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

/**
 * Controla el input para mover objetos de Box2D con un mouse joint
 */
public class Box2DInputProcessor implements InputProcessor {

    //region Metodos publicos
    public Box2DInputProcessor(Body _sueloBody, Camera _camera, World _world)
    {
        // callback para deteccion del toque
        callback = new QueryCallback() {
            public boolean reportFixture (Fixture fixture) {
                Gdx.app.log("reportFixture", "Start");
                // ignoramos el suelo
                if (fixture.getBody() == sueloBody)
                    return true;

                // si esta dentro de un cuerpo
                if (fixture.testPoint(testPoint.x, testPoint.y)) {
                    hitBody = fixture.getBody();
                    return false;
                }else
                    return true;
            }
        };

        testPoint = new Vector3();

        sueloBody = _sueloBody;

        camera = _camera;

        world = _world;
    }

    /**
     * Se llama cuando se realiza un touch
     * @param x La coordenada x del touch
     * @param y La coordenada y del touch
     * @param pointer Numero de touch
     * @param button El boton presionado si es procedente del mouse
     */
    public boolean touchDown (int x, int y, int pointer, int button) {
        //Gdx.app.log("touchDown", "Start");

        // transformamos de pixeles al mundo
        testPoint.set(x, y, 0);
        testPoint = camera.unproject(testPoint);

        // testeamos si tocamos un cuerpo
        hitBody = null;
        world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f, testPoint.x + 0.1f, testPoint.y + 0.1f);

        // si tocamos creamos y fijamos el joint
        if (hitBody != null) {
            //Gdx.app.log("Hit", hitBody.toString());
            MouseJointDef def = new MouseJointDef();
            def.bodyA = sueloBody;// es necesario
            def.bodyB = hitBody;// el objeto a mover
            def.collideConnected = true;
            def.target.set(testPoint.x, testPoint.y);
            def.maxForce = 1000.0f * hitBody.getMass();

            mouseJoint = (MouseJoint)world.createJoint(def);
            hitBody.setAwake(true);
        }
        return false;
    }

    /**
     * Se llama cuando se levanta un touch
     * @param x La coordenada x del touch
     * @param y La coordenada y del touch
     * @param pointer Numero de touch
     * @param button El boton presionado si es procedente del mouse
     */
    public boolean touchUp (int x, int y, int pointer, int button) {
        //Gdx.app.log("touchUp", "Start");
        if (mouseJoint != null) {
            // destruimos el joint
            world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
        return false;
    }

    /**
     * Se llama cuando se arrastra un touch
     * @param x La coordenada x del touch
     * @param y La coordenada y del touch
     * @param pointer Numero de touch
     */
    public boolean touchDragged (int x, int y, int pointer) {
        //Gdx.app.log("touchDragged", "Start");
        if (mouseJoint != null) {
            // transformamos de pixeles al mundo
            testPoint.set(x, y, 0);
            testPoint = camera.unproject(testPoint);
            mouseJoint.setTarget(new Vector2(testPoint.x, testPoint.y));
        }
        return false;
    }

    public boolean keyDown (int keycode) {
        return false;
    }

    public boolean keyUp (int keycode) {

        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean mouseMoved (int x, int y) {
        return false;
    }

    public boolean scrolled (int amount) {
        return false;
    }
    //endregion

    //region Propiedades privadas
    private QueryCallback callback;

    private Vector3 testPoint;

    private Body hitBody;

    private Body sueloBody;

    private Camera camera;

    private MouseJoint mouseJoint;

    private World world;
    //endregion
}
