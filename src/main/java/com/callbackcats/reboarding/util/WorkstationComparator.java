package com.callbackcats.reboarding.util;

import com.callbackcats.reboarding.domain.WorkStation;

import java.util.Comparator;

public class WorkstationComparator implements Comparator<WorkStation> {

    @Override
    public int compare(WorkStation firstWorkstation, WorkStation secondWorkstation) {
        int result = (int) (firstWorkstation.getXPosition() - secondWorkstation.getXPosition());
        if (result != 0) {
            return result;
        } else {
            return (int) (firstWorkstation.getYPosition() - secondWorkstation.getYPosition());
        }
    }
}
