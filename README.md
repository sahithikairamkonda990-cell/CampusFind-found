# CampusFind — Lost and Found Management System

CampusFind helps college students report lost/found items, search listings, submit claims, and lets administrators review claims.

## Project structure
- `frontend/` — React + Vite website
- `backend/` — Java Spring Boot REST API with H2 database
- `netlify.toml` — Netlify build settings for the frontend

## Run locally on Windows
Prerequisites: Node.js and Java 17+; Maven installed.

1. Open a Command Prompt in this extracted project folder.
2. Start backend in one terminal:
   `cd backend`
   `mvn spring-boot:run`
3. Start frontend in a second terminal:
   `cd frontend`
   `npm install`
   `npm run dev`
4. Open the URL printed by Vite (usually `http://localhost:5173`). Backend defaults to `http://localhost:8081`.

## Deploy frontend to Netlify using GitHub
Netlify deploys the **frontend only**. The Spring Boot backend is a separate service and must be deployed to a Java-capable host for sign-in, item reports, and claims to work online.

1. Extract this ZIP on your computer.
2. Upload the project files to a GitHub repository, preserving `frontend/` and `backend/` folders. Do not upload `frontend/node_modules` or `backend/target`.
3. In Netlify, import that GitHub repository. The included `netlify.toml` sets base directory `frontend`, build command `npm run build`, and publish directory `dist`.
4. To connect a deployed backend, set Netlify environment variable `VITE_API_URL` to your backend's public API base URL, ending in `/api`, then trigger a new deploy.

**Important:** `localhost` only works on your own computer. A public Netlify site cannot use your laptop's `localhost` backend. Deploy the backend separately and configure CORS to allow your Netlify site URL.

## Browser-only GitHub upload note
GitHub's web uploader is designed for files; uploading a ZIP alone will not unpack the project for Netlify. Extract the ZIP first, then upload the actual project files/folders while preserving their paths. If the browser uploader cannot preserve the folder structure, use GitHub's web upload for each folder's contents or GitHub Desktop.
