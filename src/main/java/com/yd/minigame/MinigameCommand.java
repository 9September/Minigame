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
            MinigameType type = MinigameType.TYPE1; // 기본값은 TYPE1

            // /minigame start [type] [player]
            if (args.length >= 2) {
                // 미니게임 유형 파라미터 처리
                if (args[1].equalsIgnoreCase("type1")) {
                    type = MinigameType.TYPE1;
                } else if (args[1].equalsIgnoreCase("type2")) {
                    type = MinigameType.TYPE2;
                } else {
                    sender.sendMessage(ChatColor.RED + "유효하지 않은 패턴 유형입니다. usage: /minigame start [type] [player]");
                    return true;
                }
            }

            Player target = null;
            if (args.length == 3) {
                // 플레이어 지정
                String targetName = args[2];
                target = Bukkit.getPlayerExact(targetName);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다: " + targetName);
                    return true;
                }
            } else {
                // 명령어 실행자가 플레이어인지 확인
                if (sender instanceof Player) {
                    target = (Player) sender;
                } else {
                    return true;
                }
            }

            startMinigame(target, type);
            return true;
        }

        return false;
    }

    // 미니게임 시작 메서드
    private void startMinigame(Player player, MinigameType type) {
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

        // 플레이어의 미니게임 데이터 설정
        PlayerMinigameData data = new PlayerMinigameData(keySequence, TIME_LIMIT, type);
        plugin.setPlayerMinigameData(player, data);

        // 초기 서브타이틀 전송
        plugin.sendSubtitle(player, ChatColor.RED + "남은 시간: " + data.getFormattedRemainingTime());

        // 타이머 태스크 시작
        plugin.startTimerTask();
    }

}
