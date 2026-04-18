package com.lazaroofarrill.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lazaroofarrill.InMemoryScoreboard;
import com.lazaroofarrill.Scoreboard;
import com.lazaroofarrill.Team;
import com.lazaroofarrill.TeamMatch;

class ScoreboardTest {

  // Team
  @Test
  void teamNameMayNotBeBlank() {
    assertThrows(IllegalArgumentException.class, () -> new Team("", 0));
    assertThrows(IllegalArgumentException.class, () -> new Team("   ", 0));
    assertThrows(IllegalArgumentException.class, () -> new Team(null, 0));
  }

  @Test
  void teamPointsMayNotBeNegative() {
    assertThrows(IllegalArgumentException.class, () -> new Team("Home", -1));
  }

  // TeamMatch
  @Test
  void parametersCannotBeNull() {
    var home = new Team("home", 0);
    var away = new Team("away", 0);
    var startedAt = Instant.now();
    var matchId = UUID.randomUUID();

    assertThrows(IllegalArgumentException.class, () -> new TeamMatch(null, away, startedAt, matchId));
    assertThrows(IllegalArgumentException.class, () -> new TeamMatch(home, null, startedAt, matchId));
    assertThrows(IllegalArgumentException.class, () -> new TeamMatch(home, away, null, matchId));
    assertThrows(IllegalArgumentException.class, () -> new TeamMatch(home, away, startedAt, null));
  }

  // startMatch
  @Test
  void matchesAreStartedCorrectly() throws InterruptedException {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var home = "Home";
    var away = "Away";
    scoreBoard.startMatch(home, away);
    var matches = scoreBoard.getSummary();

    assertEquals(matches.size(), 1);
    assertEquals(matches.getFirst().home().name(), home);
    assertEquals(matches.getFirst().away().name(), away);
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      Thread.sleep(20);
    }
    assertTrue(matches.getFirst().startedAt().isBefore(Instant.now()));
    assertEquals(matches.getFirst().home().points(), 0);
    assertEquals(matches.getFirst().away().points(), 0);
  }

  @Test
  void startMatchReturnsUniqueIds() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var id1 = scoreBoard.startMatch("Home", "Away");
    var id2 = scoreBoard.startMatch("Home2", "Away2");
    assertNotEquals(id1, id2);
  }

  // updateScores
  @Test
  void aMatchScoreCanBeUpdated() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var matchId = scoreBoard.startMatch("home", "away");
    scoreBoard.updateScores(matchId, 5, 4);

    var matches = scoreBoard.getSummary();
    assertEquals(matches.size(), 1);
    assertEquals(matches.getFirst().home().points(), 5);
    assertEquals(matches.getFirst().away().points(), 4);
  }

  @Test
  void updateScoresThrowsWhenMatchNotFound() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(UUID.randomUUID(), 1, 1));
  }

  @Test
  void updateScoresWithNegativePointsThrows() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var matchId = scoreBoard.startMatch("Home", "Away");
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(matchId, -1, 0));
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(matchId, 0, -1));
  }

  @Test
  void updateScoresWithNegativePointsConservesGames() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var matchId = scoreBoard.startMatch("Home", "Away");

    scoreBoard.updateScores(matchId, 2, 3);

    var summary = scoreBoard.getSummary();

    assertEquals(summary.size(), 1);

    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(matchId, -1, 0));

    var summaryAfterInvalidScore = scoreBoard.getSummary();
    assertEquals(summaryAfterInvalidScore.size(), 1);

    assertEquals(summaryAfterInvalidScore.get(0).home().points(), 2);
    assertEquals(summaryAfterInvalidScore.get(0).away().points(), 3);
  }

  // Summary
  @Test
  void matchesAreStoredSortedByPointSumDescAndThenStartTimeDesc() throws InterruptedException {
    var clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    Scoreboard scoreBoard = new InMemoryScoreboard(clock);

    var mexicoVSCanada = scoreBoard.startMatch("Mexico", "Canada");
    scoreBoard.updateScores(mexicoVSCanada, 0, 5);
    clock.advance(Duration.ofMillis(1));

    var spainVSBrazil = scoreBoard.startMatch("Spain", "Brazil");
    scoreBoard.updateScores(spainVSBrazil, 10, 2);
    clock.advance(Duration.ofMillis(1));

    var germanyVsFrance = scoreBoard.startMatch("Germany", "France");
    scoreBoard.updateScores(germanyVsFrance, 2, 2);
    clock.advance(Duration.ofMillis(1));

    var uruguayVsItaly = scoreBoard.startMatch("Uruguay", "Italy");
    scoreBoard.updateScores(uruguayVsItaly, 6, 6);
    clock.advance(Duration.ofMillis(1));

    var argentinaVsAustralia = scoreBoard.startMatch("Argentina", "Australia");
    scoreBoard.updateScores(argentinaVsAustralia, 3, 1);

    var matches = scoreBoard.getSummary();
    assertEquals(matches.size(), 5);
    assertEquals(uruguayVsItaly, matches.get(0).matchId());
    assertEquals(spainVSBrazil, matches.get(1).matchId());
    assertEquals(mexicoVSCanada, matches.get(2).matchId());
    assertEquals(argentinaVsAustralia, matches.get(3).matchId());
    assertEquals(germanyVsFrance, matches.get(4).matchId());
  }

  // Finish match
  @Test
  void matchIsRemovedAfterFinishing() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var mexicoVSCanada = scoreBoard.startMatch("Mexico", "Canada");

    var matchesBefore = scoreBoard.getSummary();
    assertEquals(matchesBefore.size(), 1);

    scoreBoard.finishMatch(mexicoVSCanada);
    var matchesAfter = scoreBoard.getSummary();
    assertEquals(matchesAfter.size(), 0);
  }

  @Test
  void matchCantbeUpdatedAfterFinishing() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var mexicoVSCanada = scoreBoard.startMatch("Mexico", "Canada");

    var matchesBefore = scoreBoard.getSummary();
    assertEquals(matchesBefore.size(), 1);

    scoreBoard.finishMatch(mexicoVSCanada);

    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(mexicoVSCanada, 1, 1));
  }

  @Test
  void finishMatchThrowsWhenMatchNotFound() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.finishMatch(UUID.randomUUID()));
  }

  static class MutableClock extends Clock {
    private Instant instant;

    MutableClock(Instant start) {
      this.instant = start;
    }

    void advance(Duration d) {
      this.instant = instant.plus(d);
    }

    @Override
    public Instant instant() {
      return instant;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }
  }
}
