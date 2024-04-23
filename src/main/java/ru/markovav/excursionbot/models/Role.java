package ru.markovav.excursionbot.models;

import lombok.Getter;

@Getter
public enum Role implements Comparable<Role> {
  PARTICIPANT(0),
  GUIDE(1),
  ADMIN(2);

  private final int value;

  Role(int value) {
    this.value = value;
  }
}
