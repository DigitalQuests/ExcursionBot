package ru.markovav.excursionbot;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class Base64Utils {
  public static String uuidToB64(UUID uuid) {
    var buffer = ByteBuffer.allocate(16);
    buffer.putLong(uuid.getMostSignificantBits());
    buffer.putLong(uuid.getLeastSignificantBits());
    return Base64.getEncoder().encodeToString(buffer.array());
  }

  public static UUID b64ToUuid(String b64) {
    var byteArray = Base64.getDecoder().decode(b64);
    var buffer = ByteBuffer.wrap(byteArray);
    return new UUID(buffer.getLong(), buffer.getLong());
  }

  private Base64Utils() {
  }
}
