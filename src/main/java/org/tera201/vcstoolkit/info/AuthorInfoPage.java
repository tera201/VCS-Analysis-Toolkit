package org.tera201.vcstoolkit.info;

import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import net.miginfocom.swing.MigLayout;
import org.repodriller.scm.entities.CommitSize;
import org.repodriller.scm.entities.DeveloperInfo;
import org.tera201.swing.chart.bar.HorizontalBarChart;
import org.tera201.swing.chart.data.pie.DefaultPieDataset;
import org.tera201.swing.chart.pie.PieChart;
import org.tera201.swing.spinner.SpinnerProgress;
import org.tera201.vcstoolkit.panels.CommitPanel;
import org.tera201.vcstoolkit.tabs.TabManager;
import org.tera201.vcstoolkit.utils.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AuthorInfoPage {
    private JLabel authorNameLabel;
    private JLabel commitCountLabel;
    private JLabel commitFrequencyLabel;
    private JLabel avgCommitTimeLabel;
    private JLabel createdBranchesLabel;
    private JLabel ownerPercentageLabel;
    private JPanel filePiePanel;
    private JPanel linesPiePanel;
    private JPanel mainPanel;
    private JPanel barChartByDayPanel;
    private JScrollPane calendarScrollPane;
    private JLabel emailLabel;
    private JPanel barChartByMonthPanel;
    private JPanel barChartByHoursPanel;
    private JLabel lastActivityLabel;
    private JPanel stableCommitPanel;

    private final TabManager tabManager;
    private JPanel spinnerPanel;
    private SpinnerProgress spinner;

    private String email;

    public AuthorInfoPage(TabManager tabManager, String email) {
        this.tabManager = tabManager;
        this.email = email;
        setShortTextForLabel(emailLabel, email, 6);
        initSpinner();
    }

    private void initSpinner() {
        spinnerPanel = new JPanel(new MigLayout("fill, insets 0, align center center", "[center]"));
        spinner = new SpinnerProgress(100, 10);
        spinnerPanel.add(spinner);
        spinner.setIndeterminate(true);
        calendarScrollPane.setViewportView(spinnerPanel);
//        somePane.setVisible(false);
    }

    public void open(Map<String, CommitSize> commitSizeMap, Map<String, DeveloperInfo> developerInfoMap) throws InterruptedException {
        updateLabels(developerInfoMap, commitSizeMap);
        intPieChart(createPieDataFile(developerInfoMap.get(email)), filePiePanel, "File actions");
        intPieChart(createPieDataLines(developerInfoMap.get(email)), linesPiePanel, "Line actions");
        intPieChart(createPieDataStable(commitSizeMap), stableCommitPanel, "Stable commits");
        initCalendarPane(commitSizeMap);
        createBarChart(barChartByHoursPanel, createBarDataByHours(commitSizeMap), "Commit by hours");
        createBarChart(barChartByDayPanel, createBarDataByDay(commitSizeMap), "Commit by day of week");
        createBarChart(barChartByMonthPanel, createBarDataByDayOfMouth(commitSizeMap), "Commit by day of month");
        createBarChart(barChartByMonthPanel, createBarDataByMonth(commitSizeMap), "Commit by month");

    }

    private void updateLabels(Map<String, DeveloperInfo> developerInfoMap, Map<String, CommitSize> commitSizeMap) {
        authorNameLabel.setText(developerInfoMap.get(email).getName());
        commitCountLabel.setText(String.valueOf(developerInfoMap.get(email).getCommits().size()));
        createdBranchesLabel.setText("");
        double lines = developerInfoMap.values().stream().mapToDouble(DeveloperInfo::getActualLinesOwner).sum();
        ownerPercentageLabel.setText(String.format("%.2f", ((developerInfoMap.get(email).getActualLinesOwner()) / lines) * 100));
        List<Integer> commitDates = commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).map(CommitSize::getDate).sorted().toList();
        List<Integer> differences = new ArrayList<>();
        for (int i = 1; i < commitDates.size(); i++) {
            differences.add(commitDates.get(i) - commitDates.get(i - 1));
        }
        int date = commitDates.stream().max(Integer::max).orElse(0);
        if (date != 0) {lastActivityLabel.setText(DateUtils.Companion.getStringDate(date));} else lastActivityLabel.setText("");
        double daysCount = commitDates.stream().map(DateUtils.Companion::timestampToLocalDate).collect(Collectors.toSet()).size();
        commitFrequencyLabel.setText(String.format("%.2f", differences.size()/daysCount) + " per day");
        avgCommitTimeLabel.setText(String.format("%.2f",  differences.stream().mapToInt(Integer::intValue).sum() / (24 * 3600.0 * differences.size())) + " day");
    }

    private void setShortTextForLabel(JLabel label, String text, int width) {
        String shortenedText = text.substring(0, width) + "...";
        label.setText(shortenedText);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                label.setText(label.getText().equals(text) ? shortenedText : text);
            }
        });
    }

    private void initCalendarPane(Map<String, CommitSize> commitSizeMap) {
        Map<Integer, CommitPanel> commitPanels = new HashMap<>();

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> yearList = new JBList<>(listModel);
        yearList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).map(commitSize -> {
            LocalDate date1 = DateUtils.Companion.timestampToLocalDate(commitSize.getDate());
            return date1.getYear();
        }).collect(Collectors.toSet()).stream().sorted(Comparator.reverseOrder()).forEach(year -> {
            listModel.addElement(Integer.toString(year));
            commitPanels.put(year, new CommitPanel(year));
        });


        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).forEach(commitSize -> {
            LocalDate date1 = DateUtils.Companion.timestampToLocalDate(commitSize.getDate());
            int year = date1.getYear();
            int day = date1.getDayOfYear();
            commitPanels.get(year).addCommitCountForDay(day, 1);
        });
        yearList.setSelectedIndex(0);

        JBSplitter splitter = new JBSplitter(false, 0.95f);
        splitter.setDividerWidth(1);



        JScrollPane listScrollPane = new JBScrollPane(yearList);

        splitter.setFirstComponent(commitPanels.get(Integer.parseInt(yearList.getSelectedValue())));
        splitter.setSecondComponent(listScrollPane);

        yearList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                splitter.setFirstComponent(commitPanels.get(Integer.parseInt(yearList.getSelectedValue())));
            }
        });

        calendarScrollPane.setViewportView(splitter);
    }

    private void intPieChart(DefaultPieDataset defaultPieDataset, JPanel panel, String name) {
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:5,5,5,5;"
                + "background:null");
        panel.setLayout(new MigLayout("wrap,fill,gap 10", "fill"));
        PieChart pieChart1 = new PieChart();
        JLabel header1 = new JLabel(name);
        header1.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:+1");
        pieChart1.setHeader(header1);
        pieChart1.getChartColor().addColor(Color.decode("#a3e635"), Color.decode("#f87171"), Color.decode("#fb923c"));
        pieChart1.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:5,5,5,5,$Component.borderColor,,20");
        pieChart1.setDataset(defaultPieDataset);
        panel.add(pieChart1, "split 5,height 360");
        pieChart1.visibleLegend(false);
        pieChart1.startAnimation();
    }

    private DefaultPieDataset createPieDataFile(DeveloperInfo developerInfo) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.addValue("Files added", developerInfo.getFileAdded());
        dataset.addValue("Files deleted", developerInfo.getFileDeleted());
        dataset.addValue("Files modified", developerInfo.getFileModified());
        return dataset;
    }

    private DefaultPieDataset createPieDataLines(DeveloperInfo developerInfo) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.addValue("Lines added", developerInfo.getLinesAdded());
        dataset.addValue("Lines deleted", developerInfo.getLinesDeleted());
        dataset.addValue("Lines modified", developerInfo.getLinesModified());
        return dataset;
    }

    private DefaultPieDataset createPieDataStable(Map<String, CommitSize> commitSizeMap) {
        AtomicReference<Integer> stableCommitCount = new AtomicReference<>(0);
        AtomicReference<Integer> unStableCommitSize = new AtomicReference<>(0);
        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).forEach( it ->{
            if (it.getStability() <= 0.2) unStableCommitSize.getAndSet(unStableCommitSize.get() + 1);
            else stableCommitCount.getAndSet(stableCommitCount.get() + 1);
        });
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.addValue("stable commit count", stableCommitCount.get());
        dataset.addValue("unstable commit count", unStableCommitSize.get());
        return dataset;
    }

    private void createBarChart(JPanel mainPanel, DefaultPieDataset dataset, String title) {
        HorizontalBarChart barChart1 = new HorizontalBarChart();
        barChart1.setValuesFormat(new DecimalFormat("#,##0"));
        JLabel header1 = new JLabel(title);
        header1.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:+1;"
                + "border:0,0,5,0");
        barChart1.setHeader(header1);
        barChart1.setBarColor(Color.decode("#f97316"));
        barChart1.setDataset(dataset);
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:5,5,5,5,$Component.borderColor,,20");
        GridConstraints constraints = new GridConstraints();
        constraints.setFill(GridConstraints.FILL_BOTH);
        constraints.setRow(0);
        constraints.setColumn(0);
        mainPanel.add(barChart1, constraints);
    }

    private DefaultPieDataset createBarDataByDay(Map<String, CommitSize> commitSizeMap) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<DayOfWeek, Integer> commitCountMap = new HashMap<>();
        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).map(CommitSize::getDate)
                        .forEach(it -> {
                            DayOfWeek day = DateUtils.Companion.getDayOfWeek(it);
                            commitCountMap.put(day, commitCountMap.getOrDefault(day, 0) + 1);
                        });
        commitCountMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(it -> dataset.addValue(String.valueOf(it.getKey()), it.getValue()));
        return dataset;
    }

    private DefaultPieDataset createBarDataByMonth(Map<String, CommitSize> commitSizeMap) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<Month, Integer> commitCountMap = new HashMap<>();
        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).map(CommitSize::getDate)
                .forEach(it -> {
                    Month month = DateUtils.Companion.getMonthOfYear(it);
                    commitCountMap.put(month, commitCountMap.getOrDefault(month, 0) + 1);
                });
        commitCountMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(it -> dataset.addValue(String.valueOf(it.getKey()), it.getValue()));
        return dataset;
    }

    private DefaultPieDataset createBarDataByHours(Map<String, CommitSize> commitSizeMap) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        int[] commitCount = new int[24];
        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).map(CommitSize::getDate)
                .forEach(it -> commitCount[DateUtils.Companion.getHourOfDay(it)]++);
        IntStream.range(0, commitCount.length).forEach(index -> {if (commitCount[index] > 0 ) dataset.addValue(index + ":00", commitCount[index]);});
        return dataset;
    }

    private DefaultPieDataset createBarDataByDayOfMouth(Map<String, CommitSize> commitSizeMap) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        int[] commitCount = new int[32];
        commitSizeMap.values().stream().filter(it -> Objects.equals(it.getAuthorEmail(), email)).map(CommitSize::getDate)
                .forEach(it -> commitCount[DateUtils.Companion.getDayOfMouth(it)]++);
        IntStream.range(0, commitCount.length).forEach(index -> {if (commitCount[index] > 0 ) dataset.addValue(String.valueOf(index + 1), commitCount[index]);});
        return dataset;
    }

        public JPanel getComponent() {
        return mainPanel;
    }
}
