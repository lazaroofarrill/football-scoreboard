package com.lazaroofarrill;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface Scoreboard {
  UUID startMatch(String home, String away, Instant time);

  List<TeamMatch> getSummary();

  void updateScores(UUID matchId, int homeScore, int awayScore);

  void finishMatch(UUID matchId);
}
