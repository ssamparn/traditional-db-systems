package com.traditional.databases.jdbconetwomanybidirectionalrelation.entity.relations;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BiDirectionalAssociationInMemoryTest {

    @Test
    void addingUserFromInverseSide_shouldSyncOwningSideBeforePersistence() {
        Role role = createRole("Architect", "Designs systems");
        User user = createUser("Ira", "Cole", "9090909090", "ira@example.com");

        role.addUser(user);

        assertThat(role.getUsers()).contains(user);
        assertThat(user.getRole()).isSameAs(role);
    }

    @Test
    void reassigningOwningSide_shouldDetachPreviousRoleAndAttachNewRoleBeforePersistence() {
        Role oldRole = createRole("Backend Engineer", "Maintains services");
        Role newRole = createRole("Staff Engineer", "Leads architecture");
        User user = createUser("Eli", "Ford", "9191919191", "eli@example.com");

        oldRole.addUser(user);
        user.setRole(newRole);

        assertThat(oldRole.getUsers()).doesNotContain(user);
        assertThat(newRole.getUsers()).contains(user);
        assertThat(user.getRole()).isSameAs(newRole);
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

