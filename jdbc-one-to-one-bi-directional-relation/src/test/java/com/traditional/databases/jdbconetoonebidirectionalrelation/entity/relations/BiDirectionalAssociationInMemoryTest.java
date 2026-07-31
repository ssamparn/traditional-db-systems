package com.traditional.databases.jdbconetoonebidirectionalrelation.entity.relations;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Organization;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BiDirectionalAssociationInMemoryTest {

    /**
     * This is an intentional in-memory unit test for bidirectional graph synchronization only.
     *
     * Why no repository usage here:
     * - The owner side (`Organization.address`) is non-nullable in persistence.
     * - During inverse-side reassignment, the old owner becomes temporarily detached (`address == null`).
     * - Flushing that temporary state would fail DB constraints by design.
     *
     * Persistence behavior is covered by integration tests that keep owner invariants valid at flush time
     * (see: reassigningInverseSide_shouldUseReplacementAddressBeforeFlushAndPersistBothOwnersAfterPersist).
     */
    @Test
    void reassigningInverseSide_shouldDetachPreviousOwnerAndLinkNewOwnerBeforePersistence() {
        Organization firstOrganization = createOrganization("ORG-INV-REASSIGN-1", "First Org");
        Organization secondOrganization = createOrganization("ORG-INV-REASSIGN-2", "Second Org");
        Address address = createAddress("Tower E", "El Paso");

        address.setOrganization(firstOrganization);
        assertThat(firstOrganization.getAddress()).isSameAs(address);
        assertThat(address.getOrganization()).isSameAs(firstOrganization);

        address.setOrganization(secondOrganization);

        assertThat(secondOrganization.getAddress()).isSameAs(address);
        assertThat(address.getOrganization()).isSameAs(secondOrganization);
        assertThat(firstOrganization.getAddress()).isNull();
    }

    private Organization createOrganization(String orgId, String name) {
        Organization organization = new Organization();
        organization.setOrgId(orgId);
        organization.setName(name);
        return organization;
    }

    private Address createAddress(String building, String city) {
        Address address = new Address();
        address.setBuilding(building);
        address.setStreet("Main Street");
        address.setCity(city);
        address.setState("Texas");
        address.setCountry("USA");
        address.setZipcode("75001");
        return address;
    }
}