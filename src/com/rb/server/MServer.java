package com.rb.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.rb.server.entity.Bullet;
import com.rb.server.entity.Door;
import com.rb.server.entity.Entity;
import com.rb.server.entity.Hitbox;
import com.rb.server.entity.scenery.Diner;
import com.rb.shared.packets.ChatMessagePacket;
import com.rb.shared.packets.LevelSwitchPacket;
import com.rb.shared.packets.PlayerConnectPacket;
import com.rb.shared.packets.PlayerDataPacket;
import com.rb.shared.packets.PlayerSpawnPacket;
import com.rb.shared.packets.ReceivedPacket;
import com.rb.shared.packets.ShootPacket;
import com.rb.shared.packets.SnapshotPacket;
import com.rb.shared.packets.LevelDataPacket;

public class MServer {

	public static MServer instance;

	private Server server;

	private List<ReceivedPacket> packetQueue;
	private Map<Connection, Player> players;

	private Map<String, Level> levels;

	public MServer(Server server) {
		this.server = server;

		packetQueue = new ArrayList<ReceivedPacket>();
		players = new HashMap<Connection, Player>();
		levels = new HashMap<String, Level>();

		loadWorldFiles();

		instance = this;
	}

	private void loadWorldFiles() {
		System.out.println("Starting to load world...");

		loadLevel("test", 20, 20);
		loadLevel("room", 10, 10);

		levels.get("test").loadEntity(new Diner(nextEntityId(), 128, 128, levels.get("test")));
		levels.get("test").loadEntity(new Door(nextEntityId(), 64, 64, levels.get("test"), levels.get("room"), 0, 0, new Hitbox(0, 0, 16, 16)));
		levels.get("room").loadEntity(new Door(nextEntityId(), 144, 144, levels.get("room"), levels.get("test"), 10, 10, new Hitbox(0, 0, 16, 16)));

		System.out.println("Finished loading world");
	}

	private void loadLevel(String name, int width, int height) {
		System.out.println("Loading level " + name);
		levels.put(name, new Level(name, width, height));
	}

	public void switchLevel(Entity entity, Level target, float x, float y) {
		entity.level.unloadEntity(entity);

		entity.level = target;
		entity.x = x;
		entity.y = y;

		entity.level.loadEntity(entity);

		if (entity instanceof Player) {
			Player player = (Player) entity;
			LevelSwitchPacket switchPacket = new LevelSwitchPacket();
			switchPacket.levelName = target.name;
			switchPacket.x = x;
			switchPacket.y = y;
			player.connection.sendTCP(switchPacket);
			sendLevelData(player);
		}
	}

	public void tick1() {
		for (Player player : players.values()) {
			sendLevelData(player);
		}
	}

	public void tick10(float delta) {
		updateNetworking();
		updateWorld(delta);
		sendSnapshot();
	}

	private void updateNetworking() {
		while (!packetQueue.isEmpty()) {
			ReceivedPacket packet = packetQueue.get(0);
			onPacketReceived(packet);
			synchronized (packetQueue) {
				packetQueue.remove(0);
			}
		}
	}

	private void updateWorld(float delta) {
		for (Level level : levels.values()) {
			level.update(delta);
		}
	}

	private void sendSnapshot() {
		for (Connection connection : players.keySet()) {
			SnapshotPacket snapshot = new SnapshotPacket();

			players.get(connection).level.createSnapshot(snapshot);
			connection.sendUDP(snapshot);
		}
		for (Level level : levels.values()) {
			level.finishSnapshot();
		}
	}

	public void onPacketReceived(Connection connection, Object object) {
		synchronized (packetQueue) {
			packetQueue.add(new ReceivedPacket(connection, object));
		}
	}

	public void onPacketReceived(ReceivedPacket packet) {
		Connection connection = packet.connection;
		Object object = packet.object;

		if (object instanceof PlayerConnectPacket) {
			PlayerConnectPacket connectPacket = (PlayerConnectPacket) object;
			String username = connectPacket.username;

			int id = nextEntityId();
			Level playerLevel = levels.get("test");
			Player player = new Player(username, id, connectPacket.color, connection, 0.0f, 0.0f, playerLevel);
			player.x = 10 * 16;
			player.y = 10 * 16;
			loadPlayer(player);

			System.out.println("Player " + username + " connected");

			ChatMessagePacket loginMessage = new ChatMessagePacket();
			loginMessage.message = username + " has joined the game";
			server.sendToAllTCP(loginMessage);

			sendSpawnData(player, id);
			sendLevelData(player);
		} else if (object instanceof PlayerDataPacket) {
			PlayerDataPacket playerData = (PlayerDataPacket) object;
			Player player = players.get(connection);
			if (player != null) {
				player.setDataPacket(playerData);
			}
		} else if (object instanceof ShootPacket) {
			ShootPacket shootPacket = (ShootPacket) object;
			Player sender = (Player) getEntityById(shootPacket.sender);
			Bullet bullet = new Bullet(nextEntityId(), shootPacket.angle, shootPacket.speed, sender, 0.0f, 0.0f, sender.level);
			bullet.x = shootPacket.x;
			bullet.y = shootPacket.y;
			loadEntity(bullet);
		} else if (object instanceof ChatMessagePacket) {
			ChatMessagePacket chatPacket = (ChatMessagePacket) object;
			String sender = players.get(connection).username;
			System.out.println("<" + sender + "> " + chatPacket.message);
			chatPacket.sender = sender;
			server.sendToAllTCP(chatPacket);
		} else {
			//System.err.println("Unknown packet type");
		}
	}

	private void sendSpawnData(Player player, int id) {
		PlayerSpawnPacket spawnPacket = new PlayerSpawnPacket();
		spawnPacket.load(id, player.x, player.y);
		player.connection.sendTCP(spawnPacket);
	}

	private void sendLevelData(Player player) {
		LevelDataPacket dataPacket = new LevelDataPacket();
		player.level.sendLevelData(player, dataPacket);
		player.connection.sendTCP(dataPacket);
	}

	public void onPlayerDisconnect(Connection connection) {
		Player player = players.get(connection);
		players.remove(connection);
		player.remove();
		//unloadPlayer(player);
		if (player != null) {
			System.out.println("Player " + player.username + " disconnected");
			ChatMessagePacket logoutMessage = new ChatMessagePacket();
			logoutMessage.message = player.username + " has left the game";
			server.sendToAllTCP(logoutMessage);
		}
	}

	private void loadPlayer(Player p) {
		players.put(p.connection, p);
		p.level.loadEntity(p);
	}

	private void loadEntity(Entity e) {
		e.level.loadEntity(e);
	}

	//	private void unloadPlayer(Player p) {
	//		players.remove(p.connection);
	//		p.level.unloadEntity(p);
	//
	//		EntityRemovePacket entityRemove = new EntityRemovePacket();
	//		entityRemove.id = p.id;
	//		server.sendToAllTCP(entityRemove);
	//	}
	//
	//	private void unloadEntity(Entity e) {
	//		e.level.unloadEntity(e);
	//
	//		EntityRemovePacket entityRemove = new EntityRemovePacket();
	//		entityRemove.id = e.id;
	//		server.sendToAllTCP(entityRemove);
	//	}

	private Entity getEntityById(int id) {
		Entity entity;
		for (Level level : levels.values()) {
			if ((entity = level.getEntityById(id)) != null) {
				return entity;
			}
		}
		return null;
	}

	static int ptr = 0;

	private int nextEntityId() {
		return (int) System.currentTimeMillis() + ptr++;
	}
}
