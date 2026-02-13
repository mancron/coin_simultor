package com.team.coin_simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 시뮬레이터의 시간을 제어하는 중앙 관리 클래스
 * - 실시간 모드와 백테스팅 모드 전환
 * - 과거 시점으로 시간 이동
 * - 모든 패널에 시간 변경 이벤트 전파
 */
public class TimeController {
    
    private static TimeController instance;
    
    private boolean isRealtimeMode = true; // true: 실시간, false: 백테스팅
    private LocalDateTime currentSimTime; // 백테스팅 시 현재 시뮬레이션 시간
    private Long currentSessionId; // 현재 활성 세션 ID
    private String userId; // 사용자 ID
    
    // 시간 변경 이벤트를 받을 리스너들
    private List<TimeChangeListener> listeners = new ArrayList<>();
    
    // 싱글톤 패턴
    private TimeController() {}
    
    public static synchronized TimeController getInstance() {
        if (instance == null) {
            instance = new TimeController();
        }
        return instance;
    }
    
    /**
     * 시간 변경 이벤트 리스너 인터페이스
     */
    public interface TimeChangeListener {
        /**
         * 시간이 변경되었을 때 호출됨
         * @param newTime 새로운 시뮬레이션 시간 (실시간이면 null)
         * @param isRealtime 실시간 모드 여부
         */
        void onTimeChanged(LocalDateTime newTime, boolean isRealtime);
    }
    
    /**
     * 리스너 등록
     */
    public void addTimeChangeListener(TimeChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 사용자 초기화 (로그인 시 호출)
     */
    public void initialize(String userId) {
        this.userId = userId;
        // 기본값: 실시간 모드
        switchToRealtimeMode();
    }
    
    /**
     * 실시간 모드로 전환
     */
    public void switchToRealtimeMode() {
        this.isRealtimeMode = true;
        this.currentSimTime = null;
        
        // 실시간 세션 ID 조회 또는 생성
        this.currentSessionId = getOrCreateRealtimeSession();
        
        // 모든 리스너에게 알림
        notifyListeners(null, true);
        
        System.out.println("[TimeController] 실시간 모드로 전환됨 (Session ID: " + currentSessionId + ")");
    }
    
    /**
     * 백테스팅 모드로 전환
     * @param startTime 시뮬레이션 시작 시점 (최소 2개월 전)
     */
    public void switchToBacktestMode(LocalDateTime startTime) {
        // 유효성 검사: 2개월 이상 과거여야 함
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        if (startTime.isAfter(twoMonthsAgo)) {
            System.err.println("[TimeController] 백테스팅은 최소 2개월 전부터 가능합니다.");
            return;
        }
        
        this.isRealtimeMode = false;
        this.currentSimTime = startTime;
        
        // 백테스팅 세션 생성
        this.currentSessionId = createBacktestSession(startTime);
        
        // 모든 리스너에게 알림
        notifyListeners(startTime, false);
        
        System.out.println("[TimeController] 백테스팅 모드로 전환됨: " + startTime + " (Session ID: " + currentSessionId + ")");
    }
    
    /**
     * 시뮬레이션 시간 이동 (백테스팅 모드에서만 작동)
     * @param minutes 이동할 분 수 (양수: 미래, 음수: 과거)
     */
    public void moveTime(int minutes) {
        if (isRealtimeMode) {
            System.out.println("[TimeController] 실시간 모드에서는 시간 이동이 불가합니다.");
            return;
        }
        
        this.currentSimTime = currentSimTime.plusMinutes(minutes);
        
        // 세션 업데이트
        updateSessionTime(currentSimTime);
        
        // 모든 리스너에게 알림
        notifyListeners(currentSimTime, false);
        
        System.out.println("[TimeController] 시간 이동: " + currentSimTime);
    }
    
    /**
     * 특정 시점으로 점프
     */
    public void jumpToTime(LocalDateTime targetTime) {
        if (isRealtimeMode) {
            System.out.println("[TimeController] 실시간 모드에서는 시간 이동이 불가합니다.");
            return;
        }
        
        this.currentSimTime = targetTime;
        updateSessionTime(targetTime);
        notifyListeners(targetTime, false);
        
        System.out.println("[TimeController] 시간 점프: " + currentSimTime);
    }
    
    /**
     * 모든 리스너에게 시간 변경 알림
     */
    private void notifyListeners(LocalDateTime newTime, boolean isRealtime) {
        for (TimeChangeListener listener : listeners) {
            try {
                listener.onTimeChanged(newTime, isRealtime);
            } catch (Exception e) {
                System.err.println("[TimeController] 리스너 알림 중 오류: " + e.getMessage());
            }
        }
    }
    
    // ==================== DB 작업 ====================
    
    /**
     * 실시간 세션 조회 또는 생성
     */
    private Long getOrCreateRealtimeSession() {
        String selectSql = "SELECT session_id FROM simulation_sessions " +
                          "WHERE user_id = ? AND session_type = 'REALTIME' AND is_active = TRUE";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("session_id");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 없으면 새로 생성
        return createNewSession("실시간 거래", "REALTIME", null);
    }
    
    /**
     * 백테스팅 세션 생성
     */
    private Long createBacktestSession(LocalDateTime startTime) {
        String sessionName = "백테스팅 " + startTime.toLocalDate();
        return createNewSession(sessionName, "BACKTEST", startTime);
    }
    
    /**
     * 새 세션 생성 (공통 로직)
     */
    private Long createNewSession(String name, String type, LocalDateTime startTime) {
        String sql = "INSERT INTO simulation_sessions " +
                    "(user_id, session_name, session_type, start_sim_time, current_sim_time, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, TRUE)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, type);
            pstmt.setTimestamp(4, startTime != null ? Timestamp.valueOf(startTime) : null);
            pstmt.setTimestamp(5, startTime != null ? Timestamp.valueOf(startTime) : null);
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 세션의 현재 시간 업데이트
     */
    private void updateSessionTime(LocalDateTime newTime) {
        String sql = "UPDATE simulation_sessions SET current_sim_time = ? WHERE session_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(newTime));
            pstmt.setLong(2, currentSessionId);
            pstmt.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ==================== Getter ====================
    
    public boolean isRealtimeMode() {
        return isRealtimeMode;
    }
    
    public LocalDateTime getCurrentSimTime() {
        return currentSimTime;
    }
    
    public Long getCurrentSessionId() {
        return currentSessionId;
    }
    
    public String getUserId() {
        return userId;
    }
}