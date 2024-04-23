package ru.markovav.excursionbot;

import java.util.Base64;
import java.util.UUID;

public class Base64Utils {
  public static String uuidToB64(UUID uuid) {
    return Base64.getUrlEncoder().encodeToString(uuid.toString().getBytes());
  }

  public static UUID b64ToUuid(String b64) {
    return UUID.fromString(new String(Base64.getUrlDecoder().decode(b64)));
  }

  private Base64Utils() {
  }
}
