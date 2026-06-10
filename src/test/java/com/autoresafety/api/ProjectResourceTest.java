package com.autoresafety.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.autoresafety.api.dto.project.ProjectDocumentDto;
import com.autoresafety.api.dto.project.SourceType;
import com.autoresafety.service.ProjectDocumentService;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;

@ExtendWith(ByteBuddyExperimentalExtension.class)
@QuarkusTest
class ProjectResourceTest extends QuarkusTestBase {

    @Inject
    ProjectDocumentService projectDocumentService;

    @Test
    void projectCrudLifecycle() {
    var location = given()
        .contentType("application/json")
        .body("""
            {"name":"Demo Project","description":"Initial test project"}
            """)
        .when()
        .post("/api/projects")
        .then()
        .statusCode(201)
        .extract()
        .header("Location");

    given()
        .when()
        .get(location)
        .then()
        .statusCode(200)
        .body("name", equalTo("Demo Project"));

    given()
        .when()
        .get("/api/projects")
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(1));

    given()
        .when()
        .delete(location)
        .then()
        .statusCode(204);
    }

    @Test
    void fullProjectEndpointReturnsAllDocumentSections() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Chapter Case Study Project")
                .domain("Aerospace")
                .owner("AutoRESafety")
                .description("Complete STPA chapter case for application")
                .currentStep(7)
                .status(com.autoresafety.domain.ProjectStatus.PENDING)
                .createdAt(Instant.parse("2026-06-09T10:00:00Z"))
                .updatedAt(Instant.parse("2026-06-09T10:00:00Z"))
                .build())
            .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                .lastUpdatedBy("tester")
                .generalSummary(ProjectDocumentDto.Step1ScopeDto.GeneralSummaryDto.builder()
                    .assumptions("Nominal")
                    .systemDefinition("Drone platform")
                    .systemBoundary("Drone plus station")
                    .outOfScope("Manufacturing")
                    .build())
                .objectives("Prevent collision")
                .resources(List.of(ProjectDocumentDto.Step1ScopeDto.ResourceDto.builder()
                    .id(1L)
                    .name("Manual")
                    .category("documentation")
                    .reference("DOC-1")
                    .sourceType(SourceType.MANUAL)
                    .build()))
                .systemComponents(List.of(
                    ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                        .id(1L)
                        .name("Autopilot")
                        .description("Navigation logic")
                        .build(),
                    ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                        .id(2L)
                        .name("Operator")
                        .description("Mission supervision")
                        .build()
                ))
                .accidents(List.of(ProjectDocumentDto.Step1ScopeDto.AccidentDto.builder()
                    .id(1L)
                    .code("A-1")
                    .description("Collision")
                    .build()))
                .hazards(List.of(ProjectDocumentDto.Step1ScopeDto.HazardDto.builder()
                    .id(1L)
                    .code("H-1")
                    .description("Loss of detection")
                    .linkedAccidents(List.of("A-1"))
                    .build()))
                .safetyConstraints(List.of(ProjectDocumentDto.Step1ScopeDto.SafetyConstraintDto.builder()
                    .id(1L)
                    .code("SC-1")
                    .statement("Maintain separation")
                    .linkedHazards(List.of("H-1"))
                    .build()))
                .responsibilities(List.of(ProjectDocumentDto.Step1ScopeDto.ResponsibilityDto.builder()
                    .id(1L)
                    .component("Navigation")
                    .responsibility("Avoid obstacles")
                    .linkedConstraints(List.of("SC-1"))
                    .build()))
                .artefacts(List.of(ProjectDocumentDto.Step1ScopeDto.ArtefactDto.builder()
                    .id(1L)
                    .name("Architecture")
                    .purpose("Describe structure")
                    .reference("ARCH-1")
                    .build()))
                .build())
            .step2Istar(ProjectDocumentDto.Step2IstarDto.builder()
                .actors(List.of(ProjectDocumentDto.Step2IstarDto.ActorDto.builder()
                    .id(1L)
                    .name("Operator")
                    .type("human")
                    .responsibilities(List.of("Start mission"))
                    .build()))
                .goalLinks(List.of(ProjectDocumentDto.Step2IstarDto.GoalLinkDto.builder()
                    .id(1L)
                    .fromActor("Operator")
                    .goal("Safe mission")
                    .linkType("delegates")
                    .build()))
                .build())
            .step3ControlStructure(ProjectDocumentDto.Step3ControlStructureDto.builder()
                .controlActions(List.of(ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                    .id(1L)
                    .controller("Autopilot")
                    .action("Adjust heading")
                    .controlledProcess("Trajectory")
                    .feedback("Telemetry")
                    .build()))
                .feedbackLoops(List.of(ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto.builder()
                    .id(1L)
                    .source("Lidar")
                    .destination("Autopilot")
                    .signal("Distance")
                    .latency("<100ms")
                    .build()))
                .build())
            .step4Ucas(ProjectDocumentDto.Step4UcasDto.builder()
                .ucas(List.of(ProjectDocumentDto.Step4UcasDto.UcaDto.builder()
                    .id(1L)
                    .controller("Autopilot")
                    .controlAction("Adjust heading")
                    .hazard("H-1")
                    .category("Not provided")
                    .build()))
                .build())
            .step5ControllerConstraints(ProjectDocumentDto.Step5ControllerConstraintsDto.builder()
                .constraints(List.of(ProjectDocumentDto.Step5ControllerConstraintsDto.ConstraintDto.builder()
                    .id(1L)
                    .ucaRef("UCA-1")
                    .constraint("Avoid obstacles")
                    .enforcementMechanism("Runtime guard")
                    .status("defined")
                    .build()))
                .build())
            .step6LossScenarios(ProjectDocumentDto.Step6LossScenariosDto.builder()
                .lossScenarios(List.of(ProjectDocumentDto.Step6LossScenariosDto.LossScenarioDto.builder()
                    .id(1L)
                    .uca("UCA-1")
                    .hazard("H-1")
                    .outcome("Impact")
                    .severity("High")
                    .mitigations(List.of("Fallback to hover"))
                    .status("open")
                    .build()))
                .safetyRequirements(List.of(ProjectDocumentDto.Step6LossScenariosDto.SafetyRequirementDto.builder()
                    .id(1L)
                    .title("SR-1")
                    .linkedScenario(1L)
                    .category("functional")
                    .owner("Safety team")
                    .dueDate(LocalDate.of(2026, 7, 15))
                    .status("planned")
                    .build()))
                .build())
            .step7ModelUpdate(ProjectDocumentDto.Step7ModelUpdateDto.builder()
                .modelChanges(List.of(ProjectDocumentDto.Step7ModelUpdateDto.ModelChangeDto.builder()
                    .id(1L)
                    .area("Navigation")
                    .change("Added guard")
                    .driver("UCA-1")
                    .impact("Lower collision risk")
                    .status("implemented")
                    .evidence(List.of("SIM-1"))
                    .build()))
                .validationTasks(List.of(ProjectDocumentDto.Step7ModelUpdateDto.ValidationTaskDto.builder()
                    .id(1L)
                    .name("HIL test")
                    .owner("QA")
                    .dueDate("2026-07-20")
                    .channel("test-lab")
                    .status("queued")
                    .build()))
                .integrationNotes(List.of(ProjectDocumentDto.Step7ModelUpdateDto.IntegrationNoteDto.builder()
                    .id(1L)
                    .summary("Schemas aligned")
                    .createdOn("2026-06-09")
                    .author("Architect")
                    .actionItems(List.of("Run regression"))
                    .build()))
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .when()
            .get("/api/projects/" + projectId + "/full")
            .then()
            .statusCode(200)
            .body("project.id", equalTo((int) projectId))
            .body("project.name", equalTo("Chapter Case Study Project"))
            .body("step1Scope.resources[0].name", equalTo("Manual"))
            .body("step2Istar.actors[0].name", equalTo("Operator"))
            .body("step3ControlStructure.controlActions[0].controller", equalTo("Autopilot"))
            .body("step4Ucas.ucas[0].category", equalTo("Not provided"))
            .body("step5ControllerConstraints.constraints[0].status", equalTo("defined"))
            .body("step6LossScenarios.safetyRequirements[0].title", equalTo("SR-1"))
            .body("step7ModelUpdate.modelChanges[0].area", equalTo("Navigation"));
    }

    @Test
    void stepThreeProjectInformationEndpointReturnsControlStructure() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Step Three Project")
                .build())
            .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                .systemComponents(List.of(ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                    .id(1L)
                    .name("Autopilot")
                    .description("Navigation logic")
                    .build()))
                .build())
            .step3ControlStructure(ProjectDocumentDto.Step3ControlStructureDto.builder()
                .controlActions(List.of(ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                    .id(1L)
                    .controller("Autopilot")
                    .action("Adjust heading")
                    .controlledProcess("Trajectory")
                    .feedback("Telemetry")
                    .build()))
                .feedbackLoops(List.of(ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto.builder()
                    .id(1L)
                    .source("Lidar")
                    .destination("Autopilot")
                    .signal("Distance")
                    .latency("<100ms")
                    .build()))
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .when()
            .get("/api/projects/step_three_project_information/" + projectId)
            .then()
            .statusCode(200)
            .body("controlActions[0].controller", equalTo("Autopilot"))
            .body("feedbackLoops[0].source", equalTo("Lidar"));
    }

    @Test
    void stepFourProjectInformationEndpointReturnsUcas() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Step Four Project")
                .build())
            .step4Ucas(ProjectDocumentDto.Step4UcasDto.builder()
                .ucas(List.of(ProjectDocumentDto.Step4UcasDto.UcaDto.builder()
                    .id(1L)
                    .ref("UCA-01")
                    .controlActionRef("CA-02")
                    .sourceActor("Autopilot")
                    .targetActor("Operator")
                    .controller("Autopilot")
                    .controlAction("Adjust heading")
                    .controlledProcess("Operator")
                    .category("Not provided")
                    .context("Context pending refinement")
                    .consequence("Consequence pending refinement")
                    .rationale("Rationale pending refinement")
                    .hazardRefs(List.of("H-1"))
                    .responsibilityId("")
                    .safetyConstraintId("")
                    .build()))
                .hazardousConditions(List.of())
                .controllerConstraints(List.of(ProjectDocumentDto.Step4UcasDto.ControllerConstraintDto.builder()
                    .id(1L)
                    .sourceUcaHc("UCA-1")
                    .constraintId("CC-01")
                    .constraintStatement("Autopilot shall issue heading correction when obstacle distance is below threshold.")
                    .build()))
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .when()
            .get("/api/projects/step_four_project_information/" + projectId)
            .then()
            .statusCode(200)
            .body("unsafeControlActions[0].ref", equalTo("UCA-01"))
            .body("unsafeControlActions[0].sourceActor", equalTo("Autopilot"))
            .body("ucas[0].controller", equalTo("Autopilot"))
            .body("ucas[0].controlAction", equalTo("Adjust heading"))
            .body("controllerConstraints[0].sourceUcaHc", equalTo("UCA-1"))
            .body("controllerConstraints[0].constraintId", equalTo("CC-01"));
    }

    @Test
    void stepFourProjectUpdateEndpointWithPathIdPersistsFullStep4Schema() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Step Four Update Project")
                .build())
            .step4Ucas(ProjectDocumentDto.Step4UcasDto.builder()
                .ucas(List.of())
                .hazardousConditions(List.of())
                .controllerConstraints(List.of())
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .contentType("application/json")
            .body("""
                    {
                        "id": 1,
                        "step4Information": {
                            "unsafeControlActions": [
                                {
                                    "id": 1,
                                    "ref": "UCA-01",
                                    "controlActionRef": "CA-02",
                                    "sourceActor": "Autopilot",
                                    "targetActor": "Operator",
                                    "controller": "Autopilot",
                                    "controlAction": "Pause mission",
                                    "controlledProcess": "Operator",
                                    "category": "Not provided",
                                    "context": "Context pending refinement",
                                    "consequence": "Consequence pending refinement",
                                    "rationale": "Rationale pending refinement",
                                    "hazardRefs": ["H-1", "aaa"],
                                    "responsibilityId": "",
                                    "safetyConstraintId": ""
                                }
                            ],
                            "hazardousConditions": [],
                            "controllerConstraints": [
                                {
                                    "id": 1,
                                    "sourceUcaHc": "UCA-01",
                                    "constraintId": "CC-01",
                                    "constraintStatement": "Autopilot shall provide Adjust heading when obstacle distance is below threshold."
                                }
                            ]
                        }
                    }
                    """)
            .when()
            .post("/api/projects/step_four_project_update/" + projectId)
            .then()
            .statusCode(200);

        given()
            .when()
            .get("/api/projects/step_four_project_information/" + projectId)
            .then()
            .statusCode(200)
            .body("unsafeControlActions[0].ref", equalTo("UCA-01"))
            .body("unsafeControlActions[0].controlActionRef", equalTo("CA-02"))
            .body("unsafeControlActions[0].hazardRefs[0]", equalTo("H-1"))
            .body("controllerConstraints[0].sourceUcaHc", equalTo("UCA-01"))
            .body("controllerConstraints[0].constraintId", equalTo("CC-01"));
    }

    @Test
    void stepFiveProjectInformationEndpointReturnsControllerConstraints() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Step Five Project")
                .build())
            .step5ControllerConstraints(ProjectDocumentDto.Step5ControllerConstraintsDto.builder()
                .constraints(List.of(ProjectDocumentDto.Step5ControllerConstraintsDto.ConstraintDto.builder()
                    .id(1L)
                    .ucaRef("UCA-1")
                    .constraint("Maintain safe heading")
                    .enforcementMechanism("Runtime guard")
                    .status("defined")
                    .build()))
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .when()
            .get("/api/projects/step_five_project_information/" + projectId)
            .then()
            .statusCode(200)
            .body("constraints[0].ucaRef", equalTo("UCA-1"))
            .body("constraints[0].status", equalTo("defined"));
    }

    @Test
    void stepSixProjectInformationEndpointReturnsLossScenarios() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Step Six Project")
                .build())
            .step6LossScenarios(ProjectDocumentDto.Step6LossScenariosDto.builder()
                .lossScenarios(List.of(ProjectDocumentDto.Step6LossScenariosDto.LossScenarioDto.builder()
                    .id(1L)
                    .uca("UCA-1")
                    .hazard("H-1")
                    .outcome("Collision")
                    .severity("High")
                    .mitigations(List.of("Fallback mode"))
                    .status("open")
                    .build()))
                .safetyRequirements(List.of(ProjectDocumentDto.Step6LossScenariosDto.SafetyRequirementDto.builder()
                    .id(1L)
                    .title("SR-1")
                    .linkedScenario(1L)
                    .category("functional")
                    .owner("Safety Team")
                    .dueDate(LocalDate.of(2026, 7, 1))
                    .status("planned")
                    .build()))
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .when()
            .get("/api/projects/step_six_project_information/" + projectId)
            .then()
            .statusCode(200)
            .body("lossScenarios[0].uca", equalTo("UCA-1"))
            .body("safetyRequirements[0].title", equalTo("SR-1"));
    }

    @Test
    void stepOneUpdateReturns422WhenExistingStepThreeControllerIsNotDefinedInSystemComponents() {
        long projectId = 1L;

        ProjectDocumentDto document = ProjectDocumentDto.builder()
            .project(ProjectDocumentDto.ProjectDto.builder()
                .id(projectId)
                .name("Validation Project")
                .build())
            .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                .systemComponents(List.of(
                    ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                        .id(1L)
                        .name("Autopilot")
                        .description("Navigation logic")
                        .build(),
                    ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                        .id(2L)
                        .name("Operator")
                        .description("Mission supervision")
                        .build()
                ))
                .build())
            .step3ControlStructure(ProjectDocumentDto.Step3ControlStructureDto.builder()
                .controlActions(List.of(ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                    .id(1L)
                    .controller("Autopilot")
                    .action("Adjust heading")
                    .controlledProcess("Trajectory")
                    .feedback("Telemetry")
                    .build()))
                .build())
            .build();

        projectDocumentService.saveOrUpdate(projectId, document);

        given()
            .contentType("application/json")
            .body("""
                {
                  "id": 1,
                  "systemComponents": [
                    {"id": 2, "name": "Operator", "description": "Mission supervision"}
                  ]
                }
                """)
            .when()
            .post("/api/projects/step_one_project_update")
            .then()
            .statusCode(422)
            .body("invalidControllers[0]", equalTo("Autopilot"));
    }

        @Test
        void stepThreeProjectUpdateEndpointPersistsMappedData() {
                long projectId = 1L;

                ProjectDocumentDto document = ProjectDocumentDto.builder()
                        .project(ProjectDocumentDto.ProjectDto.builder()
                                .id(projectId)
                                .name("Step Three Update Project")
                                .build())
                        .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                                .systemComponents(List.of(
                                        ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                                .id(1L)
                                                .name("Autopilot")
                                                .description("Navigation logic")
                                                .build(),
                                        ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                                .id(2L)
                                                .name("Operator")
                                                .description("Mission supervision")
                                                .build()
                                ))
                                .build())
                        .build();
                projectDocumentService.saveOrUpdate(projectId, document);

                given()
                        .contentType("application/json")
                        .body("""
                                {
                                    "id": 1,
                                    "step3Information": {
                                        "entities": [
                                            {"id":"ent-1","name":"Autopilot","roles":["Controller"]},
                                            {"id":"ent-2","name":"Drone trajectory","roles":["Controlled Process"]},
                                            {"id":"ent-3","name":"Operator","roles":["Controller"]},
                                            {"id":"ent-4","name":"Mission state","roles":["Controlled Process"]}
                                        ],
                                        "controlActions": [
                                            {"id":"1","ref":"CA-01","action":"Adjust heading","sourceEntityId":"ent-1","targetEntityId":"ent-2","responsibilityId":""},
                                            {"id":"2","ref":"CA-02","action":"Pause mission","sourceEntityId":"ent-3","targetEntityId":"ent-4","responsibilityId":""}
                                        ],
                                        "optionalElements": [
                                            {"id":"local-optional-element-2","type":"Control Algorithm","name":"testee","sourceKind":"entity","sourceEntityId":"ent-2","sourceExternalId":null,"destinationKind":"entity","destinationEntityId":"ent-2","destinationExternalId":null,"responsibilityId":"1"},
                                            {"id":"1","type":"Feedback","name":"Distance to obstacle","sourceKind":"external","sourceEntityId":null,"sourceExternalId":"ext-1","destinationKind":"entity","destinationEntityId":"ent-1","destinationExternalId":null,"responsibilityId":""}
                                        ]
                                    }
                                }
                                """)
                        .when()
                        .post("/api/projects/step_three_project_update")
                        .then()
                        .statusCode(200);

                given()
                        .when()
                        .get("/api/projects/step_three_project_information/" + projectId)
                        .then()
                        .statusCode(200)
                        .body("controlActions[0].controller", equalTo("Autopilot"))
                        .body("controlActions[1].controller", equalTo("Operator"))
                        .body("controlActions[0].controlledProcess", equalTo("Drone trajectory"))
                        .body("optionalElements[0].type", equalTo("Control Algorithm"))
                        .body("optionalElements[0].name", equalTo("testee"))
                        .body("feedbackLoops[0].source", equalTo("ext-1"))
                        .body("feedbackLoops[0].destination", equalTo("Autopilot"))
                        .body("feedbackLoops[0].signal", equalTo("Distance to obstacle"));
        }

        @Test
        void stepThreeProjectUpdateReturns422WhenControllerNotInStepOne() {
                long projectId = 1L;

                ProjectDocumentDto document = ProjectDocumentDto.builder()
                        .project(ProjectDocumentDto.ProjectDto.builder()
                                .id(projectId)
                                .name("Step Three Validation")
                                .build())
                        .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                                .systemComponents(List.of(ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                        .id(1L)
                                        .name("Operator")
                                        .description("Mission supervision")
                                        .build()))
                                .build())
                        .build();
                projectDocumentService.saveOrUpdate(projectId, document);

                given()
                        .contentType("application/json")
                        .body("""
                                {
                                    "id": 1,
                                    "step3Information": {
                                        "entities": [
                                            {"id":"ent-1","name":"Autopilot","roles":["Controller"]},
                                            {"id":"ent-2","name":"Drone trajectory","roles":["Controlled Process"]}
                                        ],
                                        "controlActions": [
                                            {"id":"1","ref":"CA-01","action":"Adjust heading","sourceEntityId":"ent-1","targetEntityId":"ent-2","responsibilityId":""}
                                        ],
                                        "optionalElements": []
                                    }
                                }
                                """)
                        .when()
                        .post("/api/projects/step_three_project_update")
                        .then()
                        .statusCode(422)
                        .body("invalidControllers[0]", equalTo("Autopilot"));
        }

                @Test
                void stepThreeProjectUpdateNormalizesOptionalTypeAndKind() {
                    long projectId = 1L;

                    ProjectDocumentDto document = ProjectDocumentDto.builder()
                        .project(ProjectDocumentDto.ProjectDto.builder()
                            .id(projectId)
                            .name("Step Three Optional Type Normalization")
                            .build())
                        .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                            .systemComponents(List.of(ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                .id(1L)
                                .name("Autopilot")
                                .description("Navigation logic")
                                .build()))
                            .build())
                        .build();
                    projectDocumentService.saveOrUpdate(projectId, document);

                    given()
                        .contentType("application/json")
                        .body("""
                            {
                              "id": 1,
                              "step3Information": {
                                "entities": [
                                  {"id":"ent-1","name":"Autopilot","roles":["Controller"]},
                                  {"id":"ent-2","name":"Operator","roles":["Controlled Process"]}
                                ],
                                "controlActions": [
                                  {"id":"1","ref":"CA-01","action":"Adjust heading","sourceEntityId":"ent-1","targetEntityId":"ent-2","responsibilityId":""}
                                ],
                                "optionalElements": [
                                  {"id":"opt-1","type":"control algorithm","name":"Rule set","sourceKind":"Entity","sourceEntityId":"ent-1","sourceExternalId":null,"destinationKind":"External","destinationEntityId":null,"destinationExternalId":"ext-1","responsibilityId":"1"}
                                ]
                              }
                            }
                            """)
                        .when()
                        .post("/api/projects/step_three_project_update")
                        .then()
                        .statusCode(200);

                    given()
                        .when()
                        .get("/api/projects/step_three_project_information/" + projectId)
                        .then()
                        .statusCode(200)
                        .body("optionalElements[0].type", equalTo("Control Algorithm"))
                        .body("optionalElements[0].sourceKind", equalTo("entity"))
                        .body("optionalElements[0].destinationKind", equalTo("external"));
                }

                @Test
                void stepThreeProjectUpdateReturns422ForInvalidOptionalType() {
                    long projectId = 1L;

                    ProjectDocumentDto document = ProjectDocumentDto.builder()
                        .project(ProjectDocumentDto.ProjectDto.builder()
                            .id(projectId)
                            .name("Step Three Optional Type Validation")
                            .build())
                        .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                            .systemComponents(List.of(ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                .id(1L)
                                .name("Autopilot")
                                .description("Navigation logic")
                                .build()))
                            .build())
                        .build();
                    projectDocumentService.saveOrUpdate(projectId, document);

                    given()
                        .contentType("application/json")
                        .body("""
                            {
                              "id": 1,
                              "step3Information": {
                                "entities": [
                                  {"id":"ent-1","name":"Autopilot","roles":["Controller"]},
                                  {"id":"ent-2","name":"Operator","roles":["Controlled Process"]}
                                ],
                                "controlActions": [
                                  {"id":"1","ref":"CA-01","action":"Adjust heading","sourceEntityId":"ent-1","targetEntityId":"ent-2","responsibilityId":""}
                                ],
                                "optionalElements": [
                                  {"id":"opt-1","type":"Unknown Optional Type","name":"x","sourceKind":"entity","sourceEntityId":"ent-1","sourceExternalId":null,"destinationKind":"entity","destinationEntityId":"ent-2","destinationExternalId":null,"responsibilityId":"1"}
                                ]
                              }
                            }
                            """)
                        .when()
                        .post("/api/projects/step_three_project_update")
                        .then()
                        .statusCode(422)
                        .body("message", equalTo("Invalid optionalElements.type: Unknown Optional Type"));
                }

                @Test
                void stepThreeExportJsonEndpointReturnsDownloadableData() {
                    long projectId = 1L;

                    ProjectDocumentDto document = ProjectDocumentDto.builder()
                        .project(ProjectDocumentDto.ProjectDto.builder()
                            .id(projectId)
                            .name("Step Three Export JSON")
                            .build())
                        .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                            .systemComponents(List.of(ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                .id(1L)
                                .name("Autopilot")
                                .description("Navigation logic")
                                .build()))
                            .build())
                        .step3ControlStructure(ProjectDocumentDto.Step3ControlStructureDto.builder()
                            .controlActions(List.of(ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                                .id(1L)
                                .controller("Autopilot")
                                .action("Adjust heading")
                                .controlledProcess("Operator")
                                .build()))
                            .build())
                        .build();
                    projectDocumentService.saveOrUpdate(projectId, document);

                    given()
                        .when()
                        .get("/api/projects/step_three_project_export/" + projectId + "/json")
                        .then()
                        .statusCode(200)
                        .header("Content-Disposition", containsString("step3-control-structure.json"))
                        .body("controlActions[0].action", equalTo("Adjust heading"));
                }

                @Test
                void stepThreeExportImageEndpointReturnsSvg() {
                    long projectId = 1L;

                    ProjectDocumentDto document = ProjectDocumentDto.builder()
                        .project(ProjectDocumentDto.ProjectDto.builder()
                            .id(projectId)
                            .name("Step Three Export SVG")
                            .build())
                        .step1Scope(ProjectDocumentDto.Step1ScopeDto.builder()
                            .systemComponents(List.of(ProjectDocumentDto.Step1ScopeDto.SystemComponentDto.builder()
                                .id(1L)
                                .name("Autopilot")
                                .description("Navigation logic")
                                .build()))
                            .build())
                        .step3ControlStructure(ProjectDocumentDto.Step3ControlStructureDto.builder()
                            .controlActions(List.of(ProjectDocumentDto.Step3ControlStructureDto.ControlActionDto.builder()
                                .id(1L)
                                .controller("Autopilot")
                                .action("Adjust heading")
                                .controlledProcess("Operator")
                                .build()))
                            .feedbackLoops(List.of(ProjectDocumentDto.Step3ControlStructureDto.FeedbackLoopDto.builder()
                                .id(1L)
                                .source("Sensor")
                                .destination("Autopilot")
                                .signal("Distance")
                                .latency("<100ms")
                                .build()))
                            .build())
                        .build();
                    projectDocumentService.saveOrUpdate(projectId, document);

                    given()
                        .when()
                        .get("/api/projects/step_three_project_export/" + projectId + "/image")
                        .then()
                        .statusCode(200)
                        .contentType(containsString("image/svg+xml"))
                        .header("Content-Disposition", containsString("step3-control-structure.svg"))
                        .body(containsString("<svg"))
                        .body(containsString("System Safety Control Structure"));
                }

}