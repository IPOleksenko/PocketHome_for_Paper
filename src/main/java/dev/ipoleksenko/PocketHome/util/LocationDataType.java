package dev.ipoleksenko.PocketHome.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public class LocationDataType implements PersistentDataType<byte[], Location> {

	@Override
	@NotNull
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	@NotNull
	public Class<Location> getComplexType() {
		return Location.class;
	}

	@Override
	public byte @NotNull [] toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[48]);
		UUID worldUID = complex.getWorld().getUID();

		bb.putLong(worldUID.getMostSignificantBits());
		bb.putLong(worldUID.getLeastSignificantBits());

		bb.putDouble(complex.getX());
		bb.putDouble(complex.getY());
		bb.putDouble(complex.getZ());
		bb.putFloat(complex.getYaw());
		bb.putFloat(complex.getPitch());

		return bb.array();
	}

	@Override
	@NotNull
	public Location fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);

		long mostSigBits = bb.getLong();
		long leastSigBits = bb.getLong();
		UUID worldUID = new UUID(mostSigBits, leastSigBits);

		double x = bb.getDouble();
		double y = bb.getDouble();
		double z = bb.getDouble();
		float yaw = bb.getFloat();
		float pitch = bb.getFloat();

		World world = Bukkit.getWorld(worldUID);
		return new Location(world, x, y, z, yaw, pitch);
	}
}
