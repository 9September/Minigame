package com.yd.minigame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MinigameCommand implements CommandExecutor {

    private final Minigame plugin;
    private final int TIME_LIMIT = 5; // 제한 시간 (초)
    private final int TOTAL_STAGES = 8; // 총 단계 수

    public MinigameCommand(Minigame plugin) {
        this.plugin = plugin;
    }

    // 명령어 처리 메서드
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("minigame") && args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                startMinigame(player);
                return true;
            } else {
                sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
                return true;
            }
        }
        return false;
    }

    // 미니게임 시작 메서드
    private void startMinigame(Player player) {
        if (plugin.isInMinigame(player)) {
            return;
        }

        // 사용할 키 목록
        String[] possibleKeys = {"W", "A", "S", "D"};

        // 랜덤한 키 시퀀스 생성
        List<String> keySequence = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < TOTAL_STAGES; i++) {
            int index = random.nextInt(possibleKeys.length);
            keySequence.add(possibleKeys[index]);
        }

        // 플레이어의 미니게임 데이터 설정
        PlayerMinigameData data = new PlayerMinigameData(keySequence);
        plugin.setPlayerMinigameData(player, data);

        // 제한 시간 설정
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.isInMinigame(player)) {
                    // 시간 초과 처리
                    plugin.removePlayerMinigameData(player);
                    player.sendMessage("§c시간 초과!");
                }
            }
        }.runTaskLater(plugin, TIME_LIMIT * 20); // TIME_LIMIT 초 후 실행
    }
}