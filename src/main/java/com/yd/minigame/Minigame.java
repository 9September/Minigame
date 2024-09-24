package com.yd.minigame;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Minigame extends JavaPlugin {

    // 플레이어별 미니게임 데이터를 저장하는 맵
    private HashMap<UUID, PlayerMinigameData> playerMinigameDataMap = new HashMap<>();
    private BukkitTask timerTask; // 타이머 태스크 참조 추가

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

        // 초기에는 타이머 태스크를 시작하지 않습니다.
    }

    @Override
    public void onDisable() {
        // 플러그인 비활성화 시 필요한 작업
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
        }
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

    /**
     * 플레이어에게 서브타이틀을 전송하는 메서드.
     * @param player 서브타이틀을 받을 플레이어
     * @param message 서브타이틀 메시지
     */
    public void sendSubtitle(Player player, String message) {
        player.sendTitle(" ", message, 0, 20, 0); // 제목을 공백으로 설정하고, 1초 동안 유지
    }

    /**
     * 플레이어에게 저항 버프를 부여하는 메서드
     * @param player 버프를 받을 플레이어
     */
    public void giveResistanceBuff(Player player) {
        int duration = 200; // 10초 (틱 단위)
        int amplifier = 0;  // 레벨 1

        PotionEffect resistanceEffect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, amplifier, false, false);
        player.addPotionEffect(resistanceEffect);

        // 소리 재생
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

    }

    /**
     * 타이머 스케줄러 시작.
     * 매초 모든 미니게임 참가자의 남은 시간을 감소시키고, 시간 초과 시 미니게임을 실패로 처리.
     */
    public void startTimerTask() {
        // 이미 타이머 태스크가 실행 중이면 중복으로 실행하지 않음
        if (timerTask != null && !timerTask.isCancelled()) {
            return;
        }

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (playerMinigameDataMap.isEmpty()) {
                    this.cancel();
                    timerTask = null;
                    return;
                }

                Iterator<Map.Entry<UUID, PlayerMinigameData>> iterator = playerMinigameDataMap.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, PlayerMinigameData> entry = iterator.next();
                    UUID playerId = entry.getKey();
                    PlayerMinigameData data = entry.getValue();

                    Player player = Bukkit.getPlayer(playerId);
                    if (player == null || !player.isOnline()) {
                        iterator.remove();
                        continue;
                    }

                    // 남은 시간 감소
                    data.decrementTime();

                    // 서브타이틀 업데이트
                    sendSubtitle(player, ChatColor.RED + "남은 시간: " + data.getFormattedRemainingTime());

                    if (data.getRemainingTime() <= 0) {
                        // 미니게임 실패 처리 (시간 초과)
                        iterator.remove();
                        player.sendMessage(ChatColor.RED + "시간 초과!");
                        // 실패 소리 재생
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        // 서브타이틀 초기화
                        sendSubtitle(player, ""); // 서브타이틀 제거
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L); // 1초마다 실행 (20틱 = 1초)
    }
}
