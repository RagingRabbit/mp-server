package com.rb.server.entity.scenery;

import com.rb.server.Level;
import com.rb.shared.entity.EntityType;
import com.rb.shared.packets.SnapshotPacket;
import com.rb.shared.packets.SnapshotPacket.EntityData;

public class Diner extends Scenery {

	public Diner(int id, float x, float y, Level level) {
		super(id, x, y, level);
	}

	@Override
	public void createSnapshot(SnapshotPacket snapshot) {
		EntityData data = snapshot.initEntity(id, EntityType.DINER);
		data.putFloat("x", x);
		data.putFloat("y", y);
	}

}
