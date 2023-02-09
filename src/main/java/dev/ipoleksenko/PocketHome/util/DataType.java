package dev.ipoleksenko.PocketHome.util;

public interface DataType {
	UUIDDataType UUID = new UUIDDataType();
	LocationDataType LOCATION = new LocationDataType();

	StringListDataType STRING_LIST = new StringListDataType();
	UUIDListDataType UUID_LIST = new UUIDListDataType();
}
