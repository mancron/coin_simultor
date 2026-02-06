package com.team.coin_simulator.user;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // DB 대신 임시 메모리 저장소
    private static final List<User> users = new ArrayList<>();
    private static long sequence = 1;

    // 이메일 중복 확인
    public boolean existsByEmail(String email) {
        return users.stream()
                .anyMatch(u -> u.getEmail().equals(email));
    }

    // 회원가입
    public void save(String email, String passwordHash) {
        User user = new User(sequence++, email, passwordHash);
        users.add(user);
    }

    // 로그인
    public User findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
