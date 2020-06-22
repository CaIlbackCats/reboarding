package com.callbackcats.reboarding.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class TemplateMatcher {

    public static void main(String[] args) {

//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        Mat source = null;
//        Mat template = null;
//        //Load image file
//        source = Imgcodecs.imread("office_layout.jpg");
//        template = Imgcodecs.imread("chair.jpg");
//
//        Mat outputImage = new Mat();
//        int machMethod = Imgproc.TM_CCOEFF_NORMED;
//        //Template matching method
//        Imgproc.matchTemplate(source, template, outputImage, machMethod);
//
//
//        Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
//        Point matchLoc = mmr.maxLoc;
//        //Draw rectangle on result image
//        Imgproc.rectangle(source, matchLoc, new Point(matchLoc.x + template.cols(),
//                matchLoc.y + template.rows()), new Scalar(0, 0, 255));
//
//        Imgcodecs.imwrite("modified_office_layout.jpg", source);
//        System.out.println("Completed.");


        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread("office_layout.jpg");
        Mat tpl = Imgcodecs.imread("chair_02.jpg");
        Mat result = new Mat();
        Imgproc.matchTemplate(img, tpl, result, Imgproc.TM_CCOEFF_NORMED);
        Imgproc.threshold(result, result, 0.1, 1, Imgproc.THRESH_TOZERO);
        double threshold = 0.65;
        double maxval;
        Mat dst;
        while (true) {
            Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
            Point maxp = maxr.maxLoc;
            maxval = maxr.maxVal;
            Point maxop = new Point(maxp.x + tpl.width(), maxp.y + tpl.height());
            dst = img.clone();
            if (maxval >= threshold) {

                Imgproc.rectangle(img, maxp, new Point(maxp.x + tpl.cols(),
                        maxp.y + tpl.rows()), new Scalar(0, 0, 255), 5);
                Imgproc.rectangle(result, maxp, new Point(maxp.x + tpl.cols(),
                        maxp.y + tpl.rows()), new Scalar(0, 0, 255), -1);
            } else {
                break;
            }
        }
        Imgcodecs.imwrite("modified_office_layout.jpg", dst);//save image
    }
}
