package test_flower;

import java.io.*;
import java.util.*;

public class MainClass {

    public static final char SEPARATOR = '@';
    /*
     * Complete the function below.
     *
 	 * Note: The questions in this test build upon each other. We recommend you
	 * copy your solutions to your text editor of choice before proceeding to
	 * the next question as you will not be able to revisit previous questions.
	 */

    public int solution(int A[]){
        int len = A.length;
        if(len == 0 || len == 1){
            return 1;
        }
        int start = 0;
        int end = len - 1;

        while(end >= 1 && A[end] >= A[end - 1]){
            end -- ;
        }
        if(end == 0){
            return 0;
        }
        while(start < len - 1  && A[start] <= A[start + 1]){
            start ++ ;
        }

        int max = A[start];
        int min = A[start];

        for(int i = start + 1; i <= end ; i++){
            if(max < A[i]){
                max = A[i];
            }
            if(min > A[i]){
                min = A[i];
            }
        }
        for(int i = 0 ; i < start; i++){
            if(A[i] > min){
                start = i;
                break;
            }
        }
        for(int i = len - 1 ; i >= end + 1; i--){
            if(A[i] < max){
                end = i;
                break;
            }
        }
        return end - start + 1;
    }

    public static void main(String[] args) throws IOException{
        Scanner in = new Scanner(System.in);
        int res;
        String _input;
        try {
            _input = in.nextLine();
        } catch (Exception e) {
            _input = null;
        }
//        res = countHoldings(_input);
//        System.out.println(res);
    }
}
