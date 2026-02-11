package Investment_details.Asset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.DefaultPieDataset;

import DTO.MyAssetStatusDTO;

public class Asset_PortfolioChartPanel extends JPanel {

    private DefaultPieDataset dataset;
    private JFreeChart chart;

    public Asset_PortfolioChartPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. 데이터셋 객체 생성
        dataset = new DefaultPieDataset();

        // 2. 도넛 차트(Ring Chart) 생성
        chart = ChartFactory.createRingChart(
                "포트폴리오 비중", // 차트 제목
                dataset,        // 데이터셋
                true,           // 범례(Legend) 표시 여부
                true,           // 툴팁 사용 여부
                false           // URL 링크 사용 여부
        );
        
        Font titleFont = new Font("맑은 고딕", Font.BOLD, 18);
        Font labelFont = new Font("돋움", Font.PLAIN, 12);
        Font legendFont = new Font("돋움", Font.PLAIN, 12);

        chart.getTitle().setFont(titleFont);

        // 2. 범례(Legend) 폰트 설정
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(legendFont);
        }

        // 3. 플롯(차트 내부 라벨) 폰트 설정

        
        // 3. 차트 디자인 커스텀
        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setLabelFont(labelFont);
        plot.setSectionDepth(0.35); // 도넛의 두께 설정 (0.0 ~ 1.0)
        // 라벨 포맷 설정: {0}=이름, {1}=값, {2}=퍼센트
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})")); 
        plot.setBackgroundPaint(Color.WHITE); // 배경색
        plot.setOutlineVisible(false); // 차트 테두리 제거
        plot.setLabelBackgroundPaint(new Color(220, 220, 220, 100)); // 라벨 배경 투명도

        // 4. 패널에 차트 추가
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 300));
        chartPanel.setMouseWheelEnabled(true); // 마우스 휠 줌 허용
        add(chartPanel, BorderLayout.CENTER);
    }

    // 차트 데이터 갱신 메서드
    public void updateChart(List<MyAssetStatusDTO> assetList, BigDecimal krwBalance) {
        dataset.clear(); // 기존 데이터 초기화
        
        // 1. 원화(KRW) 잔고 추가
        if (krwBalance != null && krwBalance.compareTo(BigDecimal.ZERO) > 0) {
            dataset.setValue("KRW", krwBalance);
        }

        // 2. 보유 코인 자산 추가
        if (assetList != null) {
            for (MyAssetStatusDTO dto : assetList) {
                // 평가금액이 0보다 큰 경우에만 차트에 표시
                if (dto.getTotalValue().compareTo(BigDecimal.ZERO) > 0) {
                    dataset.setValue(dto.getCurrency(), dto.getTotalValue());
                }
            }
        }
    }
}