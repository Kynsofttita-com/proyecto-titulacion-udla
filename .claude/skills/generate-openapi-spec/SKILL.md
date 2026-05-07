---
name: generate-openapi-spec
description: Generate or update OpenAPI 3.0 specification YAML for a microservice's REST API. Includes endpoints, request/response schemas, security definitions (Bearer JWT), error responses (RFC 7807 Problem Details), pagination, examples, and tags. Output is consumable by Swagger UI, Postman, and code generators.
---

# Generate OpenAPI Spec Skill

Creates OpenAPI 3.0 specifications for documenting REST APIs.

## Inputs Needed

Ask the user for:
1. **Service**: which microservice
2. **Source**: generate from existing controllers OR write from scratch (design-first)
3. **Endpoints**: list with paths and methods
4. **Domain types**: entities and DTOs

## Output

Saved to: `docs/api/<service>-openapi.yaml`

Each microservice has its own spec file. The API Gateway aggregates them.

## Template

```yaml
openapi: 3.0.3

info:
  title: MS-<Context> API
  description: |
    API para gestión de <context> en el sistema de control administrativo de escuelas de conducción.
    
    ## Authentication
    All endpoints (except `/auth/*`) require JWT Bearer token.
    
    ## Errors
    Errors follow [RFC 7807 Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807).
    
    ## Rate Limiting
    100 requests/second per user. See `X-RateLimit-*` headers.
  version: 1.0.0
  contact:
    name: Equipo Kynsoft
    email: dev@kynsoft.com
  license:
    name: Proprietary

servers:
  - url: http://localhost:8080
    description: Local development (via API Gateway)
  - url: https://dev-api.proyecto.local
    description: Development
  - url: https://api.proyecto.local
    description: Production

tags:
  - name: <Resource>
    description: Operations on <resources>

security:
  - BearerAuth: []

paths:
  /v1/<resources>:
    get:
      tags: [<Resource>]
      summary: List <resources>
      description: Returns paginated list of <resources>, with optional filters
      operationId: list<Resources>
      parameters:
        - $ref: '#/components/parameters/PageParam'
        - $ref: '#/components/parameters/SizeParam'
        - $ref: '#/components/parameters/SortParam'
        - name: estado
          in: query
          description: Filter by estado
          required: false
          schema:
            $ref: '#/components/schemas/Estado<Resource>'
      responses:
        '200':
          description: Page of <resources>
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Page<Resource>Response'
              examples:
                default:
                  $ref: '#/components/examples/Page<Resource>Example'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'
    
    post:
      tags: [<Resource>]
      summary: Create new <resource>
      description: Registers a new <resource> in the system
      operationId: create<Resource>
      parameters:
        - $ref: '#/components/parameters/IdempotencyKey'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Create<Resource>Request'
            examples:
              default:
                $ref: '#/components/examples/Create<Resource>Example'
      responses:
        '201':
          description: <Resource> created successfully
          headers:
            Location:
              description: URI of the created <resource>
              schema:
                type: string
                example: /v1/<resources>/123
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/<Resource>Response'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '409':
          $ref: '#/components/responses/Conflict'

  /v1/<resources>/{id}:
    parameters:
      - name: id
        in: path
        required: true
        description: Unique identifier of the <resource>
        schema:
          type: integer
          format: int64
          minimum: 1
        example: 123
    
    get:
      tags: [<Resource>]
      summary: Get <resource> by ID
      operationId: get<Resource>ById
      responses:
        '200':
          description: <Resource> details
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/<Resource>Response'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          $ref: '#/components/responses/NotFound'
    
    put:
      tags: [<Resource>]
      summary: Update <resource>
      operationId: update<Resource>
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Update<Resource>Request'
      responses:
        '200':
          description: Updated <resource>
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/<Resource>Response'
        '400':
          $ref: '#/components/responses/BadRequest'
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          $ref: '#/components/responses/Conflict'
    
    delete:
      tags: [<Resource>]
      summary: Delete <resource>
      operationId: delete<Resource>
      responses:
        '204':
          description: <Resource> deleted
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          $ref: '#/components/responses/NotFound'

components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT token from `/v1/auth/login`

  schemas:
    # ===== Core entity =====
    <Resource>Response:
      type: object
      required: [id, cedula, nombres, apellidos, email, fechaNacimiento, estado]
      properties:
        id:
          type: integer
          format: int64
          example: 123
        cedula:
          type: string
          pattern: '^[0-9]{10}$'
          example: '1712345678'
        nombres:
          type: string
          minLength: 2
          maxLength: 100
          example: 'Juan Carlos'
        apellidos:
          type: string
          minLength: 2
          maxLength: 100
          example: 'Pérez González'
        email:
          type: string
          format: email
          example: 'juan.perez@example.com'
        telefono:
          type: string
          pattern: '^0[0-9]{9}$'
          nullable: true
          example: '0987654321'
        fechaNacimiento:
          type: string
          format: date
          example: '2000-01-15'
        estado:
          $ref: '#/components/schemas/Estado<Resource>'
        createdAt:
          type: string
          format: date-time
          readOnly: true
          example: '2026-05-06T10:30:00-05:00'
        updatedAt:
          type: string
          format: date-time
          readOnly: true
          example: '2026-05-06T10:30:00-05:00'

    Estado<Resource>:
      type: string
      enum: [ACTIVO, INACTIVO, GRADUADO, RETIRADO]
      example: ACTIVO

    Create<Resource>Request:
      type: object
      required: [cedula, nombres, apellidos, email, fechaNacimiento]
      properties:
        cedula:
          type: string
          pattern: '^[0-9]{10}$'
          example: '1712345678'
        nombres:
          type: string
          minLength: 2
          maxLength: 100
          example: 'Juan Carlos'
        apellidos:
          type: string
          minLength: 2
          maxLength: 100
          example: 'Pérez González'
        email:
          type: string
          format: email
        telefono:
          type: string
          pattern: '^0[0-9]{9}$'
        fechaNacimiento:
          type: string
          format: date

    Update<Resource>Request:
      type: object
      properties:
        nombres:
          type: string
          minLength: 2
          maxLength: 100
        apellidos:
          type: string
          minLength: 2
          maxLength: 100
        email:
          type: string
          format: email
        telefono:
          type: string
          pattern: '^0[0-9]{9}$'

    # ===== Pagination =====
    Page<Resource>Response:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/<Resource>Response'
        page:
          type: integer
          minimum: 0
          example: 0
        size:
          type: integer
          minimum: 1
          example: 20
        totalElements:
          type: integer
          format: int64
          example: 1234
        totalPages:
          type: integer
          example: 62
        first:
          type: boolean
        last:
          type: boolean

    # ===== Errors (RFC 7807) =====
    ProblemDetail:
      type: object
      properties:
        type:
          type: string
          format: uri
          example: 'https://api.proyecto.local/errors/validation-error'
        title:
          type: string
          example: 'Validation failed'
        status:
          type: integer
          example: 400
        detail:
          type: string
          example: 'The request contains invalid data'
        instance:
          type: string
          format: uri
          example: '/v1/<resources>'
        traceId:
          type: string
          example: 'abc123def456'
        errors:
          type: array
          items:
            $ref: '#/components/schemas/ValidationError'

    ValidationError:
      type: object
      required: [field, message]
      properties:
        field:
          type: string
          example: 'cedula'
        code:
          type: string
          example: 'INVALID_FORMAT'
        message:
          type: string
          example: 'Cédula must be 10 digits'

  parameters:
    PageParam:
      name: page
      in: query
      description: Page number (zero-based)
      required: false
      schema:
        type: integer
        minimum: 0
        default: 0
    SizeParam:
      name: size
      in: query
      description: Page size
      required: false
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 20
    SortParam:
      name: sort
      in: query
      description: Sort field and direction (e.g., `apellidos,asc`)
      required: false
      schema:
        type: array
        items:
          type: string
        example: ['apellidos,asc']
    IdempotencyKey:
      name: Idempotency-Key
      in: header
      description: Unique key to ensure idempotency for safe retries (UUID recommended)
      required: false
      schema:
        type: string
        format: uuid

  responses:
    BadRequest:
      description: Validation error
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'
    Unauthorized:
      description: Missing or invalid authentication token
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'
    Forbidden:
      description: Insufficient permissions
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'
    NotFound:
      description: Resource not found
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'
    Conflict:
      description: Resource conflict (e.g., duplicate)
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'
    TooManyRequests:
      description: Rate limit exceeded
      headers:
        Retry-After:
          schema:
            type: integer
          description: Seconds to wait before retry
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/ProblemDetail'

  examples:
    Create<Resource>Example:
      value:
        cedula: '1712345678'
        nombres: 'Juan Carlos'
        apellidos: 'Pérez González'
        email: 'juan.perez@example.com'
        telefono: '0987654321'
        fechaNacimiento: '2000-01-15'
    
    Page<Resource>Example:
      value:
        content:
          - id: 123
            cedula: '1712345678'
            nombres: 'Juan Carlos'
            apellidos: 'Pérez González'
            email: 'juan.perez@example.com'
            telefono: '0987654321'
            fechaNacimiento: '2000-01-15'
            estado: 'ACTIVO'
            createdAt: '2026-05-06T10:30:00-05:00'
            updatedAt: '2026-05-06T10:30:00-05:00'
        page: 0
        size: 20
        totalElements: 234
        totalPages: 12
        first: true
        last: false
```

## Workflow

1. **Identify** all endpoints in the controller
2. **Read** existing OpenAPI spec to match conventions (or use template if new)
3. **Document** each endpoint:
   - Path + method
   - Tags
   - Summary + description
   - Parameters (path, query, header)
   - Request body schema
   - Response schemas (per status code)
4. **Define** schemas in `components/schemas`
5. **Reference** common parameters/responses (don't duplicate)
6. **Add** examples for each endpoint
7. **Validate** spec:
   ```bash
   npx @apidevtools/swagger-cli validate docs/api/<service>-openapi.yaml
   ```
8. **Render** in Swagger UI for visual review

## Auto-Generation from Spring Annotations

Alternative: SpringDoc generates spec automatically from `@Operation`, `@ApiResponse` annotations.

```bash
# Run service
mvn spring-boot:run

# Export spec
curl http://localhost:8081/v3/api-docs.yaml > docs/api/ms-<service>-openapi.yaml
```

For **design-first** approach (recommended), write spec manually then implement controllers.
For **code-first**, annotate controllers and export.

## Quality Checklist

- [ ] All paths documented with summary + description
- [ ] All status codes documented (200, 201, 400, 401, 403, 404, 409, 500)
- [ ] All schemas have examples
- [ ] Common responses (BadRequest, Unauthorized) referenced
- [ ] Pagination params standardized
- [ ] Security defined (BearerAuth)
- [ ] Error response uses ProblemDetail (RFC 7807)
- [ ] Idempotency-Key header on POST for financial ops
- [ ] Tags grouped by domain
- [ ] Spec validates without errors

## Tools

- **Validate**: `npx @apidevtools/swagger-cli validate file.yaml`
- **Lint**: Spectral (`npx @stoplight/spectral lint file.yaml`)
- **View**: Swagger UI (`http://localhost:8081/swagger-ui.html`)
- **Generate clients**: `openapi-generator-cli generate -i file.yaml -g typescript-axios`
- **Mock server**: Prism (`npx @stoplight/prism mock file.yaml`)

## Notes

- One spec per microservice (don't merge into one giant file)
- Use `$ref` aggressively to avoid duplication
- Spec is the source of truth; code follows the spec (design-first)
- Update spec BEFORE implementing changes (avoid drift)
- Examples make documentation 10x more useful — always include them
- Use `nullable: true` for optional fields (instead of omitting `required`)
