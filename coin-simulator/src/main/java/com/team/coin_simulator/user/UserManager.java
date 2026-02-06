/*package com.team.coin_simulator.user;

import java.util.ArrayList;

public class UserManager {
    private static ArrayList<User> users = new ArrayList<>();

    // 회원가입
    public static boolean register(String id, String pw) {
        for (User u : users) {
            if (u.getUserId().equals(id)) {
                return false; // 중복
            }
        }
        users.add(new User(id, pw));
        return true;
    }

    // 로그인
    public static boolean login(String id, String pw) {
        for (User u : users) {
            if (u.getUserId().equals(id) && u.getPassword().equals(pw)) {
                return true;
            }
        }
        return false;
    }
}
*/