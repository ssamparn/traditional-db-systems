package com.traditional.databases.jdbconetoonebidirectionalrelation.entity.relations;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.OrganizationRepository;
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
    private OrganizationRepository organizationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @AfterEach
    void cleanup() {
        organizationRepository.deleteAll();
        addressRepository.deleteAll();
    }

    @Test
    void settingOwningSide_shouldSynchronizeInverseSideAfterPersist() {
        Organization organization = createOrganization("ORG-OWNER-1", "Owner Corp");
        Address address = createAddress("Tower A", "Austin");

        organization.setAddress(address);

        assertThat(address.getOrganization()).isSameAs(organization);

        Organization saved = organizationRepository.saveAndFlush(organization);
        Organization reloaded = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getAddress()).isNotNull();
        assertThat(reloaded.getAddress().getOrganization()).isNotNull();
        assertThat(reloaded.getAddress().getOrganization().getId()).isEqualTo(reloaded.getId());
    }

    @Test
    void settingInverseSide_shouldSynchronizeOwningSideAfterPersist() {
        Organization organization = createOrganization("ORG-INVERSE-1", "Inverse Corp");
        Address address = createAddress("Tower B", "Dallas");

        address.setOrganization(organization);

        assertThat(organization.getAddress()).isSameAs(address);

        Organization savedOrganization = organizationRepository.saveAndFlush(organization);
        Address savedAddress = addressRepository.findById(savedOrganization.getAddress().getId()).orElseThrow();

        assertThat(savedAddress.getOrganization()).isNotNull();
        assertThat(savedAddress.getOrganization().getId()).isEqualTo(savedOrganization.getId());
    }

    @Test
    void replacingAssociation_shouldClearOldBackReferenceBeforeFlushAndKeepNewAssociationAfterPersist() {
        Organization organization = createOrganization("ORG-REPLACE-1", "Replace Corp");
        Address oldAddress = createAddress("Tower C", "Houston");
        Address newAddress = createAddress("Tower D", "San Antonio");

        organization.setAddress(oldAddress);
        assertThat(oldAddress.getOrganization()).isSameAs(organization);

        organization.setAddress(newAddress);

        assertThat(newAddress.getOrganization()).isSameAs(organization);
        assertThat(oldAddress.getOrganization()).isNull();

        Organization saved = organizationRepository.saveAndFlush(organization);
        Organization reloaded = organizationRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getAddress().getBuilding()).isEqualTo("Tower D");
        assertThat(reloaded.getAddress().getOrganization().getId()).isEqualTo(reloaded.getId());
    }

    @Test
    void reassigningInverseSide_shouldUseReplacementAddressBeforeFlushAndPersistBothOwnersAfterPersist() {
        Organization firstOrganization = createOrganization("ORG-INV-FLUSH-1", "First Org");
        Organization secondOrganization = createOrganization("ORG-INV-FLUSH-2", "Second Org");
        Address originalAddress = createAddress("Tower E", "El Paso");
        Address replacementAddress = createAddress("Tower F", "Plano");

        originalAddress.setOrganization(firstOrganization);
        originalAddress.setOrganization(secondOrganization);

        // Mandatory owner FK: previous owner must have a replacement before flush.
        firstOrganization.setAddress(replacementAddress);

        Organization savedFirst = organizationRepository.saveAndFlush(firstOrganization);
        Organization savedSecond = organizationRepository.saveAndFlush(secondOrganization);

        Organization reloadedFirst = organizationRepository.findById(savedFirst.getId()).orElseThrow();
        Organization reloadedSecond = organizationRepository.findById(savedSecond.getId()).orElseThrow();

        assertThat(reloadedFirst.getAddress()).isNotNull();
        assertThat(reloadedFirst.getAddress().getBuilding()).isEqualTo("Tower F");
        assertThat(reloadedSecond.getAddress()).isNotNull();
        assertThat(reloadedSecond.getAddress().getBuilding()).isEqualTo("Tower E");
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

