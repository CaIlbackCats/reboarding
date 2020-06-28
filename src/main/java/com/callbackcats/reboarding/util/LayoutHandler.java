package com.callbackcats.reboarding.util;

import com.callbackcats.reboarding.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LayoutHandler {

    @Value("${office.layout-path}")
    private String officeLayoutPath;

    @Value("${office.chair-path}")
    private String officeChairTemplatePath;

    @Value("${office.picture-extension}")
    private String pictureExtension;

    private static final Scalar RED_SCALAR = new Scalar(0, 0, 255);
    private static final Scalar YELLOW_SCALAR = new Scalar(0, 255, 255);
    private static final Scalar GREEN_SCALAR = new Scalar(0, 255, 0);
    private static final Scalar BLACK_SCALAR = new Scalar(0, 0, 0);

    private static final Integer CIRCLE_RADIUS = 5;
    private static final Integer FILLED_THICKNESS = -1;
    private static final Integer UNFILLED_THICKNESS = 1;
    private static final Integer MAX_PIXEL_TOLERANCE = 3;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * <p>Finds the positions of all the workstations on the predefined layout image
     * </p>
     *
     * @return the list of the points of workstations
     */
    public List<Point> getWorkstationPosition() {
        // List<Point> workstationPositions = new ArrayList<>();
        List<Point> workstationPositions = new CopyOnWriteArrayList<>();
        // List<Point> synchronizedList = Collections.synchronizedList(workstationPositions);
        Mat officeLayout = Imgcodecs.imread(officeLayoutPath);
        Mat clonedOfficeLayout = officeLayout.clone();
        blurLayout(officeLayout, clonedOfficeLayout);
        try {
            Path officeChairTemplate = Paths.get(officeChairTemplatePath);
            List<Path> workstationTemplateList = Files.find(officeChairTemplate, Integer.MAX_VALUE, (path, attribute) -> attribute.isRegularFile()).collect(Collectors.toList());

            workstationTemplateList
                    .stream()
                    .map(Path::toString)
                    .parallel()
                    .forEach(pathString -> findWorkstationPointsByTemplate(pathString, clonedOfficeLayout, workstationPositions));

        } catch (IOException e) {
            log.warn(e.getMessage());
        }
        return workstationPositions;
    }

    /**
     * <p>Creates an office layout image in byte array based on the current status of the workstations
     * </p>
     *
     * @param employeeReservations the list of reservations for the current day
     * @param dailyLayout          the list of possible workstations for the current day
     * @return byte array representing the current status of the office
     * <br>
     * for better visibility the office is colored in gray
     * <br>
     * red - workstation is currently occupied
     * <br>
     * yellow - workstation has a reservation
     * <br>
     * green - workstation is free to reserve
     */
    public byte[] createCurrentLayout(List<EmployeeReservation> employeeReservations, List<OfficeWorkstation> dailyLayout) {
        Mat sourceImage = Imgcodecs.imread(officeLayoutPath);
        Mat currentLayout = sourceImage.clone();

        colorLayoutToGray(currentLayout);

        Map<WorkStation, Boolean> takenWorkstations = getTakenWorkstations(employeeReservations);
        takenWorkstations.forEach((key, value) -> drawCircle(key, value, currentLayout));

        List<WorkStation> notReservedWorkstations = dailyLayout
                .stream()
                .map(OfficeWorkstation::getWorkstation)
                .filter(workStation -> !takenWorkstations.containsKey(workStation))
                .collect(Collectors.toList());
        notReservedWorkstations.forEach(workStation -> drawCircle(workStation, currentLayout));

        return convertMatToByteArray(currentLayout);
    }

    /**
     * <p>Creates an office layout image in byte array based on requesting employee's status
     * </p>
     *
     * @param workstation the employee's reserved workstation to show on the image
     * @param inOffice    the employee's current status
     * @return byte array representing the employee's reserved workstation
     * red - employee is in the office
     * yellow - employee is not in the office
     */
    public byte[] createPersonalLayout(WorkStation workstation, Boolean inOffice) {
        Mat sourceImage = Imgcodecs.imread(officeLayoutPath);
        Mat currentLayout = sourceImage.clone();

        colorLayoutToGray(currentLayout);

        drawCircle(workstation, inOffice, currentLayout);

        return convertMatToByteArray(currentLayout);
    }

    private byte[] convertMatToByteArray(Mat layout) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(pictureExtension, layout, buffer);

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
            Imgproc.circle(sourceImage, currentPoint, CIRCLE_RADIUS, RED_SCALAR, FILLED_THICKNESS);
        } else {
            Imgproc.circle(sourceImage, currentPoint, CIRCLE_RADIUS, YELLOW_SCALAR, FILLED_THICKNESS);
        }
        Imgproc.circle(sourceImage, currentPoint, CIRCLE_RADIUS, BLACK_SCALAR, UNFILLED_THICKNESS);
    }

    private void drawCircle(WorkStation workStation, Mat sourceImage) {
        Point currentPoint = new Point(workStation.getXPosition(), workStation.getYPosition());
        Imgproc.circle(sourceImage, currentPoint, CIRCLE_RADIUS, GREEN_SCALAR, FILLED_THICKNESS);
        Imgproc.circle(sourceImage, currentPoint, CIRCLE_RADIUS, BLACK_SCALAR, UNFILLED_THICKNESS);

    }


    private void findWorkstationPointsByTemplate(String workStationTemplatePath, Mat officeLayout, List<Point> workstationPositions) {
        Mat workstationTemplate = Imgcodecs.imread(workStationTemplatePath);
        Mat result = new Mat();
        Imgproc.matchTemplate(officeLayout, workstationTemplate, result, Imgproc.TM_CCOEFF_NORMED);
        Imgproc.threshold(result, result, 0.1, 1, Imgproc.THRESH_TOZERO);
        double threshold = 0.57;
        double maxval;
        boolean withinThreshold = true;
        while (withinThreshold) {
            Core.MinMaxLocResult maxr = Core.minMaxLoc(result);
            Point maxp = maxr.maxLoc;
            maxval = maxr.maxVal;
            if (maxval >= threshold) {
                if (!isAlreadyInWorkstationPositionList(maxp, workstationPositions)) {
                    workstationPositions.add(maxp);
                }
                Imgproc.rectangle(result, maxp, new Point(maxp.x + workstationTemplate.cols(),
                        maxp.y + workstationTemplate.rows()), RED_SCALAR, FILLED_THICKNESS);
            } else {
                withinThreshold = false;
            }
        }
    }

    private boolean isAlreadyInWorkstationPositionList(Point maxp, List<Point> workstationPositions) {
        Optional<Point> maybePoint = workstationPositions
                .stream()
                .filter(point -> isPointWithinTolerance(point, maxp)).findFirst();

        return maybePoint.isPresent();
    }

    private boolean isPointWithinTolerance(Point currentPoint, Point maxp) {
        return Math.abs(maxp.x - currentPoint.x) < MAX_PIXEL_TOLERANCE && Math.abs(maxp.y - currentPoint.y) < MAX_PIXEL_TOLERANCE;
    }

    private void blurLayout(Mat officeLayout, Mat clonedOfficeLayout) {
        Imgproc.GaussianBlur(officeLayout, clonedOfficeLayout, new Size(0, 0), 10);
        Core.addWeighted(officeLayout, 1.5, clonedOfficeLayout, -0.5, 0, clonedOfficeLayout);
    }

    private void colorLayoutToGray(Mat currentLayout) {
        Imgproc.cvtColor(currentLayout, currentLayout, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(currentLayout, currentLayout, Imgproc.COLOR_GRAY2RGB);
    }
}
