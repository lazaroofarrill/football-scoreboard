package com.lazaroofarrill.consumer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.lazaroofarrill.InMemoryScoreboard;
import com.lazaroofarrill.Scoreboard;
import com.lazaroofarrill.Team;

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
    scoreBoard.startMatch(home, away);
    var matches = scoreBoard.getSummary();

    assertEquals(matches.size(), 1);
    assertEquals(matches.getFirst().home().name(), home);
    assertEquals(matches.getFirst().away().name(), away);
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

  // Summary
  @Test
  void matchesAreStoredSortedByPointSumDescAndThenStartTimeDesc() throws InterruptedException {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    var mexicoVSCanada = scoreBoard.startMatch("Mexico", "Canada");
    scoreBoard.updateScores(mexicoVSCanada, 0, 5);
    Thread.sleep(1); // Prevent non deterministic inputs on the same millisecond on fast hardware

    var spainVSBrazil = scoreBoard.startMatch("Spain", "Brazil");
    scoreBoard.updateScores(spainVSBrazil, 10, 2);
    Thread.sleep(1);

    var germanyVsFrance = scoreBoard.startMatch("Germany", "France");
    scoreBoard.updateScores(germanyVsFrance, 2, 2);
    Thread.sleep(1);

    var uruguayVsItaly = scoreBoard.startMatch("Uruguay", "Italy");
    scoreBoard.updateScores(uruguayVsItaly, 6, 6);
    Thread.sleep(1);

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
  void finishMatchThrowsWhenMatchNotFound() {
    Scoreboard scoreBoard = new InMemoryScoreboard();
    assertThrows(IllegalArgumentException.class,
        () -> scoreBoard.finishMatch(UUID.randomUUID()));
  }
}
