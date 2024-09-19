package com.yd.minigame;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PlayerInputListener implements Listener {

    private final Minigame plugin;

    public PlayerInputListener(Minigame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMinigame(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // 위치 변화 확인 (Yaw와 Pitch 제외)
            if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
                return;
            }

            // 움직임의 방향을 계산합니다.
            double deltaX = to.getX() - from.getX();
            double deltaZ = to.getZ() - from.getZ();

            // 움직임 이벤트 취소
            event.setCancelled(true);

            String inputKey = getMovementKey(player, deltaX, deltaZ);

            if (inputKey != null) {
                handlePlayerInput(player, inputKey);
            } else {
                player.sendMessage("키 입력 감지되지 않음");
            }
        }
    }

    // 수정된 getMovementKey 메서드
    private String getMovementKey(Player player, double deltaX, double deltaZ) {
        // 움직임 벡터 생성
        Vector movementVector = new Vector(deltaX, 0, deltaZ);

        // 플레이어의 바라보는 방향(yaw)을 라디안으로 변환
        double yaw = Math.toRadians(player.getLocation().getYaw());

        // 플레이어의 로컬 좌표계에서의 전진 방향 벡터 계산
        Vector forward = new Vector(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();

        // 왼쪽 방향 벡터 계산 (부호 수정)
        Vector left = new Vector(forward.getZ(), 0, -forward.getX()).normalize();

        // 움직임 벡터를 로컬 좌표계로 변환
        double forwardDistance = movementVector.dot(forward);
        double leftDistance = movementVector.dot(left);

        // 임계값 설정
        double threshold = 0.05;

        // 전진/후진 감지
        if (forwardDistance > threshold) {
            return "W"; // 전진 (W 키)
        } else if (forwardDistance < -threshold) {
            return "S"; // 후진 (S 키)
        }

        // 좌우 이동 감지
        if (leftDistance > threshold) {
            return "A"; // 좌측 이동 (A 키)
        } else if (leftDistance < -threshold) {
            return "D"; // 우측 이동 (D 키)
        }

        return null; // 움직임이 없는 경우
    }

    private void handlePlayerInput(Player player, String inputKey) {
        PlayerMinigameData data = plugin.getPlayerMinigameData(player);

        if (data != null) {
            // 입력 쿨다운 체크
            long currentTime = System.currentTimeMillis();
            long inputCooldown = 200; // 200밀리초 쿨다운 (0.2초)

            if (currentTime - data.getLastInputTime() < inputCooldown) {
                return;
            }
            data.setLastInputTime(currentTime);

            String expectedKey = data.getCurrentExpectedKey();

            int inputKeyValue = getKeyValue(inputKey);
            int expectedKeyValue = getKeyValue(expectedKey);

            if (inputKeyValue == expectedKeyValue && inputKeyValue != 0) {
                data.incrementStageIndex();

                if (data.isCompleted()) {
                    // 미니게임 성공 처리
                    plugin.removePlayerMinigameData(player);
                    giveResistanceBuff(player);
                    player.sendMessage("§a패턴 성공!");
                }
            } else {
                // 미니게임 실패 처리
                plugin.removePlayerMinigameData(player);
                player.sendMessage("§c패턴 실패!");
            }
        }
    }

    private int getKeyValue(String key) {
        if (key == null) {
            return 0;
        }
        switch (key.toUpperCase()) {
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

    // 저항 버프 부여 (필요한 경우 구현)
    private void giveResistanceBuff(Player player) {
        // 저항 버프 부여 로직
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMinigame(player)) {
            event.setCancelled(true);
            player.sendMessage("미니게임 중에는 상호작용이 제한됩니다.");
        }
    }
}
