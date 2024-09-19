package com.yd.minigame;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class Minigame extends JavaPlugin {

    // 플레이어별 미니게임 데이터를 저장하는 맵
    private HashMap<UUID, PlayerMinigameData> playerMinigameDataMap = new HashMap<>();

    @Override
    public void onEnable() {
        // PlaceholderAPI 확장 등록
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MinigamePlaceholderExpansion(this).register();
        } else {
            getLogger().warning("PlaceholderAPI not found! The plugin will not function properly.");
        }

        // 이벤트 리스너 등록
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerInputListener(this), this);

        // 명령어 등록
        this.getCommand("minigame").setExecutor(new MinigameCommand(this));
    }

    @Override
    public void onDisable() {
        // 플러그인 비활성화 시 필요한 작업
    }

    // 플레이어의 미니게임 데이터 반환
    public PlayerMinigameData getPlayerMinigameData(Player player) {
        return playerMinigameDataMap.get(player.getUniqueId());
    }

    // 플레이어의 미니게임 데이터 설정
    public void setPlayerMinigameData(Player player, PlayerMinigameData data) {
        playerMinigameDataMap.put(player.getUniqueId(), data);
    }

    // 플레이어의 미니게임 데이터 제거
    public void removePlayerMinigameData(Player player) {
        playerMinigameDataMap.remove(player.getUniqueId());
    }

    // 플레이어가 미니게임 진행 중인지 확인
    public boolean isInMinigame(Player player) {
        return playerMinigameDataMap.containsKey(player.getUniqueId());
    }
}