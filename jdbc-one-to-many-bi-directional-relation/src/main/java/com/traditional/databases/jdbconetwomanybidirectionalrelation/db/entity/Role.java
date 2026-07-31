package com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity(name = "roles")
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Setter
    @Column(nullable = false, length = 240)
    private String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    public void addUser(User user) {
        if (user == null) {
            return;
        }
        user.setRole(this);

        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public void removeUser(User user) {
        if (user == null) {
            return;
        }

        if (user.getRole() == this) {
            user.setRole(null);
        }

        users.remove(user);
    }
}
