INSERT INTO project (id, name, description, status, created_at, updated_at)
VALUES (
  1,
  'Chapter Case Study Project',
  'Complete STPA chapter case for application',
  'PENDING',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO project_document (project_id, document, created_at, updated_at)
VALUES (
  1,
  '{
    "project": {
      "id": 1,
      "name": "Chapter Case Study Project",
      "domain": "Aerospace",
      "owner": "AutoRESafety",
      "description": "Complete STPA chapter case for application",
      "status": "PENDING",
      "currentStep": 7,
      "createdAt": "2026-02-08T12:00:00Z",
      "updatedAt": "2026-06-09T10:00:00Z"
    },
    "step1Scope": {
      "lastUpdatedBy": "admin",
      "generalSummary": {
        "assumptions": "Assume nominal operating conditions",
        "systemDefinition": "Autonomous inspection drone system",
        "systemBoundary": "Drone + control station + cloud telemetry",
        "outOfScope": "Manufacturing process and supply chain"
      },
      "objectives": "Prevent collision",
      "resources": [
        {
          "id": 1,
          "name": "Flight manual",
          "category": "documentation",
          "reference": "DOC-001",
          "sourceType": "manual"
        }
      ],
      "systemComponents": [
        {
          "id": 1,
          "name": "Autopilot",
          "description": "Executes autonomous navigation logic"
        },
        {
          "id": 2,
          "name": "Operator",
          "description": "Human operator issuing supervisory commands"
        }
      ],
      "accidents": [
        {
          "id": 1,
          "code": "A-1",
          "description": "Drone collision with obstacle"
        }
      ],
      "hazards": [
        {
          "id": 1,
          "code": "H-1",
          "description": "Loss of obstacle detection",
          "linkedAccidents": ["A-1"]
        }
      ],
      "safetyConstraints": [
        {
          "id": 1,
          "code": "SC-1",
          "statement": "The drone shall maintain safe separation from obstacles",
          "linkedHazards": ["H-1"]
        }
      ],
      "responsibilities": [
        {
          "id": 1,
          "component": "Navigation module",
          "responsibility": "Maintain separation logic",
          "linkedConstraints": ["SC-1"]
        }
      ],
      "artefacts": [
        {
          "id": 1,
          "name": "System architecture",
          "purpose": "Describe components",
          "reference": "ARCH-001"
        }
      ]
    },
    "step2Istar": {
      "actors": [
        {
          "id": 1,
          "name": "Operator",
          "type": "human",
          "responsibilities": ["Start mission", "Monitor telemetry"]
        },
        {
          "id": 2,
          "name": "Autopilot",
          "type": "software",
          "responsibilities": ["Path planning", "Obstacle avoidance"]
        }
      ],
      "goalLinks": [
        {
          "id": 1,
          "fromActor": "Operator",
          "goal": "Maintain safe mission execution",
          "linkType": "delegates"
        },
        {
          "id": 2,
          "fromActor": "Autopilot",
          "goal": "Avoid obstacles in real time",
          "linkType": "achieves"
        }
      ]
    },
    "step3ControlStructure": {
      "controlActions": [
        {
          "id": 1,
          "controller": "Autopilot",
          "action": "Adjust heading",
          "controlledProcess": "",
          "feedback": "IMU + lidar"
        },
        {
          "id": 2,
          "controller": "Autopilot",
          "action": "Pause mission",
          "controlledProcess": "Operator",
          "feedback": "Ground station UI"
        }
      ],
      "feedbackLoops": [
        {
          "id": 1,
          "source": "Lidar",
          "destination": "Autopilot",
          "signal": "Distance to obstacle",
          "latency": "<100ms"
        }
      ]
    },
    "step4Ucas": {
      "ucas": [
        {
          "id": 1,
          "controller": "Autopilot",
          "controlAction": "Adjust heading",
          "hazard": "H-1",
          "category": "Not provided"
        },
        {
          "id": 2,
          "controller": "Operator",
          "controlAction": "Pause mission",
          "hazard": "H-1",
          "category": "Provided too late"
        }
      ]
    },
    "step5ControllerConstraints": {
      "constraints": [
        {
          "id": 1,
          "ucaRef": "UCA-1",
          "constraint": "Autopilot shall command evasive heading when obstacle distance is below threshold",
          "enforcementMechanism": "Runtime rule in navigation loop",
          "status": "defined"
        }
      ]
    },
    "step6LossScenarios": {
      "lossScenarios": [
        {
          "id": 1,
          "uca": "UCA-1",
          "hazard": "H-1",
          "outcome": "Drone impacts static obstacle",
          "severity": "High",
          "mitigations": ["Add sensor health check", "Fallback to hover"],
          "status": "open"
        }
      ],
      "safetyRequirements": [
        {
          "id": 1,
          "title": "SR-1 Maintain safe obstacle clearance",
          "linkedScenario": 1,
          "category": "functional",
          "owner": "Safety team",
          "dueDate": "2026-07-15",
          "status": "planned"
        }
      ]
    },
    "step7ModelUpdate": {
      "modelChanges": [
        {
          "id": 1,
          "area": "Navigation",
          "change": "Added obstacle-clearance guard before heading update",
          "driver": "UCA-1",
          "impact": "Reduces collision likelihood",
          "status": "implemented",
          "evidence": ["SIM-REP-101", "TEST-LOG-224"]
        }
      ],
      "validationTasks": [
        {
          "id": 1,
          "name": "Hardware-in-loop obstacle test",
          "owner": "QA",
          "dueDate": "2026-07-20",
          "channel": "test-lab",
          "status": "queued"
        }
      ],
      "integrationNotes": [
        {
          "id": 1,
          "summary": "Navigation and telemetry schemas aligned",
          "createdOn": "2026-06-09",
          "author": "System Architect",
          "actionItems": ["Run regression pack", "Update ops playbook"]
        }
      ]
    }
  }'::jsonb,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON CONFLICT (project_id) DO UPDATE
SET document = EXCLUDED.document,
    updated_at = CURRENT_TIMESTAMP;

SELECT setval(
  pg_get_serial_sequence('project', 'id'),
  (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM project),
  true
);