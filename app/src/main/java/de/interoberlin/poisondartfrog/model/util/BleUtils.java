package de.interoberlin.poisondartfrog.model.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class BleUtils {
    // --------------------
    // Methods
    // --------------------

    public static UUID fromBytes(byte[] value) {
        ByteBuffer bb = ByteBuffer.wrap(value);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static String getLongUUID(UUID uuid) {
        return uuid.toString();
    }

    public static String getShortUUID(UUID uuid) {
        return getShortUUID(uuid.toString());
    }

    public static String getShortUUID(String uuid) {
        return uuid.substring(4, 8);
    }
}
