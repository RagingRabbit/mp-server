package com.rb.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rb.server.entity.Entity;
import com.rb.shared.packets.SnapshotPacket;
import com.rb.shared.packets.LevelDataPacket;

public class Level {

	public final String name;
	private int width, height;

	private List<Entity> entities;
	private List<Integer> removedEntities;

	private byte[] tileData;

	public Level(String name, int width, int height) {
		this.name = name;
		this.width = width;
		this.height = height;

		entities = new ArrayList<Entity>();
		removedEntities = new ArrayList<Integer>();

		loadLevel();
	}

	private void loadLevel() {
		tileData = new byte[width * height];
		Random random = new Random();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				tileData[x + y * width] = (byte) (random.nextBoolean() ? 20 : 30);
			}
		}
	}

	public void update(float delta) {
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			if (e.isRemoved()) {
				unloadEntity(e);
				i--;
			}
		}
		for (int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.update(delta);
		}
		calculateCollisions();
	}

	private void calculateCollisions() {
		for (int i = 0; i < entities.size(); i++) {
			for (int j = i + 1; j < entities.size(); j++) {
				Entity e1 = entities.get(i);
				Entity e2 = entities.get(j);
				if (entitiesCollide(e1, e2)) {
					e1.onCollision(e2);
					e2.onCollision(e1);
				}
			}
		}
	}

	private boolean entitiesCollide(Entity e1, Entity e2) {
		if (e1.hitbox == null || e2.hitbox == null) {
			return false;
		}
		float x01 = e1.x + e1.hitbox.x;
		float x11 = e1.x + e1.hitbox.x + e1.hitbox.w;
		float y01 = e1.y + e1.hitbox.y;
		float y11 = e1.y + e1.hitbox.y + e1.hitbox.h;

		float x02 = e2.x + e2.hitbox.x;
		float x12 = e2.x + e2.hitbox.x + e2.hitbox.w;
		float y02 = e2.y + e2.hitbox.y;
		float y12 = e2.y + e2.hitbox.y + e2.hitbox.h;

		if (x01 < x12 && x11 > x02 && y01 < y12 && y11 > y02) {
			//			System.out.println("---");
			//			System.out.println(x01);
			//			System.out.println(x11);
			//			System.out.println(y01);
			//			System.out.println(y11);
			//			System.out.println(x02);
			//			System.out.println(x12);
			//			System.out.println(y02);
			//			System.out.println(y12);
			//			System.out.println("---");
			return true;
		}
		return false;
	}

	public void createSnapshot(SnapshotPacket snapshot) {
		for (Entity entity : entities) {
			entity.createSnapshot(snapshot);
		}
		for (int entity : removedEntities) {
			snapshot.removedEntities.add(entity);
		}
	}

	public void finishSnapshot() {
		removedEntities.clear();
	}

	public void sendLevelData(Player player, LevelDataPacket dataPacket) {
		dataPacket.load(player.x, player.y, tileData, width, height);
	}

	public void loadEntity(Entity e) {
		entities.add(e);
	}

	public void unloadEntity(Entity e) {
		removedEntities.add(e.id);
		entities.remove(e);
	}

	public Entity getEntityById(int id) {
		for (Entity e : entities) {
			if (e.id == id) {
				return e;
			}
		}
		return null;
	}

}
