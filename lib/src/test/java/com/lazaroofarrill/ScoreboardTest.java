package com.lazaroofarrill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ScoreboardTest {

  // Team
  @Test
  void teamNameMayNotBeBlank() {
    assertThrows(IllegalArgumentException.class, () -> new Team("", 0));
    assertThrows(IllegalArgumentException.class, () -> new Team("   ", 0));
  }

  @Test
  void teamPointsMayNotBeNegative() {
    assertThrows(IllegalArgumentException.class, () -> new Team("Home", -1));
  }

  // startMatch
  @Test
  void matchesAreStartedCorrectly() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var home = "Home";
    var away = "Away";
    var startTime = Instant.now();
    scoreBoard.startMatch(home, away, startTime);
    var matches = scoreBoard.getSummary();

    assertEquals(matches.size(), 1);
    assertEquals(matches.getFirst().home().name(), home);
    assertEquals(matches.getFirst().away().name(), away);
    assertEquals(matches.getFirst().startedAt(), startTime);
    assertEquals(matches.getFirst().home().points(), 0);
    assertEquals(matches.getFirst().away().points(), 0);
  }

  @Test
  void startMatchReturnsUniqueIds() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var id1 = scoreBoard.startMatch("Home", "Away", Instant.now());
    var id2 = scoreBoard.startMatch("Home2", "Away2", Instant.now());
    assertNotEquals(id1, id2);
  }

  // updateScores
  @Test
  void aMatchScoreCanBeUpdated() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var matchId = scoreBoard.startMatch("home", "away", Instant.now());
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
    var matchId = scoreBoard.startMatch("Home", "Away", Instant.now());
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(matchId, -1, 0));
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.updateScores(matchId, 0, -1));
  }

  // Summary
  @Test
  void matchesAreStoredSortedByPointSumDescAndThenStartTimeDesc() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var leagueStart = Instant.now();
    var mexicoVSCanada = scoreBoard.startMatch("Mexico", "Canada", leagueStart);
    scoreBoard.updateScores(mexicoVSCanada, 0, 5);
    var spainVSBrazil = scoreBoard.startMatch("Spain", "Brazil", leagueStart.plus(1, ChronoUnit.MINUTES));
    scoreBoard.updateScores(spainVSBrazil, 10, 2);
    var germanyVsFrance = scoreBoard.startMatch("Germany", "France", leagueStart.plus(2, ChronoUnit.MINUTES));
    scoreBoard.updateScores(germanyVsFrance, 2, 2);
    var uruguayVsItaly = scoreBoard.startMatch("Uruguay", "Italy", leagueStart.plus(3, ChronoUnit.MINUTES));
    scoreBoard.updateScores(uruguayVsItaly, 6, 6);
    var argentinaVsAustralia = scoreBoard.startMatch("Argentina", "Australia", leagueStart.plus(4, ChronoUnit.MINUTES));
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
    var leagueStart = Instant.now();
    var mexicoVSCanada = scoreBoard.startMatch("Mexico", "Canada", leagueStart);

    var matchesBefore = scoreBoard.getSummary();
    assertEquals(matchesBefore.size(), 1);

    scoreBoard.finishMatch(mexicoVSCanada);
    var matchesAfter = scoreBoard.getSummary();
    assertEquals(matchesAfter.size(), 0);
  }

  @Test
  void finishMatchThrowsWhenMatchNotFound() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.finishMatch(UUID.randomUUID()));
  }
}
