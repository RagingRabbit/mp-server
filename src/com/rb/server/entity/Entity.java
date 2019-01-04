package com.rb.server.entity;

import com.rb.server.Level;
import com.rb.shared.packets.SnapshotPacket;

public class Entity {

	public final int id;
	public float x, y;
	public Level level;

	public Hitbox hitbox;

	private boolean removed;

	public Entity(int id, float x, float y, Level level) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.level = level;
	}

	public void remove() {
		removed = true;
	}

	public void update(float delta) {
	}

	public void onCollision(Entity e) {
	}

	public void createSnapshot(SnapshotPacket snapshot) {
	}

	public boolean isRemoved() {
		return removed;
	}

}
