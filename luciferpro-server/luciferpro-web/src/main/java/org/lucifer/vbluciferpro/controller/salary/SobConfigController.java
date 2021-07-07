package org.lucifer.vbluciferpro.controller.salary;


import org.lucifer.vbluciferpro.model.RespBean;
import org.lucifer.vbluciferpro.model.RespPageBean;
import org.lucifer.vbluciferpro.model.Salary;
import org.lucifer.vbluciferpro.service.EmployeeService;
import org.lucifer.vbluciferpro.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salary/sobcfg")
public class SobConfigController {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    SalaryService salaryService;

    @GetMapping("/")
    public RespPageBean getEmployeeByPageWithSalary(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return employeeService.getEmployeeByPageWithSalary(page, size);
    }

    @GetMapping("/salaries")
    public List<Salary> getAllSalaries() {
        return salaryService.getAllSalaries();
    }

    @PutMapping("/")
    public RespBean updateEmployeeSalaryById(Integer eid, Integer sid) {
        Integer result = employeeService.updateEmployeeSalaryById(eid, sid);
        // REPLACE INTO 关键词 更新为1，插入为2
        if (result == 1 || result == 2) {
            return RespBean.ok("更新成功");
        }
        return RespBean.error("更新失败");
    }

}
