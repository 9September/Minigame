package com.yd.minigame;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerInputListener implements Listener {

    private final Minigame plugin;

    public PlayerInputListener(Minigame plugin) {
        this.plugin = plugin;
    }

    // 플레이어 움직임 감지 및 키 입력 처리
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMinigame(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // 움직임이 없는 경우 무시
            if (from.getX() == to.getX() && from.getZ() == to.getZ()) {
                return;
            }

            // 플레이어를 원래 위치로 되돌립니다.
            event.setTo(from);

            // 움직임의 방향을 계산합니다.
            double deltaX = to.getX() - from.getX();
            double deltaZ = to.getZ() - from.getZ();

            String inputKey = getMovementKey(deltaX, deltaZ);

            if (inputKey != null) {
                handlePlayerInput(player, inputKey);
            }
        }
    }

    // 플레이어의 좌클릭 및 우클릭 제한
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMinigame(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMinigame(player)) {
            event.setCancelled(true);
        }
    }

    // 움직임의 방향을 기반으로 입력된 키 추정
    private String getMovementKey(double deltaX, double deltaZ) {
        if (Math.abs(deltaX) > Math.abs(deltaZ)) {
            if (deltaX > 0) {
                return "D"; // 오른쪽 이동 (D 키)
            } else if (deltaX < 0) {
                return "A"; // 왼쪽 이동 (A 키)
            }
        } else {
            if (deltaZ > 0) {
                return "S"; // 뒤로 이동 (S 키)
            } else if (deltaZ < 0) {
                return "W"; // 앞으로 이동 (W 키)
            }
        }
        return null; // 움직임이 없는 경우
    }

    // 플레이어의 키 입력 처리
    private void handlePlayerInput(Player player, String inputKey) {
        PlayerMinigameData data = plugin.getPlayerMinigameData(player);

        if (data != null) {
            String expectedKey = data.getCurrentExpectedKey();

            if (inputKey.equals(expectedKey)) {
                data.incrementStageIndex();

                if (data.isCompleted()) {
                    // 미니게임 성공 처리
                    plugin.removePlayerMinigameData(player);
                    giveResistanceBuff(player);
                    player.sendMessage("§a미니게임에 성공하여 보상을 받았습니다!");
                }
            } else {
                // 미니게임 실패 처리
                plugin.removePlayerMinigameData(player);
                player.sendMessage("§c잘못된 키를 입력하여 미니게임이 종료되었습니다.");
            }
        }
    }

    // 저항 버프 부여
    private void giveResistanceBuff(Player player) {
        int durationInTicks = 100; // 5초 (1초 = 20틱)
        int amplifier = 9; // 레벨 10 (레벨은 0부터 시작)
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, durationInTicks, amplifier));
    }
}