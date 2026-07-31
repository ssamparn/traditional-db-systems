package com.traditional.databases.jdbconetoonebidirectionalrelation.entity.orphanhandling;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.OrganizationRepository;
import com.traditional.databases.jdbconetoonebidirectionalrelation.service.OrganizationService;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.request.AddressRequest;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.request.OrganizationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OrganizationOrphanHandlingIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrganizationService organizationService;

    @AfterEach
    void cleanup() {
        organizationRepository.deleteAll();
        addressRepository.deleteAll();
    }

    @Test
    void updateOrganization_shouldRemoveReplacedAddressAsOrphan() {
        Organization organization = new Organization();
        organization.setName("Acme Inc");
        organization.setOrgId("ORG-001");

        Address initialAddress = new Address();
        initialAddress.setBuilding("Tower A");
        initialAddress.setStreet("Main Street");
        initialAddress.setCity("Austin");
        initialAddress.setState("Texas");
        initialAddress.setCountry("USA");
        initialAddress.setZipcode("73301");

        organization.setAddress(initialAddress);

        Organization savedOrganization = organizationRepository.save(organization);
        Long oldAddressId = savedOrganization.getAddress().getId();

        OrganizationRequest updateRequest = new OrganizationRequest(
                "Acme International",
                "ORG-001",
                new AddressRequest("Tower B", "Market Street", "Dallas", "Texas", "USA", "75001")
        );

        organizationService.updateOrganization(savedOrganization.getId(), updateRequest).block();

        Organization updatedOrganization = organizationRepository.findById(savedOrganization.getId()).orElseThrow();
        Long newAddressId = updatedOrganization.getAddress().getId();

        assertThat(newAddressId).isNotEqualTo(oldAddressId);
        assertThat(addressRepository.existsById(oldAddressId)).isFalse();
        assertThat(updatedOrganization.getAddress().getCity()).isEqualTo("Dallas");
    }

    @Test
    void deleteOrganization_shouldCascadeDeleteAddress() {
        Organization organization = new Organization();
        organization.setName("Delete Corp");
        organization.setOrgId("ORG-DELETE-1");

        Address address = new Address();
        address.setBuilding("Tower Z");
        address.setStreet("Delete Street");
        address.setCity("Houston");
        address.setState("Texas");
        address.setCountry("USA");
        address.setZipcode("77001");

        organization.setAddress(address);

        Organization saved = organizationRepository.save(organization);
        Long addressId = saved.getAddress().getId();

        organizationService.deleteOrganizationById(saved.getId()).block();

        assertThat(organizationRepository.existsById(saved.getId())).isFalse();
        assertThat(addressRepository.existsById(addressId)).isFalse();
    }
}

