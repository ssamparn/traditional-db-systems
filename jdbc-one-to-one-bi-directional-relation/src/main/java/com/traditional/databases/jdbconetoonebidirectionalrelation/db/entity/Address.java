package com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String building;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;

    @OneToOne(mappedBy = "address")
    private Organization organization;

    public void setOrganization(Organization organization) {
        if (this.organization == organization) {
            return;
        }

        Organization previousOrganization = this.organization;
        this.organization = organization;

        if (previousOrganization != null && previousOrganization.getAddress() == this) {
            previousOrganization.setAddress(null);
        }

        if (organization != null && organization.getAddress() != this) {
            organization.setAddress(this);
        }
    }
}