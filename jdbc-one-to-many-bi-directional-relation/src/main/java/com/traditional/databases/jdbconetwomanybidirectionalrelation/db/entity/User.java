package com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "users")
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "mobile", nullable = false, length = 20)
    private String mobile;

    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id_fk", nullable = false)
    private Role role;

    public void setRole(Role role) {
        if (this.role == role) {
            return;
        }

        Role previousRole = this.role;
        this.role = role;

        detachFromLoadedRole(previousRole);
        attachToLoadedRole(role);
    }

    private void detachFromLoadedRole(Role candidateRole) {
        if (isUsersCollectionNotLoaded(candidateRole)) {
            return;
        }
        candidateRole.getUsers().remove(this);
    }

    private void attachToLoadedRole(Role candidateRole) {
        if (isUsersCollectionNotLoaded(candidateRole)) {
            return;
        }
        if (!candidateRole.getUsers().contains(this)) {
            candidateRole.getUsers().add(this);
        }
    }

    private boolean isUsersCollectionNotLoaded(Role candidateRole) {
        if (candidateRole == null) {
            return true;
        }
        return !Persistence.getPersistenceUtil().isLoaded(candidateRole)
                || !Persistence.getPersistenceUtil().isLoaded(candidateRole, "users");
    }
}
