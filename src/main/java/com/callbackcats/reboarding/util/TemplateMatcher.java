package com.callbackcats.reboarding.util;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class TemplateMatcher {
    private static Mat dst;
    private static Set<Point> templates = new HashSet<>();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        Mat img = Imgcodecs.imread("office_layout.jpg");
        dst = img.clone();
        Imgproc.GaussianBlur(img, dst, new Size(0, 0), 10);
        Core.addWeighted(img, 1.5, dst, -0.5, 0, dst);
        for (int i = 0; i < 12; i++) {
            String templatePath = "src/main/resources/img_templates/chair_" + i + ".jpg";
            signTemplate(templatePath, img);
        }
        Imgcodecs.imwrite("modified_office_layout.jpg", dst);//save image
        System.out.println(templates.size() + " chairs found.");
        System.out.println(templates);
    }


    private static void signTemplate(String templatePath, Mat img) {
        Mat tpl = Imgcodecs.imread(templatePath);
        Mat result = new Mat();
        Imgproc.matchTemplate(img, tpl, result, Imgproc.TM_CCOEFF_NORMED);
        Imgproc.threshold(result, result, 0.1, 1, Imgproc.THRESH_TOZERO);
        double threshold = 0.57;
        double maxval;
        while (true) {
            Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
            Point maxp = maxr.maxLoc;
            maxval = maxr.maxVal;
            dst = img.clone();
            if (maxval >= threshold) {
                Imgproc.rectangle(img, maxp, new Point(maxp.x + tpl.cols(),
                        maxp.y + tpl.rows()), new Scalar(0, 0, 255), 5);
                Imgproc.rectangle(result, maxp, new Point(maxp.x + tpl.cols(),
                        maxp.y + tpl.rows()), new Scalar(0, 0, 255), -1);
                templates.add(maxp);
            } else {
                break;
            }
        }
    }

    //TODO
    private static boolean isAlreadyInTemplateSet(Point maxp) {
        Optional<Point> maybePoint = templates.stream().filter(p -> Math.abs(maxp.x - p.x) < 3 && Math.abs(maxp.y - p.y) < 3).findFirst();
        return maybePoint.isPresent();
    }
}
