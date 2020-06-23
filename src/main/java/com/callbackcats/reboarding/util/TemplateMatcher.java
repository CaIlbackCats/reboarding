package com.callbackcats.reboarding.util;

import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.dto.WorkstationData;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;


public class TemplateMatcher {
    private static Mat dst;
    private static List<Point> templates = new ArrayList<>();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static List<Point> getWorkstationPosition() {

        Mat img = Imgcodecs.imread("office_layout.jpg");
        dst = img.clone();
        Imgproc.GaussianBlur(img, dst, new Size(0, 0), 10);
        Core.addWeighted(img, 1.5, dst, -0.5, 0, dst);
        for (int i = 0; i < 12; i++) {
            String templatePath = "src/main/resources/img_templates/chair_" + i + ".jpg";
            signTemplate(templatePath, img);
        }
        Imgcodecs.imwrite("modified_office_layout.jpg", dst);//save image

        PointComparator pointComparator = new PointComparator();
        templates.sort(pointComparator);
        return templates;
    }

    public static void drawMap(List<WorkStation> workStations) {
        Mat source = Imgcodecs.imread("office_layout.jpg");

        for (int i = 0; i < workStations.size(); i++) {
            Double xPosition = workStations.get(i).getXPosition();
            Double yPosition = workStations.get(i).getYPosition();
            Point currentPoint = new Point(xPosition, yPosition);
            Imgproc.circle(source, currentPoint, 50, new Scalar(0, 0, 255), 1);
            Imgproc.circle(source, currentPoint, 3, new Scalar(0, 255, 0), 3);
            Imgproc.putText(source, String.valueOf(i), currentPoint, 2, 1, new Scalar(0, 0, 0), 2);
        }
        Imgcodecs.imwrite("daily_layout.jpg", source);
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
                if (!isAlreadyInTemplateSet(maxp)) {
                    Imgproc.rectangle(img, maxp, new Point(maxp.x + tpl.cols(),
                            maxp.y + tpl.rows()), new Scalar(0, 0, 255), 1);
                    templates.add(maxp);
                }
                Imgproc.rectangle(result, maxp, new Point(maxp.x + tpl.cols(),
                        maxp.y + tpl.rows()), new Scalar(0, 0, 255), -1);
            } else {
                break;
            }
        }
    }

    private static boolean isAlreadyInTemplateSet(Point maxp) {
        Optional<Point> maybePoint = templates.stream().filter(p -> Math.abs(maxp.x - p.x) < 3 && Math.abs(maxp.y - p.y) < 3).findFirst();
        return maybePoint.isPresent();
    }
}
