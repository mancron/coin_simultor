package DTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import type.SessionType;

public class SessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 1. PK & 식별자
    private Long sessionId;         // session_id
    private String userId;          // user_id

    // 2. 세션 설정 정보
    private String sessionName;     // session_name (예: "비트코인 불장 연습")
    
    // DB에는 문자열('REALTIME', 'BACKTEST')로 저장되지만
    // 자바에서는 Enum으로 다루는 것이 안전함
    private SessionType sessionType; 

    private BigDecimal initialSeedMoney; // initial_seed_money (초기 자본금)

    // 3. 시간 제어 (백테스팅용)
    private Timestamp startSimTime;   // start_sim_time (시뮬레이션 시작 시점)
    private Timestamp currentSimTime; // current_sim_time (현재 진행 중인 가상 시점)

    // 4. 상태
    private boolean active;           // is_active
    private Timestamp createdAt;      // created_at

    // --- 생성자 (Constructors) ---

    public SessionDTO() {
    }

    // 세션 생성용 간편 생성자
    public SessionDTO(String userId, String sessionName, SessionType sessionType, BigDecimal initialSeedMoney) {
        this.userId = userId;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.initialSeedMoney = initialSeedMoney;
        this.active = true; // 기본값 활성
    }

    // --- Getter & Setter ---
    // (Alt + Shift + S -> R 로 자동 생성)
    
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    // DB에서 꺼낼 때 String을 Enum으로 변환하는 Setter 오버로딩 (실무 팁)
    public void setSessionType(String sessionTypeStr) {
        try {
            this.sessionType = SessionType.valueOf(sessionTypeStr);
        } catch (Exception e) {
            this.sessionType = SessionType.REALTIME; // 기본값 처리
        }
    }

    public BigDecimal getInitialSeedMoney() { return initialSeedMoney; }
    public void setInitialSeedMoney(BigDecimal initialSeedMoney) { this.initialSeedMoney = initialSeedMoney; }

    public Timestamp getStartSimTime() { return startSimTime; }
    public void setStartSimTime(Timestamp startSimTime) { this.startSimTime = startSimTime; }

    public Timestamp getCurrentSimTime() { return currentSimTime; }
    public void setCurrentSimTime(Timestamp currentSimTime) { this.currentSimTime = currentSimTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "SessionDTO [id=" + sessionId + ", name=" + sessionName + 
               ", type=" + sessionType + ", seed=" + initialSeedMoney + "]";
    }
}