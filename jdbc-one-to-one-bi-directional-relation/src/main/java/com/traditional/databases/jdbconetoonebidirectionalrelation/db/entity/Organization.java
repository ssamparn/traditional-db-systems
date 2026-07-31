package com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String orgId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "address_id_fk", referencedColumnName = "id", nullable = false, unique = true)
    private Address address;

    public void setAddress(Address address) {
        if (this.address == address) {
            return;
        }

        Address previousAddress = this.address;
        this.address = address;

        if (previousAddress != null && previousAddress.getOrganization() == this) {
            previousAddress.setOrganization(null);
        }

        if (address != null && address.getOrganization() != this) {
            address.setOrganization(this);
        }
    }
}
