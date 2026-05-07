---
name: validate-ecuador-data
description: Generate validators for Ecuador-specific data formats: cédula (10-digit ID with verifier digit), license plates (ABC-1234, AB-1234A), phone numbers, RUC (13-digit tax ID), addresses, and currency formatting. Provides both backend (Java) and frontend (TypeScript) implementations.
---

# Validate Ecuador Data Skill

Generates validation logic for Ecuadorian-specific data formats.

## What This Skill Generates

Validators for:
1. **Cédula** (10-digit ID with check-digit algorithm)
2. **RUC** (13-digit tax identifier)
3. **License plates** (vehicles, motorcycles, special)
4. **Phone numbers** (mobile + landline)
5. **Currency** (USD formatting)
6. **Postal codes** / addresses
7. **Birth dates** (age validation for license types)

## Backend (Java)

### 1. Cédula Validator

```java
package com.kynsoft.shared.validation;

/**
 * Validates Ecuadorian cédula (10-digit ID with verifier digit).
 * 
 * Algorithm:
 * 1. First 2 digits: province code (01-24, except 30+)
 * 2. Third digit: < 6 for natural persons, 6 for foreigners, 9 for businesses
 * 3. Last digit: verifier (check digit calculation)
 */
public final class CedulaValidator {
    
    private static final int[] COEFICIENTES = {2, 1, 2, 1, 2, 1, 2, 1, 2};
    
    private CedulaValidator() {}

    public static boolean isValid(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }

        // Validate province (first 2 digits)
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || (provincia > 24 && provincia != 30)) {
            return false;
        }

        // Validate person type (third digit)
        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito >= 6) {
            return false;  // Not a natural person cédula
        }

        // Calculate verifier digit
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int producto = Character.getNumericValue(cedula.charAt(i)) * COEFICIENTES[i];
            if (producto > 9) producto -= 9;
            suma += producto;
        }

        int decenaSuperior = ((suma + 9) / 10) * 10;
        int digitoVerificadorEsperado = decenaSuperior - suma;
        if (digitoVerificadorEsperado == 10) digitoVerificadorEsperado = 0;
        
        int digitoVerificadorActual = Character.getNumericValue(cedula.charAt(9));
        
        return digitoVerificadorEsperado == digitoVerificadorActual;
    }
}
```

### 2. Custom Bean Validation Annotation

```java
package com.kynsoft.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CedulaConstraintValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCedula {
    String message() default "Cédula ecuatoriana inválida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

```java
public class CedulaConstraintValidator implements ConstraintValidator<ValidCedula, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;  // null is valid (use @NotNull separately)
        return CedulaValidator.isValid(value);
    }
}
```

Usage:
```java
public record CreateStudentRequest(
    @NotBlank
    @ValidCedula
    String cedula,
    
    @NotBlank @Size(min = 2, max = 100)
    String nombres,
    // ...
) {}
```

### 3. Other Validators

```java
public final class RucValidator {
    public static boolean isValid(String ruc) {
        if (ruc == null || !ruc.matches("\\d{13}")) return false;
        
        // RUC = cédula + 001 for natural persons, or different algorithm for businesses
        // Last digit (digit 13) should be 1 for businesses
        // ... full implementation
        
        // For natural persons: first 10 digits = cédula, last 3 = "001"
        if (ruc.endsWith("001")) {
            return CedulaValidator.isValid(ruc.substring(0, 10));
        }
        
        // For businesses (3rd digit = 9): different algorithm
        // (implement full mod-11 algorithm here)
        return false;
    }
}

public final class PlacaValidator {
    private static final String CAR_PATTERN = "^[A-Z]{3}-[0-9]{3,4}$";
    private static final String MOTORCYCLE_PATTERN = "^[A-Z]{2}-[0-9]{4}[A-Z]?$";
    private static final String SPECIAL_PATTERN = "^(GE|ME|CD|CC)-[0-9]{4}$";

    public static boolean isValid(String placa) {
        if (placa == null) return false;
        return placa.matches(CAR_PATTERN) 
            || placa.matches(MOTORCYCLE_PATTERN)
            || placa.matches(SPECIAL_PATTERN);
    }
    
    public static String normalize(String placa) {
        if (placa == null) return null;
        // Add dash if missing: ABC1234 → ABC-1234
        var clean = placa.replaceAll("[^A-Z0-9]", "").toUpperCase();
        if (clean.length() == 7 && clean.matches("[A-Z]{3}[0-9]{4}")) {
            return clean.substring(0, 3) + "-" + clean.substring(3);
        }
        return placa.toUpperCase();
    }
}

public final class TelefonoValidator {
    private static final String MOBILE_PATTERN = "^09[0-9]{8}$";
    private static final String LANDLINE_PATTERN = "^0[2-7][0-9]{7,8}$";

    public static boolean isValidMobile(String phone) {
        return phone != null && phone.matches(MOBILE_PATTERN);
    }

