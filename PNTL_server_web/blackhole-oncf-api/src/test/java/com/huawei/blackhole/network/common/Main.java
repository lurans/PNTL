package com.huawei.blackhole.network.common;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>();
        nums.add(1);
        nums.add(2);
        nums.add(3);

        Map<String, List> data = new HashMap<>();

        data.put("k", nums);
        Collections.reverse(data.get("k"));

        List<Integer> newNums = data.get("k");

        for (int num : newNums) {
            System.out.println(num);
        }

    }
}
