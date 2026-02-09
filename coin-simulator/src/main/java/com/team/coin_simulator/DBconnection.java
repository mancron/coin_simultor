package com.team.coin_simulator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {
    // DB 접속 정보 (본인의 환경에 맞게 수정)
    private static final String URL = "jdbc:mysql://localhost:3306/coin_db";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    private Connection connection;

    // 1. 생성자를 private으로 선언하여 외부 생성을 차단
    private DBconnection() {
        try {
            // JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
            // DB 연결 생성
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("데이터베이스 연결 성공!");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
        }
    }

    // 2. Bill Pugh Singleton 방식: 멀티스레드 환경에서 안전하고 성능이 좋음
    private static class Holder {
        private static final DBconnection INSTANCE = new DBconnection();
    }

    // 3. 외부에서 인스턴스를 가져오는 유일한 통로
    public static DBconnection getInstance() {
        return Holder.INSTANCE;
    }

    // 4. 연결 객체 반환
    public Connection getConnection() {
        try {
            // 연결이 닫혀있다면 재연결 로직을 추가할 수 있음
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}