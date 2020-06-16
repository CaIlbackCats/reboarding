package employee;

import com.callbackcats.reboarding.domain.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EmployeeImporter {


    private static final String PATH_TO_EMPLOYEE_LIST = "src\\main\\resources\\employees.txt";

    public List<Employee> importEmployees() {
        List<Employee> employees = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(PATH_TO_EMPLOYEE_LIST));
            for (int i = 0; i < lines.size(); i++) {
                String[] oneLine = lines.get(i).split(";");
                employees.add(new Employee(oneLine[0], Boolean.valueOf(oneLine[1])));
            }
            log.info("Employees imported");

        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return employees;
    }
}
