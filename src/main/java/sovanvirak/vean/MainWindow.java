package sovanvirak.vean;

import io.qt.widgets.*;
import io.qt.core.*;
import java.io.File;

/**
 * Main application window for the Multithreaded Sorting Comparison tool.
 * Handles all UI components and delegates logic to SingleThread, MultiThread, and Utils.
 *
 * @author sovanvirak.vean
 * @version 1.0
 */
public class MainWindow {

    /** Path to the currently loaded CSV file */
    private static String loadedFilePath;

    /** Currently loaded integer array */
    private static int[] originalArray;

    /**
     * Initializes and displays the main Qt window.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        QApplication.initialize(args);

        QMainWindow window = new QMainWindow();
        window.setWindowTitle("Multithreaded Sorter - Compare Mode");
        window.resize(900, 650);

        QWidget central = new QWidget();
        QVBoxLayout mainLayout = new QVBoxLayout(central);

        // ── File Attach ──
        QHBoxLayout fileLayout = new QHBoxLayout();
        QPushButton btnLoad = new QPushButton("📂 Attach CSV File");
        QLabel fileNameLabel = new QLabel("No file selected");
        fileNameLabel.setStyleSheet("color: gray;");
        fileLayout.addWidget(btnLoad);
        fileLayout.addWidget(fileNameLabel);
        fileLayout.addStretch();
        mainLayout.addLayout(fileLayout);

        // ── Original Array ──
        mainLayout.addWidget(new QLabel("📋 Original Array:"));
        QTextEdit originalDisplay = new QTextEdit();
        originalDisplay.setReadOnly(true);
        originalDisplay.setMaximumHeight(80);
        originalDisplay.setPlaceholderText("Original array will appear here...");
        mainLayout.addWidget(originalDisplay);

        // ── Sort Button ──
        QPushButton btnSort = new QPushButton("▶ Run Both & Compare");
        btnSort.setEnabled(false);
        btnSort.setStyleSheet("font-size: 14px; padding: 8px; background-color: #4CAF50; color: white;");
        mainLayout.addWidget(btnSort);

        // ── Side by Side ──
        QHBoxLayout resultsLayout = new QHBoxLayout();

        QVBoxLayout singleLayout = new QVBoxLayout();
        QLabel singleTitle = new QLabel("🔵 Single-Threaded");
        singleTitle.setStyleSheet("font-weight: bold; font-size: 13px;");
        QLabel singleTimeLabel = new QLabel("Time: -");
        singleTimeLabel.setStyleSheet("color: blue;");
        QTextEdit singleDisplay = new QTextEdit();
        singleDisplay.setReadOnly(true);
        singleLayout.addWidget(singleTitle);
        singleLayout.addWidget(singleTimeLabel);
        singleLayout.addWidget(singleDisplay);

        QVBoxLayout multiLayout = new QVBoxLayout();
        QLabel multiTitle = new QLabel("🟢 Multi-Threaded");
        multiTitle.setStyleSheet("font-weight: bold; font-size: 13px;");
        QLabel multiTimeLabel = new QLabel("Time: -");
        multiTimeLabel.setStyleSheet("color: green;");
        QTextEdit multiDisplay = new QTextEdit();
        multiDisplay.setReadOnly(true);
        multiLayout.addWidget(multiTitle);
        multiLayout.addWidget(multiTimeLabel);
        multiLayout.addWidget(multiDisplay);

        resultsLayout.addLayout(singleLayout);
        resultsLayout.addLayout(multiLayout);
        mainLayout.addLayout(resultsLayout);

        // ── Winner & Output ──
        QLabel winnerLabel = new QLabel("");
        winnerLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
        winnerLabel.setStyleSheet("font-size: 13px; font-weight: bold;");
        mainLayout.addWidget(winnerLabel);

        QLabel outputLabel = new QLabel("");
        outputLabel.setStyleSheet("color: green;");
        mainLayout.addWidget(outputLabel);

        // ── Load CSV ──
        btnLoad.clicked.connect(() -> {
            QFileDialog dialog = new QFileDialog(window, "Select CSV File");
            dialog.setNameFilter("CSV Files (*.csv)");
            dialog.setFileMode(QFileDialog.FileMode.ExistingFile);

            if (dialog.exec() == QDialog.DialogCode.Accepted.value()) {
                loadedFilePath = dialog.selectedFiles().get(0);
                originalArray = Utils.readCsv(loadedFilePath);

                if (originalArray != null) {
                    fileNameLabel.setText("✅ " + new File(loadedFilePath).getName());
                    fileNameLabel.setStyleSheet("color: green;");
                    originalDisplay.setText(Utils.arrayToString(originalArray));
                    singleDisplay.clear();
                    multiDisplay.clear();
                    singleTimeLabel.setText("Time: -");
                    multiTimeLabel.setText("Time: -");
                    winnerLabel.setText("");
                    outputLabel.setText("");
                    btnSort.setEnabled(true);
                } else {
                    originalDisplay.setText("❌ Error reading file.");
                    btnSort.setEnabled(false);
                }
            }
        });

        // ── Run Both ──
        btnSort.clicked.connect(() -> {
            if (originalArray == null) return;
            btnSort.setEnabled(false);
            winnerLabel.setText("⏳ Sorting...");

            // Single-threaded
            SingleThread single = new SingleThread(originalArray);
            single.sort();
            singleDisplay.setText(Utils.arrayToString(single.getResult()));
            singleTimeLabel.setText("Time: " + Utils.formatTime(single.getExecutionTime()));

            // Multi-threaded
            MultiThread multi = new MultiThread(originalArray);
            multi.sort();
            multiDisplay.setText(Utils.arrayToString(multi.getResult()));
            multiTimeLabel.setText("Time: " + Utils.formatTime(multi.getExecutionTime()));

            // Winner
            long st = single.getExecutionTime();
            long mt = multi.getExecutionTime();
            if (mt < st) {
                winnerLabel.setText("🏆 Multi-thread wins by " + Utils.formatTime(st - mt) + " faster!");
                winnerLabel.setStyleSheet("color: green; font-size: 13px; font-weight: bold;");
            } else {
                winnerLabel.setText("🏆 Single-thread wins by " + Utils.formatTime(mt - st) + " faster!");
                winnerLabel.setStyleSheet("color: blue; font-size: 13px; font-weight: bold;");
            }

            // Save output
            String outputPath = Utils.getSortedFilePath(loadedFilePath);
            if (Utils.writeCsv(outputPath, multi.getResult())) {
                outputLabel.setText("💾 Saved: " + new File(outputPath).getName());
            }

            btnSort.setEnabled(true);
        });

        window.setCentralWidget(central);
        window.show();

        QApplication.exec();
        QApplication.shutdown();
    }
}