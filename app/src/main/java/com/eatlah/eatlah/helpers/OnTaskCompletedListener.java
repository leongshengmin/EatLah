package com.eatlah.eatlah.helpers;

import java.util.List;

public interface OnTaskCompletedListener {
    void onTaskCompleted(List<String> list);
    void onTaskCompleted(String address);
}
