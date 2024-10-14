package com.yd.minigame;

import java.util.List;

public class PlayerMinigameData {
    private int currentStageIndex;
    private List<String> keySequence;
    private long lastInputTime;
    private int remainingTime; // 남은 시간 (초 단위)
    private MinigameType minigameType;

    public PlayerMinigameData(List<String> keySequence, int timeLimit, MinigameType minigameType) {
        this.currentStageIndex = 0;
        this.keySequence = keySequence;
        this.lastInputTime = 0;
        this.remainingTime = timeLimit;
        this.minigameType = minigameType;
    }

    // 현재 진행 중인 스테이지 인덱스 반환
    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    // 스테이지 인덱스 증가
    public void incrementStageIndex() {
        currentStageIndex++;
    }

    // 현재 기대되는 키 반환
    public String getCurrentExpectedKey() {
        if (currentStageIndex < keySequence.size()) {
            return keySequence.get(currentStageIndex);
        }
        return null;
    }

    public MinigameType getMinigameType() {
        return minigameType;
    }

    public long getLastInputTime() {
        return lastInputTime;
    }

    public void setLastInputTime(long lastInputTime) {
        this.lastInputTime = lastInputTime;
    }

    // 미니게임 완료 여부 확인
    public boolean isCompleted() {
        return currentStageIndex >= keySequence.size();
    }

    // 남은 시간 반환
    public int getRemainingTime() {
        return remainingTime;
    }

    // 남은 시간 설정
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    // 남은 시간 감소
    public void decrementTime() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public String getFormattedRemainingTime() {
        int seconds = getRemainingTime();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
