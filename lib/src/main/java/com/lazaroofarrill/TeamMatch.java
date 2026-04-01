package com.lazaroofarrill;

import java.time.Instant;
import java.util.UUID;

public record TeamMatch(Team home, Team away, Instant startedAt, UUID matchId) {
  public TeamMatch {
    if (home == null || away == null || startedAt == null || matchId == null) {
      throw new IllegalArgumentException("None of the argumens can be null");
    }
  }
}
