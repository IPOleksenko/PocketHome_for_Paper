package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UUIDListDataType implements PersistentDataType<byte[], List> {
	@Override
	@NotNull
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	@NotNull
	public Class<List> getComplexType() {
		return List.class;
	}

	@Override
	public byte @NotNull [] toPrimitive(@NotNull List complex, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[complex.size() * 16]);

		for (UUID uuid : (List<UUID>) complex) {
			bb.putLong(uuid.getMostSignificantBits());
			bb.putLong(uuid.getLeastSignificantBits());
		}

		return bb.array();
	}

	@Override
	@NotNull
	public List<UUID> fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);
		List<UUID> uuids = new ArrayList<UUID>();

		for (int i = 0; i < primitive.length / 16; ++i) {
			long mostSigBits = bb.getLong();
			long leastSigBits = bb.getLong();
			uuids.add(new UUID(mostSigBits, leastSigBits));
		}

		return uuids;
	}
}
