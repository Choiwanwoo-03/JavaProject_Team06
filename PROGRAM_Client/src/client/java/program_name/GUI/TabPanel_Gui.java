package GUI;

public class TabPanel_Gui {
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initActionDB(); // 동작 등록 데이터 불러오기

        // JTabbedPane 설정 (탭 구조만 유지)
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("대시보드", buildDashboardPanel("대시보드"));
        tabs.addTab("동작 등록", buildActionPanel("동작 등록"));
        tabs.addTab("가이드", buildEmptyPanel("가이드")); 
        tabs.addTab("목표 달성 트래커", buildEmptyPanel("목표 달성 트래커"));
        tabs.addTab("도감", buildEmptyPanel("도감"));

        add(tabs, BorderLayout.CENTER);
}
