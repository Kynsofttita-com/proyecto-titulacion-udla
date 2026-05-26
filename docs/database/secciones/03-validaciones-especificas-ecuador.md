# 3. Validaciones específicas Ecuador

[← Volver al índice](../schema.md)

> Validaciones de formato aplicadas a campos con semántica ecuatoriana (cédula, RUC, placa, teléfono, etc.).

---

## Constraints a nivel BD

Validaciones implementadas con `CHECK` constraints. La validación completa (incluyendo dígito verificador de cédula) se ejecuta en backend con custom validators (`@CedulaEcuador`, `@PlacaEcuador`, etc.).

| Dato | Constraint BD |
|------|---------------|
| **Cédula** | `LENGTH(cedula) = 10 AND cedula ~ '^[0-9]{10}$'` |
| **RUC** | `LENGTH(ruc) = 13 AND ruc ~ '^[0-9]{13}$'` |
| **Placa vehículo** | `placa ~ '^[A-Z]{3}-[0-9]{4}$\|^[A-Z]{2}-[0-9]{4}[A-Z]$'` |
| **Email** | `email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'` |
| **Teléfono móvil** | `LENGTH(telefono) = 10 AND telefono ~ '^09[0-9]{8}$'` |
| **Monto USD** | `monto > 0` (validación de positividad) |

> El **dígito verificador de cédula** (algoritmo módulo 10) se valida en backend, no en BD (lógica compleja no expresable como CHECK).

---

## Formatos válidos por tipo

| Dato | Formato esperado | Ejemplo |
|------|------------------|---------|
| Cédula | 10 dígitos con dígito verificador módulo 10 | `1712345678` |
| RUC persona natural | 10 dígitos de cédula + `001` | `1712345678001` |
| RUC empresa | 13 dígitos terminando en `001` | `1791234567001` |
| Placa auto | 3 letras + guion + 4 dígitos | `ABC-1234` |
| Placa moto | 2 letras + guion + 4 dígitos + 1 letra | `AB-1234A` |
| Teléfono móvil | 10 dígitos iniciando con `09` | `0991234567` |
| Teléfono fijo Quito | 9 dígitos iniciando con `02` | `022345678` |
| Moneda | USD `NUMERIC(10,2)` formato `$1,234.56` | `1500.00` |
| Licencia conducir | Lista cerrada | A, A1, B, C, C1, D, D1, E, F, PROFESIONAL |

---

## Implementación

- **Backend:** Custom validators en `common-validation` (`@CedulaEcuador`, `@PlacaEcuador`, `@RucEcuador`, `@TelefonoEcuador`).
- **Frontend:** Funciones en `utils/validators.ts` + integración con VeeValidate + Yup.
- **BD:** Constraints `CHECK` para validación final defensiva.
