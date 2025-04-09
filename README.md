DriveSync is a full-stack web application that enables users to log in with Google, and upload, download, list, and delete files from their own Google Drive. It is built with a React frontend and a Spring Boot backend using OAuth2 authentication and Google Drive APIs.


Overview

This app demonstrates:

Google OAuth2 login via Spring Security.
CSRF protection using cookie-based tokens.
File upload using `multipart/form-data`.
File download with headers and blob handling.
File delete functionality synced with Google Drive.
Frontend-backend communication secured via Axios and cookies.
Integration with Google Drive API using `com.google.api.services.drive`.


Tech Stack


Frontend:- React, Axios, JavaScript
Backend:- Spring Boot, Spring Security
Auth:- Google OAuth2
Storage API:- Google Drive REST API
Security:- CSRF, CORS, OAuth2 Scopes
Testing:- JUnit 5, Mockito, MockMvc

Setup Instructions

Prerequisites

- Java 17+
- Node.js and npm
- Gradle
- Google Cloud project with:
  - OAuth2 credentials (Client ID/Secret)
  - Drive API enabled

Backend Setup

1.Clone the repository


git clone https://github.com/tpothineni/DriveSYNC


2. Configure application properties

Update the application.properties:

3. Build the app

gradle build

4. Run the Spring boot app

java -jar build/libs/CustomUpload-0.0.1-SNAPSHOT.jar

Spring Boot runs on `http://localhost:8080`.



Frontend Setup

1. Navigate to frontend directory

2. Install dependencies

npm install

3.Start the React app

npm start

React runs on `http://localhost:3000`.

Running the Application

Steps:

1. Visit http://localhost:3000.
2. Click Login with Google.
3. Authorize the app to access your Google Drive.
4. Upload a file using the form.
5. View the list of your Drive files.
6. Use Download or Delete on each file.
7. Click Logout to sign out.


Assumptions

- Each authenticated user interacts with their own Google Drive.
- CSRF token is managed via cookie + Axios header (`X-XSRF-TOKEN`).
- The app is run locally with React and Spring Boot running on different ports (CORS handled accordingly).

Design Decisions

- OAuth2 Integration uses Spring Security's built-in support for minimal config.
- Drive API Integration is done via the Google Java Client (`Drive` class) instead of raw REST calls for better abstraction.
- CSRF Handling is done using `CookieCsrfTokenRepository.withHttpOnlyFalse()` so the token can be read by the frontend.
- Axios Interceptor is used to automatically attach CSRF tokens to requests.