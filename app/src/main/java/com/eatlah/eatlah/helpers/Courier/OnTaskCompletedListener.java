package com.eatlah.eatlah.helpers.Courier;

import java.util.List;

public interface OnTaskCompletedListener {
    void onTaskCompleted(List<String> list);
    void onTaskCompleted(String address);
}
