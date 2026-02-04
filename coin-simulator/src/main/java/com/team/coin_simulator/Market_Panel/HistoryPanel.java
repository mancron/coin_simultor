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
		
		for(int i = 0; i < 10; i++) {
		    CoinRowPanel row = new CoinRowPanel("BTC", "95,000,000", "+2.5%");
		    coinListContainer.add(row);
		    
		    // 실시간 업데이트를 위해 Map 등에 저장해두면 좋습니다.
		    // coinMap.put("BTC", row); 
		}
		
	}
	
	/*
	 *사용 
	 * addNewCoin(name, "0", "0%");
	 * */
	
	public void addNewCoin(String symbol, String price, String fluc) {
	    // 객체 생성
	    CoinRowPanel row = new CoinRowPanel(symbol, price, fluc);
	    // 리스트 컨테이너에 시각적으로 추가
	    coinListContainer.add(row);
	    // 맵에 저장 (나중에 찾기 위함)
	    coinMap.put(symbol, row);
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
