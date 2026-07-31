package com.traditional.databases.jdbconetwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void createUser_underExistingRole_shouldReturnCreated() {
        Role role = createRole("API Developer", "Builds APIs");

        UserRequest request = new UserRequest("Maya", "Khan", "9988771122", "maya@example.com");

        webTestClient.post()
                .uri("/api/v1/user/create/role/{roleId}", role.getId())
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.email").isEqualTo("maya@example.com")
                .jsonPath("$.roleId").isEqualTo(role.getId());
    }

    @Test
    void reassignUserRole_shouldMoveUserToTargetRole() {
        Role sourceRole = createRole("Support Engineer", "Handles incidents");
        Role targetRole = createRole("Site Reliability Engineer", "Owns reliability");

        User user = new User();
        user.setFirstName("Ria");
        user.setLastName("Shah");
        user.setMobile("9000001111");
        user.setEmail("ria@example.com");
        user.setRole(sourceRole);
        user = userRepository.saveAndFlush(user);

        webTestClient.put()
                .uri("/api/v1/user/reassign/{userId}/role/{roleId}", user.getId(), targetRole.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(user.getId())
                .jsonPath("$.roleId").isEqualTo(targetRole.getId());
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.saveAndFlush(role);
    }
}

