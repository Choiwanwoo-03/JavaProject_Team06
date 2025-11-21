package GUI;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class DashBoard_Gui extends JFrame {
    // 날짜 및 시간 포맷
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // 대시보드 패널에 필요한 필드들
    JTable tbAction; 
    JLabel TotalCO2;
    JTextField tfSearch;
    DefaultTableModel table;

    public DashBoard_Gui () {
        super("대시보드 구현");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("대시보드", buildDashboardPanel("대시보드"));
        // tabs.addTab("동작 등록", buildEmptyPanel("동작 등록"));
        // tabs.addTab("가이드", buildEmptyPanel("가이드")); 
        // tabs.addTab("목표 달성 트래커", buildEmptyPanel("목표 달성 트래커"));
        // tabs.addTab("도감", buildEmptyPanel("도감"));

        add(tabs, BorderLayout.CENTER);
    }

    // 메인 함수
    public static void main(String[] args) {
        // Swing 애플리케이션 시작
        SwingUtilities.invokeLater(() -> new test().setVisible(true));
    }

    private JPanel buildDashboardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 대시보드 가장 위에 표시 되는 총 예상 배출량
        TotalCO2 = new JLabel("오늘의 총 예상 배출량: 0.00 kg CO₂e", SwingConstants.CENTER);
        TotalCO2.setFont(TotalCO2.getFont().deriveFont(Font.BOLD, 16f));
        p.add(TotalCO2, BorderLayout.NORTH);

        // 대시보드에 표시 되는 테이블 모델
        table = new DefaultTableModel(new String[]{
                "날짜", "시간", "동작", "수량", "단위", "CO₂e(kg)"
        }, 0){
            public boolean isCellEditable(int r, int c){return false;}
        };

        // 테이블 모델을 보여주는 역할
        tbAction = new JTable(table);
        tbAction.setFillsViewportHeight(true);
        p.add(new JScrollPane(tbAction), BorderLayout.CENTER);

        //Right, Left 패널을 담는 종합 패널
        JPanel South = new JPanel(new BorderLayout(6,6));

        // 검색 텍스트 필드, 필터, 필터 해제 버튼을 담는 Left패널
        JPanel Left = new JPanel(new FlowLayout(FlowLayout.LEFT));
    
        // 검색 텍스트 필드
        Left.add(new JLabel("검색:"));
        tfSearch = new JTextField(20);
        Left.add(tfSearch); 

        // 필터 버튼
        JButton Filter = new JButton("필터");
        // Filter.addActionListener(e -> applyFilter());
        Left.add(Filter);

        // 필터 해제 버튼
        JButton removeFilter = new JButton("필터 해제");
        // removeFilter.addActionListener(e -> {tfSearch.setText(""); loadAllDataFromServer(); });
        Left.add(removeFilter);

        South.add(Left, BorderLayout.WEST);

        // 수정, 삭제 버튼을 담는 Right패널
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 수정 버튼
        JButton Edit = new JButton("수정");
        // Edit.addActionListener(e -> editSelectedAction());
        right.add(Edit);

        // 삭제 버튼
        JButton Delete = new JButton("삭제");
        // Delete.addActionListener(e -> deleteSelectedAction());
        right.add(Delete);

        South.add(right, BorderLayout.EAST);

        //매인 패널 중앙에 South 패널 위치
        p.add(South, BorderLayout.SOUTH);

        return p;
    }

}
