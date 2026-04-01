package com.lazaroofarrill;


public record Team(String name, int points) {
  public Team(String name, int points) {
    if (name.trim().equals("")) {
      throw new IllegalArgumentException("Name may not be blank");
    }
    if (points < 0) {
      throw new IllegalArgumentException("Points may not be negative");
    }
    this.name = name;
    this.points = points;
  }

  public Team withPoints(int newPoints) {
    if (newPoints < 0) {
      throw new IllegalArgumentException("Points cannot be negative");
    }
    return new Team(this.name, newPoints);
  }
}

