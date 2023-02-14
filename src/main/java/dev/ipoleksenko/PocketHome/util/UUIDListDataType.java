package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class UUIDListDataType implements PersistentDataType<int[], List> {
	@Override
	@NotNull
	public Class<int[]> getPrimitiveType() {
		return int[].class;
	}

	@Override
	@NotNull
	public Class<List> getComplexType() {
		return List.class;
	}

	@Override
	public int @NotNull [] toPrimitive(@NotNull List complex, @NotNull PersistentDataAdapterContext context) {
		int[] primitive = new int[complex.size() * 4];
		List<int[]> ints = ((List<UUID>) complex).stream().map(UUIDDataType::toInts).toList();

		for (int i = 0; i < ints.size(); ++i) {
			primitive[i] = ints.get(i / 4)[i % 4];
			primitive[++i] = ints.get(i / 4)[i % 4];
			primitive[++i] = ints.get(i / 4)[i % 4];
			primitive[++i] = ints.get(i / 4)[i % 4];
		}

		return primitive;
	}

	@Override
	@NotNull
	public List<UUID> fromPrimitive(int @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		int[][] ints = new int[primitive.length / 4][4];

		for (int i = 0; i < primitive.length / 4; ++i)
			System.arraycopy(primitive, i * 4, ints[i], 0, 4);

		return new LinkedList<>(Arrays.stream(ints).map(UUIDDataType::fromInts).toList());
	}
}
