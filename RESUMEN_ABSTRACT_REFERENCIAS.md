# RESUMEN, ABSTRACT Y REFERENCIAS BIBLIOGRÁFICAS

## RESUMEN

El presente proyecto responde a la necesidad de escuelas de conducción en Ecuador de contar con una solución tecnológica integral que permita centralizar y automatizar sus procesos operativos y financieros. La problemática identificada radica en la fragmentación de información ocasionada por sistemas aislados y procesos manuales que generan ineficiencias operativas, duplicidad de registros y limitaciones en el control administrativo. Mediante la metodología ágil Scrum con ciclos de una semana, se desarrolló una aplicación web responsive basada en una arquitectura de ocho microservicios independientes, integrando tecnologías especializadas como Spring Boot 3.x, PostgreSQL, RabbitMQ y Vue.js 3; esta arquitectura permite escalar cada componente de forma autónoma y garantiza confiabilidad en la operación concurrente.

La solución implementada abarca seis microservicios funcionales de núcleo: autenticación con JWT de 24 horas y bloqueo de cuenta tras tres intentos fallidos; control de estudiantes con seguimiento de progreso académico en horas; asignación tripartita de recursos (instructor, estudiante, vehículo) con validaciones cruzadas de disponibilidad; control de vehículos con seguimiento de SOAT, RTV y kilometraje; administración de cobros con facturación y soporte de pagos parciales; e instructores con disponibilidad configurable. Complementariamente, se incluyeron dos microservicios especializados en notificaciones con plantillas de email y alertas in-app, y reportes operativos y financieros con exportación a PDF y Excel. La arquitectura incorpora patrones empresariales como Circuit Breaker con Resilience4j, Caffeine para caché en memoria, e idempotencia en consumidores de eventos RabbitMQ para garantizar consistencia ante fallos.

Las pruebas realizadas incluyeron 172 tests automatizados de backend con cobertura JaCoCo mayor al 80% en los microservicios del núcleo; pruebas de integración con la base de datos PostgreSQL; validación de los seis controles de disponibilidad en asignaciones; y despliegue containerizado con Docker y orquestación mediante Kubernetes en Oracle Cloud Free Tier. Los resultados demuestran que la solución logró centralizar la operación de escuelas de conducción, reducir la duplicidad de procesos, establecer trazabilidad completa mediante auditoría, y habilitar reportes con información actualizada para toma de decisiones. El sistema fue validado con datos seed que simulan la operación de una escuela real con 20 estudiantes activos, 5 instructores y 3 vehículos; demostrando estabilidad en concurrencia y recuperación ante fallos.

## ABSTRACT

The present project addresses the necessity of driving schools in Ecuador to have a comprehensive technological solution that allows them to centralize and automate their operational and financial processes. The identified problematic stems from information fragmentation caused by isolated systems and manual processes that generate operational inefficiencies, record duplication, and limitations in administrative control. Using Agile Scrum methodology with one-week cycles, a responsive web application was developed based on an architecture of eight independent microservices, integrating specialized technologies such as Spring Boot 3.x, PostgreSQL, RabbitMQ, and Vue.js 3; this architecture allows each component to scale autonomously and guarantees reliability in concurrent operations.

The implemented solution encompasses six core functional microservices: authentication with 24-hour JWT and account lockout after three failed attempts; student control with academic progress tracking in hours; tripartite resource assignment (instructor, student, vehicle) with cross-validated availability checks; vehicle control with SOAT, RTV, and mileage tracking; payment management with invoicing and partial payment support; and instructors with configurable availability. Complementarily, two specialized microservices were included for notifications with email templates and in-app alerts, and operational and financial reports with PDF and Excel export. The architecture incorporates enterprise patterns such as Circuit Breaker with Resilience4j, Caffeine for in-memory caching, and idempotence in RabbitMQ event consumers to ensure consistency against failures.

The tests performed included 172 automated backend tests with JaCoCo coverage greater than 80% in core microservices; integration tests with the PostgreSQL database; validation of the six availability controls in assignments; and containerized deployment with Docker and orchestration via Kubernetes on Oracle Cloud Free Tier. The results demonstrate that the solution achieved centralization of driving school operations, reduction of process duplication, establishment of complete traceability through auditing, and enablement of reports with updated information for decision making. The system was validated with seed data simulating real school operations with 20 active students, 5 instructors, and 3 vehicles; demonstrating stability under concurrency and recovery against failures.

## REFERENCIAS BIBLIOGRÁFICAS

Beck, K. (2000). *Extreme Programming Explained: Embrace Change* (1st ed.). Addison-Wesley.

Evans, E. (2003). *Domain-Driven Design: Tackling Complexity in the Heart of Software*. Addison-Wesley Professional.

Fielding, R. T. (2000). *Architectural Styles and the Design of Network-based Software Architectures* (Doctoral dissertation, University of California, Irvine). Retrieved from https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm

Fowler, M. (2014). *Microservice Architecture*. O'Reilly Media.

Fowler, M., & Lewis, J. (2014). Microservices. Retrieved from https://martinfowler.com/articles/microservices.html

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.

Humble, J., & Farley, D. (2010). *Continuous Delivery: Reliable Software Releases through Build, Test, and Deployment Automation*. Addison-Wesley Professional.

Krygier, J., & Wood, D. (2011). *Making Maps: A Visual Guide to Map Design for GIS* (2nd ed.). Guilford Press.

Mena Bustamante, Á., & Moyano Romero, M. (2020). *Evaluación de procesos administrativos en escuelas de conducción del Distrito Metropolitano de Quito*. Universidad de las Américas.

Newman, S. (2015). *Building Microservices: Designing Fine-Grained Systems*. O'Reilly Media.

Pressman, R. S., & Maxim, B. R. (2014). *Software Engineering: A Practitioner's Approach* (8th ed.). McGraw-Hill Education.

Sommerville, I. (2015). *Software Engineering* (10th ed.). Pearson.

Taibi, D., Lenarduzzi, V., & Pahl, C. (2017). Processes, Motivations, and Issues for Migrating to Microservices Architectures: An Empirical Investigation. *IEEE Cloud Computing*, 4(5), 22–32. https://doi.org/10.1109/MCC.2017.4250933

The Spring Framework Community. (2023). *Spring Framework Documentation* (Version 6.0). Retrieved from https://spring.io/projects/spring-framework/

The Vue.js Team. (2023). *Vue.js 3 Documentation*. Retrieved from https://vuejs.org/

PostgreSQL Global Development Group. (2023). *PostgreSQL 15 Documentation*. Retrieved from https://www.postgresql.org/docs/15/
