package ru.Darvin.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.Darvin.Service.DepartmentsService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentsController {

    private final DepartmentsService departmentsService;

    @Autowired
    public DepartmentsController(DepartmentsService departmentsService) {
        this.departmentsService = departmentsService;
    }

    @GetMapping("/names")
    public List<String> getDepartmentNames() {
        return departmentsService.getUniqueDepartmentNames();
    }
}
