package com.traditional.databases.jdbconetooneunidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetooneunidirectionalrelation.service.EmployeeService;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.EmployeeRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employee/create")
    public Mono<ResponseEntity<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        return employeeService.createEmployee(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/employee/get/all")
    public Flux<EmployeeResponse> getEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/employee/get/{employeeId}")
    public Mono<ResponseEntity<EmployeeResponse>> getEmployee(@PathVariable Long employeeId) {
        return employeeService.getEmployeeById(employeeId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @PutMapping("/employee/update/{employeeId}")
    public Mono<ResponseEntity<EmployeeResponse>> updateEmployee(@PathVariable Long employeeId,
                                                                 @RequestBody EmployeeRequest request) {
        return employeeService.updateEmployee(employeeId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/employee/delete/{employeeId}")
    public Mono<ResponseEntity<EmployeeResponse>> deleteEmployee(@PathVariable Long employeeId) {
        return employeeService.deleteEmployeeById(employeeId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

