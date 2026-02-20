package com.team.coin_simulator;

import javax.swing.*;
import java.awt.*;

import com.team.coin_simulator.Market_Panel.HistoryPanel;
import com.team.coin_simulator.Market_Order.OrderPanel;
import com.team.coin_simulator.chart.CandleChartPanel;
import com.team.coin_simulator.orderbook.OrderBookPanel;

import DAO.UpbitWebSocketDao;
import DAO.UserDAO;
import Investment_details.Investment_details_MainPanel;

import com.team.coin_simulator.login.ChangePasswordFrame;
import com.team.coin_simulator.login.LoginFrame;

public class MainFrame extends JFrame implements TimeController.TimeChangeListener {

    // 상단 패널
    private JPanel topPanel;
    private TimeControlPanel timeControlPanel;
    private JButton btnToggleView;

    // ✅ 프로필 UI
    private JLabel profileIconLabel;

    // 메인 컨텐츠 (CardLayout)
    private CardLayout mainCardLayout;
    private JPanel mainContentPanel;

    // 거래 화면 컴포넌트
    private JPanel tradingPanel;
    private HistoryPanel historyPanel;
    private CandleChartPanel chartPanel;
    private OrderBookPanel orderBookPanel;
    private OrderPanel orderPanel;

    // 투자내역 화면 컴포넌트
    private Investment_details_MainPanel investmentPanel;

    // 상태 관리
    private TimeController timeController;

    // ✅ 로그인 유저
    private final String currentUserId;
    private String currentNickname;
    private String currentProfileImagePath;

    private boolean isTradingView = true;

    private static final String CARD_TRADING = "TRADING";
    private static final String CARD_INVESTMENT = "INVESTMENT";

