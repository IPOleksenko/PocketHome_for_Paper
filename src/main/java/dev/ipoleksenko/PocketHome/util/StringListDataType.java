package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StringListDataType implements PersistentDataType<String, List> {
	@Override
	public @NotNull Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public @NotNull Class<List> getComplexType() {
		return List.class;
	}

	@Override
	public @NotNull String toPrimitive(@NotNull List complex, @NotNull PersistentDataAdapterContext context) {
		return String.join(";", complex);
	}

	@Override
	public @NotNull List fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
		return new LinkedList<>(Arrays.stream(primitive.split(";")).toList());
	}
}
