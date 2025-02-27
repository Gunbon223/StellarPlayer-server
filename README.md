# Stellar Player - Music Streaming Platform
<div align="center">
<img src="https://res.cloudinary.com/dll5rlqx9/image/upload/v1740631257/prj-img/biejsm3hzu66d4ajtmyh.png" alt="logo" width="200"/>
</div>

## 📡 Project Architecture

Stellar Player is a full-stack music streaming platform built with a modern tech stack:

**Frontend:** Next.js, TypeScript, React  
**Backend:** Java Spring Boot RESTful API  
**Repositories:**
- 🎨 [Frontend Repository](https://github.com/Gunbon223/stellar-player-nextjs)
- ⚙️ [Backend Repository](https://github.com/Gunbon223/StellarPlayer-server)

## ✨ Features

- **Music Streaming**: High-quality audio streaming from our servers
- **User Authentication**: Secure registration, login, and profile management
- **Playlist Management**: Create, edit, and share custom playlists
- **Search Functionality**: Find music by artist, album, or track name
- **User Libraries**: Save favorites and organize your music collection
- **Responsive Design**: Seamless experience across all devices
- **Playback Controls**: Play, pause, skip, repeat, shuffle functionality
- **Audio Visualization**: Dynamic visual effects synchronized with music
- **Social Features**: Follow artists and friends, share music
- **Offline Listening**: Download tracks for offline playback

## 🛠️ Technology Stack

### Frontend (Next.js/TypeScript)
- **Framework**: Next.js with TypeScript
- **State Management**: React Context API / Redux
- **Styling**: CSS / Tailwind CSS / Styled Components
- **Routing**: Next.js built-in routing
- **API Integration**: Axios / SWR for data fetching
- **Authentication**: JWT token-based auth with secure storage

### Backend (Java)
- **Framework**: Spring Boot
- **API**: RESTful endpoints
- **Database**: MySQL
- **Authentication**: JWT implementation with Spring Security
- **Cloud Storage**: Cloudinary for audio file storage
- **Caching**: Redis for performance optimization
- **Audio Processing**: Java audio libraries for format conversion

## 🚀 Setup & Installation

### Prerequisites
- Node.js (v16+) and npm/yarn
- Java 17+ and Maven/Gradle
- MySQL database instance

### Backend Setup
1. Clone the backend repository:
   ```bash
   git clone https://github.com/Gunbon223/StellarPlayer-server.git
   cd StellarPlayer-server
   ```

2. Configure the database:
   - Create a new database instance
   - Update `application.properties` with your database credentials

3. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   # or
   java -jar target/stellar-player-server-1.0.0.jar
   ```

4. The API will be available at `http://localhost:8080`

### Frontend Setup
1. Clone the frontend repository:
   ```bash
   git clone https://github.com/Gunbon223/stellar-player-nextjs.git
   cd stellar-player-nextjs
   ```

2. Install dependencies:
   ```bash
   npm install
   # or
   yarn install
   ```

3. Configure environment variables:
   Create a `.env.local` file with:
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8080
   # Add other necessary environment variables
   ```

4. Start the development server:
   ```bash
   npm run dev
   # or
   yarn dev
   ```

5. Access the application at `http://localhost:3000`

## 📊 System Architecture

```
                                ┌─────────────────┐       ┌─────────────────────┐      ┌─────────────────┐
                                │                 │       │                     │      │                 │
                                │   NextJS        │◄──────►    Java Spring      │◄─────►   Database      │
                                │   Frontend      │  API  │    Backend          │      │   Storage       │
                                │                 │ Calls │                     │      │                 │
                                └─────────────────┘       └─────────────────────┘      └─────────────────┘
                                        ▲                          │                          │
                                        │                          ▼                          │
                                ┌─────────────────┐       ┌─────────────────────┐             │
                                │                 │       │                     │             │
                                │    Users        │       │   Cloud Storage     │◄────────────┘
                                │    Browsers     │       │   (Music Files)     │
                                │                 │       │                     │
                                └─────────────────┘       └─────────────────────┘
```
## 🔐 Authentication Flow

1. User registers/logs in through the frontend
2. Backend validates credentials and issues a JWT token
3. Frontend stores token in secure storage
4. Token is included in API requests for authenticated endpoints
5. Backend validates token for each protected request

## 📱 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Music
- `GET /api/tracks` - Get all tracks
- `GET /api/tracks/{id}` - Get track by ID
- `GET /api/albums` - Get all albums
- `GET /api/artists` - Get all artists

### Playlists
- `GET /api/playlists/user/{userId}` - Get user playlists
- `POST /api/playlists` - Create new playlist
- `PUT /api/playlists/{id}` - Update playlist
- `DELETE /api/playlists/{id}` - Delete playlist

## 📦 Deployment

### Backend Deployment
- The Java backend developed by Spring boot

### Frontend Deployment
- Optimized for deployment on Vercel (Next.js platform)


### Testing
- Frontend: Jest and React Testing Library
- Backend: JUnit and Mockito

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Contributors

- [Gunbon223](https://github.com/Gunbon223) - Project creator and maintainer

## 📸 Screenshots

![Dashboard](https://via.placeholder.com/800x450?text=Stellar+Player+Dashboard)
![Player](https://via.placeholder.com/800x450?text=Music+Player+Interface)
![Playlists](https://via.placeholder.com/800x450?text=Playlist+Management)

---

© 2025 Stellar Player. All Rights Reserved.
