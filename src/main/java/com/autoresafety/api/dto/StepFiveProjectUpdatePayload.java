package com.autoresafety.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StepFiveProjectUpdatePayload(
    @NotNull @Positive Long id,
    @NotNull Step5Information step5Information
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Step5Information(
        List<LossScenarioPayload> lossScenarios,
        List<SafetyRequirementPayload> safetyRequirements
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LossScenarioPayload(
        String id,
        String description,
        List<String> associatedUnsafeBehaviorIds,
        String sourceRationale
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SafetyRequirementPayload(
        String id,
        String description,
        List<String> addressedLossScenarioIds
    ) {
    }
}
