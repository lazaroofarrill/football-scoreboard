package com.lazaroofarrill;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

public class InMemoryScoreboard implements Scoreboard {
  private final Clock clock;

  private final TreeSet<TeamMatch> sortedMatches = new TreeSet<>(
      Comparator
          .comparingInt((TeamMatch t) -> t.home().points() + t.away().points()).reversed()
          .thenComparing(Comparator.comparing(TeamMatch::startedAt).reversed())
          .thenComparing(TeamMatch::matchId)); // Final segregator
  private final HashMap<UUID, TeamMatch> indexedMatches = new HashMap<>();

  public InMemoryScoreboard() {
    this.clock = Clock.systemUTC();
  }

  public InMemoryScoreboard(Clock clock) {
    this.clock = clock;
  }

  @Override
  public UUID startMatch(String home, String away) {
    var uuid = UUID.randomUUID();
    var match = new TeamMatch(
        new Team(home, 0),
        new Team(away, 0),
        Instant.now(clock),
        uuid);
    sortedMatches.add(match);
    indexedMatches.put(uuid, match);
    return uuid;
  }

  @Override
  public void updateScores(UUID matchId, int homeScore, int awayScore) {
    var target = findTeamMatch(matchId);

    sortedMatches.remove(target);
    try {
      var newMatch = new TeamMatch(
          target.home().withPoints(homeScore),
          target.away().withPoints(awayScore),
          target.startedAt(), target.matchId());
      sortedMatches.add(newMatch);
      indexedMatches.put(newMatch.matchId(), newMatch);
    } catch (IllegalArgumentException e) {
      sortedMatches.add(target);
      throw e;
    }
  }

  @Override
  public void finishMatch(UUID matchId) {
    var target = findTeamMatch(matchId);
    sortedMatches.remove(target);
    indexedMatches.remove(target.matchId());
  }

  @Override
  public List<TeamMatch> getSummary() {
    return new ArrayList<>(sortedMatches);
  }

  private TeamMatch findTeamMatch(UUID matchId) {
    var target = indexedMatches.get(matchId);
    if (target == null) {
      throw new IllegalArgumentException("The match with the requested ID was not found");
    }
    return target;
  }
}
