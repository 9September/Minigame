package com.yd.minigame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinigameCommand implements CommandExecutor {

    private final Minigame plugin;
    private final int TIME_LIMIT = 10; // 제한 시간 (초), 필요에 따라 조정
    private final int TOTAL_STAGES = 8; // 총 단계 수

    public MinigameCommand(Minigame plugin) {
        this.plugin = plugin;
    }

    // 명령어 처리 메서드
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("minigame") && args.length > 0 && args[0].equalsIgnoreCase("start")) {
            if (args.length == 1) {
                // /minigame start
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    startMinigame(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "콘솔에서는 플레이어를 지정하여 미니게임을 시작할 수 없습니다.");
                }
                return true;
            } else if (args.length == 2) {
                // /minigame start <player>
                String targetName = args[1];
                Player target = Bukkit.getPlayerExact(targetName);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + targetName);
                    return true;
                }
                startMinigame(target);
                return true;
            } else {
                return true;
            }
        }
        return false;
    }

    // 미니게임 시작 메서드
    private void startMinigame(Player player) {
        if (plugin.isInMinigame(player)) {
            player.sendMessage(ChatColor.RED + "이미 패턴이 진행 중입니다.");
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

        // 플레이어의 미니게임 데이터 설정, with remainingTime = TIME_LIMIT
        PlayerMinigameData data = new PlayerMinigameData(keySequence, TIME_LIMIT);
        plugin.setPlayerMinigameData(player, data);

        // 초기 서브타이틀 전송
        plugin.sendSubtitle(player, ChatColor.RED + "남은 시간: " + data.getFormattedRemainingTime());

        // 타이머 태스크 시작
        plugin.startTimerTask();
    }
}
