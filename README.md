# StreamElements Tip Tracker

A Micronaut-based web application that displays a ranked list of top tippers from a StreamElements channel.

## Prerequisites

- **Java 25**: Ensure Java 25 is installed and set as your `JAVA_HOME`.
- **StreamElements JWT Token**: Required to authenticate with the API (see "Finding your JWT Token" below).

## How to Run

### Command Line (Gradle)

**Powershell:**
```powershell
.\gradlew.bat run
```

**Bash:**
```bash
./gradlew run
```

The application will be available at `http://localhost:8080`.

### IntelliJ IDEA
1.  Open the project in IntelliJ IDEA.
2.  Wait for Gradle to sync.
3.  Locate `src/main/groovy/gg/xp/Application.groovy`.
4.  Right-click the `main` method and select **Run 'Application'**.
5.  To set environment variables or Micronaut environments, edit the **Run Configuration**.

## Admin Panel (Recommended)

Once the application is running, the easiest way to configure it is via the Admin Panel:

1.  Open your browser to `http://localhost:8080/admin`.
2.  **JWT Token Form**: Enter your StreamElements JWT token. This is a write-only field for security.
3.  **Time Range Form**: Set an optional Start and/or End date for tip tracking using the datetime picker.
4.  **Server Actions**: You can manually trigger a tip refresh or shut down the application from here.

Settings changed in the Admin Panel are saved to `data/settings.yml` and will persist across restarts.

---

## Advanced Configuration (Manual)

While the Admin Panel is the preferred method, you can also provide initial configuration through environment variables or local files.

### 1. Environment Variables
Set these variables before starting the application:

**Powershell:**
```powershell
$env:SE_JWT_TOKEN="your_jwt_token_here"
$env:SE_START_TIMESTAMP="2024-01-01T00:00:00Z"
.\gradlew.bat run
```

**Bash:**
```bash
export SE_JWT_TOKEN="your_jwt_token_here"
export SE_START_TIMESTAMP="2024-01-01T00:00:00Z"
./gradlew run
```

### 2. Local Configuration File
Create `src/main/resources/application-local.yml` (this file is ignored by Git):
```yaml
streamelements:
  jwt-token: "your_jwt_token_here"
  start-timestamp: "2024-01-01T00:00:00Z"
```
Run with: `.\gradlew.bat run -Dmicronaut.environments=local`

---

## Finding your JWT Token

1.  Log in to the [StreamElements Dashboard](https://streamelements.com/dashboard).
2.  Go to [Channel Settings](https://streamelements.com/dashboard/account/channels).
3.  Locate the correct channel and click the copy button next to "JWT Token".

![JWT Location Placeholder](where-to-find-jwt.png)

## Additional Details

### Persistence
Dynamic settings (JWT, Start, and End dates) updated via the Admin page are saved to `data/settings.yml`. This directory and file are created automatically.

### Configuring Dates
The application tracks tips within the specified date range. Dates should be in ISO-8601 format (e.g., `2024-01-01T00:00:00Z`). If dates are left empty in the Admin Panel, the application uses its default behavior (tracking from the configured start timestamp with no end date).

