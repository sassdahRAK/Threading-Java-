package sovanvirak.vean;

/**
 * Single-threaded merge sort implementation.
 * Sorts the entire array sequentially on one thread.
 *
 * @author sovanvirak.vean
 * @version 1.0
 */
public class SingleThread {

    /** The array to be sorted */
    private final int[] array;

    /** Execution time in nanoseconds after sorting */
    private long executionTime;

    /**
     * Constructs a SingleThread sorter with a copy of the given array.
     *
     * @param original the original unsorted array (will not be modified)
     */
    public SingleThread(int[] original) {
        this.array = original.clone();
        this.executionTime = 0;
    }

    /**
     * Executes the single-threaded merge sort and records execution time.
     */
    public void sort() {
        long start = System.nanoTime();
        mergeSort(array, 0, array.length - 1);
        this.executionTime = System.nanoTime() - start;
    }

    /**
     * Recursively divides and sorts the array using merge sort.
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
            merge(arr, left, mid, right);
        }
    }

    /**
     * Merges two sorted subarrays into one sorted subarray in-place.
     *
     * @param arr   the array containing both subarrays
     * @param left  start index of left subarray
     * @param mid   end index of left subarray
     * @param right end index of right subarray
     */
    private void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right)
            temp[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        System.arraycopy(temp, 0, arr, left, temp.length);
    }

    /**
     * Returns the sorted array.
     *
     * @return sorted integer array
     */
    public int[] getResult() {
        return array;
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

