package com.traditional.databases.jdbconetwomanybidirectionalrelation.entity.orphanhandling;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.service.RoleService;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RoleOrphanHandlingIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void updatingRole_shouldDeleteUsersRemovedFromCollectionAsOrphans() {
        Role role = new Role();
        role.setName("Observability");
        role.setDescription("Owns observability stack");

        User firstUser = new User();
        firstUser.setFirstName("Liam");
        firstUser.setLastName("Gray");
        firstUser.setMobile("8881000001");
        firstUser.setEmail("liam@example.com");

        User secondUser = new User();
        secondUser.setFirstName("Nia");
        secondUser.setLastName("Ray");
        secondUser.setMobile("8881000002");
        secondUser.setEmail("nia@example.com");

        role.addUser(firstUser);
        role.addUser(secondUser);

        Role savedRole = roleRepository.saveAndFlush(role);
        Long removedUserId = savedRole.getUsers().getFirst().getId();

        RoleRequest updateRequest = new RoleRequest(
                "Observability",
                "Owns observability stack updated",
                List.of(new UserRequest("Omar", "Lee", "8881000003", "omar@example.com"))
        );

        roleService.updateRole(savedRole.getId(), updateRequest).block();

        Role updatedRole = roleRepository.findByIdWithUsers(savedRole.getId()).orElseThrow();

        assertThat(updatedRole.getUsers()).hasSize(1);
        assertThat(updatedRole.getUsers().getFirst().getEmail()).isEqualTo("omar@example.com");
        assertThat(userRepository.existsById(removedUserId)).isFalse();
    }

    @Test
    void deletingRole_shouldCascadeDeleteUsers() {
        Role role = new Role();
        role.setName("Compliance");
        role.setDescription("Handles compliance checks");

        User user = new User();
        user.setFirstName("Sara");
        user.setLastName("Jain");
        user.setMobile("8881000004");
        user.setEmail("sara@example.com");

        role.addUser(user);

        Role savedRole = roleRepository.saveAndFlush(role);
        Long userId = savedRole.getUsers().getFirst().getId();

        roleService.deleteRole(savedRole.getId()).block();

        assertThat(roleRepository.existsById(savedRole.getId())).isFalse();
        assertThat(userRepository.existsById(userId)).isFalse();
    }
}

