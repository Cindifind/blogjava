package com.example.demo.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int n = Integer.parseInt(st.nextToken());
        int C = Integer.parseInt(st.nextToken());
        int len = (int) (n / 0.75) + 1;
        Map<Integer, Integer> map = new HashMap<>(len);

        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            int num = Integer.parseInt(st.nextToken());
            map.put(num, map.getOrDefault(num, 0) + 1);
        }

        long result = 0;
        if (C == 0) {
            for (int count : map.values()) {
                result += (long) count * (count - 1);
            }
        } else {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                Integer bCount = map.get(entry.getKey() - C);
                if (bCount != null) {
                    result += (long) entry.getValue() * bCount;
                }
            }
        }

        System.out.println(result);
    }
}
