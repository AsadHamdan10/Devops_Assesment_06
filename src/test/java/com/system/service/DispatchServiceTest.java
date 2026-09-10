package com.system.service;

import com.system.exception.InvalidRequestException;
import com.system.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class DispatchServiceTest {

    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new DispatchService();
    }

    // =========================================================================
    // POSITIVE TESTING SCENARIOS
    // =========================================================================
    @Nested
    @DisplayName("Positive Functional Paths")
    class PositiveTests {

        @Test
        @DisplayName("Should successfully assign available matching ambulance immediately")
        void testSuccessfulImmediateDispatch() {
            Ambulance amb = new Ambulance("AMB-01", AmbulanceType.ICU, "John Doe", 12.9716, 77.5946);
            dispatchService.registerAmbulance(amb);

            EmergencyRequest request = new EmergencyRequest("P-101", EmergencySeverity.CRITICAL, "Location A", "Hospital X", AmbulanceType.ICU);
            dispatchService.submitEmergencyRequest(request);

            assertEquals("DISPATCHED", request.getStatus());
            assertEquals("AMB-01", request.getAssignedAmbulanceId());
            assertEquals(AmbulanceState.DISPATCHED, amb.getState());
        }

        @Test
        @DisplayName("Should allocate closest match based on calculated coordinate distance metrics")
        void testClosestAmbulanceAllocation() {
            Ambulance farAmb = new Ambulance("AMB-01", AmbulanceType.BASIC, "Driver 1", 10.0, 10.0);
            Ambulance closeAmb = new Ambulance("AMB-02", AmbulanceType.BASIC, "Driver 2", 0.1, 0.1);
            dispatchService.registerAmbulance(farAmb);
            dispatchService.registerAmbulance(closeAmb);

            EmergencyRequest request = new EmergencyRequest("P-102", EmergencySeverity.NORMAL, "Location B", "Hospital Y", AmbulanceType.BASIC);
            dispatchService.submitEmergencyRequest(request);

            assertEquals("AMB-02", request.getAssignedAmbulanceId());
        }

        @Test
        @DisplayName("Should arrange queue elements in descending severity prioritization order")
        void testQueuePriorityAndAutoAllocation() {
            EmergencyRequest normalReq = new EmergencyRequest("P-01", EmergencySeverity.NORMAL, "Loc 1", "Hosp X", AmbulanceType.BASIC);
            EmergencyRequest criticalReq = new EmergencyRequest("P-02", EmergencySeverity.CRITICAL, "Loc 2", "Hosp X", AmbulanceType.BASIC);
            EmergencyRequest highReq = new EmergencyRequest("P-03", EmergencySeverity.HIGH, "Loc 3", "Hosp X", AmbulanceType.BASIC);

            dispatchService.submitEmergencyRequest(normalReq);
            dispatchService.submitEmergencyRequest(criticalReq);
            dispatchService.submitEmergencyRequest(highReq);

            Queue<EmergencyRequest> queue = dispatchService.getWaitingQueue();
            assertEquals(3, queue.size());
            assertEquals(EmergencySeverity.CRITICAL, queue.peek().getSeverity());

            Ambulance amb = new Ambulance("AMB-BASIC", AmbulanceType.BASIC, "Driver X", 0.0, 0.0);
            dispatchService.registerAmbulance(amb);
            dispatchService.updateAmbulanceState("AMB-BASIC", AmbulanceState.AVAILABLE);

            assertEquals("DISPATCHED", criticalReq.getStatus());
            assertEquals("AMB-BASIC", criticalReq.getAssignedAmbulanceId());
        }
    }

    // =========================================================================
    // NEGATIVE TESTING SCENARIOS
    // =========================================================================
    @Nested
    @DisplayName("Negative Validation & Boundary Fault Paths")
    class NegativeTests {

        @Test
        @DisplayName("Should throw InvalidRequestException when passing a null patient identification identifier")
        void testNullPatientIdException() {
            EmergencyRequest badRequest = new EmergencyRequest(null, EmergencySeverity.CRITICAL, "Location A", "Hospital X", AmbulanceType.ICU);

            assertThrows(InvalidRequestException.class, () -> dispatchService.submitEmergencyRequest(badRequest));
        }

        @Test
        @DisplayName("Should throw InvalidRequestException when location value contains null string content")
        void testNullLocationException() {
            EmergencyRequest badRequest = new EmergencyRequest("P-500", EmergencySeverity.HIGH, null, "Hospital X", AmbulanceType.BASIC);

            assertThrows(InvalidRequestException.class, () -> dispatchService.submitEmergencyRequest(badRequest));
        }

        @Test
        @DisplayName("Should throw InvalidRequestException when requesting state changes on missing IDs")
        void testNonExistentAmbulanceStateUpdate() {
            assertThrows(InvalidRequestException.class, () -> dispatchService.updateAmbulanceState("MISSING-ID", AmbulanceState.AVAILABLE));
        }

        @Test
        @DisplayName("Should prevent cross-type vehicle pollution assignments")
        void testNoMismatchedAmbulanceTypeAllocation() {
            Ambulance basicAmb = new Ambulance("AMB-BASIC", AmbulanceType.BASIC, "Driver 1", 0.0, 0.0);
            dispatchService.registerAmbulance(basicAmb);

            EmergencyRequest icuRequest = new EmergencyRequest("P-700", EmergencySeverity.CRITICAL, "Location A", "Hospital X", AmbulanceType.ICU);
            dispatchService.submitEmergencyRequest(icuRequest);

            assertEquals("QUEUED", icuRequest.getStatus());
            assertNull(icuRequest.getAssignedAmbulanceId());
        }

        @Test
        @DisplayName("Should throw InvalidRequestException if negative distance metric values are calculated")
        void testNegativeDistanceCalculations() {
            assertThrows(InvalidRequestException.class, () -> dispatchService.calculateETA(-15.5));
        }

        @Test
        @DisplayName("Should safely queue request when absolutely no ambulances are registered")
        void testAbsoluteResourceUnavailability() {
            // Arrange: System is empty, no ambulances registered at all
        EmergencyRequest urgentRequest = new EmergencyRequest(
            "P-999", EmergencySeverity.CRITICAL, "Remote Location", "City Hospital", AmbulanceType.ICU
        );

    // Act
    dispatchService.submitEmergencyRequest(urgentRequest);

    // Assert: The system shouldn't crash; it must safely queue the request
    assertEquals("QUEUED", urgentRequest.getStatus());
    assertNull(urgentRequest.getAssignedAmbulanceId());
    assertEquals(1, dispatchService.getWaitingQueue().size());
}

    }
}

