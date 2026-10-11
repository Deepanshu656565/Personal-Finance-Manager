# Syfe Personal Finance Manager — Combined Project

This project contains the Java Spring Boot backend and a plain HTML/CSS/JavaScript frontend served by the same application.

## Run on Windows / VS Code

1. Install JDK 17 or newer and Maven.
2. Open this folder in VS Code (the folder containing `pom.xml`).
3. In the VS Code terminal run:

   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

4. Open **http://localhost:8080/** in your browser.
5. Use **Login** with an account registered through `POST /api/auth/register`. API requests use the same origin and session cookie.

## API health check

Open http://localhost:8080/api/health. Expected response: `{"status":"UP"}`.

## Frontend files

The frontend is in `src/main/resources/static/`:
- `index.html`
- `style.css`
- `app.js`

Spring Boot serves these files from the same host as the API, so local browser CORS configuration is not required.

## Important before submission

- The frontend/backend bundle is a starter implementation and has not been verified against Syfe's full `financial_manager_tests.sh` suite.
- Run `mvn clean test`, then test the provided assignment script against the deployed API if the recruiter supplied it.
- The configured H2 database is in-memory by default; its data resets when the application restarts. Configure persistent database storage before relying on data after deployment.
- Review API validation, session security, DTO architecture, error responses and unit-test coverage against the assignment requirements before submission.


### Login troubleshooting
Open the app at `http://localhost:8080/`. If you have not created an account in this database yet, click **Register** first, then log in. The local default database is now file-based H2 (`./financedb`) so local accounts/data survive a normal application restart. If you previously used the old in-memory database, register your account again once. Use the same email and password for later logins.
