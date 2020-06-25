package com.callbackcats.reboarding.util;

import com.callbackcats.reboarding.domain.EmployeeReservation;
import com.callbackcats.reboarding.domain.OfficeWorkstation;
import com.callbackcats.reboarding.domain.ReservationType;
import com.callbackcats.reboarding.domain.WorkStation;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LayoutHandler {
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
        Mat sourceImage = Imgcodecs.imread("office_layout.jpg");

        for (int i = 0; i < workStations.size(); i++) {
            Double xPosition = workStations.get(i).getXPosition();
            Double yPosition = workStations.get(i).getYPosition();
            Point currentPoint = new Point(xPosition, yPosition);
            Imgproc.circle(sourceImage, currentPoint, 50, new Scalar(0, 0, 255), 1);
            Imgproc.circle(sourceImage, currentPoint, 3, new Scalar(0, 255, 0), 3);
            Imgproc.putText(sourceImage, String.valueOf(i), currentPoint, 2, 1, new Scalar(0, 0, 0), 2);
        }
        Imgcodecs.imwrite("daily_layout.jpg", sourceImage);
    }

    public byte[] createCurrentLayout(List<EmployeeReservation> employeeReservations, List<OfficeWorkstation> dailyLayout) {
        Mat sourceImage = Imgcodecs.imread("office_layout.jpg");
        Mat currentLayout = sourceImage.clone();
        Map<WorkStation, Boolean> takenWorkstations = getTakenWorkstations(employeeReservations);
        takenWorkstations.forEach((key, value) -> drawCircle(key, value, currentLayout));

        List<WorkStation> notReservedWorkstations = dailyLayout
                .stream()
                .map(OfficeWorkstation::getWorkstation)
                .filter(workStation -> !takenWorkstations.containsKey(workStation))
                .collect(Collectors.toList());
        notReservedWorkstations.forEach(workStation -> drawCircle(workStation, currentLayout));
        //    Imgcodecs.imwrite("daily_layout.jpg", currentLayout);

        return convertMatToByteArray(currentLayout);
    }

    public byte[] createPersonalLayout(WorkStation workStation) {
        Mat sourceImage = Imgcodecs.imread("office_layout.jpg");
        Mat currentLayout = sourceImage.clone();

        Point currentPoint = new Point(workStation.getXPosition(), workStation.getYPosition());
        Imgproc.circle(currentLayout, currentPoint, 3, new Scalar(0, 255, 255), -1);


        return convertMatToByteArray(currentLayout);
    }

    private byte[] convertMatToByteArray(Mat layout) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", layout, buffer);

        return buffer.toArray();
    }

    private Map<WorkStation, Boolean> getTakenWorkstations(List<EmployeeReservation> employeeReservations) {
        Map<WorkStation, Boolean> workstationTaken = new HashMap<>();

        for (EmployeeReservation employeeReservation : employeeReservations) {
            if (employeeReservation.getReserved().getReservationType().equals(ReservationType.RESERVED)) {
                WorkStation workStation = employeeReservation.getWorkStation();
                Boolean inOffice = employeeReservation.getEmployee().getInOffice();
                workstationTaken.put(workStation, inOffice);
            }
        }
        return workstationTaken;
    }

    private void drawCircle(WorkStation workStation, Boolean inOffice, Mat sourceImage) {
        Point currentPoint = new Point(workStation.getXPosition(), workStation.getYPosition());
        if (inOffice) {
            Imgproc.circle(sourceImage, currentPoint, 3, new Scalar(0, 0, 255), -1);
        } else {
            Imgproc.circle(sourceImage, currentPoint, 3, new Scalar(0, 255, 255), -1);
        }
    }

    private void drawCircle(WorkStation workStation, Mat sourceImage) {
        Point currentPoint = new Point(workStation.getXPosition(), workStation.getYPosition());
        Imgproc.circle(sourceImage, currentPoint, 3, new Scalar(0, 255, 0), -1);
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
