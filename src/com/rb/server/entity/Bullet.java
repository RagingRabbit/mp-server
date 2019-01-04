package com.rb.server.entity;

import com.rb.server.Level;
import com.rb.server.Player;
import com.rb.shared.entity.EntityType;
import com.rb.shared.packets.SnapshotPacket;
import com.rb.shared.packets.SnapshotPacket.EntityData;

public class Bullet extends Entity {

	public float dx, dy;

	public float lifeLength;
	public float timer;

	private Player sender;

	public Bullet(int id, float angle, float speed, Player sender, float x, float y, Level level) {
		super(id, x, y, level);
		this.dy = (float) (Math.sin(angle) * speed);
		this.dx = (float) (Math.cos(angle) * speed);
		this.hitbox = new Hitbox(-3, -3, 6, 6);

		this.lifeLength = 1.0f;
		this.timer = 0.0f;

		this.sender = sender;
	}

	public void update(float delta) {
		x += dx * delta;
		y += dy * delta;

		timer += delta;
		if (timer >= lifeLength) {
			super.remove();
		}
	}

	@Override
	public void onCollision(Entity e) {
		if (e instanceof Player) {
			Player p = (Player) e;
			if (p != sender) {
				super.remove();
				p.damage(1);
			}
		}
	}

	public void createSnapshot(SnapshotPacket snapshot) {
		EntityData data = snapshot.initEntity(id, EntityType.BULLET);
		data.putFloat("x", x);
		data.putFloat("y", y);
		data.putFloat("dx", dx);
		data.putFloat("dy", dy);
	}

}
