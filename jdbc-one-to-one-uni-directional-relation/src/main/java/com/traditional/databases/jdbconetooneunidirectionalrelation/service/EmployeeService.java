package com.traditional.databases.jdbconetooneunidirectionalrelation.service;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Employee;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.EmployeeRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.mapper.EmployeeMapper;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.EmployeeRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Mono<EmployeeResponse> createEmployee(EmployeeRequest request) {
        return Mono.just(request)
                .doOnNext(EmployeeRequestValidator::validate)
                .map(employeeMapper::toEntity)
                .map(employeeRepository::save)
                .map(employeeMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<EmployeeResponse> getAllEmployees() {
        return Flux.fromIterable(employeeRepository.findAll())
                .map(employeeMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<EmployeeResponse> getEmployeeById(Long employeeId) {
        return findById(employeeId)
                .map(employeeMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<EmployeeResponse> updateEmployee(Long employeeId, EmployeeRequest request) {
        return findById(employeeId)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(ignored -> EmployeeRequestValidator.validate(request))
                .map(employee -> employeeMapper.updateEntity(employeeId, employee, request))
                .map(employeeRepository::save)
                .map(employeeMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<EmployeeResponse> deleteEmployeeById(Long employeeId) {
        return findById(employeeId)
                .publishOn(Schedulers.boundedElastic())
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return employee;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(employeeMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Employee> findById(Long employeeId) {
        return Mono.fromSupplier(() -> employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Employee not found with Id: " + employeeId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

