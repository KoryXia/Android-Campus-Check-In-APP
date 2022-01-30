package com.example.check_in.utils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {
    private MutableLiveData<ChoosedPoint> currentChoosedPoint;

    public MutableLiveData<ChoosedPoint> getCurrentChoosedPoint() {
        if (currentChoosedPoint == null) {
            currentChoosedPoint = new MutableLiveData<>();
        }
        return currentChoosedPoint;
    }

}
