# User Favourites API Documentation

## Overview

The User Favourites API allows authenticated users to manage their favourite tracks, albums, artists, and playlists. The system implements proper JWT validation and user-specific access control. All favourite responses include the date when items were added to favourites.

## Authentication

All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Access Control

- **Regular Users**: Can only access their own favourites
- **Admin Users**: Can access any user's favourites by providing the `userId` parameter
- **Statistics Endpoints**: Admin access only

## Base URL

```
/api/favourites
```

## Track Favourites

### Get User's Favourite Tracks (Non-Paginated)
```http
GET /api/favourites/tracks?userId={userId}
```

**Parameters:**
- `userId` (optional): User ID (defaults to authenticated user, admin can specify any user)

**Response:**
```json
{
  "tracks": [
    {
      "id": 1,
      "title": "Song Title",
      "duration": 240,
      "status": true,
      "path": "/path/to/song.mp3",
      "cover": "/path/to/cover.jpg",
      "lyrics": "Song lyrics...",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "artists": [
        {
          "id": 1,
          "name": "Artist Name",
          "bio": "Artist biography"
        }
      ],
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "count": 1
}
```

### Get User's Favourite Tracks (Paginated)
```http
GET /api/favourites/tracks/paginated?page={page}&pageSize={pageSize}&sortOrder={sortOrder}&sortBy={sortBy}&userId={userId}
```

**Parameters:**
- `page` (optional, default: 0): Page number (zero-based)
- `pageSize` (optional, default: 10): Number of items per page
- `sortOrder` (optional, default: "desc"): Sort direction ("asc" or "desc")
- `sortBy` (optional, default: "created"): Field to sort by ("id", "title", "created", "updated")
- `userId` (optional): User ID (defaults to authenticated user, admin can specify any user)

