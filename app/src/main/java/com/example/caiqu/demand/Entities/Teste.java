package com.example.caiqu.demand.Entities;

/**
 * Created by caiqu on 26/06/2017.
 */

public class Teste {

    public int find(int[] v, int x){
        int n = v.length;
        int i;
        for (i = 0; i < n/2; i ++) {
            if (v[i] == x) return i;
            if (v[n-i] == x) return n-i;
        }
        return -1;
    }

    public int somaMaxCaminho(int[][] grid) {
        int m = 4;
        int n = 4;
        int sum = 0;
        int aux = 0;
        int before = 0;
        int last = 0;
        int i = m - 1;
        int j = n - 1;
        int a = 3;
        int b = 3;

        for(  i = m-1; i == m-2; i--){
            for(j = n-1; j == n-2; j--){
                sum = before + grid[i][j];
                before = sum;
                last = grid[i][j];
            }
            if (aux + before < sum) aux = sum;
            m--;
            n--;
        }

        return sum;
    }

}
