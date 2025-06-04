# StellarPlayer Music Recommendation System API

## Overview

The StellarPlayer Music Recommendation System is a sophisticated Machine Learning-based recommendation engine that provides personalized music suggestions using multiple algorithms including Content-Based Filtering, Collaborative Filtering, and Hybrid approaches.

## Features

- **8 Recommendation Types**: Content-Based, Collaborative, Hybrid, Similar Tracks, Trending, New Releases, Genre-Based, Artist-Based
- **Machine Learning Algorithms**: TF-IDF + Cosine Similarity, User-Based Collaborative Filtering
- **Intelligent Scoring**: User interaction tracking with sophisticated scoring algorithms
- **Hybrid Approach**: Combines multiple algorithms for enhanced accuracy
- **Real-time Learning**: Continuous improvement through user interaction recording

## Base URL

```
http://localhost:8080/api/recommendations
```

## Authentication

All endpoints require proper authentication. Include JWT token in Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Endpoints

### 1. Get Personalized Recommendations (Flexible)

**GET** `/personalized/{userId}`

Get personalized recommendations with full customization options.

#### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `userId` | Integer | Required | User ID |
| `type` | String | "HYBRID" | Recommendation type |
| `limit` | Integer | 15 | Number of recommendations |
| `minScore` | Double | 2.0 | Minimum interaction score |
| `diversityFactor` | Double | 0.3 | Diversity factor (0.0-1.0) |
| `includeKnown` | Boolean | false | Include known tracks |

#### Recommendation Types
- `CONTENT_BASED` - Based on track features
- `COLLABORATIVE` - Based on similar users
- `HYBRID` - Combination of both (recommended)
- `SIMILAR_TRACKS` - Similar to specific track
- `TRENDING` - Popular tracks
- `NEW_RELEASES` - Recently added tracks
- `GENRE_BASED` - Based on preferred genres
- `ARTIST_BASED` - Based on preferred artists

#### Example Request
```bash
GET /api/recommendations/personalized/123?type=HYBRID&limit=20&minScore=2.5&diversityFactor=0.4
```

#### Example Response
```json
{
  "recommendedTracks": [
    {
      "track": {
        "id": 456,
        "title": "Amazing Song",
        "duration": 180,
        "artists": [{"id": 10, "name": "Great Artist"}],
        "genres": [{"id": 5, "name": "Pop"}],
        "playCount": 1500
      },
      "recommendationScore": 0.85,
      "confidence": 0.78,
      "reason": "Based on similar users and your preferences",
      "tags": ["pop", "Great Artist", "medium duration"]
    }
  ],
  "recommendationType": "HYBRID",
  "totalRecommendations": 20,
  "averageConfidence": 0.72,
  "algorithm": "Hybrid (Collaborative + Content-Based)"
}
```

---

### 2. Hybrid Recommendations (Recommended)

**GET** `/hybrid/{userId}`

Get the best recommendations using the hybrid algorithm.

#### Parameters
- `userId` (Integer, required): User ID
- `limit` (Integer, default: 10): Number of recommendations
- `minScore` (Double, default: 2.0): Minimum interaction score
- `includeKnown` (Boolean, default: false): Include known tracks

#### Example Request
```bash
GET /api/recommendations/hybrid/123?limit=15&minScore=2.5
```

---

### 3. Content-Based Recommendations

**GET** `/content-based/{userId}`

Get recommendations based on track features and user preferences.

#### Parameters
- `userId` (Integer, required): User ID
- `limit` (Integer, default: 10): Number of recommendations
- `minScore` (Double, default: 2.0): Minimum interaction score

#### Example Request
```bash
GET /api/recommendations/content-based/123?limit=12
```

---

### 4. Collaborative Filtering Recommendations

**GET** `/collaborative/{userId}`

Get recommendations based on similar users' preferences.

#### Parameters
- `userId` (Integer, required): User ID
- `limit` (Integer, default: 10): Number of recommendations
- `minScore` (Double, default: 2.0): Minimum interaction score

#### Example Request
```bash
GET /api/recommendations/collaborative/123?limit=15
```

---

### 5. Similar Tracks

**GET** `/similar/{trackId}`

Get tracks similar to a specific track.

#### Parameters
- `trackId` (Integer, required): Track ID to find similar tracks for
- `limit` (Integer, default: 10): Number of recommendations

#### Example Request
```bash
GET /api/recommendations/similar/789?limit=8
```

---

### 6. Trending Tracks

**GET** `/trending`

Get currently trending tracks based on recent activity.

#### Parameters
- `limit` (Integer, default: 20): Number of recommendations

#### Example Request
```bash
GET /api/recommendations/trending?limit=25
```

---

### 7. New Releases

**GET** `/new-releases`

Get recently released tracks.

#### Parameters
- `limit` (Integer, default: 20): Number of recommendations

#### Example Request
```bash
GET /api/recommendations/new-releases?limit=15
```

---

### 8. Genre-Based Recommendations

**GET** `/genre`

Get recommendations based on specific genres.

