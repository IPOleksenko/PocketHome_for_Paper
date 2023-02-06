package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class UUIDListDataType implements PersistentDataType<int[][], List> {
	@Override
	@NotNull
	public Class<int[][]> getPrimitiveType() {
		return int[][].class;
	}

	@Override
	@NotNull
	public Class<List> getComplexType() {
		return List.class;
	}

	@Override
	public int @NotNull [][] toPrimitive(@NotNull List complex, @NotNull PersistentDataAdapterContext context) {
		int[][] primitive = new int[complex.size()][4];
		List<int[]> ints = ((List<UUID>) complex).stream().map(UUIDDataType::toInts).toList();

		for (int i = 0; i < ints.size(); ++i)
			primitive[i] = ints.get(i);

		return primitive;
	}

	@Override
	@NotNull
	public List<UUID> fromPrimitive(int @NotNull [][] primitive, @NotNull PersistentDataAdapterContext context) {
		return Arrays.stream(primitive).map(UUIDDataType::fromInts).toList();
	}
}
