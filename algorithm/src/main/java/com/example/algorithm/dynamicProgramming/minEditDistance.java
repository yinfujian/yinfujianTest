package com.example.algorithm.dynamicProgramming;

/**
 * 编辑距离指的是，将一个字符串变换为另一个字符串所需要的最小操作次数。插入一个字符，删除一个字符以及讲一个字符修改为另一个字符的操作次数
 * 字符串str1和str2分别标识长度分别为len1和len2的字符串，定义二维数组d记录
 */
public class minEditDistance {

    public static void main(String[] args) {
        System.out.println(new minEditDistance().minEditDistance("CTGA", "ACGCTA"));
    }


    public int minEditDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        char[] c1 = str1.toCharArray();
        char[] c2 = str2.toCharArray();
        int[][] d = new int[len1+1][len2+1];
        // 二维数组初始化
        int i, j;
        // 默认最大编辑距离，最大的字符是多少，就是多少
        for (i = 0; i <= len1; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= len2; j++) {
            d[0][j] = j;
        }

        // 遍历str1的字符数组
        for (i = 1; i <= len1; i++) {
            // 遍历str2的字符数组
            for (j = 1; j <= len2; j++) {
                // 前j中的每一个字符和前i字符的编辑距离
                if (c1[i - 1] == c2[j - 1]) {
                    // 当前的两个字符相等
                    // 前i-1个字符，和前j-1个字符的距离就是当前子串的最小编辑距离
                    d[i][j] = d[i - 1][j - 1];
                } else {
                    //当前两个字符不想等
                    int temp = Math.min(d[i - 1][j], d[i][j - 1]);
                    d[i][j] = Math.min(temp, d[i - 1][j - 1]) + 1;
                }
            }
        }
        return d[len1][len2];
    }
}
