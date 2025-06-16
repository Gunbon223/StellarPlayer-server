# 🎵 Stellar Player - Artist Content Management API Documentation

## Overview
This API provides comprehensive artist content management capabilities with role-based access control and approval workflow. Artists can manage their tracks and albums, while admins oversee relationships and content approval.

## Authentication
All endpoints require Bearer token authentication:
```
Authorization: Bearer <your-jwt-token>
```

---

## 📁 File Upload Endpoints

### 1. Upload Song (Original - No Auth)
**Endpoint:** `POST /api/files/upload/song`  
**Authentication:** None  
**Description:** Original song upload endpoint without permission validation.

#### Request Parameters:
- `audioFile` (required): Audio file
- `coverFile` (optional): Cover image file
- `title` (required): Song title
- `releaseYear` (required): Release year
- `artistNames` (required): Comma-separated artist names
- `albumTitle` (optional): Album title
- `genreNames` (optional): Comma-separated genre names

#### Response:
```json
{
  "success": true,
  "message": "Song uploaded successfully",
  "track": {
    "id": 123,
    "title": "Amazing Song",
    "duration": 240,
    "path": "https://cloudinary.com/audio/track.mp3",
    "cover": "https://cloudinary.com/image/cover.jpg",
    "releaseYear": 2024,
    "album": {
      "id": 45,
      "title": "Great Album",
      "cover": "https://cloudinary.com/image/album-cover.jpg"
    },
    "artists": [
      {
        "id": 1,
        "name": "John Doe",
        "avatar": "https://cloudinary.com/image/artist-avatar.jpg"
      }
    ],
    "genres": [
      {
        "id": 2,
        "name": "Pop"
      }
    ],
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### 2. Upload Artist Song (New - With Auth & Approval)
**Endpoint:** `POST /api/files/upload/artist-song`  
**Authentication:** Required (Artist role)  
**Description:** Artist-specific upload with permission validation and approval workflow.

#### Request Parameters:
Same as above, plus:
- `Authorization` header: Bearer token

#### Success Response:
```json
{
  "success": true,
  "message": "Song uploaded successfully and is awaiting admin approval",
  "track": {
    "id": 124,
    "title": "Pending Song",
    "duration": 180,
    "path": "https://cloudinary.com/audio/track.mp3",
    "cover": "https://cloudinary.com/image/cover.jpg",
    "releaseYear": 2024,
    "status": false,
    "album": null,
    "artists": [
      {
        "id": 1,
        "name": "Artist Name",
        "avatar": "https://cloudinary.com/image/avatar.jpg"
      }
    ],
    "genres": [],
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

#### Error Responses:
```json
// 403 Forbidden
{
  "error": "You don't have permission to upload songs for the specified artists",
  "status": "error"
}

// 400 Bad Request
{
  "error": "Audio file is required",
  "status": "error"
}

// 409 Conflict (Duplicate)
{
  "error": "Song already exists with same title and artists",
  "status": "duplicate",
  "existingTrackId": 123,
  "songTitle": "Existing Song",
  "artistNames": "Artist Name"
}
```

---

## 🎤 Artist Content Management (`/api/artist`)

### 1. Get My Artists
**Endpoint:** `GET /api/artist/my-artists`  
**Authentication:** Artist role required

#### Response:
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "bio": "Amazing artist biography",
    "avatar": "https://cloudinary.com/image/artist.jpg",
    "active": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### 2. Get My Tracks
**Endpoint:** `GET /api/artist/my-tracks`  
**Authentication:** Artist role required

#### Response:
```json
[
  {
    "id": 123,
    "title": "My Song",
    "duration": 240,
    "status": true,
    "path": "https://cloudinary.com/audio/song.mp3",
    "cover": "https://cloudinary.com/image/cover.jpg",
    "lyrics": "Song lyrics here...",
    "releaseYear": 2024,
    "playCount": 1500,
    "artists": [
      {
        "id": 1,
        "name": "Artist Name"
      }
    ],
    "album": {
      "id": 45,
      "title": "Album Title"
    },
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### 3. Get Track by ID
**Endpoint:** `GET /api/artist/track/{id}`  
**Authentication:** Artist role required

#### Response:
```json
{
  "id": 123,
  "title": "Track Title",
  "duration": 240,
  "status": true,
  "path": "https://cloudinary.com/audio/track.mp3",
  "cover": "https://cloudinary.com/image/cover.jpg",
  "lyrics": "Track lyrics...",
  "releaseYear": 2024,
  "playCount": 1500,
  "artists": [...],
  "album": {...},
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Error Response:
```json
// 403 Forbidden
{
  "message": "You don't have permission to access this track"
}
```

### 4. Update Track
**Endpoint:** `PUT /api/artist/track/{id}`  
**Authentication:** Artist role required

#### Request Body:
```json
{
  "title": "Updated Track Title",
  "duration": 250,
  "lyrics": "Updated lyrics...",
  "status": true,
  "playCount": 1600,
  "releaseYear": 2024
}
```

#### Response:
```json
{
  "id": 123,
  "title": "Updated Track Title",
  "duration": 250,
  "status": true,
  "lyrics": "Updated lyrics...",
  "releaseYear": 2024,
  "playCount": 1600,
  // ... other fields
  "updatedAt": "2024-01-15T11:00:00"
}
```

### 5. Delete Track
**Endpoint:** `DELETE /api/artist/track/{id}`  
**Authentication:** Artist role required

#### Response:
```json
{
  "message": "Track deleted successfully"
}
```

### 6. Get My Albums
**Endpoint:** `GET /api/artist/my-albums`  
**Authentication:** Artist role required

#### Response:
```json
[
  {
    "id": 45,
    "title": "Great Album",
    "cover": "https://cloudinary.com/image/album.jpg",
    "status": true,
    "releaseDate": "2024-01-01",
    "artists": [
      {
        "id": 1,
        "name": "Artist Name"
      }
    ],
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### 7. Get Album by ID
**Endpoint:** `GET /api/artist/album/{id}`  
**Authentication:** Artist role required

#### Response:
```json
{
  "id": 45,
  "title": "Album Title",
  "cover": "https://cloudinary.com/image/album.jpg",
  "status": true,
  "releaseDate": "2024-01-01",
  "artists": [...],
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 8. Create Album
**Endpoint:** `POST /api/artist/album`  
**Authentication:** Artist role required

#### Request Body:
```json
{
  "title": "New Album",
  "cover": "https://cloudinary.com/image/new-album.jpg",
  "status": true,
  "releaseDate": "2024-02-01",
  "artists": [
    {
      "id": 1,
      "name": "Artist Name"
    }
  ]
}
```

#### Response:
```json
{
  "id": 46,
  "title": "New Album",
  "cover": "https://cloudinary.com/image/new-album.jpg",
  "status": true,
  "releaseDate": "2024-02-01",
  "artists": [...],
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

### 9. Update Album
**Endpoint:** `PUT /api/artist/album/{id}`  
**Authentication:** Artist role required

#### Request Body:
```json
{
  "title": "Updated Album Title",
  "cover": "https://cloudinary.com/image/updated-album.jpg",
  "status": true
}
```

### 10. Delete Album
**Endpoint:** `DELETE /api/artist/album/{id}`  
**Authentication:** Artist role required

#### Response:
```json
{
  "message": "Album deleted successfully",
  "album_title": "Deleted Album",
  "album_id": 45
}
```

### 11. Add Track to Album
**Endpoint:** `POST /api/artist/album/{albumId}/track/{trackId}`  
**Authentication:** Artist role required

#### Response:
```json
{
  "id": 123,
  "title": "Track Title",
  "album": {
    "id": 45,
    "title": "Album Title"
  },
  // ... other track fields
}
```

### 12. Remove Track from Album
**Endpoint:** `DELETE /api/artist/album/{albumId}/track/{trackId}`  
**Authentication:** Artist role required

#### Response:
```json
{
  "id": 123,
  "title": "Track Title",
  "album": null,
  // ... other track fields
}
```

---

## 🛡️ Admin Management (`/api/admin/user-artist`)

### 1. Link User to Artist
**Endpoint:** `POST /api/admin/user-artist/link`  
**Authentication:** Admin role required

#### Query Parameters:
- `userId` (required): User ID
- `artistId` (required): Artist ID

#### Response:
```json
{
  "id": 1,
  "user": {
    "id": 2,
    "name": "Artist User",
    "email": "artist@example.com"
  },
  "artist": {
    "id": 1,
    "name": "Artist Name"
  },
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 2. Unlink User from Artist
**Endpoint:** `DELETE /api/admin/user-artist/unlink`  
**Authentication:** Admin role required

#### Query Parameters:
- `userId` (required): User ID
- `artistId` (required): Artist ID

#### Response:
```json
{
  "message": "User unlinked from artist successfully"
}
```

### 3. Get User's Artists
**Endpoint:** `GET /api/admin/user-artist/user/{userId}/artists`  
**Authentication:** Admin role required

#### Response:
```json
[
  {
    "id": 1,
    "name": "Artist Name",
    "bio": "Artist biography",
    "avatar": "https://cloudinary.com/image/artist.jpg",
    "active": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### 4. Get All User-Artist Relationships
**Endpoint:** `GET /api/admin/user-artist/relationships`  
**Authentication:** Admin role required

#### Response:
```json
[
  {
    "id": 1,
    "user": {
      "id": 2,
      "name": "Artist User"
    },
    "artist": {
      "id": 1,
      "name": "Artist Name"
    },
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

### 5. Check User Permissions
**Endpoint:** `GET /api/admin/user-artist/user/{userId}/artist/{artistId}/can-manage`  
**Authentication:** Admin role required

#### Response:
```json
{
  "canManage": true
}
```

---

## 🎵 Track Approval Management

### 1. Get Unapproved Tracks
**Endpoint:** `GET /api/admin/user-artist/tracks/unapproved`  
**Authentication:** Admin role required

#### Query Parameters:
- `page` (default: 0): Page number
- `pageSize` (default: 10): Items per page
- `sortBy` (default: "id"): Sort field
- `sortOrder` (default: "desc"): Sort direction

#### Response:
```json
{
  "tracks": [
    {
      "id": 124,
      "title": "Pending Song",
      "duration": 180,
      "status": false,
      "path": "https://cloudinary.com/audio/track.mp3",
      "cover": "https://cloudinary.com/image/cover.jpg",
      "releaseYear": 2024,
      "playCount": 0,
      "artists": [
        {
          "id": 1,
          "name": "Artist Name"
        }
      ],
      "album": null,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 50,
  "pageSize": 10
}
```

### 2. Approve Track
**Endpoint:** `POST /api/admin/user-artist/tracks/{trackId}/approve`  
**Authentication:** Admin role required

#### Response:
```json
{
  "message": "Track approved successfully",
  "track": {
    "id": 124,
    "title": "Approved Song",
    "status": true,
    // ... other track fields
    "updatedAt": "2024-01-15T11:00:00"
  }
}
```

### 3. Reject Track
**Endpoint:** `POST /api/admin/user-artist/tracks/{trackId}/reject`  
**Authentication:** Admin role required

#### Response:
```json
{
  "message": "Track rejected successfully",
  "track": {
    "id": 124,
    "title": "Rejected Song",
    "status": false,
    // ... other track fields
    "updatedAt": "2024-01-15T11:00:00"
  }
}
```

### 4. Bulk Approve Tracks
**Endpoint:** `POST /api/admin/user-artist/tracks/bulk-approve`  
**Authentication:** Admin role required

#### Request Body:
```json
[123, 124, 125, 126]
```

#### Response:
```json
{
  "message": "Tracks approved successfully",
  "approvedCount": 4
}
```

### 5. Bulk Reject Tracks
**Endpoint:** `POST /api/admin/user-artist/tracks/bulk-reject`  
**Authentication:** Admin role required

#### Request Body:
```json
[127, 128, 129]
```

#### Response:
```json
{
  "message": "Tracks rejected successfully",
  "rejectedCount": 3
}
```

---

## 🔧 Usage Examples

### Artist Workflow Example:

```bash
# 1. Get my artists
curl -X GET "http://localhost:8080/api/artist/my-artists" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 2. Upload a new song
curl -X POST "http://localhost:8080/api/files/upload/artist-song" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -F "audioFile=@song.mp3" \
  -F "coverFile=@cover.jpg" \
  -F "title=My New Song" \
  -F "releaseYear=2024" \
  -F "artistNames=John Doe,Jane Smith" \
  -F "albumTitle=Greatest Hits" \
  -F "genreNames=Pop,Rock"

# 3. Get my tracks
curl -X GET "http://localhost:8080/api/artist/my-tracks" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# 4. Update a track
curl -X PUT "http://localhost:8080/api/artist/track/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated Song Title", "lyrics": "New lyrics..."}'
```

### Admin Workflow Example:

```bash
# 1. Link user to artist
curl -X POST "http://localhost:8080/api/admin/user-artist/link?userId=2&artistId=1" \
  -H "Authorization: Bearer admin-jwt-token..."

# 2. Get unapproved tracks
curl -X GET "http://localhost:8080/api/admin/user-artist/tracks/unapproved?page=0&pageSize=10" \
  -H "Authorization: Bearer admin-jwt-token..."

# 3. Approve a track
curl -X POST "http://localhost:8080/api/admin/user-artist/tracks/124/approve" \
  -H "Authorization: Bearer admin-jwt-token..."

# 4. Bulk approve tracks
curl -X POST "http://localhost:8080/api/admin/user-artist/tracks/bulk-approve" \
  -H "Authorization: Bearer admin-jwt-token..." \
  -H "Content-Type: application/json" \
  -d '[123, 124, 125]'
```

---

## 🚨 Error Handling

### Common Error Responses:

#### 401 Unauthorized
```json
{
  "message": "Invalid JWT token: Token expired"
}
```

#### 403 Forbidden
```json
{
  "message": "Access denied. Artist privileges required"
}
```

#### 404 Not Found
```json
{
  "message": "Track not found"
}
```

#### 400 Bad Request
```json
{
  "message": "Title is required"
}
```

#### 409 Conflict
```json
{
  "error": "Song already exists with same title and artists",
  "status": "duplicate",
  "existingTrackId": 123,
  "songTitle": "Duplicate Song",
  "artistNames": "Artist Name"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Failed to upload song: Cloudinary upload failed",
  "status": "error"
}
```

---

## 📋 Notes

1. **File Upload Limits**: Maximum file size is 50MB for audio files, 10MB for images
2. **Supported Audio Formats**: MP3, WAV, FLAC, M4A
3. **Supported Image Formats**: JPG, PNG, WebP
4. **Token Expiration**: JWT tokens expire after 24 hours
5. **Rate Limiting**: 100 requests per minute per user
6. **Track Status**: `false` = unapproved, `true` = approved and live
7. **Artist Names**: Use comma or forward slash as separators: "Artist1,Artist2" or "Artist1/Artist2"
8. **Genre Names**: Multiple genres supported with same separator format

---

## 🎯 Quick Reference

| Action | Endpoint | Method | Auth | Role |
|--------|----------|--------|------|------|
| Upload Song (Original) | `/api/files/upload/song` | POST | ❌ | - |
| Upload Artist Song | `/api/files/upload/artist-song` | POST | ✅ | Artist |
| Get My Artists | `/api/artist/my-artists` | GET | ✅ | Artist |
| Get My Tracks | `/api/artist/my-tracks` | GET | ✅ | Artist |
| Update Track | `/api/artist/track/{id}` | PUT | ✅ | Artist |
| Delete Track | `/api/artist/track/{id}` | DELETE | ✅ | Artist |
| Create Album | `/api/artist/album` | POST | ✅ | Artist |
| Link User-Artist | `/api/admin/user-artist/link` | POST | ✅ | Admin |
| Get Unapproved Tracks | `/api/admin/user-artist/tracks/unapproved` | GET | ✅ | Admin |
| Approve Track | `/api/admin/user-artist/tracks/{id}/approve` | POST | ✅ | Admin |
| Bulk Approve | `/api/admin/user-artist/tracks/bulk-approve` | POST | ✅ | Admin |

---

This documentation covers all the artist content management APIs with comprehensive examples and error handling. The system provides secure, role-based access control with approval workflows for content moderation. 