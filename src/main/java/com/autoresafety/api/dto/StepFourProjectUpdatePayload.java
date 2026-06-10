package com.autoresafety.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StepFourProjectUpdatePayload(
        @NotNull @Positive Long id,
        Step4Information step4Information
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Step4Information(
            @JsonAlias("ucas") List<UcaPayload> unsafeControlActions,
            List<HazardousConditionPayload> hazardousConditions,
            List<ControllerConstraintPayload> controllerConstraints
    ) {
        public List<UcaPayload> ucas() {
            return unsafeControlActions;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UcaPayload(
            Long id,
            String ref,
            String controlActionRef,
            String sourceActor,
            String targetActor,
            String controller,
            String controlAction,
            String controlledProcess,
            String category,
            String context,
            String consequence,
            String rationale,
            List<String> hazardRefs,
            String responsibilityId,
            String safetyConstraintId,
            String hazard
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HazardousConditionPayload(
            Long id,
            String ref,
            String description,
            String context,
            String consequence,
            List<String> hazardRefs
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ControllerConstraintPayload(
            Long id,
            String sourceUcaHc,
            String constraintId,
            String constraintStatement
    ) {
    }
}
