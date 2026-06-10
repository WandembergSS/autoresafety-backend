package com.autoresafety.api.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StepFourProjectUpdatePayload(
        @NotNull @Positive Long id,
        Step4Information step4Information
) {
    public record Step4Information(
            List<UcaPayload> ucas,
            List<ControllerConstraintPayload> controllerConstraints
    ) {
    }

    public record UcaPayload(
            Long id,
            String controller,
            String controlAction,
            String hazard,
            String category
    ) {
    }

    public record ControllerConstraintPayload(
            Long id,
            String sourceUcaHc,
            String constraintId,
            String constraintStatement
    ) {
    }
}
