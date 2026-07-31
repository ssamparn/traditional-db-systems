package com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "workstations")
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String deskCode;

    @Column(nullable = false, length = 64)
    private String building;

    @Column(nullable = false)
    private Integer floorNumber;

    @Column(nullable = false, length = 32)
    private String zone;
}
