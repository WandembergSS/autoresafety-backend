package com.autoresafety.api.dto;

import java.util.List;

public record StepFiveProjectInformationDto(
    Long projectId,
    int step,
    AvailableInputs availableInputs,
    CurrentData currentData,
    Defaults defaults
) {
    public record AvailableInputs(
        List<StepFiveUnsafeBehavior> unsafeBehaviors
    ) {
    }

    public record CurrentData(
        List<StepFiveLossScenario> lossScenarios,
        List<StepFiveSafetyRequirement> safetyRequirements
    ) {
    }

    public record Defaults(
        String nextLossScenarioId,
        String nextSafetyRequirementId
    ) {
    }

    public record StepFiveUnsafeBehavior(
        String id,
        String type,
        String title,
        String description,
        List<String> hazards
    ) {
    }

    public record StepFiveLossScenario(
        String id,
        String description,
        List<String> associatedUnsafeBehaviorIds,
        String sourceRationale
    ) {
    }

    public record StepFiveSafetyRequirement(
        String id,
        String description,
        List<String> addressedLossScenarioIds
    ) {
    }
}
