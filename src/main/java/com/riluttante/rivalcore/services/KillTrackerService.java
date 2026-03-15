package com.riluttante.rivalcore.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KillTrackerService {

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, String> playerNames = new HashMap<>();

    public void recordKill(UUID uuid, String name) {
        kills.merge(uuid, 1, Integer::sum);
        playerNames.put(uuid, name);
    }

    public int getKills(UUID uuid) {
        return kills.getOrDefault(uuid, 0);
    }

    public String getPlayerName(UUID uuid) {
        return playerNames.getOrDefault(uuid, "?");
    }

    public List<Map.Entry<UUID, Integer>> getTopKillers(int n) {
        return kills.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(n)
                .toList();
    }

    public void clear() {
        kills.clear();
        playerNames.clear();
    }
}
