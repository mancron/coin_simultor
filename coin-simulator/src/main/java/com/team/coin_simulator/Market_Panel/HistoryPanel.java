package com.team.coin_simulator.Market_Panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class HistoryPanel extends JPanel{
	
	JPanel coinListContainer,coinHeadPanel;
	JScrollPane scrollPanel;
	private HashMap<String, CoinRowPanel> coinMap = new HashMap<>();
	
	public HistoryPanel() {
		setLayout(new BorderLayout());
		
		coinHeadPanel = new JPanel(new GridLayout(1,3));
		coinHeadPanel.setBackground(Color.white);
		coinHeadPanel.setBorder(new MatteBorder(0,0,1,0,Color.LIGHT_GRAY));
		
		coinHeadPanel.add(new JLabel("코인명",SwingConstants.CENTER));
		coinHeadPanel.add(new JLabel("현재가",SwingConstants.CENTER));
		coinHeadPanel.add(new JLabel("전일대비",SwingConstants.CENTER));
		
		coinListContainer = new JPanel();
		coinListContainer.setLayout(new BoxLayout(coinListContainer,BoxLayout.Y_AXIS));
		coinListContainer.setBackground(Color.white);
		
		scrollPanel = new JScrollPane(coinListContainer);
		scrollPanel.setBorder(null);
		
		add(coinHeadPanel,BorderLayout.NORTH);
		add(scrollPanel,BorderLayout.CENTER);
		

		addNewCoin("BTC","95000000","+2.5");

		startRealTimeUpdate();
		
	}
	
	private void startRealTimeUpdate() {
	    Thread updateThread = new Thread(() -> {
	        while (true) {
	            try {
	                // 1. 여기서 실제로는 DB나 API로부터 데이터를 가져와야 함
	                // 현재는 테스트를 위해 랜덤 값이나 로직을 시뮬레이션
	                
	                // 2. Swing UI 업데이트는 반드시 invokeLater 안에서 실행
	                javax.swing.SwingUtilities.invokeLater(() -> {
	                    // 예시: 비트코인 가격을 랜덤하게 변경
	                    double randomPrice = 95000000 + (Math.random() * 100000 - 50000);
	                    updateCoinPrice("BTC", String.format("%,.0f", randomPrice), "+0.5%");
	                });

	                Thread.sleep(1000); // 1초마다 반복
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	                break;
	            }
	        }
	    });
	    updateThread.setDaemon(true); // 프로그램 종료 시 스레드도 자동 종료되도록 설정
	    updateThread.start();
	}
	
	
	//addNewCoin(name, "0", "0%");
	
	public void addNewCoin(String coinName, String price, String fluc) {
	    // 객체 생성
	    CoinRowPanel row = new CoinRowPanel(coinName, price, fluc);
	    // 리스트 컨테이너에 시각적으로 추가
	    coinListContainer.add(row);
	    // 맵에 저장 (나중에 찾기 위함)
	    coinMap.put(coinName, row);
	}
	
	
	
	public void updateCoinPrice(String symbol, String newPrice, String newFluc) {
	    if (coinMap.containsKey(symbol)) {
	        // 변수명 몰라도 이름만으로 찾아서 업데이트 가능
	        coinMap.get(symbol).updateData(newPrice, newFluc, Color.RED);
	    }
	}
	
	public static void main(String[] args) {
	    JFrame frame = new JFrame("코인 내역");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(400, 600);
	    

	    frame.add(new HistoryPanel()); 
	    
	    frame.setVisible(true);
	}

}