    public static boolean isValidLandline(String phone) {
        return phone != null && phone.matches(LANDLINE_PATTERN);
    }

    public static boolean isValid(String phone) {
        return isValidMobile(phone) || isValidLandline(phone);
    }
}
```

### 4. Currency Formatter

```java
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {
    private static final Locale ECUADOR = new Locale("es", "EC");
    private static final NumberFormat USD_FORMAT;
    
    static {
        USD_FORMAT = NumberFormat.getCurrencyInstance(ECUADOR);
        USD_FORMAT.setMinimumFractionDigits(2);
        USD_FORMAT.setMaximumFractionDigits(2);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return USD_FORMAT.format(amount);
    }
}
```

## Frontend (TypeScript)

### 1. Cédula Validator

```typescript
// src/utils/validators/cedula.ts

const COEFICIENTES = [2, 1, 2, 1, 2, 1, 2, 1, 2]

export function validateCedula(cedula: string): boolean {
  if (!cedula || !/^\d{10}$/.test(cedula)) return false

  // Validate province
  const provincia = parseInt(cedula.substring(0, 2), 10)
  if (provincia < 1 || (provincia > 24 && provincia !== 30)) return false

  // Validate person type
  const tercerDigito = parseInt(cedula.charAt(2), 10)
  if (tercerDigito >= 6) return false

  // Verifier digit calculation
  let suma = 0
  for (let i = 0; i < 9; i++) {
    let producto = parseInt(cedula.charAt(i), 10) * COEFICIENTES[i]
    if (producto > 9) producto -= 9
    suma += producto
  }

  const decenaSuperior = Math.ceil(suma / 10) * 10
  let digitoEsperado = decenaSuperior - suma
  if (digitoEsperado === 10) digitoEsperado = 0

  return digitoEsperado === parseInt(cedula.charAt(9), 10)
}
```

### 2. License Plate Validator

```typescript
// src/utils/validators/placa.ts

const CAR_REGEX = /^[A-Z]{3}-[0-9]{3,4}$/
const MOTORCYCLE_REGEX = /^[A-Z]{2}-[0-9]{4}[A-Z]?$/
const SPECIAL_REGEX = /^(GE|ME|CD|CC)-[0-9]{4}$/

export function validatePlaca(placa: string): boolean {
  if (!placa) return false
  return CAR_REGEX.test(placa) || MOTORCYCLE_REGEX.test(placa) || SPECIAL_REGEX.test(placa)
}

export function normalizePlaca(placa: string): string {
  if (!placa) return ''
  const clean = placa.replace(/[^A-Z0-9]/gi, '').toUpperCase()
  if (/^[A-Z]{3}[0-9]{4}$/.test(clean)) {
    return `${clean.substring(0, 3)}-${clean.substring(3)}`
  }
  return placa.toUpperCase()
}
```

### 3. Phone Validator

```typescript
// src/utils/validators/telefono.ts

const MOBILE_REGEX = /^09\d{8}$/
const LANDLINE_REGEX = /^0[2-7]\d{7,8}$/

export function validateMobile(phone: string): boolean {
  return MOBILE_REGEX.test(phone ?? '')
}

export function validateLandline(phone: string): boolean {
  return LANDLINE_REGEX.test(phone ?? '')
}

export function validatePhone(phone: string): boolean {
  return validateMobile(phone) || validateLandline(phone)
}

export function formatPhone(phone: string): string {
  if (!phone) return ''
  // Format: 0987654321 → 098-765-4321
  if (validateMobile(phone)) {
    return `${phone.substring(0, 3)}-${phone.substring(3, 6)}-${phone.substring(6)}`
  }
  return phone
}
```

### 4. Currency Formatter

```typescript
// src/utils/formatters/currency.ts

