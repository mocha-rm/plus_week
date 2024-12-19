# Plus_Week

## Health Check API
GET http://43.202.25.151:8080/health

## SignUp
POST http://43.202.25.151:8080/users

Request - JSON
```json
{
    "email": "test@mail.com",
    "nickname": "user1",
    "password": "1234",
    "role": "user"
}
```

## LogIn
POST http://43.202.25.151:8080/users/login

Request - JSON
```json
{
    "email": "test@mail.com",
    "password": "1234"
}
```

## Logout
POST http://43.202.25.151:8080/users/logout

## Report User
POST http://43.202.25.151:8080/admins/report-users

Request - JSON
```json
{
    "userIds": [1]
}
```

## Create Item
POST http://43.202.25.151:8080/items

Request - JSON
```json
{
    "name": "iphone2",
    "description": "iphone2",
    "managerId": 1,
    "ownerId": 1
}
```

## Create Reservation
POST http://43.202.25.151:8080/reservations

Request - JSON
```json
{
    "itemId": 1,
    "userId": 1,
    "startAt": "2024-12-15T14:30:00",
    "endAt": "2024-12-15T14:30:00"
}
```

## GetAllReservation
GET http://43.202.25.151:8080/reservations

Response - JSON
```json
[
    {
        "id": 1,
        "nickname": "user",
        "itemName": "iphone2",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    },
    {
        "id": 2,
        "nickname": "user",
        "itemName": "iphone3",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    },
    {
        "id": 3,
        "nickname": "user",
        "itemName": "iphone4",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    },
    {
        "id": 4,
        "nickname": "user",
        "itemName": "iphone5",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    }
]
```

## SearchReservation
GET http://43.202.25.151:8080/reservations/search?userId=1&itemId=1

Response - JSON
```json
[
    {
        "id": 4,
        "nickname": "user",
        "itemName": "iphone",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    },
    {
        "id": 3,
        "nickname": "user",
        "itemName": "iphone",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    },
    {
        "id": 2,
        "nickname": "user",
        "itemName": "iphone",
        "status": "PENDING",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    },
    {
        "id": 1,
        "nickname": "user",
        "itemName": "iphone",
        "status": "EXPIRED",
        "startAt": "2024-12-15T14:30:00",
        "endAt": "2024-12-15T14:30:00"
    }
]
```

## UpdateReservation
PATCH http://43.202.25.151:8080/reservations/{id}/update-status

Request - JSON
```json
{
    "status": "EXPIRED"
}
```
Response - JSON
```json
{
    "id": 2,
    "nickname": "user",
    "itemName": "iphone",
    "status": "CANCELED",
    "startAt": "2024-12-15T14:30:00",
    "endAt": "2024-12-15T14:30:00"
}
```



