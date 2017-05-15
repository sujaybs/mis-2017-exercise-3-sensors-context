package com.example.sujaybshalawadi.mis3;

class HammingWindow {
    private int length;

    HammingWindow(int length) {
        this.length = length;
    }

    float getValue(int index) {
        return 0.54f - 0.46f * (float) Math.cos(2 * Math.PI * index / (length - 1));
    }
}