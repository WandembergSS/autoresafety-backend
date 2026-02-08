package com.autoresafety.api.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.autoresafety.domain.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Full "project document" DTO that matches the JSON shape provided by the client.
 * Intended for future GET/POST usage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDocumentDto {
        private ProjectDto project;
        private Step1ScopeDto step1Scope;
        private Step2IstarDto step2Istar;
        private Step3ControlStructureDto step3ControlStructure;
        private Step4UcasDto step4Ucas;
        private Step5ControllerConstraintsDto step5ControllerConstraints;
        private Step6LossScenariosDto step6LossScenarios;
        private Step7ModelUpdateDto step7ModelUpdate;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProjectDto {
                @Positive
                private Long id;

                @NotBlank
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

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step1ScopeDto {
                private String lastUpdatedBy;
                private GeneralSummaryDto generalSummary;
                private String objectives;
                private List<ResourceDto> resources;
                private List<SystemComponentDto> systemComponents;
                private List<AccidentDto> accidents;
                private List<HazardDto> hazards;
                private List<SafetyConstraintDto> safetyConstraints;
                private List<ResponsibilityDto> responsibilities;
                private List<ArtefactDto> artefacts;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class GeneralSummaryDto {
                        private String assumptions;

                        private String systemDefinition;

                        private String systemBoundary;

                        private String outOfScope;
                }



                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ResourceDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String category;

                        private String reference;

                        private SourceType sourceType;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SystemComponentDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String description;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class AccidentDto {
                        @Positive
                        private Long id;

                        private String code;

                        private String description;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class HazardDto {
                        @Positive
                        private Long id;

                        private String code;

                        private String description;

                        private List<String> linkedAccidents;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SafetyConstraintDto {
                        @Positive
                        private Long id;

                        private String code;

                        private String statement;

                        private List<String> linkedHazards;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ResponsibilityDto {
                        @Positive
                        private Long id;

                        private String component;

                        private String responsibility;

                        private List<String> linkedConstraints;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ArtefactDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String purpose;

                        private String reference;
                }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step2IstarDto {
                private List<ActorDto> actors;
                private List<GoalLinkDto> goalLinks;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ActorDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String type;
                            private List<String> responsibilities;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class GoalLinkDto {
                        @Positive
                        private Long id;

                        private String fromActor;

                        private String goal;

                        private String linkType;
                }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step3ControlStructureDto {
                private List<ControlActionDto> controlActions;
                private List<FeedbackLoopDto> feedbackLoops;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ControlActionDto {
                        @Positive
                        private Long id;

                        private String controller;

                        private String action;

                        private String controlledProcess;

                        private String feedback;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class FeedbackLoopDto {
                        @Positive
                        private Long id;

                        private String source;

                        private String destination;

                        private String signal;

                        private String latency;
                }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step4UcasDto {
                private List<UcaDto> ucas;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class UcaDto {
                        @Positive
                        private Long id;

                        private String controller;

                        private String controlAction;

                        private String hazard;

                        private String category;
                }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step5ControllerConstraintsDto {
                private List<ConstraintDto> constraints;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ConstraintDto {
                        @Positive
                        private Long id;

                        private String ucaRef;

                        private String constraint;

                        private String enforcementMechanism;

                        private String status;
                }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step6LossScenariosDto {
                private List<LossScenarioDto> lossScenarios;
                private List<SafetyRequirementDto> safetyRequirements;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class LossScenarioDto {
                        @Positive
                        private Long id;

                        private String uca;

                        private String hazard;

                        private String outcome;

                        private String severity;
                            private List<String> mitigations;

                        private String status;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class SafetyRequirementDto {
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

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Step7ModelUpdateDto {
                private List<ModelChangeDto> modelChanges;
                private List<ValidationTaskDto> validationTasks;
                private List<IntegrationNoteDto> integrationNotes;

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ModelChangeDto {
                        @Positive
                        private Long id;

                        private String area;

                        private String change;

                        private String driver;

                        private String impact;

                        private String status;
                            private List<String> evidence;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class ValidationTaskDto {
                        @Positive
                        private Long id;

                        private String name;

                        private String owner;

                        private String dueDate;

                        private String channel;

                        private String status;
                }

                @Data
                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                public static class IntegrationNoteDto {
                        @Positive
                        private Long id;

                        private String summary;

                        private String createdOn;

                        private String author;
                            private List<String> actionItems;
                }
        }
}
