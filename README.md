# DevOps Microservice — `/DevOps`

Microservicio REST en **Java 17 + Spring Boot 3**, protegido con **API Key +
JWT de un solo uso**, contenerizado y desplegado en **Azure Kubernetes
Service (AKS)** mediante **Terraform** y un pipeline de **Azure DevOps**
completamente automatizado (Build → Test → Package → Deploy).

## Pruebas

| Ambiente    | URL                                           | Se despliega desde                     |
| ----------- | --------------------------------------------- | -------------------------------------- |
| Producción | `https://4.157.22.117.sslip.io/DevOps`      | rama `main` (con aprobación manual) |
| Test        | `https://test.4.157.22.117.sslip.io/DevOps` | rama `testing`                       |
| Desarrollo  | `https://dev.4.157.22.117.sslip.io/DevOps`  | rama `develop`                       |

Los 3 con **HTTPS real** (certificado de Let's Encrypt, renovación automática),
comparten una sola IP pública (para ahorrar cuota) y se diferencian por
hostname; cada uno vive en su propio namespace de Kubernetes, con su propio
Redis y su propio secreto JWT — completamente aislados entre sí.

```bash
# 1. Obtener un JWT de un solo uso
curl -X POST "https://4.157.22.117.sslip.io/auth/token" \
  -H "X-Parse-REST-API-Key: 2f5ae96c-b558-4c7b-a590-a501ae1c3f6c"

# 2. Usarlo en el endpoint de negocio
curl -X POST "https://4.157.22.117.sslip.io/DevOps" \
  -H "X-Parse-REST-API-Key: 2f5ae96c-b558-4c7b-a590-a501ae1c3f6c" \
  -H "X-JWT-KWY: <token-del-paso-1>" \
  -H "Content-Type: application/json" \
  -d '{"message":"This is a test","to":"Juan Perez","from":"Rita Asturia","timeToLifeSec":45}'

# -> {"message":"Hello Juan Perez your message will be send"}
```

## Contrato del endpoint

- `POST /DevOps` — único método permitido. Cualquier otro (`GET`, `PUT`,
  `DELETE`, `PATCH`, `HEAD`, `OPTIONS`) devuelve el string literal
  `"ERROR"` con status `405`.
- Protegido con el header `X-Parse-REST-API-Key` (API Key fija dada por el
  banco) **y** un JWT de un solo uso en `X-JWT-KWY`, obtenido previamente
  vía `POST /auth/token` (el "API Manager" del reto).
- Un JWT ya usado, o ausente, o con API Key inválida → `401` + `"ERROR"`.

## Arquitectura

```
Cliente
   │
   ▼
Ingress (nginx) + TLS ── IP pública compartida, ruteo por hostname
   │
   ├── devops-dev    (namespace) ── Deployment 2+ réplicas + Redis propio
   ├── devops-test   (namespace) ── Deployment 2+ réplicas + Redis propio
   └── devops-prod   (namespace) ── Deployment 2+ réplicas + Redis propio
```

Cada réplica del microservicio comparte el mismo Redis **dentro de su
namespace**, para que la protección de "JWT de un solo uso" funcione
correctamente sin importar a qué réplica caiga cada petición (ver sección
de bugs corregidos más abajo — este fue un hallazgo real durante las
pruebas, no un diseño trivial).

Un solo clúster de AKS aloja los 3 ambientes (aislados por namespace) en
vez de 3 clústeres separados, para minimizar costo.

## Stack tecnológico

| Categoría                   | Tecnología                                              |
| ---------------------------- | -------------------------------------------------------- |
| Lenguaje / Framework         | Java 17, Spring Boot 3.3.4, Spring Security              |
| Seguridad                    | JJWT (JWT), API Key, Redis (tokens de un solo uso)       |
| Pruebas                      | JUnit 5, MockMvc, JaCoCo, Checkstyle, SpotBugs           |
| Contenedores                 | Docker, Docker Compose, Nginx                            |
| Orquestación                | Kubernetes (AKS), Kustomize, nginx-ingress, cert-manager |
| Infraestructura como código | Terraform (providers `azurerm`, `helm`)              |
| CI/CD                        | Azure DevOps Pipelines                                   |
| Cloud                        | Microsoft Azure (AKS, ACR, Key Vault, Load Balancer)     |

## Estructura del repositorio

```
├── src/                          Código fuente del microservicio (Java)
├── Dockerfile                    Build de la imagen (multi-stage)
├── docker-compose.yml            Stack local: 2 réplicas + Nginx + Redis
├── nginx/                        Config del load balancer local
├── terraform/
│   ├── bootstrap/                 Remote state (se aplica una sola vez)
│   └── platform/                  AKS, ACR, Key Vault, Helm (nginx-ingress, cert-manager)
├── kubernetes/
│   ├── base/                      Manifiestos comunes (Kustomize)
│   ├── overlays/{dev,test,prod}   Config por ambiente + Ingress/TLS
│   └── cluster/                   ClusterIssuer de Let's Encrypt
├── azure-pipelines.yml           Pipeline como código
├── templates/deploy-steps.yml    Pasos de deploy reutilizados por ambiente
├── postman/                      Colección + plan de pruebas manuales
├── checkstyle.xml                Reglas de Clean Code
└── sonar-project.properties      Config de análisis estático
```

## Probarlo localmente

```powershell
docker compose up --build
```

Levanta 2 réplicas del microservicio + Nginx como load balancer + Redis.
Prueba con el mismo flujo `curl` de la sección "Demo en vivo", cambiando
`https://4.157.22.117.sslip.io` por `http://localhost`.

## Cumplimiento 

| Requisito                                                                                          | Estado                               |
| -------------------------------------------------------------------------------------------------- | ------------------------------------ |
| Endpoint `/DevOps`, contrato JSON exacto                                                         | Verificado                           |
| Otros métodos HTTP →`"ERROR"`                                                                  | Probado en los 6 métodos            |
| Protegido con la API Key dada, en headers                                                          | Verificado                           |
| JWT de un solo uso, suministrado vía `/auth/token`                                              | Verificado                           |
| Contenerizado, desplegable en cualquier lado                                                       | Docker local + AKS                   |
| Load balancer, mínimo 2 nodos del mismo microservicio                                             | Probadoen Azure                      |
| Infraestructura como código                                                                       | Terraform, incluido Helm             |
| Pipeline como código, en un repositorio                                                           | `azure-pipelines.yml`              |
| Dependency management                                                                              | Maven                                |
| Mínimo 2 stages (build/test)                                                                      | 7 stages                             |
| Automático, cualquier rama, main→prod, ambientes extra, ejecutable a demanda, cualquier versión | Probado en vivo                      |
| Pruebas automáticas                                                                               | 27 tests                             |
| Revisión estática de código                                                                     | Checkstyle + SpotBugs                |
| "Dynamic grow" (autoscaling)                                                                       | HPA probado en vivo (2→5 réplicas) |
| API Manager (API Key + JWT)                                                                        | Verificado                           |
| Clean Code + TDD + cobertura                                                                       | 89.9% (mínimo exigido 80%)          |

## Problemas reales encontrados y corregidos

Evidencia de las pruebas exhaustivas realizadas contra la infraestructura
real (no solo "código que compila"):

1. **Reuso de JWT entre réplicas**: el estado de "token consumido" vivía en
   memoria de cada instancia; con 2+ réplicas, un token podía reusarse si
   la segunda llamada caía en otra réplica. Corregido moviendo el estado a
   **Redis compartido** (`ConsumedTokenStore` / `RedisConsumedTokenStore`).
2. **Colisión de variables de entorno de Kubernetes**: Kubernetes inyecta
   automáticamente una variable `REDIS_PORT` por cada `Service` llamado
   "redis" en el namespace, en formato URL — chocaba con la propiedad de
   configuración del microservicio y lo hacía fallar al iniciar. Corregido
   con `enableServiceLinks: false`.
3. **Health probe de Azure apuntando a `/`**: nginx responde 404 ahí (sin
   un Ingress que matchee), por lo que Azure nunca marcaba el backend como
   sano y el tráfico externo nunca llegaba. Corregido apuntando el probe a
   `/healthz`.
4. **Límite de cuota de IPs públicas** (3 máximo en la suscripción):
   resuelto consolidando los 3 ambientes detrás de un único Ingress
   Controller compartido en vez de una IP por ambiente.
5. **HTTPS real**: implementado con Let's Encrypt + cert-manager + un
   hostname vía `sslip.io`, sin necesitar comprar un dominio propio.

## Licencia

MIT — ver [`LICENSE`](LICENSE).
