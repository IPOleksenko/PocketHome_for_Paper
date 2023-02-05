package dev.ipoleksenko.PocketHome.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class OfflinePlayerDataType implements PersistentDataType<byte[], OfflinePlayer> {

	@Override
	@NotNull
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	@NotNull
	public Class<OfflinePlayer> getComplexType() {
		return OfflinePlayer.class;
	}

	@Override
	public byte @NotNull [] toPrimitive(@NotNull OfflinePlayer complex, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		UUID playerUID = complex.getUniqueId();

		bb.putLong(playerUID.getMostSignificantBits());
		bb.putLong(playerUID.getLeastSignificantBits());

		return bb.array();
	}

	@Override
	@NotNull
	public OfflinePlayer fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);
		long mostSigBits = bb.getLong();
		long leastSigBits = bb.getLong();
		UUID playerUID = new UUID(mostSigBits, leastSigBits);

		return Bukkit.getOfflinePlayer(playerUID);
	}
}
