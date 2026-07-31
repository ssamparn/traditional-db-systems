package com.traditional.databases.jdbconetwomanybidirectionalrelation.entity.relations;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BiDirectionalAssociationIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void addingUsersFromInverseSide_shouldSynchronizeOwningSideBeforeFlushAndPersistAssociationsAfterPersist() {
        Role role = createRole("Platform", "Platform owners");
        User user = createUser("Ana", "Moore", "7000000001", "ana@example.com");

        role.addUser(user);

        Role savedRole = roleRepository.saveAndFlush(role);
        User savedUser = savedRole.getUsers().getFirst();

        assertThat(savedUser.getRole()).isNotNull();
        assertThat(savedUser.getRole().getId()).isEqualTo(savedRole.getId());
    }

    @Test
    void reassigningOwningSide_shouldDetachPreviousOwnerBeforeFlushAndPersistNewOwnerAfterPersist() {
        Role oldRole = roleRepository.saveAndFlush(createRole("Operations", "Ops owners"));
        Role newRole = roleRepository.saveAndFlush(createRole("Security", "Security owners"));

        User user = createUser("Ben", "Scott", "7000000002", "ben@example.com");
        user.setRole(oldRole);
        User savedUser = userRepository.saveAndFlush(user);

        savedUser.setRole(newRole);
        userRepository.saveAndFlush(savedUser);

        User reloaded = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(reloaded.getRole().getId()).isEqualTo(newRole.getId());
        assertThat(oldRole.getUsers()).doesNotContain(savedUser);
    }

    @Test
    void removingFromInverseSide_shouldClearAssociationBeforeFlushAndDeleteOrphanAfterPersist() {
        Role role = createRole("Data", "Data owners");
        User user = createUser("Cara", "Nash", "7000000003", "cara@example.com");

        role.addUser(user);
        Role savedRole = roleRepository.saveAndFlush(role);
        Long userId = savedRole.getUsers().getFirst().getId();

        savedRole.removeUser(savedRole.getUsers().getFirst());
        roleRepository.saveAndFlush(savedRole);

        assertThat(userRepository.existsById(userId)).isFalse();
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return role;
    }

    private User createUser(String firstName, String lastName, String mobile, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMobile(mobile);
        user.setEmail(email);
        return user;
    }
}

