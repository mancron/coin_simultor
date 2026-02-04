package com.team.coin_simulator.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/coin_simulator";
    private static final String USER = "root";
    private static final String PASSWORD = "비밀번호";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("DB 연결 실패", e);
        }
    }
}
