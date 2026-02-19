package Investment_details.OpenOrder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import DAO.AssetDAO;
import DAO.OpenOrderDAO;
import DTO.OrderDTO;

/**
 * 미체결 주문 메인 패널
 * 
 * 레이아웃:
 * ┌─────────────────────────────────────────────┐
 * │  OpenOrder_TopControlPanel (NORTH - 필터)   │
 * ├─────────────────────────────────────────────┤
 * │  OpenOrder_CenterDisplayPanel (CENTER - 테이블) │
 * └─────────────────────────────────────────────┘
 */
public class OpenOrder_MainPanel extends JPanel {
    
    private OpenOrder_TopControlPanel topPanel;
    private OpenOrder_CenterDisplayPanel tablePanel;
    
    private final OpenOrderDAO dao;
    private final AssetDAO assetDAO;
    private final String userId;
    
    private String currentMarket = "ALL";
    
    public OpenOrder_MainPanel(String userId) {
        this.userId = userId;
        this.dao = new OpenOrderDAO();
        this.assetDAO = new AssetDAO();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 1. 상단 컨트롤 패널
        topPanel = new OpenOrder_TopControlPanel();
        
        // 2. 테이블 패널
        tablePanel = new OpenOrder_CenterDisplayPanel();
        
        // 3. 레이아웃 조립
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        
        // 4. 이벤트 리스너 등록
        setupListeners();
        
        // 5. 초기 데이터 로드
        refreshData();
    }
    
    /**
     * 이벤트 리스너 설정
     */
    private void setupListeners() {
        // 마켓 필터 변경
        topPanel.addMarketFilterListener(e -> {
            currentMarket = topPanel.getSelectedMarket();
            refreshData();
        });
        
        // 일괄 취소
        topPanel.addCancelAllListener(e -> {
            handleCancelAll();
        });
        
        // 개별 주문 취소
        tablePanel.setCancelOrderListener(orderId -> {
            handleCancelOrder(orderId);
        });
    }
    
    /**
     * 데이터 새로고침
     */
    private void refreshData() {
        List<OrderDTO> orders;
        
        if ("ALL".equals(currentMarket)) {
            orders = dao.getOpenOrders(userId);
        } else {
            orders = dao.getOpenOrdersByMarket(userId, currentMarket);
        }
        
        tablePanel.updateData(orders);
    }
    
    /**
     * 개별 주문 취소 처리
     */
    private void handleCancelOrder(long orderId) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "이 주문을 취소하시겠습니까?",
            "주문 취소 확인",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dao.cancelOrder(orderId);
            
            if (success) {
                // TODO: 잠긴 자산 해제 로직 추가 (AssetDAO 사용)
                // 주문 취소 시 locked 금액을 balance로 이동
                
                JOptionPane.showMessageDialog(
                    this,
                    "주문이 취소되었습니다.",
                    "취소 완료",
                    JOptionPane.INFORMATION_MESSAGE
                );
                refreshData();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "주문 취소에 실패했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * 일괄 취소 처리
     */
    private void handleCancelAll() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "모든 미체결 주문을 취소하시겠습니까?",
            "일괄 취소 확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            int canceledCount = dao.cancelAllOrders(userId);
            
            if (canceledCount > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    canceledCount + "개의 주문이 취소되었습니다.",
                    "취소 완료",
                    JOptionPane.INFORMATION_MESSAGE
                );
                refreshData();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "취소할 주문이 없습니다.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }
    
    /**
     * 외부에서 데이터 새로고침 (주문 체결 후 등)
     */
    public void refresh() {
        refreshData();
    }
    
    /**
     * 독립 실행 테스트
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("미체결 주문 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            
            OpenOrder_MainPanel panel = new OpenOrder_MainPanel("user_01");
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}