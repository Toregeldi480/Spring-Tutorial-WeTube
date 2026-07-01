# WeTube - Video Streaming Platform

A microservices-based video streaming platform built with Spring Boot, allowing users to upload, transcode, and stream videos with adaptive bitrate HLS.

## Tech Stack

| Category | Technology |
|----------|------------|
| **Backend Framework** | Spring Boot 3.x |
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway |
| **Database** | PostgreSQL 16 |
| **Message Broker** | Apache Kafka |
| **Video Processing** | Jaffree (FFmpeg Wrapper) |
| **Streaming Protocol** | HTTP Live Streaming (HLS) |
| **Authentication** | JWT (Access & Refresh Tokens) |
| **Build Tool** | Maven |
| **Containerization** | Docker & Docker Compose |
| **Runtime** | Java 21 |

## Microservices

### 🔐 Gateway Service (Default Port: 8080)
- Routes requests to appropriate microservices
- Validates JWT tokens from cookies
- Handles CORS configuration
- Token extraction from HttpOnly cookies

### 👤 User Service (Default Port: 8081)
- User registration and login
- JWT access token and refresh token generation
- Token refresh endpoint
- Password encryption with BCrypt

### 🎬 Video Service (Default Port: 8082)
- Video upload with metadata (title, description)
- Multipart file handling
- Video metadata storage in PostgreSQL
- Produces Kafka messages for transcoding
- Serves HLS playlists and segments

### 🔄 Transcoding Service (Default Port: 8084)
- Consumes video processing messages from Kafka
- FFmpeg-based video transcoding
- Generates adaptive HLS streams (1080p, 720p, 480p)
- Outputs master M3U8 playlist with multiple quality variants

### 📋 Registry Service (Default Port: 8761)
- Netflix Eureka service discovery
- Service health monitoring
- Load balancing support

## Features

- **Adaptive Bitrate Streaming**: Videos are transcoded into multiple quality levels (1080p, 720p, 480p) with HLS for smooth playback across different network conditions
- **Secure Authentication**: JWT-based authentication with access and refresh tokens stored in HttpOnly cookies
- **Asynchronous Processing**: Video transcoding happens asynchronously via Kafka, allowing immediate upload response
- **HLS Playback**: Client-side HLS playback using hls.js with automatic quality switching
- **Cookie-based Auth**: Secure HttpOnly cookies prevent XSS attacks on authentication tokens
- **Video Metadata**: Upload videos with custom titles and descriptions

## Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Maven (for local development)

## Setup & Run

### 1. Clone the repository
```bash
git clone https://github.com/Toregeldi480/Spring-Tutorial-WeTube.git
cd Spring-Tutorial-WeTube
```
###  2. Build
```bash
chmod +x ./scripts/build.sh
./scripts/build.sh
```

### 3. Run
```bash
docker compose up -d
docker compose ps # Should return Up or Healthy
```

### 4. Logging
```bash
docker compose logs -f
```

### 5. Access Database
```bash
docker exec -i postgres psql -U admin -d wetube
SELECT * FROM user_service.users;              # access users table
SELECT * FROM user_service.user_subscriptions; # access user subscriptions table
SELECT * FROM video_service.videos;            # access videos table
SELECT * FROM video_service.video_ratings;     # access video ratings table
```

### 6. Access Video Storage
```bash
docker exec transcoding-service ls /app/videos
```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login and receive JWT tokens |
| `POST` | `/auth/refresh` | Refresh access token |
| `POST` | `/auth/logout` | Logout and invalidate tokens |

### User Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/user/me` | Get current authenticated user |
| `GET` | `/user/@{username}` | Get user by username |
| `GET` | `/user/all` | Get all users |
| `GET` | `/user/{userId}/subscriptions` | Get user's subscriptions |
| `GET` | `/user/{channelId}/subscribers` | Get channel's subscribers |
| `POST` | `/user/subscribe/{channelId}` | Subscribe to a channel |
| `POST` | `/user/unsubscribe/{channelId}` | Unsubscribe from a channel |

### Video Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/video/upload` | Upload a new video |
| `GET` | `/video/{videoId}/metadata` | Get video metadata |
| `GET` | `/video/search` | Search videos by keyword |
| `GET` | `/video/{videoId}/master.m3u8` | Get master HLS playlist |
| `GET` | `/video/{quality}/playlist.m3u8` | Get quality-specific playlist |
| `GET` | `/video/{quality}/{segment}` | Get video segment |

#### Upload Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `file` | `MultipartFile` | Yes | Video file to upload |
| `title` | `String` | Yes | Video title |
| `description` | `String` | No | Video description |

#### Search Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `query` | `String` | Search keyword |

#### HLS Streaming

| Parameter | Type | Description |
|-----------|------|-------------|
| `videoId` | `UUID` | Video identifier |
| `quality` | `String` | Stream quality (`stream_0`, `stream_1`, `stream_2`) |
| `segment` | `String` | Segment filename (e.g., `segment001.ts`) |

### Video Rating Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/video-rating/video/like/{videoId}` | Like a video |
| `POST` | `/video-rating/video/dislike/{videoId}` | Dislike a video |
| `GET` | `/video-rating/video/{videoId}/likes` | Get video likes |
| `GET` | `/video-rating/video/{videoId}/dislikes` | Get video dislikes |
| `GET` | `/video-rating/user/{userId}/likes` | Get user's liked videos |
| `GET` | `/video-rating/user/{userId}/dislikes` | Get user's disliked videos |

## Project Structure
```bash
WeTube/
├── frontend/                 # Simple Frontend
├── gateway-service/          # Spring Cloud Gateway
├── user-service/             # User management & auth
├── video-service/            # Video upload & storage
├── transcoding-service/      # FFmpeg video processing
├── registry-service/         # Eureka service discovery
├── docker-compose.yml        # Container orchestration
├── scripts/                  # Scripts
└── init-scripts/             # Database initialization
```
