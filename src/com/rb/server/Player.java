package com.rb.server;

import com.esotericsoftware.kryonet.Connection;
import com.rb.server.entity.Entity;
import com.rb.server.entity.Hitbox;
import com.rb.shared.entity.EntityType;
import com.rb.shared.packets.PlayerDataPacket;
import com.rb.shared.packets.SnapshotPacket;
import com.rb.shared.packets.SnapshotPacket.EntityData;

public class Player extends Entity {
	public String username;
	public Connection connection;

	public int color;
	public boolean direction;
	public boolean running;
	public int health;

	public Player(String username, int id, int color, Connection connection, float x, float y, Level level) {
		super(id, x, y, level);
		this.username = username;
		this.connection = connection;
		this.level = level;

		this.hitbox = new Hitbox(-8, -16, 16, 16);
		this.health = 5;
	}

	public void damage(int amount) {
		health -= amount;
	}

	public void createSnapshot(SnapshotPacket snapshot) {
		EntityData data = snapshot.initEntity(id, EntityType.PLAYER);
		data.putString("username", username);
		data.putInt("color", color);
		data.putFloat("x", x);
		data.putFloat("y", y);
		data.putBoolean("direction", direction);
		data.putBoolean("running", running);
		data.putInt("health", health);
	}

	public void setDataPacket(PlayerDataPacket data) {
		x = data.x;
		y = data.y;
		direction = data.direction;
		running = data.running;
	}
}
