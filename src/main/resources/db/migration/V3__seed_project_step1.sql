INSERT INTO project (id, name, description, status, created_at, updated_at)
VALUES (2, 'Example Project', 'Step 1 sample', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO project_document (project_id, document, created_at, updated_at)
VALUES (
  2,
  '{
    "project": {
      "id": 2,
      "name": "Example Project",
      "domain": "Aerospace",
      "owner": "AutoRESafety",
      "description": "Step 1 sample",
      "status": "PENDING",
      "currentStep": 1,
      "createdAt": "2026-02-08T12:00:00Z",
      "updatedAt": "2026-02-08T12:00:00Z"
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
          "name": "Onboard controller",
          "description": "Executes navigation logic"
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
    }
  }'::jsonb,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON CONFLICT (project_id) DO UPDATE
SET document = EXCLUDED.document,
    updated_at = CURRENT_TIMESTAMP;