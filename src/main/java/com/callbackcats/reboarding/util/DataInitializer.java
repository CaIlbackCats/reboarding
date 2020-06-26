package com.callbackcats.reboarding.util;

import com.callbackcats.reboarding.domain.Employee;
import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.repository.EmployeeRepository;
import com.callbackcats.reboarding.repository.WorkStationRepository;
import com.callbackcats.reboarding.service.OfficeWorkstationService;
import employee.EmployeeImporter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DataInitializer {

    private final DataSource dataSource;
    private final EmployeeRepository employeeRepository;
    private final WorkStationRepository workStationRepository;
    private final LayoutHandler layoutHandler;
    private final OfficeWorkstationService officeWorkstationService;

    @Value("${data-initialize.path.office-options}")
    private String officeOptionsPath;

    @Value("${data-initialize.path.reservations}")
    private String reservationPath;

    @Value("${data-initialize.closed-positions.min-position.x}")
    private Integer minClosedPositionX;

    @Value("${data-initialize.closed-positions.max-position.x}")
    private Integer maxClosedPositionX;

    @Value("${data-initialize.closed-positions.min-position.y}")
    private Integer minClosedPositionY;

    private EmployeeImporter employeeImporter = new EmployeeImporter();

    public DataInitializer(EmployeeRepository employeeRepository, DataSource dataSource, WorkStationRepository workStationRepository, LayoutHandler layoutHandler, OfficeWorkstationService officeWorkstationService) {
        this.employeeRepository = employeeRepository;
        this.dataSource = dataSource;
        this.workStationRepository = workStationRepository;
        this.layoutHandler = layoutHandler;
        this.officeWorkstationService = officeWorkstationService;
    }


    //In order to initialize some employees and workstations to the db upon application start
    @EventListener
    public void init(ApplicationStartedEvent event) throws SQLException {
        initEmployees();
        List<Point> workstationPoints = initWorkstations();


        initSqlData(officeOptionsPath);
        log.info("Office options data initialized");

        generateCurrentLayout(workstationPoints);

        initSqlData(reservationPath);
        log.info("Reservation data initialized");

        log.info("Data initialized");
    }

    private void initSqlData(String sqlPath) {
        try {
            EncodedResource encodedResource = new EncodedResource(new ClassPathResource(sqlPath));
            ScriptUtils.executeSqlScript(dataSource.getConnection(), encodedResource);
        } catch (SQLException e) {
            log.warn(e.getMessage());
        }
    }

    private void initEmployees() {
        List<Employee> employees = employeeImporter.importEmployees();
        employeeRepository.saveAll(employees);
    }

    private List<Point> initWorkstations() {
        List<Point> workstationPositions = layoutHandler.getWorkstationPosition();
        List<WorkStation> workStations = workstationPositions
                .stream()
                .map(WorkStation::new)
                .collect(Collectors.toList());
        workStationRepository.saveAll(workStations);
        log.info("Workstations initialized");
        log.info("number of workstations:\t" + workstationPositions.size());

        return workstationPositions;
    }

    private void generateCurrentLayout(List<Point> workstationPoints) {
        LocalDate today = LocalDate.now();
        List<Point> closedWorkstations = workstationPoints
                .stream()
                .filter(point -> point.x >= minClosedPositionX
                        && point.x <= maxClosedPositionX
                        && point.y >= minClosedPositionY)
                .collect(Collectors.toList());

        officeWorkstationService.saveOfficeWorkstation(closedWorkstations, today);
        log.info("Initialized base layout");
    }
}