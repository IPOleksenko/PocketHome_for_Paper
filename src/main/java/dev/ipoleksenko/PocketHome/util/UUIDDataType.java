package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDDataType implements PersistentDataType<byte[], UUID> {

	@Override
	@NotNull
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	@NotNull
	public Class<UUID> getComplexType() {
		return UUID.class;
	}

	@Override
	public byte @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(complex.getMostSignificantBits());
		bb.putLong(complex.getLeastSignificantBits());
		return bb.array();
	}

	@Override
	@NotNull
	public UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);
		long mostSigBits = bb.getLong();
		long leastSigBits = bb.getLong();
		return new UUID(mostSigBits, leastSigBits);
	}
}