    // ✅ 변경: userId를 생성자에서 받는다
    public MainFrame(String userId) {
        super("가상화폐 모의투자 시스템");

        this.currentUserId = userId;

        timeController = TimeController.getInstance();
        timeController.initialize(currentUserId);
        timeController.addTimeChangeListener(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);

        // 프로필 먼저 로드
        loadProfileFromDb();

        initComponents();
        initWebSocket();

        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        mainCardLayout = new CardLayout();
        mainContentPanel = new JPanel(mainCardLayout);

        tradingPanel = createTradingPanel();
        investmentPanel = new Investment_details_MainPanel(currentUserId);

        mainContentPanel.add(tradingPanel, CARD_TRADING);
        mainContentPanel.add(investmentPanel, CARD_INVESTMENT);

        add(mainContentPanel, BorderLayout.CENTER);

        mainCardLayout.show(mainContentPanel, CARD_TRADING);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        timeControlPanel = new TimeControlPanel();
        panel.add(timeControlPanel, BorderLayout.CENTER);

        // ✅ 오른쪽: [투자내역 보기 버튼] + [프로필 아이콘]
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        rightPanel.setBackground(Color.WHITE);

        // 투자내역 보기 버튼(기존 유지)
        btnToggleView = new JButton("투자내역 보기");
        btnToggleView.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnToggleView.setForeground(Color.WHITE);
        btnToggleView.setBackground(new Color(52, 152, 219));
        btnToggleView.setFocusPainted(false);
        btnToggleView.setBorderPainted(false);
        btnToggleView.setPreferredSize(new Dimension(150, 35));
        btnToggleView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnToggleView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnToggleView.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnToggleView.setBackground(new Color(52, 152, 219));
            }
        });
        btnToggleView.addActionListener(e -> toggleView());

        rightPanel.add(btnToggleView);

        // ✅ 프로필 아이콘(사람모양 느낌: 기본 아이콘 사용, 사진 있으면 사진 표시)
        profileIconLabel = new JLabel();
        profileIconLabel.setPreferredSize(new Dimension(34, 34));
        profileIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setProfileIcon(currentProfileImagePath);

        profileIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showProfileMenu(profileIconLabel, 0, profileIconLabel.getHeight());
            }
        });

        rightPanel.add(profileIconLabel);

        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // -----------------------------
    // 프로필: DB 로드 / 아이콘 적용 / 메뉴
    // -----------------------------
    private void loadProfileFromDb() {
        UserDAO.ProfileInfo info = UserDAO.getProfile(currentUserId);
        if (info != null) {
            currentNickname = info.nickname;
            currentProfileImagePath = info.imagePath;
        }
    }

    private void setProfileIcon(String path) {
        try {
            if (path == null || path.isBlank()) {
                // 기본 사람 아이콘 느낌(대체 아이콘)
                profileIconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
                return;
            }
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(34, 34, Image.SCALE_SMOOTH);
            profileIconLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            profileIconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        }
    }

    private void showProfileMenu(Component comp, int x, int y) {
        JPopupMenu menu = new JPopupMenu();

        String nick = (currentNickname == null || currentNickname.isBlank()) ? "(닉네임 없음)" : currentNickname;

        JMenuItem info = new JMenuItem("ID: " + currentUserId + " / " + nick);
        info.setEnabled(false);
        menu.add(info);
        menu.addSeparator();

        JMenuItem editNick = new JMenuItem("닉네임 변경");
        editNick.addActionListener(e -> changeNickname());
        menu.add(editNick);

        JMenuItem uploadPhoto = new JMenuItem("사진 등록/변경");
        uploadPhoto.addActionListener(e -> changeProfilePhoto());
        menu.add(uploadPhoto);

        menu.addSeparator();

        JMenuItem changePw = new JMenuItem("비밀번호 변경");
        changePw.addActionListener(e -> openChangePassword());
        menu.add(changePw);

        JMenuItem logout = new JMenuItem("로그아웃");
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        menu.addSeparator();
        menu.add(logout);

        menu.show(comp, x, y);
    }

    private void changeNickname() {
        String input = JOptionPane.showInputDialog(this, "새 닉네임을 입력하세요.", currentNickname == null ? "" : currentNickname);
        if (input == null) return;

        String newNick = input.trim();
        if (newNick.isEmpty()) return;

        boolean ok = UserDAO.updateNickname(currentUserId, newNick);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "닉네임 변경 실패", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentNickname = newNick;
        JOptionPane.showMessageDialog(this, "닉네임이 변경되었습니다.");
    }

    private void changeProfilePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("프로필 사진 선택");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();

        boolean ok = UserDAO.updateProfileImagePath(currentUserId, path);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "프로필 사진 저장 실패", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentProfileImagePath = path;
        setProfileIcon(currentProfileImagePath);
        JOptionPane.showMessageDialog(this, "프로필 사진이 변경되었습니다.");
    }

    private void openChangePassword() {
        new ChangePasswordFrame(currentUserId);
    }

    // -----------------------------
    // 기존 기능들
    // -----------------------------
    private JPanel createTradingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        historyPanel = new HistoryPanel();
        historyPanel.setPreferredSize(new Dimension(350, 0));
        historyPanel.addCoinSelectionListener(this::onCoinSelected);

        JPanel centerArea = new JPanel(new BorderLayout());

        chartPanel = new CandleChartPanel("BTC 차트");
        chartPanel.setPreferredSize(new Dimension(0, 500));

        orderBookPanel = new OrderBookPanel("BTC");
        orderBookPanel.setPreferredSize(new Dimension(0, 350));
        orderBookPanel.setBorder(BorderFactory.createTitledBorder("호가창"));

        centerArea.add(chartPanel, BorderLayout.CENTER);
        centerArea.add(orderBookPanel, BorderLayout.SOUTH);

        orderPanel = new OrderPanel();
        orderPanel.setPreferredSize(new Dimension(350, 0));

        panel.add(historyPanel, BorderLayout.WEST);
        panel.add(centerArea, BorderLayout.CENTER);
        panel.add(orderPanel, BorderLayout.EAST);

        return panel;
    }

    private void toggleView() {
        isTradingView = !isTradingView;

        if (isTradingView) {
            mainCardLayout.show(mainContentPanel, CARD_TRADING);
            btnToggleView.setText("투자내역 보기");
            btnToggleView.setBackground(new Color(52, 152, 219));
        } else {
            mainCardLayout.show(mainContentPanel, CARD_INVESTMENT);
            btnToggleView.setText("거래화면 보기");
            btnToggleView.setBackground(new Color(46, 204, 113));
            investmentPanel.refreshAll();
        }
    }

    private void onCoinSelected(String coinSymbol) {
        if (coinSymbol == null || coinSymbol.isEmpty()) return;

        System.out.println("[MainFrame] 코인 선택됨: " + coinSymbol);

        chartPanel.changeMarket(coinSymbol);
        updateOrderBookPanel(coinSymbol);

        if (orderPanel != null) {
            orderPanel.setSelectedCoin(coinSymbol);
        }
    }

    private void updateOrderBookPanel(String coinSymbol) {
        if (orderBookPanel != null) {
            orderBookPanel.closeConnection();
        }

        orderBookPanel = new OrderBookPanel(coinSymbol);
        orderBookPanel.setPreferredSize(new Dimension(0, 350));
        orderBookPanel.setBorder(BorderFactory.createTitledBorder(coinSymbol + " 호가창"));

        JPanel centerArea = (JPanel) ((JPanel) tradingPanel.getComponent(1));
        centerArea.remove(1);
        centerArea.add(orderBookPanel, BorderLayout.SOUTH);

        centerArea.revalidate();
        centerArea.repaint();
    }

    private void initWebSocket() {
        if (timeController.isRealtimeMode()) {
            UpbitWebSocketDao.getInstance().start();
        }
    }

    @Override
    public void onTimeChanged(java.time.LocalDateTime newTime, boolean isRealtime) {
        SwingUtilities.invokeLater(() -> {
            if (isRealtime) {
                System.out.println("[MainFrame] 실시간 모드로 전환됨");
                UpbitWebSocketDao.getInstance().start();
            } else {
                System.out.println("[MainFrame] 백테스팅 모드로 전환됨: " + newTime);
                UpbitWebSocketDao.getInstance().close();
                loadHistoricalData(newTime);
            }
        });
    }

    private void loadHistoricalData(java.time.LocalDateTime targetTime) {
        System.out.println("[MainFrame] 과거 데이터 로드 중: " + targetTime);
        chartPanel.loadHistoricalData(targetTime);
    }

    @Override
    public void dispose() {
        UpbitWebSocketDao.getInstance().close();
        if (orderBookPanel != null) orderBookPanel.closeConnection();
        DBConnection.close();
        super.dispose();
    }

    // 테스트용 main
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainFrame("test@onbit.com"));
    }
}
