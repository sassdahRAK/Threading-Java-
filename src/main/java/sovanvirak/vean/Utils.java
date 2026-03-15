package sovanvirak.vean;
import java.io.*;
import java.util.*;

/**
 * Utility class for reading and writing CSV files.
 * Handles all file I/O operations for integer arrays.
 *
 * @author sovanvirak.vean
 * @version 1.0
 */
public class Utils {

    /** Private constructor — utility class should not be instantiated */
    private Utils() {}

    /**
     * Reads a CSV file and returns its contents as an integer array.
     * Supports single-row CSV with comma-separated integers.
     *
     * @param filePath the absolute path to the CSV file
     * @return integer array of parsed values, or null if an error occurs
     */
    public static int[] readCsv(String filePath) {
        List<Integer> numbers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                for (String val : line.split(",")) {
                    String trimmed = val.trim();
                    if (!trimmed.isEmpty()) {
                        numbers.add(Integer.parseInt(trimmed));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("IO Error reading CSV: " + e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in CSV: " + e.getMessage());
            return null;
        }
        return numbers.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Writes an integer array to a CSV file as a single comma-separated row.
     *
     * @param filePath the absolute path to the output CSV file
     * @param array    the integer array to write
     * @return true if write was successful, false otherwise
     */
    public static boolean writeCsv(String filePath, int[] array) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
                if (i < array.length - 1) sb.append(",");
            }
            bw.write(sb.toString());
            return true;
        } catch (IOException e) {
            System.err.println("IO Error writing CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates the output file path for a sorted CSV.
     * Example: /path/data.csv → /path/data-sorted.csv
     *
     * @param originalPath the original file path
     * @return new file path with "-sorted" appended before extension
     */
    public static String getSortedFilePath(String originalPath) {
        int dot = originalPath.lastIndexOf('.');
        return dot == -1
                ? originalPath + "-sorted.csv"
                : originalPath.substring(0, dot) + "-sorted.csv";
    }

    /**
     * Converts an integer array to a readable bracketed string.
     * Example: [1, 2, 3, 4]
     *
     * @param array the integer array to convert
     * @return formatted string representation
     */
    public static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    /**
     * Formats nanoseconds into a human-readable time string.
     * Automatically selects µs, ms, or s based on magnitude.
     *
     * @param nanos time in nanoseconds
     * @return formatted time string
     */
    public static String formatTime(long nanos) {
        if (nanos < 1_000_000) return nanos / 1_000 + " µs";
        if (nanos < 1_000_000_000) return nanos / 1_000_000 + " ms";
        return String.format("%.2f s", nanos / 1_000_000_000.0);
    }
}

