# MDD

MDD (Monde de Dév) is a social network for developers, built as an MVP for ORION. Users subscribe to programming topics, follow a feed, publish posts and comment on them. The user interface is in French.

The project documentation is in [docs/documentation-projet.md](docs/documentation-projet.md) (in French).

## Prerequisites

- Java 25
- Node.js 26
- Docker

## Back-end

From the `back` folder, copy `.env.example` to `.env`, then fill in `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` and `JWT_SECRET`. Docker creates the database with the first two on its first start. `JWT_SECRET` signs the login tokens: it must be at least 32 characters long, or the API refuses to start.

Start the API on `http://localhost:8080`. This also starts MySQL and applies the database migrations:

```bash
./mvnw spring-boot:run
```

On Windows, use `./mvnw.cmd` instead of `./mvnw`.

Other commands:

- `./mvnw verify`: runs the tests and checks the 80% coverage gate. The report is in `target/site/jacoco/index.html`.
- `./mvnw spring-boot:test-run`: starts the API on `http://localhost:8080` with a throwaway MySQL container, for the end-to-end tests. Nothing is kept once it stops.
- `./mvnw spotless:apply`: formats the Java code.

## Front-end

From the `front` folder, install the dependencies and start the application on `http://localhost:4200`:

```bash
npm ci
npm start
```

Other commands:

- `npm run test:coverage`: runs the unit and integration tests and checks the 80% coverage gate. The report is in `coverage/mdd-front/index.html`.
- `npm run e2e:ci`: runs the Cypress tests in a headless browser, on its own server on port 4201. Start the API with `./mvnw spring-boot:test-run` first.
- `npm run lint`: checks the code with ESLint.
- `npm run format`: formats the code with Prettier.