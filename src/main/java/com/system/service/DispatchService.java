package com.system.service;

import com.system.exception.InvalidRequestException;
import com.system.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class DispatchService {
    private final Map<String, Ambulance> ambulances = new ConcurrentHashMap<>();
    private final List<EmergencyRequest> history = Collections.synchronizedList(new ArrayList<>());
    
    // Thread-safe Priority Queue sorting by severity rank (descending)
    private final PriorityBlockingQueue<EmergencyRequest> waitingQueue = new PriorityBlockingQueue<>(
            11, Comparator.comparingInt((EmergencyRequest r) -> r.getSeverity().getRank()).reversed()
    );

    public void registerAmbulance(Ambulance ambulance) {
        if (ambulance == null || ambulance.getId() == null) {
            throw new InvalidRequestException("Invalid ambulance details provided");
        }
        ambulances.put(ambulance.getId(), ambulance);
    }

    public synchronized void submitEmergencyRequest(EmergencyRequest request) {
        validateRequest(request);
        history.add(request);

        Ambulance bestMatch = findBestAmbulance(request);
        if (bestMatch != null) {
            dispatchAmbulance(request, bestMatch);
        } else {
            request.setStatus("QUEUED");
            waitingQueue.add(request);
        }
    }

    private Ambulance findBestAmbulance(EmergencyRequest request) {
        Ambulance bestMatch = null;
        double minDistance = Double.MAX_VALUE;

        // Arbitrary baseline target location mapping for testing distance computations
        double requestLat = 0.0; 
        double requestLong = 0.0;

        for (Ambulance ambulance : ambulances.values()) {
            if (ambulance.getState() == AmbulanceState.AVAILABLE && ambulance.getType() == request.getRequiredType()) {
                double distance = calculateDistance(ambulance.getCurrentLatitude(), ambulance.getCurrentLongitude(), requestLat, requestLong);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestMatch = ambulance;
                }
            }
        }
        return bestMatch;
    }

    private void dispatchAmbulance(EmergencyRequest request, Ambulance ambulance) {
        ambulance.setState(AmbulanceState.DISPATCHED);
        request.setAssignedAmbulanceId(ambulance.getId());
        request.setStatus("DISPATCHED");
    }

    public synchronized void updateAmbulanceState(String ambulanceId, AmbulanceState newState) {
        Ambulance ambulance = ambulances.get(ambulanceId);
        if (ambulance == null) {
            throw new InvalidRequestException("Ambulance ID not found");
        }
        
        ambulance.setState(newState);

        if (newState == AmbulanceState.AVAILABLE) {
            processWaitingQueue();
        }
    }

    private void processWaitingQueue() {
        List<EmergencyRequest> temp = new ArrayList<>();
        while (!waitingQueue.isEmpty()) {
            EmergencyRequest request = waitingQueue.poll();
            Ambulance bestMatch = findBestAmbulance(request);
            if (bestMatch != null) {
                dispatchAmbulance(request, bestMatch);
            } else {
                temp.add(request);
            }
        }
        waitingQueue.addAll(temp);
    }

    public double calculateETA(double distanceInKm) {
        if (distanceInKm < 0) {
            throw new InvalidRequestException("Distance cannot be negative");
        }
        double averageSpeedKmh = 50.0;
        return (distanceInKm / averageSpeedKmh) * 60;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));
    }

    private void validateRequest(EmergencyRequest request) {
        if (request == null || request.getPatientId() == null || request.getPickupLocation() == null) {
            throw new InvalidRequestException("Invalid registration parameters");
        }
    }

    public List<EmergencyRequest> getHistory() { return new ArrayList<>(history); }
    public Queue<EmergencyRequest> getWaitingQueue() { return waitingQueue; }
}

