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
            return String.valueOf(getKeyValue(data.getCurrentExpectedKey()));
        }

        return null;
    }

    private int getKeyValue(String key) {
        switch (key) {
            case "W":
                return 1;
            case "A":
                return 2;
            case "S":
                return 3;
            case "D":
                return 4;
            default:
                return 0;
        }
    }
}