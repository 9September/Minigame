package com.yd.minigame;

import java.util.List;

public class PlayerMinigameData {
    private int currentStageIndex;
    private List<String> keySequence;

    public PlayerMinigameData(List<String> keySequence) {
        this.currentStageIndex = 0;
        this.keySequence = keySequence;
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

    // 미니게임 완료 여부 확인
    public boolean isCompleted() {
        return currentStageIndex >= keySequence.size();
    }
}