package com.callbackcats.reboarding.util;

import org.opencv.core.Point;

import java.util.Comparator;

public class PointComparator implements Comparator<Point> {
    @Override
    public int compare(Point firstPoint, Point secondPoint) {
        int result = (int) (firstPoint.x - secondPoint.x);
        if (result != 0) {
            return result;
        } else {
            return (int) (firstPoint.y - secondPoint.y);
        }
    }
}
