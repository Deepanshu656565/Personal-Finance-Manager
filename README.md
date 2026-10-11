# Personal Finance Manager

A simple personal finance application with a Java Spring Boot backend and an HTML, CSS, and JavaScript frontend.

# Run Locally on Windows

1. Install JDK 17 or newer and Maven.
2. Open this project folder in VS Code (the folder containing `pom.xml`).
3. Open the terminal and run:

   ```powershell
   mvn clean test
   mvn spring-boot:run
   ```

4. Open http://localhost:8080/ in your browser.

## Register and Log In

If you are using a new database, register an account first. Then log in with the same email and password.

## API Health Check

Open http://localhost:8080/api/health.

Expected response:

```json
{"status":"UP"}
```

## Frontend Files

The frontend files are located in `src/main/resources/static/`:

 `index.html`
 `style.css`
 `app.js`

Spring Boot serves the frontend and API from the same application, so separate CORS configuration is not needed for local use.

## Database

The application uses an H2 SQL database. The local file-based database is configured to retain data across normal application restarts. Data on a hosting platform may not persist unless persistent storage or a managed database is configured.

