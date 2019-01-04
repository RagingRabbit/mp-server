package com.rb.server.entity;

import com.rb.server.Level;
import com.rb.server.MServer;
import com.rb.server.Player;

public class Door extends Entity {

	private Level target;
	private float posX, posY;

	public Door(int id, float x, float y, Level level, Level target, float posX, float posY, Hitbox hitbox) {
		super(id, x, y, level);

		this.target = target;
		this.posX = posX;
		this.posY = posY;
		this.hitbox = hitbox;
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void onCollision(Entity e) {
		if (e instanceof Player) {
			Player p = (Player) e;
			MServer.instance.switchLevel(p, target, posX, posY);
		}
	}

}
