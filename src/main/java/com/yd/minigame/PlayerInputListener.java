package com.yd.minigame;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PlayerInputListener implements Listener {

    private final Minigame plugin;

    public PlayerInputListener(Minigame plugin) {
        this.plugin = plugin;
    }

    private String getMovementKey(Player player, double deltaX, double deltaZ) {
        // 움직임 벡터 생성
        Vector movementVector = new Vector(deltaX, 0, deltaZ);

        // 플레이어의 바라보는 방향(yaw)을 라디안으로 변환
        double yaw = Math.toRadians(player.getLocation().getYaw());

        // 플레이어의 로컬 좌표계에서의 전진 방향 벡터 계산
        Vector forward = new Vector(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();

        // 왼쪽 방향 벡터 계산
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.isInMinigame(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // 위치 변화 확인
            if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
                return;
            }

            // 플레이어가 공중에 있는지 확인
            if (!player.isOnGround()) {
                // 플레이어를 가장 가까운 지면으로 텔레포트
                Location groundLocation = getGroundLocation(player);
                if (groundLocation != null) {
                    player.teleport(groundLocation);

                } else {
                    // 지면을 찾지 못한 경우, Y 좌표를 1씩 감소시켜 지면을 찾음
                    Location newLocation = player.getLocation();
                    while (newLocation.getY() > 0 && newLocation.getBlock().isPassable()) {
                        newLocation.subtract(0, 1, 0);
                    }
                    player.teleport(newLocation);
                }
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
                player.sendMessage(ChatColor.YELLOW + "키 입력 감지되지 않음");
            }
        }
    }

    private Location getGroundLocation(Player player) {
        Location location = player.getLocation().clone();

        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        // 플레이어의 현재 Y 좌표부터 아래로 내려가면서 지면을 찾습니다.
        int y = location.getBlockY();

        while (y > 0) {
            Block blockBelow = world.getBlockAt(x, y - 1, z);
            if (!blockBelow.getType().isAir() && blockBelow.getType().isSolid()) {
                // 지면을 찾았으면, 플레이어의 위치를 해당 블록 위로 설정
                Location groundLocation = new Location(world, x + 0.5, y, z + 0.5);
                groundLocation.setYaw(location.getYaw());
                groundLocation.setPitch(location.getPitch());
                return groundLocation;
            }
            y--;
        }
        // 지면을 찾지 못한 경우 null 반환
        return null;
    }

    private void handlePlayerInput(Player player, String inputKey) {
        PlayerMinigameData data = plugin.getPlayerMinigameData(player);

        if (data != null) {
            // 입력 쿨다운 체크
            long currentTime = System.currentTimeMillis();
            long inputCooldown = 500; // 500밀리초 쿨다운

            if (currentTime - data.getLastInputTime() < inputCooldown) {
                return;
            }
            data.setLastInputTime(currentTime);

            String expectedKey = data.getCurrentExpectedKey();

            int inputKeyValue = getKeyValue(inputKey, data.getMinigameType());
            int expectedKeyValue = getKeyValue(expectedKey, data.getMinigameType());

            if (inputKeyValue == expectedKeyValue && inputKeyValue != 0) {
                data.incrementStageIndex();

                // 소리 재생
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

                if (data.isCompleted()) {
                    // 미니게임 성공 처리
                    plugin.removePlayerMinigameData(player);
                    giveResistanceBuff(player);
                    player.sendMessage(ChatColor.GREEN + "패턴 성공!");
                    // 서브타이틀 초기화
                    plugin.sendSubtitle(player, "");
                }
            } else {
                // 미니게임 실패 처리
                plugin.removePlayerMinigameData(player);
                player.sendMessage(ChatColor.RED + "패턴 실패!");
                // 실패 소리 재생
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                // 서브타이틀 초기화
                plugin.sendSubtitle(player, "");
            }
        }
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

    // 저항 버프 부여
    private void giveResistanceBuff(Player player) {
        PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 5, false, false);
        player.addPotionEffect(resistance);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInMinigame(player)) {
            event.setCancelled(true);
        }
    }
}

