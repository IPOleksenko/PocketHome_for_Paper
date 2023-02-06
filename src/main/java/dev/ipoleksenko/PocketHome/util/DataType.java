package dev.ipoleksenko.PocketHome.util;

public interface DataType {
	UUIDDataType UUID = new UUIDDataType();
	LocationDataType LOCATION = new LocationDataType();

	UUIDListDataType UUID_LIST = new UUIDListDataType();
}
