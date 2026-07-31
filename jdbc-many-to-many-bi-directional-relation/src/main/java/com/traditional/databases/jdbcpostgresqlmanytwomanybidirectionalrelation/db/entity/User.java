package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String mobile;

    @Column(unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id_fk"),
        inverseJoinColumns = @JoinColumn(name = "role_id_fk")
    )
    private List<Role> roles = new ArrayList<>();

    public void addRole(Role role) {
        if (role == null || roles.contains(role)) {
            return;
        }
        roles.add(role);
        if (!role.getUsers().contains(this)) {
            role.getUsers().add(this);
        }
    }

    public void removeRole(Role role) {
        if (role == null || !roles.contains(role)) {
            return;
        }
        roles.remove(role);
        role.getUsers().remove(this);
    }
}
