# Live Football World Cup Scoreboard

A simple in-memory library for tracking live football match scores.

## Requirements

- Java 21+
- Gradle

## Building
```bash
./gradlew build
```

## Testing
```bash
./gradlew test
```

## Assumptions
- Match start time is recorded internally on 
- Matches are identified by a UUID returned from `startMatch`. The spec does not specify how matches are identified for `updateScores` and `finishMatch`.
- Given that the application is a football scoreboard it is expected to have a disproportionate ratio of reads vs writes, hence a TreeSet was chosen.
