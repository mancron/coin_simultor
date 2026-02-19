package databasetestdata;

	
public class databasecd {

    public static void main(String[] args) {
        
        // ---------------------------------------------------------
        // [Step 1] DB 스키마 및 테이블 생성
        // ---------------------------------------------------------
        System.out.println("========== [1단계] DB 초기화 및 테이블 생성 ==========");
        try {
            // DownloadData 클래스의 초기화 메서드 호출
            // (이전 대화에서 메서드명을 initDatabase로 수정했다고 가정)
        	CreateDatabase.initDatabase(); 
            System.out.println(">> 테이블 준비 완료.\n");
        } catch (Exception e) {
            System.err.println(">> [1단계 실패] 테이블 생성 중 오류: " + e.getMessage());
            return; // 테이블이 없으면 진행 불가하므로 종료
        }

        // ---------------------------------------------------------
        // [Step 2] 데이터 다운로드 (업비트 크롤링)
        // ---------------------------------------------------------
        System.out.println("========== [2단계] 과거 데이터 수집 시작 ==========");
        
        // 권장 설정: 60 (1시간 봉). 
        // 1 (1분 봉)은 시간이 매우 오래 걸리므로(약 1시간), 테스트 후 자기 전에 돌리세요.
        int unit = 1; 
        
        try {
        	DownloadDatabase.import6MonthsData(unit);
            System.out.println("\n>> [완료] 모든 작업이 성공적으로 끝났습니다.");
        } catch (Exception e) {
            System.err.println("\n>> [2단계 실패] 데이터 수집 중 오류: " + e.getMessage());
            System.err.println("Tip: 'candle_acc_trade_volume' 컬럼 사이즈를 (30,8)로 늘렸는지 확인하세요.");
        }
    }
}