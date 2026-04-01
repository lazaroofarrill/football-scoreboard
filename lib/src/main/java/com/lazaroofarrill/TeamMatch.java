package com.lazaroofarrill;

import java.time.Instant;
import java.util.UUID;

public record TeamMatch(Team home, Team away, Instant startedAt, UUID matchId) {
}
