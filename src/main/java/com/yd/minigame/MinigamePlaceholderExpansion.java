package com.yd.minigame;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class MinigamePlaceholderExpansion extends PlaceholderExpansion {

    private final Minigame plugin;

    public MinigamePlaceholderExpansion(Minigame plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "minigame";
    }

    @Override
    public String getAuthor() {
        return "YD";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; // 서버 리로드 후에도 유지
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("stage")) {
            PlayerMinigameData data = plugin.getPlayerMinigameData(player);
            if (data == null || data.getCurrentExpectedKey() == null) {
                return "0";
            }
            return String.valueOf(getKeyValue(data.getCurrentExpectedKey(), data.getMinigameType()));
        }

        return null;
    }

    private int getKeyValue(String key, MinigameType type) {
        if (key == null) {
            return 0;
        }
        switch (key.toUpperCase()) {
            case "W":
                return type == MinigameType.TYPE1 ? 1 : 5;
            case "A":
                return type == MinigameType.TYPE1 ? 2 : 6;
            case "S":
                return type == MinigameType.TYPE1 ? 3 : 7;
            case "D":
                return type == MinigameType.TYPE1 ? 4 : 8;
            default:
                return 0;
        }
    }
}