**Response:**
```json
{
  "tracks": [
    {
      "id": 1,
      "title": "Song Title",
      "duration": 240,
      "status": true,
      "path": "/path/to/song.mp3",
      "cover": "/path/to/cover.jpg",
      "lyrics": "Song lyrics...",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "artists": [
        {
          "id": 1,
          "name": "Artist Name",
          "bio": "Artist biography"
        }
      ],
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 25,
  "totalPages": 3,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

### Add Track to Favourites
```http
POST /api/favourites/tracks/{trackId}?userId={userId}
```

**Response:**
```json
{
  "message": "Track added to favourites successfully",
  "favourite": {
    "id": 1,
    "createdAt": "2024-01-01T12:00:00",
    "user": {...},
    "track": {...}
  }
}
```

### Remove Track from Favourites
```http
DELETE /api/favourites/tracks/{trackId}?userId={userId}
```

**Response:**
```json
{
  "message": "Track removed from favourites successfully"
}
```

### Check if Track is Favourite
```http
GET /api/favourites/tracks/{trackId}/check?userId={userId}
```

**Response:**
```json
{
  "isFavourite": true
}
```

## Album Favourites

### Get User's Favourite Albums (Non-Paginated)
```http
GET /api/favourites/albums?userId={userId}
```

**Response:**
```json
{
  "albums": [
    {
      "id": 1,
      "title": "Album Title",
      "cover": "/path/to/album-cover.jpg",
      "releaseDate": "2023-06-15T00:00:00",
      "status": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "artists": [
        {
          "id": 1,
          "name": "Artist Name",
          "bio": "Artist biography"
        }
      ],
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "count": 1
}
```

### Get User's Favourite Albums (Paginated)
```http
GET /api/favourites/albums/paginated?page={page}&pageSize={pageSize}&sortOrder={sortOrder}&sortBy={sortBy}&userId={userId}
```

**Parameters:**
- `page` (optional, default: 0): Page number (zero-based)
- `pageSize` (optional, default: 10): Number of items per page
- `sortOrder` (optional, default: "desc"): Sort direction ("asc" or "desc")
- `sortBy` (optional, default: "created"): Field to sort by ("id", "title", "created", "updated")
- `userId` (optional): User ID (defaults to authenticated user, admin can specify any user)

**Response:**
```json
{
  "albums": [
    {
      "id": 1,
      "title": "Album Title",
      "cover": "/path/to/album-cover.jpg",
      "releaseDate": "2023-06-15T00:00:00",
      "status": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "artists": [
        {
          "id": 1,
          "name": "Artist Name",
          "bio": "Artist biography"
        }
      ],
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 15,
  "totalPages": 2,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

### Add Album to Favourites
```http
POST /api/favourites/albums/{albumId}?userId={userId}
```

### Remove Album from Favourites
```http
DELETE /api/favourites/albums/{albumId}?userId={userId}
```

### Check if Album is Favourite
```http
GET /api/favourites/albums/{albumId}/check?userId={userId}
```

## Artist Favourites

### Get User's Favourite Artists (Non-Paginated)
```http
GET /api/favourites/artists?userId={userId}
```

**Response:**
```json
{
  "artists": [
    {
      "id": 1,
      "name": "Artist Name",
      "bio": "Artist biography",
      "image": "/path/to/artist-image.jpg",
      "status": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "count": 1
}
```

### Get User's Favourite Artists (Paginated)
```http
GET /api/favourites/artists/paginated?page={page}&pageSize={pageSize}&sortOrder={sortOrder}&sortBy={sortBy}&userId={userId}
```

**Parameters:**
- `page` (optional, default: 0): Page number (zero-based)
- `pageSize` (optional, default: 10): Number of items per page
- `sortOrder` (optional, default: "desc"): Sort direction ("asc" or "desc")
- `sortBy` (optional, default: "created"): Field to sort by ("id", "name", "created", "updated")
- `userId` (optional): User ID (defaults to authenticated user, admin can specify any user)

**Response:**
```json
{
  "artists": [
    {
      "id": 1,
      "name": "Artist Name",
      "bio": "Artist biography",
      "image": "/path/to/artist-image.jpg",
      "status": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 8,
  "totalPages": 1,
  "pageSize": 10,
  "hasNext": false,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": true
}
```

### Add Artist to Favourites
```http
POST /api/favourites/artists/{artistId}?userId={userId}
```

### Remove Artist from Favourites
```http
DELETE /api/favourites/artists/{artistId}?userId={userId}
```

### Check if Artist is Favourite
```http
GET /api/favourites/artists/{artistId}/check?userId={userId}
```

## Playlist Favourites

### Get User's Favourite Playlists (Non-Paginated)
```http
GET /api/favourites/playlists?userId={userId}
```

**Response:**
```json
{
  "playlists": [
    {
      "id": 1,
      "name": "My Playlist",
      "description": "Playlist description",
      "cover": "/path/to/playlist-cover.jpg",
      "status": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "count": 1
}
```

### Get User's Favourite Playlists (Paginated)
```http
GET /api/favourites/playlists/paginated?page={page}&pageSize={pageSize}&sortOrder={sortOrder}&sortBy={sortBy}&userId={userId}
```

**Parameters:**
- `page` (optional, default: 0): Page number (zero-based)
- `pageSize` (optional, default: 10): Number of items per page
- `sortOrder` (optional, default: "desc"): Sort direction ("asc" or "desc")
- `sortBy` (optional, default: "created"): Field to sort by ("id", "name", "created", "updated")
- `userId` (optional): User ID (defaults to authenticated user, admin can specify any user)

**Response:**
```json
{
  "playlists": [
    {
      "id": 1,
      "name": "My Playlist",
      "description": "Playlist description",
      "cover": "/path/to/playlist-cover.jpg",
      "status": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "favouriteAddedAt": "2024-01-15T14:30:00"
    }
  ],
  "currentPage": 0,
  "totalItems": 12,
  "totalPages": 2,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

### Add Playlist to Favourites
```http
POST /api/favourites/playlists/{playlistId}?userId={userId}
```

### Remove Playlist from Favourites
```http
DELETE /api/favourites/playlists/{playlistId}?userId={userId}
```

### Check if Playlist is Favourite
```http
GET /api/favourites/playlists/{playlistId}/check?userId={userId}
```

## Statistics (Admin Only)

### Get Track Favourite Count
```http
GET /api/favourites/stats/tracks/{trackId}
```

**Response:**
```json
{
  "trackId": 1,
  "favouriteCount": 25
}
```

### Get Album Favourite Count
```http
GET /api/favourites/stats/albums/{albumId}
```

### Get Artist Favourite Count
```http
GET /api/favourites/stats/artists/{artistId}
```

### Get Playlist Favourite Count
```http
GET /api/favourites/stats/playlists/{playlistId}
```

## Error Responses

### 400 Bad Request
```json
{
  "message": "Failed to add track to favourites: Track is already in favourites"
}
```

### 401 Unauthorized
```json
{
  "message": "Invalid JWT token: Token is expired"
}
```

### 403 Forbidden
```json
{
  "message": "Access denied. You can only access your own favourites"
}
```

### 404 Not Found
```json
{
  "message": "Failed to remove track from favourites: Track not found in favourites"
}
```

## Usage Examples

### JavaScript/Frontend Usage

```javascript
// Get paginated favourite tracks
const getPaginatedFavouriteTracks = async (page = 0, pageSize = 10, sortBy = 'created', sortOrder = 'desc') => {
  const response = await fetch(`/api/favourites/tracks/paginated?page=${page}&pageSize=${pageSize}&sortBy=${sortBy}&sortOrder=${sortOrder}`, {
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  
  const result = await response.json();
  // result.tracks now contains FavouriteTrackDTO objects with favouriteAddedAt field
  return result;
};

// Get user's favourite tracks (non-paginated)
const getFavouriteTracks = async () => {
  const response = await fetch('/api/favourites/tracks', {
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  
  const result = await response.json();
  // result.tracks contains FavouriteTrackDTO objects with favouriteAddedAt field
  return result.tracks;
};

// Display favourite with added date
const displayFavouriteTrack = (track) => {
  console.log(`Track: ${track.title}`);
  console.log(`Added to favourites: ${new Date(track.favouriteAddedAt).toLocaleDateString()}`);
  console.log(`Artists: ${track.artists.map(a => a.name).join(', ')}`);
};

// Add track to favourites
const addToFavourites = async (trackId) => {
  const response = await fetch(`/api/favourites/tracks/${trackId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${userToken}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (response.ok) {
    const result = await response.json();
    console.log(result.message);
  }
};

// Check if track is favourite
const checkFavourite = async (trackId) => {
  const response = await fetch(`/api/favourites/tracks/${trackId}/check`, {
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  
  const result = await response.json();
  return result.isFavourite;
};
```

### cURL Examples

```bash
# Get paginated favourite tracks
curl -X GET "http://localhost:8080/api/favourites/tracks/paginated?page=0&pageSize=5&sortBy=title&sortOrder=asc" \
  -H "Authorization: Bearer your-jwt-token"

# Get paginated favourite albums
curl -X GET "http://localhost:8080/api/favourites/albums/paginated?page=1&pageSize=20&sortBy=created&sortOrder=desc" \
  -H "Authorization: Bearer your-jwt-token"

# Add track to favourites
curl -X POST "http://localhost:8080/api/favourites/tracks/1" \
  -H "Authorization: Bearer your-jwt-token"

# Get favourite tracks (non-paginated) - returns FavouriteTrackDTO with favouriteAddedAt
curl -X GET "http://localhost:8080/api/favourites/tracks" \
  -H "Authorization: Bearer your-jwt-token"

# Check if track is favourite
curl -X GET "http://localhost:8080/api/favourites/tracks/1/check" \
  -H "Authorization: Bearer your-jwt-token"

# Admin: Get track favourite count
curl -X GET "http://localhost:8080/api/favourites/stats/tracks/1" \
  -H "Authorization: Bearer admin-jwt-token"
```

## Pagination Parameters

### Supported Sort Fields
- **id**: Sort by favourite record ID
- **title**: Sort by track/album title or artist/playlist name
- **created** / **createddate** / **createdat**: Sort by creation date (default)
- **updated** / **updateddate** / **updatedat**: Sort by last update date

### Sort Order
- **asc**: Ascending order
- **desc**: Descending order (default)

### Page Parameters
- **page**: Zero-based page number (default: 0)
- **pageSize**: Number of items per page (default: 10, recommended max: 100)

### Response Pagination Metadata
- **currentPage**: Current page number (zero-based)
- **totalItems**: Total number of favourite items
- **totalPages**: Total number of pages
- **pageSize**: Number of items per page
- **hasNext**: Whether there is a next page
- **hasPrevious**: Whether there is a previous page
- **isFirst**: Whether this is the first page
- **isLast**: Whether this is the last page

## Database Schema

The system uses separate tables for each favourite type:

### user_favourite_track
- `id` (Primary Key)
- `user_id` (Foreign Key to users table)
- `track_id` (Foreign Key to tracks table)
- `created_at`
- `updated_at`
- Unique constraint on (user_id, track_id)

### user_favourite_album
- `id` (Primary Key)
- `user_id` (Foreign Key to users table)
- `album_id` (Foreign Key to albums table)
- `created_at`
- `updated_at`
- Unique constraint on (user_id, album_id)

### user_favourite_artist
- `id` (Primary Key)
- `user_id` (Foreign Key to users table)
- `artist_id` (Foreign Key to artists table)
- `created_at`
- `updated_at`
- Unique constraint on (user_id, artist_id)

### user_favourite_playlist
- `id` (Primary Key)
- `user_id` (Foreign Key to users table)
- `playlist_id` (Foreign Key to playlists table)
- `created_at`
- `updated_at`
- Unique constraint on (user_id, playlist_id)

## Benefits of This Design

1. **Type Safety**: Each favourite type has its own entity with proper foreign key relationships
2. **Performance**: No nullable columns, better indexing, more efficient queries
3. **Maintainability**: Clear separation of concerns, easier to understand and maintain
4. **Extensibility**: Easy to add specific fields for different favourite types if needed
5. **Database Integrity**: Proper foreign key constraints ensure data consistency
6. **Security**: JWT validation and user-specific access control
7. **Scalability**: Separate tables allow for better query optimization and indexing
8. **Pagination Support**: Efficient pagination with Spring Data JPA for large datasets
9. **Rich Response Data**: DTOs include complete entity information plus favourite metadata
10. **Temporal Tracking**: `favouriteAddedAt` field allows tracking when items were favourited

## Security Features

- JWT token validation on all endpoints
- User can only access their own favourites (unless admin)
- Admin role verification for statistics endpoints
- Proper error handling and logging
- Protection against duplicate favourites
- Validation of entity existence before operations
- Pagination limits to prevent excessive data retrieval

## Response Format Changes

### New DTO Structure

All favourite endpoints now return DTOs that include:

1. **Complete Entity Information**: All fields from the original entity (track, album, artist, playlist)
2. **Favourite Added Date**: `favouriteAddedAt` field showing when the item was added to favourites
3. **Associated Entities**: Related entities like artists for tracks/albums

### Track Favourite Response
- Includes all track fields: `id`, `title`, `duration`, `status`, `path`, `cover`, `lyrics`, `createdAt`, `updatedAt`
- Includes associated `artists` array
- Includes `favouriteAddedAt` timestamp

### Album Favourite Response
- Includes all album fields: `id`, `title`, `cover`, `releaseDate`, `status`, `createdAt`, `updatedAt`
- Includes associated `artists` array
- Includes `favouriteAddedAt` timestamp

### Artist Favourite Response
- Includes all artist fields: `id`, `name`, `bio`, `image`, `status`, `createdAt`, `updatedAt`
- Includes `favouriteAddedAt` timestamp

### Playlist Favourite Response
- Includes all playlist fields: `id`, `name`, `description`, `cover`, `status`, `createdAt`, `updatedAt`
- Includes `favouriteAddedAt` timestamp

### cURL Examples

```bash
# Get paginated favourite tracks
curl -X GET "http://localhost:8080/api/favourites/tracks/paginated?page=0&pageSize=5&sortBy=title&sortOrder=asc" \
  -H "Authorization: Bearer your-jwt-token"

# Get paginated favourite albums
curl -X GET "http://localhost:8080/api/favourites/albums/paginated?page=1&pageSize=20&sortBy=created&sortOrder=desc" \
  -H "Authorization: Bearer your-jwt-token"

# Add track to favourites
curl -X POST "http://localhost:8080/api/favourites/tracks/1" \
  -H "Authorization: Bearer your-jwt-token"

# Get favourite tracks (non-paginated) - returns FavouriteTrackDTO with favouriteAddedAt
curl -X GET "http://localhost:8080/api/favourites/tracks" \
  -H "Authorization: Bearer your-jwt-token"

# Check if track is favourite
curl -X GET "http://localhost:8080/api/favourites/tracks/1/check" \
  -H "Authorization: Bearer your-jwt-token"

# Admin: Get track favourite count
curl -X GET "http://localhost:8080/api/favourites/stats/tracks/1" \
  -H "Authorization: Bearer admin-jwt-token"
``` 