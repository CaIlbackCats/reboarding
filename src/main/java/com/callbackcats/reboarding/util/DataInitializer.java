package com.callbackcats.reboarding.util;

import com.callbackcats.reboarding.domain.Employee;
import com.callbackcats.reboarding.domain.WorkStation;
import com.callbackcats.reboarding.repository.EmployeeRepository;
import com.callbackcats.reboarding.repository.WorkStationRepository;
import employee.EmployeeImporter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Point;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class DataInitializer {

    private final DataSource dataSource;
    private final EmployeeRepository employeeRepository;
    private final WorkStationRepository workStationRepository;

    private EmployeeImporter employeeImporter = new EmployeeImporter();

    public DataInitializer(EmployeeRepository employeeRepository, DataSource dataSource, WorkStationRepository workStationRepository) {
        this.employeeRepository = employeeRepository;
        this.dataSource = dataSource;
        this.workStationRepository = workStationRepository;
    }


    //In order to initialize some employees to the db upon application start
    @EventListener
    public void init(ApplicationStartedEvent event) throws SQLException {
        initEmployees();
        initWorkstations();

        String INIT_DATA = "init_data.sql";
        EncodedResource encodedResource = new EncodedResource(new ClassPathResource(INIT_DATA));

        ScriptUtils.executeSqlScript(dataSource.getConnection(), encodedResource);
        log.info("Data initialized");
    }

    private void initEmployees() {
        List<Employee> employees = employeeImporter.importEmployees();
        employeeRepository.saveAll(employees);
    }

    private void initWorkstations() {
        List<Point> workstationPositions = LayoutHandler.getWorkstationPosition();
        List<WorkStation> workStations = workstationPositions
                .stream()
                .map(WorkStation::new)
                .collect(Collectors.toList());
        workStationRepository.saveAll(workStations);
        log.info("Workstations initialized");
    }
}
