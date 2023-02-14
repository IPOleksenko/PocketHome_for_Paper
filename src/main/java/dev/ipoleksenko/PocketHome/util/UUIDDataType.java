package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UUIDDataType implements PersistentDataType<int[], UUID> {

	public static int @NotNull [] toInts(@NotNull UUID uuid) {
		int[] ints = new int[4];
		long mostSigBits = uuid.getMostSignificantBits();
		long leastSigBits = uuid.getLeastSignificantBits();

		ints[0] = (int) (mostSigBits >> 32);
		ints[1] = (int) mostSigBits;
		ints[2] = (int) (leastSigBits >> 32);
		ints[3] = (int) leastSigBits;

		return ints;
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull UUID fromInts(int @NotNull [] ints) {
		long mostSigBits = (long) ints[0] << 32 | ints[1] & 0xFFFFFFFFL;
		long leastSigBits = (long) ints[2] << 32 | ints[3] & 0xFFFFFFFFL;

		return new UUID(mostSigBits, leastSigBits);
	}

	@Override
	@NotNull
	public Class<int[]> getPrimitiveType() {
		return int[].class;
	}

	@Override
	@NotNull
	public Class<UUID> getComplexType() {
		return UUID.class;
	}

	@Override
	public int @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
		return toInts(complex);
	}

	@Override
	@NotNull
	public UUID fromPrimitive(int @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		return fromInts(primitive);
	}
}
