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

import static org.assertj.core.api.Assertions.assertThat;

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

        Role reloadedSourceRole = roleRepository.findByIdWithUsers(sourceRole.getId()).orElseThrow();
        Role reloadedTargetRole = roleRepository.findByIdWithUsers(targetRole.getId()).orElseThrow();

        assertThat(reloadedSourceRole.getUsers()).extracting(User::getId).doesNotContain(user.getId());
        assertThat(reloadedTargetRole.getUsers()).extracting(User::getId).contains(user.getId());
    }

    @Test
    void onboardingMultipleUsersUnderSameRole_shouldPersistTeamAndExposeStableAssociationState() {
        Role role = createRole("Platform Engineer", "Builds and runs platform services");

        webTestClient.post()
                .uri("/api/v1/user/create/role/{roleId}", role.getId())
                .bodyValue(new UserRequest("Ava", "Miller", "9810010001", "ava.platform@example.com"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/user/create/role/{roleId}", role.getId())
                .bodyValue(new UserRequest("Noah", "Brown", "9810010002", "noah.platform@example.com"))
                .exchange()
                .expectStatus().isCreated();

        Role reloadedRole = roleRepository.findByIdWithUsers(role.getId()).orElseThrow();

        assertThat(reloadedRole.getUsers()).hasSize(2);
        assertThat(reloadedRole.getUsers())
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("ava.platform@example.com", "noah.platform@example.com");
    }

    @Test
    void deletingUser_shouldRemoveUserWithoutDeletingOwningRole() {
        Role role = createRole("Operations Engineer", "Runs production operations");

        User user = new User();
        user.setFirstName("Mina");
        user.setLastName("Patel");
        user.setMobile("9810010003");
        user.setEmail("mina.ops@example.com");
        user.setRole(role);
        user = userRepository.saveAndFlush(user);

        webTestClient.delete()
                .uri("/api/v1/user/delete/{userId}", user.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(user.getId());

        assertThat(userRepository.existsById(user.getId())).isFalse();
        assertThat(roleRepository.existsById(role.getId())).isTrue();
    }

    @Test
    void updatingUserProfile_shouldUpdateFieldsAndKeepRoleAssociation() {
        Role role = createRole("Analytics Engineer", "Owns analytics platform");

        User user = new User();
        user.setFirstName("Kira");
        user.setLastName("Gomez");
        user.setMobile("9810010004");
        user.setEmail("kira.analytics@example.com");
        user.setRole(role);
        user = userRepository.saveAndFlush(user);

        UserRequest updateRequest = new UserRequest(
                "Kira-Updated",
                "Gomez-Updated",
                "9810019999",
                "kira.updated@example.com"
        );

        webTestClient.put()
                .uri("/api/v1/user/update/{userId}", user.getId())
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(user.getId())
                .jsonPath("$.firstName").isEqualTo("Kira-Updated")
                .jsonPath("$.lastName").isEqualTo("Gomez-Updated")
                .jsonPath("$.mobile").isEqualTo("9810019999")
                .jsonPath("$.email").isEqualTo("kira.updated@example.com")
                .jsonPath("$.roleId").isEqualTo(role.getId());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        Role reloadedRole = roleRepository.findByIdWithUsers(role.getId()).orElseThrow();

        assertThat(updatedUser.getFirstName()).isEqualTo("Kira-Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("Gomez-Updated");
        assertThat(updatedUser.getMobile()).isEqualTo("9810019999");
        assertThat(updatedUser.getEmail()).isEqualTo("kira.updated@example.com");
        assertThat(reloadedRole.getUsers()).extracting(User::getId).contains(user.getId());
    }

    @Test
    void updatingUserProfile_withInvalidMobileOrEmail_shouldReturnBadRequest() {
        Role role = createRole("Quality Engineer", "Owns quality gates");

        User user = new User();
        user.setFirstName("Nora");
        user.setLastName("Blake");
        user.setMobile("9810010101");
        user.setEmail("nora.quality@example.com");
        user.setRole(role);
        user = userRepository.saveAndFlush(user);

        UserRequest invalidMobileRequest = new UserRequest(
                "Nora",
                "Blake",
                "123456789012345678901",
                "nora.mobile@example.com"
        );

        webTestClient.put()
                .uri("/api/v1/user/update/{userId}", user.getId())
                .bodyValue(invalidMobileRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("user.mobile must be at most 20 characters");

        UserRequest invalidEmailRequest = new UserRequest(
                "Nora",
                "Blake",
                "9810010102",
                "invalid-email"
        );

        webTestClient.put()
                .uri("/api/v1/user/update/{userId}", user.getId())
                .bodyValue(invalidEmailRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("user.email must be a valid email address");

        User unchangedUser = userRepository.findById(user.getId()).orElseThrow();

        assertThat(unchangedUser.getFirstName()).isEqualTo("Nora");
        assertThat(unchangedUser.getLastName()).isEqualTo("Blake");
        assertThat(unchangedUser.getMobile()).isEqualTo("9810010101");
        assertThat(unchangedUser.getEmail()).isEqualTo("nora.quality@example.com");
        assertThat(unchangedUser.getRole().getId()).isEqualTo(role.getId());
    }

    private Role createRole(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.saveAndFlush(role);
    }
}

