package sovanvirak.vean;

import io.qt.widgets.*;
import io.qt.core.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class MainWindow {

    private static int[] originalArray;
    private static String loadedFilePath;

    public static void main(String[] args) {
        QApplication.initialize(args);

        QMainWindow window = new QMainWindow();
        window.setWindowTitle("Multithreaded Sorter - Compare Mode");
        window.resize(900, 650);

        QWidget central = new QWidget();
        QVBoxLayout mainLayout = new QVBoxLayout(central);

        // ── Top: File Attach ──
        QHBoxLayout fileLayout = new QHBoxLayout();
        QPushButton btnLoad = new QPushButton("📂 Attach CSV File");
        QLabel fileNameLabel = new QLabel("No file selected");
        fileNameLabel.setStyleSheet("color: gray;");
        fileLayout.addWidget(btnLoad);
        fileLayout.addWidget(fileNameLabel);
        fileLayout.addStretch();
        mainLayout.addLayout(fileLayout);

        // ── Original Array Display ──
        mainLayout.addWidget(new QLabel("📋 Original Array:"));
        QTextEdit originalDisplay = new QTextEdit();
        originalDisplay.setReadOnly(true);
        originalDisplay.setPlaceholderText("Original array will appear here...");
        originalDisplay.setMaximumHeight(80);
        mainLayout.addWidget(originalDisplay);

        // ── Sort Button ──
        QPushButton btnSort = new QPushButton("▶ Run Both & Compare");
        btnSort.setEnabled(false);
        btnSort.setStyleSheet("font-size: 14px; padding: 8px; background-color: #4CAF50; color: white;");
        mainLayout.addWidget(btnSort);

        // ── Side by Side Results ──
        QHBoxLayout resultsLayout = new QHBoxLayout();

        // Single Thread Column
        QVBoxLayout singleLayout = new QVBoxLayout();
        QLabel singleTitle = new QLabel("🔵 Single-Threaded");
        singleTitle.setStyleSheet("font-weight: bold; font-size: 13px;");
        QLabel singleTimeLabel = new QLabel("Time: -");
        singleTimeLabel.setStyleSheet("color: blue;");
        QTextEdit singleDisplay = new QTextEdit();
        singleDisplay.setReadOnly(true);
        singleDisplay.setPlaceholderText("Single-threaded result...");
        singleLayout.addWidget(singleTitle);
        singleLayout.addWidget(singleTimeLabel);
        singleLayout.addWidget(singleDisplay);

        // Multi Thread Column
        QVBoxLayout multiLayout = new QVBoxLayout();
        QLabel multiTitle = new QLabel("🟢 Multi-Threaded");
        multiTitle.setStyleSheet("font-weight: bold; font-size: 13px;");
        QLabel multiTimeLabel = new QLabel("Time: -");
        multiTimeLabel.setStyleSheet("color: green;");
        QTextEdit multiDisplay = new QTextEdit();
        multiDisplay.setReadOnly(true);
        multiDisplay.setPlaceholderText("Multi-threaded result...");
        multiLayout.addWidget(multiTitle);
        multiLayout.addWidget(multiTimeLabel);
        multiLayout.addWidget(multiDisplay);

        resultsLayout.addLayout(singleLayout);
        resultsLayout.addLayout(multiLayout);
        mainLayout.addLayout(resultsLayout);

        // ── Winner Label ──
        QLabel winnerLabel = new QLabel("");
        winnerLabel.setStyleSheet("font-size: 13px; font-weight: bold;");
        winnerLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
        mainLayout.addWidget(winnerLabel);

        // ── Output File Label ──
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
                fileNameLabel.setText("✅ " + new File(loadedFilePath).getName());
                fileNameLabel.setStyleSheet("color: green;");

                originalArray = readCsv(loadedFilePath);
                if (originalArray != null) {
                    originalDisplay.setText(arrayToString(originalArray));
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

        // ── Sort Both ──
        btnSort.clicked.connect(() -> {
            if (originalArray == null) return;
            btnSort.setEnabled(false);
            winnerLabel.setText("⏳ Sorting...");

            // ── Single-Threaded Sort ──
            int[] singleSorted = originalArray.clone();
            long singleStart = System.nanoTime();
            mergeSort(singleSorted, 0, singleSorted.length - 1);
            long singleTime = System.nanoTime() - singleStart;

            singleDisplay.setText(arrayToString(singleSorted));
            singleTimeLabel.setText("Time: " + formatTime(singleTime));

            // ── Multi-Threaded Sort ──
            int[] multiSorted = originalArray.clone();
            int mid = multiSorted.length / 2;
            int[] result = new int[multiSorted.length];
            CountDownLatch latch = new CountDownLatch(2);

            long multiStart = System.nanoTime();

            // Sorting Thread 0 — left half
            Thread t0 = new Thread(() -> {
                mergeSort(multiSorted, 0, mid - 1);
                latch.countDown();
            });

            // Sorting Thread 1 — right half
            Thread t1 = new Thread(() -> {
                mergeSort(multiSorted, mid, multiSorted.length - 1);
                latch.countDown();
            });

            t0.start();
            t1.start();

            try {
                latch.await(); // wait for both sorting threads
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Merging Thread — merge both halves
            Thread mergeThread = new Thread(() -> {
                merge(multiSorted, result, 0, mid - 1, multiSorted.length - 1);
            });
            mergeThread.start();
            try {
                mergeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long multiTime = System.nanoTime() - multiStart;

            multiDisplay.setText(arrayToString(result));
            multiTimeLabel.setText("Time: " + formatTime(multiTime));

            // ── Winner ──
            if (multiTime < singleTime) {
                long diff = singleTime - multiTime;
                winnerLabel.setText("🏆 Multi-thread wins by " + formatTime(diff) + " faster!");
                winnerLabel.setStyleSheet("color: green; font-size: 13px; font-weight: bold;");
            } else {
                long diff = multiTime - singleTime;
                winnerLabel.setText("🏆 Single-thread wins by " + formatTime(diff) + " faster!");
                winnerLabel.setStyleSheet("color: blue; font-size: 13px; font-weight: bold;");
            }

            // ── Save sorted output ──
            String outputPath = getSortedFilePath(loadedFilePath);
            writeCsv(outputPath, result);
            outputLabel.setText("💾 Saved: " + new File(outputPath).getName());

            btnSort.setEnabled(true);
        });

        window.setCentralWidget(central);
        window.show();

        QApplication.exec();
        QApplication.shutdown();
    }

    // ── Merge Sort ──
    private static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            mergeInPlace(arr, left, mid, right);
        }
    }

    private static void mergeInPlace(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right)
            temp[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    // ── Final Merge (for merge thread) ──
    private static void merge(int[] arr, int[] result, int left, int mid, int right) {
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right)
            result[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        while (i <= mid) result[k++] = arr[i++];
        while (j <= right) result[k++] = arr[j++];
    }

    // ── Read CSV → int[] ──
    private static int[] readCsv(String filePath) {
        List<Integer> numbers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                for (String val : line.split(",")) {
                    String trimmed = val.trim();
                    if (!trimmed.isEmpty())
                        numbers.add(Integer.parseInt(trimmed));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return numbers.stream().mapToInt(Integer::intValue).toArray();
    }

    // ── Write int[] → CSV ──
    private static void writeCsv(String filePath, int[] array) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
                if (i < array.length - 1) sb.append(",");
            }
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ──
    private static String getSortedFilePath(String path) {
        int dot = path.lastIndexOf('.');
        return dot == -1 ? path + "-sorted.csv" : path.substring(0, dot) + "-sorted.csv";
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    private static String formatTime(long nanos) {
        if (nanos < 1_000_000) return nanos / 1_000 + " µs";
        if (nanos < 1_000_000_000) return nanos / 1_000_000 + " ms";
        return String.format("%.2f s", nanos / 1_000_000_000.0);
    }
}