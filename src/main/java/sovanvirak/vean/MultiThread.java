package sovanvirak.vean;

import java.util.concurrent.CountDownLatch;

/**
 * Multithreaded merge sort implementation.
 * Uses two sorting threads for each half and one merging thread.
 *
 * @author sovanvirak.vean
 * @version 1.0
 */
public class MultiThread {

    /** Shared array split into two halves for parallel sorting */
    private final int[] array;

    /** Final merged result array */
    private final int[] result;

    /** Execution time in nanoseconds after sorting */
    private long executionTime;

    /**
     * Constructs a MultiThread sorter with a copy of the given array.
     *
     * @param original the original unsorted array (will not be modified)
     */
    public MultiThread(int[] original) {
        this.array = original.clone();
        this.result = new int[original.length];
        this.executionTime = 0;
    }

    /**
     * Executes the multithreaded sort:
     * Thread 0 sorts the left half,
     * Thread 1 sorts the right half,
     * Merge thread combines both halves into result.
     */
    public void sort() {
        int mid = array.length / 2;
        CountDownLatch latch = new CountDownLatch(2);

        long start = System.nanoTime();

        // Sorting Thread 0 — left half
        Thread t0 = new Thread(() -> {
            mergeSort(array, 0, mid - 1);
            latch.countDown();
        });

        // Sorting Thread 1 — right half
        Thread t1 = new Thread(() -> {
            mergeSort(array, mid, array.length - 1);
            latch.countDown();
        });

        t0.start();
        t1.start();

        try {
            latch.await(); // wait for both sorting threads to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Sorting interrupted: " + e.getMessage());
        }

        // Merging Thread — merges both sorted halves
        Thread mergeThread = new Thread(() ->
                mergeFinal(array, result, 0, mid - 1, array.length - 1)
        );
        mergeThread.start();

        try {
            mergeThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Merge interrupted: " + e.getMessage());
        }

        this.executionTime = System.nanoTime() - start;
    }

    /**
     * Recursively divides and sorts a subarray using merge sort.
     *
     * @param arr   the array to sort
     * @param left  starting index
     * @param right ending index
     */
    private void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            mergeInPlace(arr, left, mid, right);
        }
    }

    /**
     * Merges two sorted subarrays in-place.
     *
     * @param arr   source array
     * @param left  start of left subarray
     * @param mid   end of left subarray
     * @param right end of right subarray
     */
    private void mergeInPlace(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right)
            temp[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    /**
     * Merges two sorted halves of arr into the result array.
     *
     * @param arr    source array with two sorted halves
     * @param result destination array for merged output
     * @param left   start index
     * @param mid    end of left half
     * @param right  end of right half
     */
    private void mergeFinal(int[] arr, int[] result, int left, int mid, int right) {
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right)
            result[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        while (i <= mid) result[k++] = arr[i++];
        while (j <= right) result[k++] = arr[j++];
    }

    /**
     * Returns the final merged sorted array.
     *
     * @return sorted integer array
     */
    public int[] getResult() {
        return result;
    }

    /**
     * Returns the execution time of the last sort operation.
     *
     * @return execution time in nanoseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }
}