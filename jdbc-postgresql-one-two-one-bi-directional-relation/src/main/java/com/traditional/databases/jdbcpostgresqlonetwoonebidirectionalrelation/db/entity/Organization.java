package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String orgId;

    @OneToOne(targetEntity = Address.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id_fk", referencedColumnName = "id") // Join Column mapping
//    @JoinTable(name = "organization_address", // Join Table mapping
//        joinColumns = { @JoinColumn(name = "organization_id") },
//        inverseJoinColumns = { @JoinColumn(name = "address_id") }
//    )
    private Address address;

    // Note: With @JoinColumn a new column will be created and in @JoinTable a new table will be created
}
