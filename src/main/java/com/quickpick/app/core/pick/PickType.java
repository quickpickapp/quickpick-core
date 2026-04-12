package com.quickpick.app.core.pick;

public enum PickType {
  QUESTION,
  SURVEY;

  public static boolean isValid(String value) {
    try {
      valueOf(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