#### Parameters
- `genreIds` (List<Integer>, required): List of genre IDs
- `limit` (Integer, default: 15): Number of recommendations

#### Example Request
```bash
GET /api/recommendations/genre?genreIds=1,3,5&limit=20
```

---

### 9. Artist-Based Recommendations

**GET** `/artist`

Get recommendations based on specific artists.

#### Parameters
- `artistIds` (List<Integer>, required): List of artist IDs
- `limit` (Integer, default: 15): Number of recommendations

#### Example Request
```bash
GET /api/recommendations/artist?artistIds=10,15,22&limit=18
```

---

### 10. Custom Recommendations (Advanced)

**POST** `/`

Create custom recommendations with full control over parameters.

#### Request Body
```json
{
  "userId": 123,
  "type": "HYBRID",
  "limit": 15,
  "seedTrackId": 456,
  "seedArtistIds": [10, 15],
  "seedGenreIds": [1, 3, 5],
  "minInteractionScore": 2.5,
  "includeKnownTracks": false,
  "diversityFactor": 0.4
}
```

#### Example Request
```bash
POST /api/recommendations
Content-Type: application/json

{
  "userId": 123,
  "type": "CONTENT_BASED",
  "limit": 20,
  "minInteractionScore": 3.0,
  "diversityFactor": 0.5
}
```

---

### 11. Record User Interaction

**POST** `/interaction`

Record user interactions for machine learning improvement.

#### Parameters
- `userId` (Integer, required): User ID
- `trackId` (Integer, required): Track ID
- `interactionType` (String, required): Type of interaction
- `listenTime` (Long, optional): Listen time in seconds

#### Interaction Types
- `play` - User played the track
- `skip` - User skipped the track
- `like` - User liked the track
- `unlike` - User unliked the track
- `share` - User shared the track

#### Example Request
```bash
POST /api/recommendations/interaction?userId=123&trackId=456&interactionType=play&listenTime=145
```

---

## Response Format

All recommendation endpoints return the following structure:

```json
{
  "recommendedTracks": [
    {
      "track": {
        "id": Integer,
        "title": String,
        "duration": Integer,
        "artists": [{"id": Integer, "name": String}],
        "genres": [{"id": Integer, "name": String}],
        "album": {"id": Integer, "title": String},
        "playCount": Integer,
        "status": Boolean
      },
      "recommendationScore": Double,
      "confidence": Double,
      "reason": String,
      "tags": [String]
    }
  ],
  "recommendationType": String,
  "totalRecommendations": Integer,
  "averageConfidence": Double,
  "algorithm": String
}
```

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid parameter: limit must be between 1 and 100",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "User with ID 123 not found",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "Error generating recommendations",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## Algorithm Details

### Content-Based Filtering
- Uses TF-IDF vectorization on track features
- Cosine similarity for track comparison
- Features: genres, artists, duration, popularity
- Confidence: 80% of similarity score

### Collaborative Filtering
- User-based collaborative filtering
- Cosine similarity + Jaccard coefficient
- Finds similar users based on interaction patterns
- Confidence: 70% of similarity score

### Hybrid Algorithm
- Combines collaborative (60%) and content-based (40%)
- Weighted score combination
- Enhanced accuracy through algorithm fusion

### Interaction Scoring
Formula: `playCount + listenTimeRatio*2 + likes*1.5 + shares - skips*0.5`
- Range: 0.0 - 5.0
- Real-time calculation
- Influences future recommendations

## Configuration

The system can be configured via `application.properties`:

```properties
# Hybrid weights
stellarplayer.recommendation.hybrid.collaborative-weight=0.6
stellarplayer.recommendation.hybrid.content-based-weight=0.4

# Content-based settings
stellarplayer.recommendation.content-based.confidence-multiplier=0.8
stellarplayer.recommendation.content-based.similarity-threshold=0.1

# Collaborative settings
stellarplayer.recommendation.collaborative.confidence-multiplier=0.7
stellarplayer.recommendation.collaborative.max-similar-users=50

# General settings
stellarplayer.recommendation.general.default-min-interaction-score=2.0
stellarplayer.recommendation.general.trending-days=7
stellarplayer.recommendation.general.new-releases-days=30
```

## Best Practices

1. **Use Hybrid Algorithm**: Provides the best balance of accuracy and diversity
2. **Record Interactions**: Always record user interactions for continuous learning
3. **Adjust Diversity**: Use `diversityFactor` to balance accuracy vs discovery
4. **Monitor Confidence**: Higher confidence scores indicate better recommendations
5. **Handle New Users**: System provides fallback recommendations for users with no history

## Rate Limiting

- 100 requests per minute per user
- 1000 requests per hour per user
- Burst limit: 20 requests per 10 seconds

## Dependencies

The recommendation system uses:
- Apache Commons Math3 (statistical calculations)
- Apache Mahout (machine learning algorithms)
- Apache Lucene (text processing and TF-IDF)
- Spring Boot (framework)
- MySQL (data persistence) 