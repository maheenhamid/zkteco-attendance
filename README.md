# ZKTeco F18 Attendance Management System

A BioTime-style, multi-tenant (institute-based) attendance management system for the ZKTeco F18 device, built on Spring Boot + React. There is **no local institute/class table** — institutes and classes are always resolved live from the Shebashikkha public API; only their numeric IDs are stored locally.

## Architecture

```
backend/    Spring Boot 2.7 (Java 8 target) REST API + ZKTeco ADMS/iClock protocol handler
frontend/   React 18 + Vite + Tailwind CSS admin panel
docs/       API sample requests (REST Client format)
docker-compose.yml   Local MySQL 8 for development
```

- **Auth model**: two distinct concepts. `Operator` = a panel login account (JWT, RBAC via `Role`/`Permission`). `DeviceUser` = a biometric-enrolled person shown on the "User Management" page (institute/class/device/on-device privilege), pushed to the physical device via queued `DeviceCommand`s.
- **Multi-tenancy**: every `Operator` (except `SUPER_ADMIN`) has an `instituteId`. All list/search endpoints are scoped server-side to that institute regardless of what the client requests — see `SecurityUtils.resolveInstituteId()` in the backend.
- **ZKTeco protocol**: `IClockController` implements the ADMS/iClock push protocol (`/iclock/cdata`, `/iclock/getrequest`, `/iclock/device-command-result`) exactly as the F18 firmware expects — plain `text/plain` responses, SN-based device identity, no JWT on that channel.

## Prerequisites

| Tool | Required | Notes |
|---|---|---|
| JDK | 8+ | Backend targets Java 8 bytecode; compiles fine with a newer JDK (tested with JDK 17) via Maven's `java.version=8` property. |
| Maven | 3.6+ | Already used to build/verify this project. |
| MySQL | 8.0 | Not detected on this machine. Use `docker-compose up -d` if Docker is available, or install MySQL 8 Community Server directly. |
| Node.js | 18+ recommended | This machine has **Node 14.17.3**, which is too old for the latest Vite/React tooling. The frontend here is pinned to **Vite 2.9 + @vitejs/plugin-react 1.x** specifically so it works on Node 14 — `npm install` and `npm run build` were verified working. If you upgrade Node to 18+, you can bump `vite`/`@vitejs/plugin-react` to their latest majors in `frontend/package.json`. |

## Backend setup

Review `backend/src/main/resources/application.yml` for the default local values, then configure via environment variables (all have sane local defaults, see `application.yml`):

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=zkteco_attendance
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET=<a long random string in production>
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Start MySQL (choose one):

```bash
# Option A: Docker
docker-compose up -d

# Option B: local MySQL 8 install - create the DB once
mysql -uroot -p -e "CREATE DATABASE zkteco_attendance;"
```

Run the backend (Flyway migrates the schema and a `DataSeeder` creates the default operator on first boot):

```bash
mvn spring-boot:run
```

Default login: **admin / Admin@123** — change this immediately in production (Roles & Permissions page, or `PUT /api/operators/{id}`).

Run tests:

```bash
mvn test
```

## Frontend setup

```bash
cd frontend
cp .env.example .env   # adjust VITE_API_BASE_URL if the backend isn't on localhost:8080
npm install
npm run dev             # http://localhost:5173
```

Production build:

```bash
npm run build            # outputs to frontend/dist
npm run preview          # sanity-check the build locally
```

## Pointing the ZKTeco F18 at this server

On the device: **Menu → Comm → Ethernet/Cloud Server Setting (ADMS)**:
- Server address: the IP of the machine running this backend (per the spec, `192.168.1.115`)
- Server port: the backend's port (default `8080`)
- Enable Domain Name: Off (use raw IP)

The device will then call `GET/POST /iclock/cdata`, `GET /iclock/getrequest`, and `POST /iclock/device-command-result` automatically — no further device-side config needed. The first heartbeat auto-registers the device (visible in Device Management with an "Unassigned Device ..." name); assign it to an institute there via Edit.

## Deployment

**Backend** (Linux, systemd example):
```bash
mvn -f backend/pom.xml clean package -DskipTests
# copy target/attendance.jar to the server, then:
java -jar attendance.jar
```
All configuration is environment-variable driven (see `application.yml`) — wire `DB_*`/`JWT_SECRET`/`CORS_ALLOWED_ORIGINS` into the systemd unit's `Environment=` lines (or an `.env` loaded by your process manager). On Windows, run the jar as a Windows Service via NSSM or similar.

**Frontend**: build (`npm run build`) and serve `frontend/dist` as static files via Nginx, reverse-proxying `/api` and `/iclock` to the backend:

```nginx
server {
  listen 80;
  root /var/www/zkteco-frontend/dist;
  location / { try_files $uri /index.html; }
  location /api/    { proxy_pass http://127.0.0.1:8080; }
  location /iclock/ { proxy_pass http://127.0.0.1:8080; }
}
```

## API samples

See [docs/api-samples.http](docs/api-samples.http) for ready-to-run sample requests (login, devices, users, attendance, roles, and the raw ZKTeco ADMS protocol calls) in VS Code REST Client / JetBrains HTTP Client format.

## Known limitations / next steps

- The ZKTeco protocol implementation follows the published ADMS/iClock spec but has **not been tested against a physical F18** in this environment (no device on this network) — verify against real hardware before production use, particularly the exact `TransFlag`/option string your firmware version expects.
- `DeviceUser` → device assignment is single-device (`deviceId`) per the spec's single "Select Device" dropdown; extending to push one user to multiple devices would mean a many-to-many join table plus one `DeviceCommand` per target device.
- No automated frontend tests were added; `mvn test` covers the ZKTeco line-parsing logic (`IClockParserTest`) on the backend.
