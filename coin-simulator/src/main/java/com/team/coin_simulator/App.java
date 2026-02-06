package com.team.coin_simulator;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // UI 쓰레드에서 실행하는 것이 Swing의 정석입니다.
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}