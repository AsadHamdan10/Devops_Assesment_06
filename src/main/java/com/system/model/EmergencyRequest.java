package com.system.model;

import java.util.UUID;

public class EmergencyRequest {
    private final String id;
    private final String patientId;
    private final EmergencySeverity severity;
    private final String pickupLocation;
    private final String destinationHospital;
    private final AmbulanceType requiredType;
    private String status;
    private String assignedAmbulanceId;

    public EmergencyRequest(String patientId, EmergencySeverity severity, String pickupLocation, 
                            String destinationHospital, AmbulanceType requiredType) {
        this.id = UUID.randomUUID().toString();
        this.patientId = patientId;
        this.severity = severity;
        this.pickupLocation = pickupLocation;
        this.destinationHospital = destinationHospital;
        this.requiredType = requiredType;
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public EmergencySeverity getSeverity() { return severity; }
    public String getPickupLocation() { return pickupLocation; }
    public String getDestinationHospital() { return destinationHospital; }
    public AmbulanceType getRequiredType() { return requiredType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedAmbulanceId() { return assignedAmbulanceId; }
    public void setAssignedAmbulanceId(String assignedAmbulanceId) { this.assignedAmbulanceId = assignedAmbulanceId; }
}

