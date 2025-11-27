# SPExtractor Frontend

This folder contains the frontend application served by the Spring Boot backend.

## Development

### Run Frontend Dev Server (for development)
```bash
cd src/main/resources/static
npm install
npm start
```
This starts a local server on http://localhost:3000

### Run Full Application (Backend + Frontend)
```bash
# From project root
./mvnw spring-boot:run
```
Backend serves frontend at https://localhost:8443

## Structure
- `index.html` - Main entry point
- `pages/` - Page components (sites, drives, files, cache)
- `styles/` - CSS stylesheets
- `server/` - Authentication and settings
- `utils/` - Utility functions
- `assets/` - Images and static assets

## Deployment
When deployed, the Spring Boot application serves these static files automatically from the `/` path.
