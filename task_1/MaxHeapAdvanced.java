import java.lang.Math;
import java.util.Scanner;

public class MaxHeapAdvanced {
    int capacity = 0;
    int size = 0;
    int x = 0;
    Integer[] arr;

    public MaxHeapAdvanced(int cap, int x){
        // construcutor 
        this.capacity = cap;
        this.size = 0;
        arr = new Integer[this.capacity];
        if(x < 1 || x > 10){
            double max_childern = 1e3 + 24;
            throw new RuntimeException("x can`t be less than 1 or greater than 10 max children per node "+ max_childern);
        }
        this.x = x;
    }

    private int parent(int i){
        return (int)((i - 1) / Math.pow(2, x));
    }

    private int child_node_i(int parent, int child) {
        int child_idx = (int)(Math.pow(2, x) * parent + child);
        // -1 indicates that there is no childern to current node as it would be outside of memory
        return child_idx >= capacity ? -1 : child_idx; 
    }

    public void insert(int element){
        if(size == capacity){
            throw new RuntimeException("Size is full cant insert new element");
        }
        arr[size] = element;
        int element_idx = size;
        size++;

        while(arr[element_idx] > arr[parent(element_idx)]) {
            int temp = arr[element_idx];
            arr[element_idx] = arr[parent(element_idx)];
            arr[parent(element_idx)] = temp;
            element_idx = parent(element_idx);
        }
    }

    public int getMax(){
        return arr[0];
    }

    private void heapify(int idx){
        int max_child_idx = idx;
        for(int i=1; i < Math.pow(2,x) + 1; i++) {
            int child_node = child_node_i(idx, i);
            if(
                child_node != -1 && 
                arr[child_node] != null && 
                arr[child_node] > arr[max_child_idx] 
            ) {
                max_child_idx = child_node_i(idx, i);
            }
        }
        if(idx != max_child_idx){
            int temp = arr[idx];
            arr[idx] = arr[max_child_idx];
            arr[max_child_idx] = temp;
            heapify(max_child_idx);
        }
    }

    public int pop(){
        if (size <= 0){
            throw new RuntimeException("No elements in list to pop");
        }
        int ans = getMax();
        arr[0] = arr[size - 1];
        arr[size - 1] = null;
        size -= 1;
        heapify(0);
        return ans;
    }

    public static long runAdvancedHeapAlgorithm(int cap, int x){
        long startTime = System.currentTimeMillis();
        MaxHeapAdvanced advanced = new MaxHeapAdvanced(cap, x);
        StringBuffer sb = new StringBuffer();
        // Insert all elements from ascending order 
        for(int i=0;i<cap;i++){
            advanced.insert(i+1);
        }
        // Poll all elements from heap
        for(int i=0;i<cap;i++){
            int res = advanced.pop();
            sb.append(res);
            sb.append(i < cap - 1 ? "->" : "");
        }
        return System.currentTimeMillis() - startTime;
    }

    public static double averageTime(int cap, int x, int iterations) {
        long totalTime = 0;
        for (int i = 0; i < iterations; i++){
            totalTime += runAdvancedHeapAlgorithm(cap, x);
        }
        return totalTime / (double) iterations;
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        int cap = (int)5e4;
        int iterations = (int)2e1;

        System.out.print("Choose x from 1 - 10: ");
        int x = sc.nextInt();
        sc.close();
        
        double avgTime = averageTime(cap, x, iterations);
        System.out.println("Average execution time over " + iterations + " iterations for " + cap + " elements: " + avgTime + " milliseconds");

        long singleRunTime = runAdvancedHeapAlgorithm(cap, x);
        System.out.println("Single run execution time: " + singleRunTime + " milliseconds");
    }
}

/*
 * Execution Summary:
 * O(log_2^x*(N)) for insertion or O(log n / x)
 * O((2^x)*(log_2^x)*n) or O(2^x/x * log n) for popping a single element
 * O(N*(2^x)*(log2^x)) for popping all elements from list
 * The Algorithm performs better at x being from 2 ~ 4 and performs worst above 7
 * Here are some examples:
 * Here x represents for 2^x childern for every node
 * Choose x from 1 - 10: 5
 * Average execution time over 100 iterations for 50000 elements: 165.91 milliseconds
 * Single run execution time: 164 milliseconds
 * Choose x from 1 - 10: 2
 * Average execution time over 100 iterations for 50000 elements: 18.65 milliseconds
 * Single run execution time: 18 milliseconds
 * Choose x from 1 - 10: 1
 * Average execution time over 100 iterations for 50000 elements: 122.72 milliseconds
 * Single run execution time: 128 milliseconds
 * Choose x from 1 - 10: 4
 * Average execution time over 100 iterations for 50000 elements: 117.55 milliseconds
 * Single run execution time: 128 milliseconds
 * Choose x from 1 - 10: 9
 * Average execution time over 20 iterations for 50000 elements: 1413.35 milliseconds
 * Single run execution time: 1411 milliseconds
 * Choose x from 1 - 10: 10
 * Average execution time over 20 iterations for 50000 elements: 3208.35 milliseconds
 * Single run execution time: 5344 milliseconds
 */
