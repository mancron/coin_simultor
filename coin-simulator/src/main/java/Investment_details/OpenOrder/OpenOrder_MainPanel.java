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
 * │  OpenOrder_TopControlPanel (NORTH - 필터)    │
 * ├─────────────────────────────────────────────┤
 * │  OpenOrder_CenterDisplayPanel (CENTER - 테이블)│
 * └─────────────────────────────────────────────┘
 */
public class OpenOrder_MainPanel extends JPanel {
    
    private OpenOrder_TopControlPanel topPanel;
    private OpenOrder_CenterDisplayPanel tablePanel;
    
    private final OpenOrderDAO dao;
    private final AssetDAO assetDAO;
    private final String userId;
    private long sessionId; // 💡 세션 ID 필드 추가
    
    private String currentMarket = "ALL";
    
    // 💡 생성자에 sessionId 추가
    public OpenOrder_MainPanel(String userId, long sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.dao = new OpenOrderDAO();
        this.assetDAO = new AssetDAO();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        topPanel = new OpenOrder_TopControlPanel();
        tablePanel = new OpenOrder_CenterDisplayPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        
        setupListeners();
        refreshData();
    }
    
    // 💡 세션 변경 시 호출될 메서드 추가
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }
    
    private void setupListeners() {
        topPanel.addMarketFilterListener(e -> {
            currentMarket = topPanel.getSelectedMarket();
            refreshData();
        });
        
        topPanel.addCancelAllListener(e -> handleCancelAll());
        tablePanel.setCancelOrderListener(orderId -> handleCancelOrder(orderId));
    }
    
    private void refreshData() {
        List<OrderDTO> orders;
        
        // 💡 DAO 호출 시 sessionId를 넘겨주도록 수정
        if ("ALL".equals(currentMarket)) {
            orders = dao.getOpenOrders(userId, sessionId);
        } else {
            orders = dao.getOpenOrdersByMarket(userId, sessionId, currentMarket);
        }
        
        tablePanel.updateData(orders);
    }
    
    private void handleCancelOrder(long orderId) {
        int confirm = JOptionPane.showConfirmDialog(
            this, "이 주문을 취소하시겠습니까?", "주문 취소 확인", JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dao.cancelOrder(orderId);
            if (success) {
                JOptionPane.showMessageDialog(this, "주문이 취소되었습니다.", "취소 완료", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "주문 취소에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleCancelAll() {
        int confirm = JOptionPane.showConfirmDialog(
            this, "모든 미체결 주문을 취소하시겠습니까?", "일괄 취소 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // 💡 일괄 취소 시에도 현재 세션의 주문만 취소하도록 변경
            int canceledCount = dao.cancelAllOrders(userId, sessionId);
            
            if (canceledCount > 0) {
                JOptionPane.showMessageDialog(this, canceledCount + "개의 주문이 취소되었습니다.", "취소 완료", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "취소할 주문이 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    public void refresh() {
        refreshData();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("미체결 주문 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            
            OpenOrder_MainPanel panel = new OpenOrder_MainPanel("user_01", 0L); // 테스트용 0L
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}