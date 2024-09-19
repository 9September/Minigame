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
        PlayerMinigameData data = plugin.getPlayerMinigameData(player);

        if (data == null) {
            return "NONE";
        }

        if (identifier.equals("stage")) {
            return data.getCurrentExpectedKey();
        }

        return null;
    }
}