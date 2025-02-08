package org.tera201.vcstoolkit.info;

import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import kotlin.Pair;
import net.miginfocom.swing.MigLayout;
import org.repodriller.scm.entities.BlameManager;
import org.repodriller.scm.entities.CommitSize;
import org.repodriller.scm.entities.DeveloperInfo;
import org.tera201.swing.spinner.SpinnerProgress;
import org.tera201.vcstoolkit.panels.CommitPanel;
import org.tera201.vcstoolkit.tabs.*;
import org.tera201.vcstoolkit.utils.DateCalculator;
import org.tera201.swing.chart.ChartLegendRenderer;
import org.tera201.swing.chart.data.category.DefaultCategoryDataset;
import org.tera201.swing.chart.data.pie.DefaultPieDataset;
import org.tera201.swing.chart.line.LineChart;
import org.tera201.swing.chart.pie.PieChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class InfoPage {
    private JPanel panel1;
    private JPanel chartPanel;
    private JPanel commitPanel;
    private JScrollPane commitScrollPanel;
    private JScrollPane linechartScrollPane;
    private JLabel authorLabel;
    private JLabel curAuthorLabel;
    private JLabel rowsLabel;
    private JLabel rowSizeLabel;
    private JLabel revisionLabel;
    private JPanel mainInfoPane;
    private JLabel sizeLabel;
    private JPanel labelPane;
    private JLabel mainInfoLabel;
    private JPanel SpinnerPanel;
    private SpinnerProgress spinner;
    LineChart lineChart = new LineChart();
    private final TabManager tabManager;
    private String lastPathNode;

    public InfoPage(TabManager tabManager) {
        this.tabManager = tabManager;
        System.out.println(InfoPageUtilsKt.getPathByTab(tabManager));
        initSpinner();
    }

    private int getPaddingInPixels(float padding) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int dpi = toolkit.getScreenResolution();
        double cmToInch = 0.393701;
        return  (int)(dpi * cmToInch * padding);
    }

    public void open(Map<String, CommitSize> commitSizeMap, Map<String, DeveloperInfo> developerInfoMap) throws InterruptedException {
        String path = InfoPageUtilsKt.getPathByTab(tabManager);
        lastPathNode = (path != null) ? path.substring(path.lastIndexOf("/") + 1) : null;

        spinner.setIndeterminate(false);
        Thread.sleep(100);
        mainInfoPane.setVisible(true);
        updateLabels(developerInfoMap, commitSizeMap);
        intPieChart(developerInfoMap);
        initCalendarPane(commitSizeMap);
        initLineChart(commitSizeMap);
    }

    private void initSpinner() {
        SpinnerPanel = new JPanel(new MigLayout("fill, insets 0, align center center", "[center]"));
        spinner = new SpinnerProgress(100, 10);
        SpinnerPanel.add(spinner);
        spinner.setIndeterminate(true);
        linechartScrollPane.setViewportView(SpinnerPanel);
        mainInfoPane.setVisible(false);
    }

    private void updateLabels(Map<String, DeveloperInfo> developerInfoMap, Map<String, CommitSize> commitSizeMap) {
        mainInfoLabel.setText(lastPathNode);
        authorLabel.setText(commitSizeMap.values().stream().min(Comparator.comparingInt(CommitSize::getDate)).get().getAuthorName());
        sizeLabel.setText(String.valueOf(commitSizeMap.values().stream().max(Comparator.comparingInt(CommitSize::getDate)).get().getProjectSize()));
        curAuthorLabel.setText(developerInfoMap.values().stream().max(Comparator.comparingLong(DeveloperInfo::getActualLinesOwner)).get().getName());
        rowsLabel.setText(String.valueOf(developerInfoMap.values().stream().mapToLong(DeveloperInfo::getActualLinesOwner).sum()));
        rowSizeLabel.setText(String.valueOf(developerInfoMap.values().stream().mapToLong(DeveloperInfo::getActualLinesSize).sum()));
        setShortTextForLabel(revisionLabel, commitSizeMap.values().stream().max(Comparator.comparingInt(CommitSize::getDate)).get().getName(), 6);
        labelPane.setBorder(new EmptyBorder( getPaddingInPixels(0.5f), getPaddingInPixels(0.5f), 0, 0));
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

    private void intPieChart(Map<String, DeveloperInfo> developerInfoMap) {
        chartPanel.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:5,5,5,5;"
                + "background:null");
        chartPanel.setLayout(new MigLayout("wrap,fill,gap 10", "fill"));
        PieChart pieChart1 = new PieChart();
        JLabel header1 = new JLabel("Authors impact");
        header1.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:+1");
        pieChart1.setHeader(header1);
        pieChart1.getChartColor().addColor(Color.decode("#f87171"), Color.decode("#fb923c"), Color.decode("#fbbf24"), Color.decode("#a3e635"), Color.decode("#34d399"), Color.decode("#22d3ee"), Color.decode("#818cf8"), Color.decode("#c084fc"));
        pieChart1.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:5,5,5,5,$Component.borderColor,,20");
        pieChart1.setDataset(createPieData(developerInfoMap));
        chartPanel.add(pieChart1, "split 5,height 360");
        pieChart1.startAnimation();
    }

    private void initLineChart(Map<String, CommitSize> commitSizeMap) {
        lineChart = new LineChart();
        lineChart.setChartType(LineChart.ChartType.CURVE);
        lineChart.putClientProperty(FlatClientProperties.STYLE, ""
                + "border:5,5,5,5,$Component.borderColor,,20");
        linechartScrollPane.setViewportView(lineChart);
        createLineChartData(commitSizeMap.values());
        lineChart.startAnimation();
    }

    private void initCalendarPane(Map<String, CommitSize> commitSizeMap) {
        CommitPanel commitPanel1 = new CommitPanel();

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> yearList = new JBList<>(listModel);
        yearList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Calendar calendar = Calendar.getInstance();
        commitSizeMap.values().stream().map(commitSize -> {
            Date date = new Date((long) commitSize.getDate() * 1000);
            calendar.setTime(date);
            return calendar.get(Calendar.YEAR);
        }).collect(Collectors.toSet()).stream().sorted(Comparator.reverseOrder()).forEach(year -> {
            listModel.addElement(Integer.toString(year));
        });

        JBSplitter splitter = new JBSplitter(false, 0.95f);
        splitter.setDividerWidth(1);



        JScrollPane listScrollPane = new JBScrollPane(yearList);

        splitter.setFirstComponent(commitPanel1);
        splitter.setSecondComponent(listScrollPane);

        yearList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int year = Integer.parseInt(yearList.getSelectedValue());
                commitPanel1.updatePanel(year);

                commitSizeMap.values().stream().map(it -> {
                    calendar.setTime(new Date((long) it.getDate() * 1000));
                    return new Pair<> (calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
                }).filter(it -> year == it.component1()).forEach(it ->
                    commitPanel1.addCommitCountForDay(it.component2(), 1)
                );
                splitter.updateUI();
            }
        });
        yearList.setSelectedIndex(0);

        commitScrollPanel.setViewportView(splitter);
    }

    private DefaultPieDataset createPieData(BlameManager blameManager) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        blameManager.getRootPackageInfo().getAuthorInfo().forEach((s, blameAuthorInfo) -> {dataset.addValue(s, blameAuthorInfo.getLineCount());});
        return dataset;
    }

    private DefaultPieDataset createPieData(Map<String, DeveloperInfo> developerInfoMap) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        developerInfoMap.values().forEach(developerInfo -> dataset.addValue(developerInfo.getName(), developerInfo.getChanges()));
        return dataset;
    }

    private void createLineChartData(Collection<CommitSize> commitSizeCollection) {
        DefaultCategoryDataset<String, String> categoryDataset = new DefaultCategoryDataset<>();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        commitSizeCollection.stream().collect(Collectors.groupingBy(
                e -> df.format(new Date((long) e.getDate() * 1000)),
                Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparingInt(CommitSize::getDate)),
                        optionalEvent -> optionalEvent.orElse(null)
                )
        )).values().stream().sorted(Comparator.comparingInt(CommitSize::getDate)).forEach(commitSize -> {
            Date date = new Date((long) commitSize.getDate() * 1000);
            String formattedDate = df.format(date);
            categoryDataset.addValue(commitSize.getProjectSize(), "Project ", formattedDate);
        });

        /**
         * Control the legend we do not show all legend
         */
        try {
            Date date = df.parse(categoryDataset.getColumnKey(0));
            Date dateEnd = df.parse(categoryDataset.getColumnKey(categoryDataset.getColumnCount() - 1));

            DateCalculator dcal = new DateCalculator(date, dateEnd);
            long diff = dcal.getDifferenceDays();
            long valuesCount = categoryDataset.getColumnKeys().size();

            double d = Math.ceil((valuesCount / 20f));
            lineChart.setLegendRenderer(new ChartLegendRenderer() {
                @Override
                public Component getLegendComponent(Object legend, int index) {
                    if (index % d == 0) {
                        return super.getLegendComponent(legend, index);
                    } else {
                        return null;
                    }
                }
            });
        } catch (ParseException e) {
            System.err.println(e);
        }

        lineChart.setValuesFormat(new DecimalFormat("#,##0.## B"));

        lineChart.setCategoryDataset(categoryDataset);
        lineChart.getChartColor().addColor(Color.decode("#38bdf8"), Color.decode("#fb7185"), Color.decode("#34d399"));
        JLabel header = new JLabel("Project Size Evolution");
        header.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:+1;"
                + "border:0,0,5,0");
        lineChart.setHeader(header);
    }

    public JPanel getComponent() {
        return panel1;
    }
}
