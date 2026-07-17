# Contributing — Guía para Contribuidores

Gracias por tu interés en contribuir al proyecto de titulación. Esta guía te ayudará a colaborar de manera efectiva.

## 📋 Requisitos previos

- Leer [CLAUDE.md](./CLAUDE.md) (contexto técnico)
- Leer [DECISIONES.md](./DECISIONES.md) (decisiones arquitectónicas)
- Conocer [Git Flow](https://datasift.github.io/gitflow/IntroducingGitFlow.html)

## 🚀 Flujo de trabajo

### 1. Crear rama de trabajo

```bash
git checkout main
git pull origin main

# Crear rama descriptiva
git checkout -b feature/descripcion-breve
# o
git checkout -b fix/descripcion-del-bug
# o
git checkout -b docs/actualizacion-documentacion
```

**Convención de nombres:**
- `feature/*` — Nueva funcionalidad
- `fix/*` — Corrección de bug
- `docs/*` — Documentación
- `refactor/*` — Refactorización
- `test/*` — Tests

### 2. Hacer cambios locales

```bash
# Editar archivos
nano archivo.java
# o usar tu IDE

# Verificar cambios
git status
git diff
```

**Convenciones de código:**
- **Java**: PascalCase (clases), camelCase (métodos)
- **Vue**: kebab-case (componentes), camelCase (JS)
- **BD**: snake_case (tablas/columnas)
- **Indentación**: 4 espacios (Java), 2 espacios (Vue)

### 3. Ejecutar tests locales

```bash
# Backend (Java)
cd backend
mvn clean test

# Frontend (Vue)
cd frontend
npm run test

# Todos
mvn verify
npm run test:e2e
```

**Requisito:** 80%+ cobertura de tests

### 4. Commit con mensaje descriptivo

```bash
# Formato: <tipo>: <descripción corta>
git commit -m "feat: agregar validación de SOAT en asignaciones"
git commit -m "fix: corregir desfase de 5 horas en timestamps"
git commit -m "docs: actualizar README con instrucciones Docker"

# Tipos válidos:
# - feat: Nueva funcionalidad
# - fix: Corrección de bug
# - docs: Cambios en documentación
# - refactor: Refactorización (sin cambiar behavior)
# - test: Agregar o actualizar tests
# - chore: Cambios en build, deps, etc.
```

### 5. Push y crear Pull Request

```bash
# Push a origin
git push -u origin feature/descripcion-breve

# Crear PR via GitHub CLI
gh pr create --title "Descripción breve" --body "
## Descripción
Explicar QUÉ se hizo y POR QUÉ.

## Testing
- [ ] Tests unitarios pasados
- [ ] Tests de integración pasados
- [ ] Probado en Docker localmente

## Checklist
- [ ] Código formateado (lint/prettier)
- [ ] Sin code smells (SonarQube)
- [ ] Documentación actualizada
"
```

### 6. Revisión y merge

- Esperar code review
- Responder comentarios
- Hacer ajustes si es necesario
- Merge automático después de approval

---

## 📝 Estilos de código

### Java (Spring Boot)

```java
// ✅ Bien
public class StudentService {
    public ResponseEntity<StudentDTO> getStudent(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));
        return ResponseEntity.ok(mapper.toDTO(student));
    }
}

// ❌ Mal
public class student_service {  // Nombre incorrecto
    public ResponseEntity getStudent(Long id) {  // Sin tipo genérico
        // Lógica compleja sin manejo de errores
    }
}
```

### Vue.js 3

```vue
<!-- ✅ Bien -->
<template>
  <div class="student-form">
    <input v-model="student.name" type="text" />
    <button @click="saveStudent">Guardar</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const student = ref({ name: '', email: '' })

const saveStudent = () => {
  // Implementación
}
</script>

<style scoped>
.student-form {
  padding: 1rem;
}
</style>

<!-- ❌ Mal -->
<template>
  <div>
    <input v-model="name" />
    <button onclick="save()">Guardar</button>
  </div>
</template>
```

### SQL (Migraciones Flyway)

```sql
-- ✅ Bien
CREATE TABLE estudiantes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    cedula VARCHAR(10) UNIQUE NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO',
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ❌ Mal
CREATE TABLE estudiantes (
    id INT,  -- Debe ser BIGSERIAL
    nombre TEXT,  -- Sin restricción de tamaño
    estado VARCHAR(20),  -- Sin DEFAULT
    created TIMESTAMP  -- Sin DEFAULT
);
```

---

## 🧪 Testing

### Requisito: 80%+ cobertura

```bash
# Ver cobertura actual
mvn jacoco:report
open target/site/jacoco/index.html
```

### Ejemplo: Unit Test

```java
@Test
void testCreateAssignmentWithValidData() {
    // Arrange
    AssignmentDTO dto = new AssignmentDTO(1L, 2L, 3L, LocalDateTime.now());
    
    // Act
    AssignmentResponse response = assignmentService.create(dto);
    
    // Assert
    assertNotNull(response.getId());
    assertEquals(1L, response.getInstructorId());
}
```

### Ejemplo: Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
class AssignmentControllerIT {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetAssignment() throws Exception {
        mockMvc.perform(get("/api/asignaciones/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

---

## 📚 Documentación

### Actualizar docs al hacer cambios

**En código:**
```java
/**
 * Crea una nueva asignación (instructor + estudiante + vehículo).
 * Valida automáticamente disponibilidad y restricciones cross-MS.
 *
 * @param dto DTO con instructor_id, student_id, vehicle_id
 * @return AssignmentResponse con ID de asignación creada
 * @throws InstructorNotAvailableException si instructor no está disponible
 * @throws StudentAlreadyHasActiveAssignmentException si estudiante ya tiene clase
 */
public AssignmentResponse create(AssignmentDTO dto) { ... }
```

**En README/docs:**
- Actualizar si cambias arquitectura
- Documentar nuevos endpoints en [docs/api/](./docs/api/)
- Actualizar DECISIONES.md si fue decisión importante

---

## 🔍 Checklist antes de hacer push

- [ ] Código compila sin errores
- [ ] Tests pasan (80%+ cobertura)
- [ ] Lint/formatter pasó (mvn spotless:apply)
- [ ] Sin secrets en el código (.env, keys, passwords)
- [ ] Commit message es descriptivo
- [ ] Rama está actualizada (git pull origin main)
- [ ] Documentación actualizada si aplica

---

## 🐛 Reportar bugs

**Usar GitHub Issues con template:**

```markdown
## Descripción
[Descripción clara del bug]

## Pasos para reproducir
1. ...
2. ...
3. ...

## Comportamiento esperado
[QUÉ debería pasar]

## Comportamiento actual
[QUÉ está pasando]

## Entorno
- OS: Windows/Mac/Linux
- Java: 21
- Docker: sí/no
- Rama: main/feature-X
```

---

## ✨ Mejoras sugeridas

¿Idea para mejorar el proyecto?

1. Crear issue con etiqueta `enhancement`
2. Describir el problema que resolvería
3. Proponer solución (opcional)
4. Aguardar feedback

---

## 🚫 Reglas importantes

**NUNCA:**
- ❌ Commitear credenciales (.env, keys)
- ❌ Hacer force push a main
- ❌ Mezclar múltiples features en un PR
- ❌ Dejar código sin tests
- ❌ Ignorar code review comments

**SIEMPRE:**
- ✅ Crear rama nueva para cada feature
- ✅ Escribir tests para nuevo código
- ✅ Actualizar documentación
- ✅ Respetar convenciones de código
- ✅ Responder a reviews constructivamente

---

## 📞 Contacto

**Autores del proyecto:**
- Raúl Sebastián Cruz Baño
- Hernán Mateo Jurado Moran

**Preguntas técnicas:**
- Crear issue en GitHub
- Contactar tutor: víctor.gómez@udla.edu.ec

---

## 🙏 Agradecimiento

¡Gracias por contribuir al proyecto! Tu aporte es importante.
