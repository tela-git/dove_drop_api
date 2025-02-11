# DOVE DROP API

## Overview
DOVE DROP API is the backend service for the DOVE DROP app, built using Ktor. It provides authentication and will support real-time chat functionality in the future.

## Features
- JWT authentication
- MongoDB Atlas integration
- Password hashing services
- Ktor-based API
- Authentication flow completed
- Upcoming: WebSocket-based chat system

## Tech Stack
- **Kotlin** (for Ktor backend)
- **Ktor** (server framework)
- **MongoDB Atlas** (cloud database)
- **JWT** (authentication mechanism)
- **SHA256** (password hashing)

## Current Progress
✅ Authentication flow (JWT, hashing, MongoDB storage) is implemented.

⏳ Next step: Implementing a real-time chat system using WebSockets.

## Setup Instructions
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd dove-drop-api
   ```
2. Configure environment variables for MongoDB and JWT secrets.
3. Run the application:
   ```bash
   ./gradlew run
   ```

## Future Plans
- Implement chat system using WebSockets.
- Enhance security and optimize API performance.
- Add additional features as per app requirements.

---
Stay tuned for updates as the development progresses!