const formatter = new Intl.NumberFormat('es-EC', {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

export function formatCurrency(amount: number | string | null | undefined): string {
  if (amount == null) return '$0.00'
  const num = typeof amount === 'string' ? parseFloat(amount) : amount
  if (isNaN(num)) return '$0.00'
  return formatter.format(num)
}

export function parseCurrency(formatted: string): number {
  if (!formatted) return 0
  const cleaned = formatted.replace(/[^\d.-]/g, '')
  return parseFloat(cleaned) || 0
}
```

### 5. Date Formatter

```typescript
// src/utils/formatters/date.ts
import { format, parseISO, differenceInYears } from 'date-fns'
import { es } from 'date-fns/locale'

export function formatDate(date: string | Date, formatStr: string = 'dd/MM/yyyy'): string {
  if (!date) return ''
  const d = typeof date === 'string' ? parseISO(date) : date
  return format(d, formatStr, { locale: es })
}

export function calculateAge(birthDate: string | Date): number {
  if (!birthDate) return 0
  const birth = typeof birthDate === 'string' ? parseISO(birthDate) : birthDate
  return differenceInYears(new Date(), birth)
}

export function isMinimumAge(birthDate: string | Date, minimumAge: number): boolean {
  return calculateAge(birthDate) >= minimumAge
}
```

### 6. License Type Age Check

```typescript
// src/utils/validators/license.ts

export type LicenseType = 'A1' | 'A' | 'B' | 'C1' | 'C' | 'D1' | 'D' | 'E1' | 'E' | 'F' | 'G'

const MINIMUM_AGE: Record<LicenseType, number> = {
  A1: 16,
  A: 18,
  B: 18,
  C1: 18,
  C: 21,
  D1: 21,
  D: 24,
  E1: 24,
  E: 24,
  F: 18,
  G: 21
}

export function getMinimumAgeForLicense(type: LicenseType): number {
  return MINIMUM_AGE[type]
}

export function canApplyForLicense(birthDate: string | Date, licenseType: LicenseType): boolean {
  const age = calculateAge(birthDate)
  return age >= MINIMUM_AGE[licenseType]
}
```

### 7. VeeValidate Integration

```typescript
// src/utils/validators/yup-extensions.ts
import * as yup from 'yup'
import { validateCedula } from './cedula'
import { validatePlaca } from './placa'
import { validatePhone } from './telefono'

yup.addMethod(yup.string, 'cedulaEcuatoriana', function (message: string = 'Cédula inválida') {
  return this.test('cedula', message, (value) => !value || validateCedula(value))
})

yup.addMethod(yup.string, 'placaEcuatoriana', function (message: string = 'Placa inválida') {
  return this.test('placa', message, (value) => !value || validatePlaca(value))
})

yup.addMethod(yup.string, 'telefonoEcuatoriano', function (message: string = 'Teléfono inválido') {
  return this.test('telefono', message, (value) => !value || validatePhone(value))
})

declare module 'yup' {
  interface StringSchema {
    cedulaEcuatoriana(message?: string): this
    placaEcuatoriana(message?: string): this
    telefonoEcuatoriano(message?: string): this
  }
}
```

Usage:
```typescript
import * as yup from 'yup'

const studentSchema = yup.object({
  cedula: yup.string().required('Cédula es requerida').cedulaEcuatoriana(),
  email: yup.string().required('Email es requerido').email('Email inválido'),
  telefono: yup.string().telefonoEcuatoriano(),
  fechaNacimiento: yup.date()
    .required('Fecha de nacimiento requerida')
    .max(new Date(), 'Fecha no puede estar en el futuro')
})
```

## Test Files

```typescript
// __tests__/cedula.test.ts
import { describe, it, expect } from 'vitest'
import { validateCedula } from '../cedula'

describe('validateCedula', () => {
  it('returns true for valid cédula', () => {
    expect(validateCedula('1712345678')).toBe(true)  // valid example
    expect(validateCedula('1714616123')).toBe(true)
  })

  it('returns false for invalid cédula', () => {
    expect(validateCedula('1234567890')).toBe(false)  // wrong verifier
    expect(validateCedula('123')).toBe(false)         // wrong length
    expect(validateCedula('abcdefghij')).toBe(false)  // not digits
    expect(validateCedula('')).toBe(false)
    expect(validateCedula(null as any)).toBe(false)
  })

  it('returns false for invalid province code', () => {
    expect(validateCedula('2512345678')).toBe(false)  // province 25 doesn't exist
    expect(validateCedula('0012345678')).toBe(false)  // province 00 invalid
  })

  it('returns false for company RUC (3rd digit ≥ 6)', () => {
    expect(validateCedula('1712345678')).toBe(true)   // 3rd digit = 1 (natural)
    expect(validateCedula('1791234567')).toBe(false)  // 3rd digit = 9 (business)
  })
})
```

## Workflow

1. **Identify** which validators are needed
2. **Generate** backend Java validators (in `shared` package)
3. **Generate** frontend TypeScript validators (in `src/utils/validators/`)
4. **Generate** Bean Validation annotations for Java DTOs
5. **Generate** Yup extensions for frontend forms
6. **Generate** test files for all validators
7. **Run** tests: `mvn test` (Java) and `npm run test` (TS)
8. **Verify** consistency: same validation logic backend & frontend

## Quality Checklist

- [ ] Algorithm matches official source (ANT, SRI documents)
- [ ] Edge cases handled (null, empty, wrong length, wrong format)
- [ ] Backend has Bean Validation annotation (@ValidCedula etc.)
- [ ] Frontend has Yup extension method
- [ ] Tests cover valid + invalid inputs
- [ ] Same logic on backend and frontend (consistency)
- [ ] Spanish error messages
- [ ] Documentation includes algorithm reference

## Notes

- Validation is defense-in-depth: validate on frontend (UX) AND backend (security)
- Don't trust frontend validation alone — always re-validate on backend
- Test with real-world data when possible
- Keep validators in `shared` module to use across microservices
- Reference source: ANT Reglamento de Tránsito, SRI documentation
