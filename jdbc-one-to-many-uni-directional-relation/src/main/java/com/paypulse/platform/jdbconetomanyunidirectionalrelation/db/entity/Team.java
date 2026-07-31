package com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity(name = "teams")
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 64)
    private String teamCode;

    @Setter
    @Column(nullable = false, length = 120)
    private String name;

    @Setter
    @Column(nullable = false, length = 240)
    private String description;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "team_id_fk", nullable = false)
    private List<Member> members = new ArrayList<>();

    public void addMember(Member member) {
        if (member == null || members.contains(member)) {
            return;
        }
        members.add(member);
    }

    public void removeMember(Member member) {
        if (member == null) {
            return;
        }
        members.remove(member);
    }
}

