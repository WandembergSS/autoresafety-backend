package com.autoresafety.api.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StepThreeProjectUpdatePayload(
        @NotNull @Positive Long id,
        Step3Information step3Information
) {
    public record Step3Information(
            List<EntityPayload> entities,
            List<ControlActionPayload> controlActions,
            List<OptionalElementPayload> optionalElements
    ) {
    }

    public record EntityPayload(
            String id,
            String entityCandidateId,
            String name,
            List<String> roles
    ) {
    }

    public record ControlActionPayload(
            String id,
            String ref,
            String action,
            String sourceEntityId,
            String targetEntityId,
            String responsibilityId
    ) {
    }

    public record OptionalElementPayload(
            String id,
            String type,
            String name,
            String sourceKind,
            String sourceEntityId,
            String sourceExternalId,
            String destinationKind,
            String destinationEntityId,
            String destinationExternalId,
            String responsibilityId
    ) {
    }
}
