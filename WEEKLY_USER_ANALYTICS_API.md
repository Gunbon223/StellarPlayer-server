# Weekly User Analytics API Documentation

## Overview
Simple API to get weekly new user registrations with subscription breakdown.

## Endpoint 
GET /api/admin/users/analytics/weekly

## Authentication
Requires admin authentication:


## Response Format

### Success Response (200 OK)
```json
{
  "period": "This Week",
  "week_start": "2024-01-15",
  "week_end": "2024-01-21", 
  "total_new_users": 5,
  "subscribed_users": 1,
  "non_subscribed_users": 4,
  "subscription_rate": "20.0%",
  "generated_at": "2024-01-18T10:30:00"
}
```

### Response Fields
| Field | Type | Description |
|-------|------|-------------|
| `period` | String | Always "This Week" |
| `week_start` | String | Start date of current week (Monday) |
| `week_end` | String | End date of current week (Sunday) |
| `total_new_users` | Integer | Total new users registered this week |
| `subscribed_users` | Integer | New users who have active subscriptions |
| `non_subscribed_users` | Integer | New users without subscriptions |
| `subscription_rate` | String | Percentage of new users with subscriptions |
| `generated_at` | String | Timestamp when report was generated |

### Error Responses

**401 Unauthorized**
```json
{
  "message": "Invalid JWT token",
  "status": 401
}
```

**403 Forbidden**
```json
{
  "message": "Access denied. Admin privileges required",
  "status": 403
}
```

## Example Usage

### cURL
```bash
curl -X GET "http://localhost:8080/api/admin/users/analytics/weekly" \
     -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript/Fetch
```javascript
fetch('/api/admin/users/analytics/weekly', {
  headers: {
    'Authorization': `Bearer ${adminToken}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Weekly Analytics:', data);
  console.log(`Subscription Rate: ${data.subscription_rate}`);
});
```

### Example Response Scenarios

**Scenario 1: High Subscription Rate**
```json
{
  "period": "This Week",
  "week_start": "2024-01-15",
  "week_end": "2024-01-21",
  "total_new_users": 10,
  "subscribed_users": 8,
  "non_subscribed_users": 2,
  "subscription_rate": "80.0%",
  "generated_at": "2024-01-18T10:30:00"
}
```

**Scenario 2: No New Users**
```json
{
  "period": "This Week", 
  "week_start": "2024-01-15",
  "week_end": "2024-01-21",
  "total_new_users": 0,
  "subscribed_users": 0,
  "non_subscribed_users": 0,
  "subscription_rate": "0.0%",
  "generated_at": "2024-01-18T10:30:00"
}
```

**Scenario 3: Your Current Situation**
```json
{
  "period": "This Week",
  "week_start": "2024-01-15", 
  "week_end": "2024-01-21",
  "total_new_users": 5,
  "subscribed_users": 1,
  "non_subscribed_users": 4, 
  "subscription_rate": "20.0%",
  "generated_at": "2024-01-18T10:30:00"
}
```

## Implementation Notes

### Subscription Logic
The API checks each user's subscription status using the `checkUserSubscriptionStatus()` method. You need to implement this method based on your actual subscription table structure.

### Week Calculation
- Week starts on Monday and ends on Sunday
- Uses current week (today's week)
- Time range: Monday 00:00:00 to Sunday 23:59:59

### Accuracy
- Counts only users registered within the current week
- Subscription status is checked individually for each user
- Percentage is rounded to 2 decimal places

## Frontend Integration

### Chart.js Example
```javascript
// Fetch and display weekly data
async function loadWeeklyAnalytics() {
  const response = await fetch('/api/admin/users/analytics/weekly', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const data = await response.json();
  
  // Create pie chart for subscription breakdown
  const ctx = document.getElementById('weeklyChart').getContext('2d');
  new Chart(ctx, {
    type: 'pie',
    data: {
      labels: ['Subscribed Users', 'Non-Subscribed Users'],
      datasets: [{
        data: [data.subscribed_users, data.non_subscribed_users],
        backgroundColor: ['#4CAF50', '#FF9800']
      }]
    },
    options: {
      title: {
        display: true,
        text: `Weekly User Analytics (${data.week_start} to ${data.week_end})`
      }
    }
  });
}
```

### React Example
```jsx
import { useState, useEffect } from 'react';

function WeeklyAnalytics() {
  const [data, setData] = useState(null);
  
  useEffect(() => {
    fetch('/api/admin/users/analytics/weekly', {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    })
    .then(res => res.json())
    .then(setData);
  }, []);
  
  if (!data) return <div>Loading...</div>;
  
  return (
    <div className="weekly-analytics">
      <h3>This Week's User Analytics</h3>
      <p>Period: {data.week_start} to {data.week_end}</p>
      <div className="stats">
        <div>Total New Users: {data.total_new_users}</div>
        <div>Subscribed: {data.subscribed_users}</div>
        <div>Non-Subscribed: {data.non_subscribed_users}</div>
        <div>Subscription Rate: {data.subscription_rate}</div>
      </div>
    </div>
  );
}
```

## Testing

### Test Cases
1. **Week with no new users** - Should return all zeros
2. **Week with all subscribed users** - Should return 100%
3. **Week with no subscribed users** - Should return 0%
4. **Mixed subscription status** - Should calculate correct percentage

### Sample Test Data
To test the API properly, you need:
1. Users registered within the current week
2. Some users with active subscriptions
3. Some users without subscriptions

This will give you accurate subscription rate calculations.