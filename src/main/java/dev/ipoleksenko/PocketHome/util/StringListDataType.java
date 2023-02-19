package dev.ipoleksenko.PocketHome.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StringListDataType implements PersistentDataType<String, List<String>> {
	@Override
	public @NotNull Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public @NotNull Class<List<String>> getComplexType() {
		return (Class<List<String>>) (Class<?>) List.class;
	}

	@Override
	public @NotNull String toPrimitive(@NotNull List<String> complex, @NotNull PersistentDataAdapterContext context) {
		return String.join(";", complex);
	}

	@Override
	public @NotNull List<String> fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
		return new LinkedList<>(Arrays.stream(primitive.split(";")).toList());
	}
}
