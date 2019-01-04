package com.rb.server;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.rb.shared.entity.EntityType;
import com.rb.shared.packets.ChatMessagePacket;
import com.rb.shared.packets.EntityRemovePacket;
import com.rb.shared.packets.LevelSwitchPacket;
import com.rb.shared.packets.PlayerConnectPacket;
import com.rb.shared.packets.PlayerDataPacket;
import com.rb.shared.packets.PlayerSpawnPacket;
import com.rb.shared.packets.ShootPacket;
import com.rb.shared.packets.SnapshotPacket;
import com.rb.shared.packets.SnapshotPacket.EntityData;
import com.rb.shared.packets.TestPacket;
import com.rb.shared.packets.LevelDataPacket;

public class ServerMain {
	private static final int PORT_TCP = 4444;
	private static final int PORT_UDP = 4445;

	private JFrame frame;
	private Server server;
	private Kryo kryo;
	private boolean running;

	private MServer world;

	public ServerMain(int tcpPort, int udpPort) {
		initFrame();
	}

	public void start() throws IOException {
		server = new Server(15000, 15000);
		server.start();
		server.bind(PORT_TCP, PORT_UDP);

		kryo = server.getKryo();

		kryo.register(EntityType.class);

		kryo.register(TestPacket.class);
		kryo.register(PlayerConnectPacket.class);
		kryo.register(PlayerDataPacket.class);
		kryo.register(SnapshotPacket.class);
		kryo.register(EntityData.class);
		kryo.register(PlayerSpawnPacket.class);
		kryo.register(EntityRemovePacket.class);
		kryo.register(LevelSwitchPacket.class);

		kryo.register(LevelDataPacket.class);

		kryo.register(ChatMessagePacket.class);

		kryo.register(ShootPacket.class);

		kryo.register(int[].class);
		kryo.register(byte[].class);
		kryo.register(boolean[].class);
		kryo.register(ArrayList.class);
		kryo.register(HashMap.class);

		server.addListener(new Listener() {
			@Override
			public void disconnected(Connection connection) {
				world.onPlayerDisconnect(connection);
			}

			@Override
			public void received(Connection connection, Object object) {
				world.onPacketReceived(connection, object);
			}
		});

		world = new MServer(server);

		long lastTick10 = System.nanoTime();
		long lastSecond = System.nanoTime();

		running = true;
		while (running) {
			long now = System.nanoTime();
			if (now - lastSecond >= 1e9) {
				lastSecond = now;
				world.tick1();
			}

			if (now - lastTick10 >= 1e9 / 10) {
				long delta = now - lastTick10;
				lastTick10 = now;
				world.tick10(delta / 1e9f);
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void initFrame() {
		frame = new JFrame();
		frame.setTitle("Server");
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				running = false;
				server.close();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ServerMain server = new ServerMain(PORT_TCP, PORT_UDP);
		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
