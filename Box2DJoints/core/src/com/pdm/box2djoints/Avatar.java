package com.pdm.box2djoints;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Bob on 7/10/2015.
 */
public class Avatar {

    //region Metodos publicos
    public Avatar(World world, Texture texture, float spriteHeight, Vector2 _position, float _rotation, float density, float friction, float restitution)
    {
        sprite = new Sprite(texture);

        // configuramos el sprite
        float ratio = texture.getWidth()/(float)texture.getHeight();
        float spriteWidth = spriteHeight * ratio;
        halfWidth = spriteWidth / 2.0f;
        halfHeight = spriteHeight / 2.0f;
        sprite.setSize(spriteWidth, spriteHeight);
        sprite.setOriginCenter();
        //Gdx.app.log("Scale: ", "X: " + String.valueOf(sprite.getScaleX()) + " Y: " + String.valueOf(sprite.getScaleY()));
        //Gdx.app.log("Size: ", "X: " + String.valueOf(sprite.getWidth()) + " Y: " + String.valueOf(sprite.getHeight()));


        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(_position);
        bodyDef.angle = _rotation;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape caja = new  PolygonShape();
        caja.setAsBox(halfWidth, halfHeight);
        fixtureDef.shape = caja;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;
        Fixture fixture = body.createFixture(fixtureDef);

        caja.dispose();

        body.setUserData(this);
        fixture.setUserData(this);
    }

    /**
     * Actualiza la posicion de la fruta
     */
    public void update()
    {
        rotation = body.getAngle()* MathUtils.radiansToDegrees;
        sprite.setRotation(rotation);

        position = body.getPosition();
        sprite.setPosition(position.x - halfWidth, position.y - halfHeight);

        //Gdx.app.log("Rotation", "ang: " + String.valueOf(rotation));
        //Gdx.app.log("Position", "X: " + String.valueOf(sprite.getX()) + " Y: " + String.valueOf(sprite.getY()));
    }

    /**
     * Dibuja la fruta
     * @param batch El spritebatch a utilizar
     */
    public void render(SpriteBatch batch)
    {
        sprite.draw(batch);
    }
    //endregion

    //region Propiedades privadas
    /**
     * El cuerpo de Box2D
     */
    private Body body;

    /**
     * El sprite
     */
    private Sprite sprite;

    /**
     * La posicion del objeto
     */
    private Vector2 position;

    /**
     * La rotacion del objeto
     */
    private float rotation;

    /**
     * Medio ancho del objeto
     */
    private float halfWidth;

    /**
     * Medio alto del objeto
     */
    private float halfHeight;
    //endregion
}
