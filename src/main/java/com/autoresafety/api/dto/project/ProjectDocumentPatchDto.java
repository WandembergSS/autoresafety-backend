package com.autoresafety.api.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.autoresafety.domain.ProjectStatus;
import com.autoresafety.api.dto.project.SourceType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * PATCH DTO: all fields are nullable so callers can send partial updates.
 * Intended for future PATCH usage.
 */
@JsonInclude(NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDocumentPatchDto {
        private ProjectPatchDto project;
        private Step1ScopePatchDto step1Scope;
        private Step2IstarPatchDto step2Istar;
        private Step3ControlStructurePatchDto step3ControlStructure;
        private Step4UcasPatchDto step4Ucas;
        private Step5ControllerConstraintsPatchDto step5ControllerConstraints;
        private Step6LossScenariosPatchDto step6LossScenarios;
        private Step7ModelUpdatePatchDto step7ModelUpdate;

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProjectPatchDto {
                @Positive
                private Long id;
                private String name;

                private String domain;

                private String owner;

                private String description;

                private ProjectStatus status;

                @PositiveOrZero
                private Integer currentStep;

                private Instant createdAt;
                private Instant updatedAt;
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step1ScopePatchDto {
                private String lastUpdatedBy;
                private GeneralSummaryPatchDto generalSummary;
                private List<ObjectivePatchDto> objectives;
                private List<ResourcePatchDto> resources;
                private List<SystemComponentPatchDto> systemComponents;
                private List<AccidentPatchDto> accidents;
                private List<HazardPatchDto> hazards;
                private List<SafetyConstraintPatchDto> safetyConstraints;
                private List<ResponsibilityPatchDto> responsibilities;
                private List<ArtefactPatchDto> artefacts;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class GeneralSummaryPatchDto {
                        private String assumptions;

                        private String systemDefinition;

                        private String systemBoundary;

                        private String outOfScope;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ObjectivePatchDto {
                        @Positive
                        private Long id;

                        private String focus;

                        private String stakeholder;

                        private String priority;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ResourcePatchDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String category;

                        private String reference;

                        private SourceType sourceType;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SystemComponentPatchDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String description;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class AccidentPatchDto {
                        @Positive
                        private Long id;

                        private String code;

                        private String description;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class HazardPatchDto {
                        @Positive
                        private Long id;

                        private String code;

                        private String description;

                        private List<String> linkedAccidents;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SafetyConstraintPatchDto {
                        @Positive
                        private Long id;

                        private String code;

                        private String statement;

                        private List<String> linkedHazards;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ResponsibilityPatchDto {
                        @Positive
                        private Long id;

                        private String component;

                        private String responsibility;

                        private List<String> linkedConstraints;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ArtefactPatchDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String purpose;

                        private String reference;
                }
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step2IstarPatchDto {
                private List<ActorPatchDto> actors;
                private List<GoalLinkPatchDto> goalLinks;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ActorPatchDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String type;
                            private List<String> responsibilities;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class GoalLinkPatchDto {
                        @Positive
                        private Long id;

                        private String fromActor;

                        private String goal;

                        private String linkType;
                }
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step3ControlStructurePatchDto {
                private List<ControlActionPatchDto> controlActions;
                private List<FeedbackLoopPatchDto> feedbackLoops;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ControlActionPatchDto {
                        @Positive
                        private Long id;

                        private String controller;

                        private String action;

                        private String controlledProcess;

                        private String feedback;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class FeedbackLoopPatchDto {
                        @Positive
                        private Long id;

                        private String source;

                        private String destination;

                        private String signal;

                        private String latency;
                }
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step4UcasPatchDto {
                private List<UcaPatchDto> ucas;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class UcaPatchDto {
                        @Positive
                        private Long id;

                        private String controller;

                        private String controlAction;

                        private String hazard;

                        private String category;
                }
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step5ControllerConstraintsPatchDto {
                private List<ConstraintPatchDto> constraints;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ConstraintPatchDto {
                        @Positive
                        private Long id;

                        private String ucaRef;

                        private String constraint;

                        private String enforcementMechanism;

                        private String status;
                }
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step6LossScenariosPatchDto {
                private List<LossScenarioPatchDto> lossScenarios;
                private List<SafetyRequirementPatchDto> safetyRequirements;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class LossScenarioPatchDto {
                        @Positive
                        private Long id;

                        private String uca;

                        private String hazard;

                        private String outcome;

                        private String severity;
                            private List<String> mitigations;

                        private String status;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SafetyRequirementPatchDto {
                        @Positive
                        private Long id;

                        private String title;

                        @Positive
                        private Long linkedScenario;

                        private String category;

                        private String owner;

                        private LocalDate dueDate;

                        private String status;
                }
        }

        @JsonInclude(NON_NULL)
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step7ModelUpdatePatchDto {
                private List<ModelChangePatchDto> modelChanges;
                private List<ValidationTaskPatchDto> validationTasks;
                private List<IntegrationNotePatchDto> integrationNotes;

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ModelChangePatchDto {
                        @Positive
                        private Long id;

                        private String area;

                        private String change;

                        private String driver;

                        private String impact;

                        private String status;
                            private List<String> evidence;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ValidationTaskPatchDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String owner;

                        private String dueDate;

                        private String channel;

                        private String status;
                }

                @JsonInclude(NON_NULL)
                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class IntegrationNotePatchDto {
                        @Positive
                        private Long id;

                        private String summary;

                        private String createdOn;

                        private String author;
                            private List<String> actionItems;
                }
        }
}
