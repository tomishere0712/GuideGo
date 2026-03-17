# GuideGo — API Documentation

> Base URL: `https://localhost:{port}/api`
> Tất cả request/response đều dùng **Content-Type: application/json**
> Các endpoint yêu cầu xác thực phải gửi header: `Authorization: Bearer <token>`

---

## Mục lục

1. [Auth](#1-auth)
2. [User](#2-user)
3. [Tour](#3-tour)
4. [Guide](#4-guide)
5. [Review](#5-review)
6. [Cart](#6-cart)
7. [Booking](#7-booking)
8. [Payment](#8-payment)
9. [Quy ước chung](#9-quy-ước-chung)

---

## 1. Auth

### POST `/api/auth/register`

Đăng ký tài khoản mới (Tourist).

**Auth:** Không cần

**Request Body:**
```json
{
  "full_name": "Nguyen Van A",
  "email": "nguyenvana@gmail.com",
  "password": "123456",
  "phone": "0901234567",
  "avatar_url": "https://example.com/avatar.jpg"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `full_name` | string | Có | |
| `email` | string | Có | Phải đúng định dạng email |
| `password` | string | Có | Tối thiểu 6 ký tự |
| `phone` | string | Có | Đúng 10 chữ số |
| `avatar_url` | string | Không | URL ảnh đại diện |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Register successfully."
}
```

**Response 400:**
```json
{
  "statusCode": 400,
  "message": "Email already exists."
}
```

---

### POST `/api/auth/login`

Đăng nhập, nhận JWT token.

**Auth:** Không cần

**Request Body:**
```json
{
  "email": "nguyenvana@gmail.com",
  "password": "123456"
}
```

| Field | Type | Bắt buộc |
|---|---|---|
| `email` | string | Có |
| `password` | string | Có |

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response 400:**
```json
{
  "token": null
}
```

> **Lưu ý:** Lưu token vào `AsyncStorage` hoặc `SecureStore`. Gửi kèm mọi request bảo vệ qua header `Authorization: Bearer <token>`.
> Token có hiệu lực **60 phút** (cấu hình trong `appsettings.json`).

---

## 2. User

> **Auth:** Bắt buộc (tất cả endpoints)

### GET `/api/user`

Lấy danh sách tất cả users.

**Auth:** Admin

**Response 200:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "full_name": "Nguyen Van A",
    "email": "nguyenvana@gmail.com",
    "phone": "0901234567",
    "avatar_url": "https://example.com/avatar.jpg",
    "role": "Tourist",
    "is_active": true,
    "created_at": "2024-01-15T08:30:00Z"
  }
]
```

---

### GET `/api/user/{id}`

Lấy thông tin một user theo ID.

**Auth:** Admin

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của user |

**Response 200:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "full_name": "Nguyen Van A",
  "email": "nguyenvana@gmail.com",
  "phone": "0901234567",
  "avatar_url": "https://example.com/avatar.jpg",
  "role": "Tourist",
  "is_active": true,
  "created_at": "2024-01-15T08:30:00Z"
}
```

**Response 404:**
```json
{ "message": "User not found." }
```

---

### POST `/api/user`

Tạo user mới (Admin tạo user với bất kỳ role nào).

**Auth:** Admin

**Request Body:**
```json
{
  "full_name": "Tran Thi B",
  "email": "tranthib@gmail.com",
  "password": "123456",
  "phone": "0912345678",
  "avatar_url": null,
  "role": "Guide"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `full_name` | string | Có | |
| `email` | string | Có | |
| `password` | string | Có | Tối thiểu 6 ký tự |
| `phone` | string | Có | Đúng 10 chữ số |
| `avatar_url` | string\|null | Không | |
| `role` | string | Có | `Tourist`, `Guide`, `Company`, `Admin` |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "User created successfully."
}
```

---

### PUT `/api/user/{id}`

Cập nhật thông tin user.

**Auth:** Người dùng (chỉ cập nhật chính mình) hoặc Admin (cập nhật bất kỳ ai)

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của user cần cập nhật |

**Request Body:**
```json
{
  "full_name": "Nguyen Van A Updated",
  "email": "nguyenvana@gmail.com",
  "phone": "0901234567",
  "password": "newpassword123",
  "avatar_url": "https://example.com/new-avatar.jpg",
  "role": null
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `full_name` | string | Có | |
| `email` | string | Có | |
| `phone` | string | Không | 10 chữ số |
| `password` | string | Không | Tối thiểu 6 ký tự |
| `avatar_url` | string\|null | Không | |
| `role` | string\|null | Không | Chỉ Admin mới được đổi role |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "User updated successfully."
}
```

**Response 403:**
```json
{
  "statusCode": 403,
  "message": "You don't have permission to update this user."
}
```

---

### DELETE `/api/user/{id}`

Xóa mềm user (đặt `is_active = false`).

**Auth:** Admin

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của user cần xóa |

**Response 200:**
```json
{ "message": "User deleted successfully." }
```

**Response 404:**
```json
{ "message": "User not found." }
```

---

## 3. Tour

> **Auth:** GET endpoints không cần token. POST/PUT/DELETE yêu cầu role Guide hoặc Admin.

### GET `/api/tour`

Lấy toàn bộ tour đang hoạt động (kèm Location, Guide, Schedules).

**Auth:** Không cần

**Response 200:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Tour Hà Nội 3 ngày 2 đêm",
    "description": "Khám phá Hà Nội cùng hướng dẫn viên chuyên nghiệp",
    "location_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
    "location_name": "Hồ Hoàn Kiếm",
    "city": "Hà Nội",
    "guide_id": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
    "guide_name": "Nguyen Van Guide",
    "guide_experience_years": 5,
    "guide_languages": ["Vietnamese", "English"],
    "guide_is_verified": true,
    "price_per_person": 1500000.00,
    "max_people": 20,
    "duration_days": 3,
    "rating": 4.5,
    "is_active": true,
    "created_at": "2024-01-15T08:30:00Z",
    "updated_at": "2024-01-15T08:30:00Z",
    "schedules": [
      {
        "id": "4fa85f64-5717-4562-b3fc-2c963f66afa6",
        "start_date": "2024-02-01",
        "end_date": "2024-02-03",
        "available_slots": 15
      }
    ]
  }
]
```

---

### GET `/api/tour/search`

Tìm kiếm và lọc tour theo nhiều tiêu chí, có phân trang.

**Auth:** Không cần

**Query Params:**

| Param | Type | Mặc định | Mô tả |
|---|---|---|---|
| `keyword` | string | - | Tìm theo tên hoặc mô tả tour |
| `city` | string | - | Lọc theo tên thành phố |
| `location_id` | guid | - | Lọc theo ID địa điểm |
| `guide_language` | string | - | Lọc theo ngôn ngữ hướng dẫn viên |
| `verified_guide_only` | bool | `false` | Chỉ hiện tour có guide đã xác minh |
| `min_price` | decimal | - | Giá tối thiểu (VNĐ) |
| `max_price` | decimal | - | Giá tối đa (VNĐ) |
| `start_date` | date | - | Lọc tour có lịch từ ngày này (format: `yyyy-MM-dd`) |
| `end_date` | date | - | Lọc tour có lịch đến ngày này |
| `sort_by` | string | `created_at` | Sắp xếp theo trường |
| `page` | int | `1` | Trang hiện tại |
| `page_size` | int | `20` | Số lượng kết quả mỗi trang |

**Ví dụ request:**
```
GET /api/tour/search?keyword=hà nội&city=Hà Nội&min_price=500000&max_price=3000000&verified_guide_only=true&page=1&page_size=10
```

**Response 200:**
```json
{
  "items": [ /* danh sách TourResponseDto */ ],
  "page": 1,
  "page_size": 10,
  "total_items": 42,
  "total_pages": 5
}
```

---

### GET `/api/tour/{id}`

Lấy chi tiết một tour theo ID.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của tour |

**Response 200:** Trả về object `TourResponseDto` (xem cấu trúc ở GET `/api/tour`)

**Response 404:**
```json
{
  "statusCode": 404,
  "message": "Không tìm thấy tour."
}
```

---

### POST `/api/tour`

Tạo tour mới.

**Auth:** Guide hoặc Admin

**Request Body:**
```json
{
  "title": "Tour Đà Nẵng 4 ngày 3 đêm",
  "description": "Khám phá Đà Nẵng, Hội An, Bà Nà Hills",
  "location_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
  "guide_id": null,
  "price_per_person": 2500000.00,
  "max_people": 15,
  "duration_days": 4
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `title` | string | Có | Tối đa 200 ký tự |
| `description` | string\|null | Không | |
| `location_id` | guid | Có | ID địa điểm phải tồn tại |
| `guide_id` | guid\|null | Không | Admin có thể chỉ định; Guide tự động map theo account |
| `price_per_person` | decimal | Có | Phải > 0 |
| `max_people` | int | Có | 1 – 1000 |
| `duration_days` | int | Có | 1 – 365 |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Tour created successfully.",
  "data": { /* TourResponseDto */ }
}
```

**Response 403:**
```json
{
  "statusCode": 403,
  "message": "Bạn không có quyền tạo tour."
}
```

---

### PUT `/api/tour/{id}`

Cập nhật tour. Guide chỉ được cập nhật tour của mình; Admin cập nhật bất kỳ tour nào.

**Auth:** Guide (owner) hoặc Admin

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của tour |

**Request Body:** (Giống với Create, tất cả field đều bắt buộc)
```json
{
  "title": "Tour Đà Nẵng Updated",
  "description": "Mô tả cập nhật",
  "location_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
  "guide_id": null,
  "price_per_person": 3000000.00,
  "max_people": 20,
  "duration_days": 4
}
```

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Tour updated successfully."
}
```

**Response 403:** Không có quyền
**Response 404:** Không tìm thấy tour

---

### DELETE `/api/tour/{id}`

Xóa mềm tour (`is_active = false`).

**Auth:** Guide (owner) hoặc Admin

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của tour |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Tour deleted successfully."
}
```

---

## 4. Guide

> **Auth:** Tất cả endpoints hiện không yêu cầu token (public).

### GET `/api/guides`

Lấy danh sách tất cả hướng dẫn viên.

**Auth:** Không cần

**Response 200:**
```json
{
  "message": "Get all guides successfully",
  "data": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "userId": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
      "experienceYears": 5,
      "languages": ["Vietnamese", "English", "French"],
      "description": "Hướng dẫn viên có kinh nghiệm 5 năm tại Hà Nội",
      "rating": 4.7,
      "isVerified": true
    }
  ]
}
```

---

### GET `/api/guides/{id}`

Lấy thông tin một hướng dẫn viên theo ID guide.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của guide profile |

**Response 200:**
```json
{
  "message": "Get guide successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
    "experienceYears": 5,
    "languages": ["Vietnamese", "English"],
    "description": "Mô tả hướng dẫn viên",
    "rating": 4.7,
    "isVerified": true
  }
}
```

**Response 404:**
```json
{ "message": "Guide not found" }
```

---

### GET `/api/guides/user/{userId}`

Lấy thông tin guide theo User ID (dùng khi có token của user đang đăng nhập).

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `userId` | guid | ID của user account |

**Response 200:**
```json
{
  "message": "Get guide by user successfully",
  "data": { /* GuideDto */ }
}
```

**Response 404:**
```json
{ "message": "Guide not found for this user" }
```

---

### POST `/api/guides`

Đăng ký trở thành hướng dẫn viên. Sau khi đăng ký cần chờ Admin xác minh.

**Auth:** Không cần (nhưng cần truyền `userId` hợp lệ trong body)

**Request Body:**
```json
{
  "userId": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
  "experienceYears": 3,
  "languages": ["Vietnamese", "English"],
  "description": "Tôi có 3 năm kinh nghiệm làm hướng dẫn viên tại Hội An"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `userId` | guid | Có | User phải tồn tại và chưa là guide |
| `experienceYears` | int | Có | Số năm kinh nghiệm |
| `languages` | string[] | Có | Danh sách ngôn ngữ |
| `description` | string\|null | Không | Giới thiệu bản thân |

**Response 200:**
```json
{
  "message": "Guide registration submitted successfully. Waiting for admin verification.",
  "data": { /* GuideDto */ }
}
```

**Response 400:**
```json
{ "message": "User has already registered as a guide" }
```

---

### PUT `/api/guides/{id}`

Cập nhật thông tin hướng dẫn viên.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của guide profile |

**Request Body:**
```json
{
  "experienceYears": 6,
  "languages": ["Vietnamese", "English", "Japanese"],
  "description": "Cập nhật mô tả mới"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `experienceYears` | int\|null | Không | |
| `languages` | string[]\|null | Không | |
| `description` | string\|null | Không | |

**Response 200:**
```json
{ "message": "Guide updated successfully" }
```

**Response 404:**
```json
{ "message": "Guide not found. Update failed." }
```

---

### DELETE `/api/guides/{id}`

Xóa hướng dẫn viên.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của guide profile |

**Response 200:**
```json
{ "message": "Guide deleted successfully" }
```

---

### PUT `/api/guides/{id}/verify`

Admin xác minh hướng dẫn viên (`isVerified = true`).

**Auth:** Admin (chưa enforce trong code, nên kiểm tra kỹ)

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của guide profile |

**Response 200:**
```json
{ "message": "Guide verified successfully" }
```

---

### DELETE `/api/guides/{id}/reject`

Admin từ chối yêu cầu đăng ký hướng dẫn viên (xóa guide profile).

**Auth:** Admin (chưa enforce trong code)

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của guide profile |

**Response 200:**
```json
{ "message": "Guide request rejected successfully" }
```

---

## 5. Review

> **Auth:** Tất cả endpoints yêu cầu role **Tourist**.

### GET `/api/review`

Lấy danh sách các review của **chính người dùng đang đăng nhập**.

**Auth:** Tourist

**Response 200:**
```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "tour_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
    "user_id": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
    "rating": 5,
    "comment": "Tour rất tuyệt vời, hướng dẫn viên nhiệt tình!",
    "created_at": "2024-01-20T10:00:00Z"
  }
]
```

---

### GET `/api/review/{id}`

Lấy chi tiết một review của chính người dùng.

**Auth:** Tourist

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của review |

**Response 200:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tour_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
  "user_id": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
  "rating": 5,
  "comment": "Tour rất tuyệt vời!",
  "created_at": "2024-01-20T10:00:00Z"
}
```

**Response 404:**
```json
{
  "statusCode": 404,
  "message": "Review not found."
}
```

---

### POST `/api/review`

Tạo review mới cho một tour.

**Auth:** Tourist

**Request Body:**
```json
{
  "tour_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
  "rating": 5,
  "comment": "Tour rất tuyệt vời, hướng dẫn viên nhiệt tình!"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `tour_id` | guid | Có | |
| `rating` | int | Có | 1 – 5 |
| `comment` | string\|null | Không | |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Review created successfully."
}
```

**Response 400:**
```json
{
  "statusCode": 400,
  "message": "You have already reviewed this tour."
}
```

---

### PUT `/api/review/{id}`

Cập nhật review của chính người dùng.

**Auth:** Tourist

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của review |

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Tour tốt nhưng có thể cải thiện thêm"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `rating` | int | Có | 1 – 5 |
| `comment` | string\|null | Không | |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Review updated successfully."
}
```

**Response 404:**
```json
{
  "statusCode": 404,
  "message": "Review not found."
}
```

---

### DELETE `/api/review/{id}`

Xóa review của chính người dùng.

**Auth:** Tourist

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của review |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Review deleted successfully."
}
```

---

## 6. Cart

> **Auth:** Yêu cầu role **Tourist** hoặc **Admin**.

### GET `/api/cart`

Lấy giỏ hàng của người dùng đang đăng nhập.

**Auth:** Tourist / Admin

**Response 200:**
```json
{
  "cart_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "user_id": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
  "items": [
    {
      "id": "4fa85f64-5717-4562-b3fc-2c963f66afa6",
      "tour_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
      "tour_title": "Tour Hà Nội 3 ngày 2 đêm",
      "schedule_id": "5fa85f64-5717-4562-b3fc-2c963f66afa6",
      "start_date": "2024-02-01",
      "end_date": "2024-02-03",
      "people_count": 2,
      "price_per_person": 1500000.00,
      "line_total": 3000000.00
    }
  ],
  "total_amount": 3000000.00
}
```

---

### POST `/api/cart/items`

Thêm một tour vào giỏ hàng.

**Auth:** Tourist / Admin

**Request Body:**
```json
{
  "tour_id": "1fa85f64-5717-4562-b3fc-2c963f66afa6",
  "schedule_id": "5fa85f64-5717-4562-b3fc-2c963f66afa6",
  "people_count": 2
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `tour_id` | guid | Có | Tour phải đang hoạt động |
| `schedule_id` | guid | Có | Lịch tour phải còn slot trống |
| `people_count` | int | Có | 1 – 1000; phải ≤ số slot còn lại |

**Response 200:**
```json
{
  "statusCode": 200,
  "message": "Item added to cart successfully."
}
```

**Response 400:**
```json
{
  "statusCode": 400,
  "message": "Not enough available slots."
}
```

---

## 7. Booking

> **Auth:** Không enforce trong code (public), nhưng cần `userId` hợp lệ.

### POST `/api/bookings`

Tạo booking từ toàn bộ giỏ hàng. Mỗi cart item sẽ trở thành một booking riêng.

**Auth:** Không cần (tự truyền cart_id)

**Request Body:**
```json
{
  "CartId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `CartId` | guid | Có | ID giỏ hàng (lấy từ GET `/api/cart`) |

**Response 200:** Trả về danh sách bookings được tạo
```json
[
  {
    "id": "6fa85f64-5717-4562-b3fc-2c963f66afa6",
    "userId": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
    "scheduleId": "5fa85f64-5717-4562-b3fc-2c963f66afa6",
    "tourTitle": "Tour Hà Nội 3 ngày 2 đêm",
    "startDate": "2024-02-01",
    "endDate": "2024-02-03",
    "peopleCount": 2,
    "totalPrice": 3000000.00,
    "status": "Pending",
    "createdAt": "2024-01-25T09:00:00Z"
  }
]
```

**Response 404:**
```json
{ "message": "Cart not found." }
```

**Response 400:**
```json
{ "message": "Cart is empty." }
```

---

### GET `/api/bookings`

Lấy danh sách booking của một user.

**Auth:** Không cần

**Query Params:**

| Param | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `userId` | guid | Có | ID của user |

**Ví dụ:**
```
GET /api/bookings?userId=2fa85f64-5717-4562-b3fc-2c963f66afa6
```

**Response 200:** Danh sách `BookingResponseDto`

---

### GET `/api/bookings/{id}`

Lấy chi tiết một booking.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của booking |

**Response 200:**
```json
{
  "id": "6fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "2fa85f64-5717-4562-b3fc-2c963f66afa6",
  "scheduleId": "5fa85f64-5717-4562-b3fc-2c963f66afa6",
  "tourTitle": "Tour Hà Nội 3 ngày 2 đêm",
  "startDate": "2024-02-01",
  "endDate": "2024-02-03",
  "peopleCount": 2,
  "totalPrice": 3000000.00,
  "status": "Pending",
  "createdAt": "2024-01-25T09:00:00Z"
}
```

**Response 404:**
```json
{ "message": "Booking not found." }
```

---

### PUT `/api/bookings/{id}/cancel`

Hủy booking.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của booking |

**Response 200:**
```json
{ "message": "Booking cancelled successfully." }
```

**Response 400:** Nếu booking không thể hủy (đã hoàn thành hoặc đã hủy trước đó)
```json
{ "message": "Booking cannot be cancelled." }
```

**Response 404:**
```json
{ "message": "Booking not found." }
```

---

## 8. Payment

> **Auth:** Không enforce trong code (public).

### POST `/api/payments`

Tạo payment cho một booking.

**Auth:** Không cần

**Request Body:**
```json
{
  "BookingId": "6fa85f64-5717-4562-b3fc-2c963f66afa6",
  "PaymentMethod": "CreditCard"
}
```

| Field | Type | Bắt buộc | Ghi chú |
|---|---|---|---|
| `BookingId` | guid | Có | ID booking đã tạo |
| `PaymentMethod` | string | Có | Ví dụ: `"CreditCard"`, `"BankTransfer"`, `"Cash"` |

**Response 200:**
```json
{
  "id": "7fa85f64-5717-4562-b3fc-2c963f66afa6",
  "bookingId": "6fa85f64-5717-4562-b3fc-2c963f66afa6",
  "amount": 3000000.00,
  "paymentMethod": "CreditCard",
  "status": "Pending",
  "paidAt": null
}
```

**Response 404:**
```json
{ "message": "Booking not found." }
```

**Response 400:**
```json
{ "message": "Payment already exists for this booking." }
```

---

### PUT `/api/payments/{id}/confirm`

Xác nhận thanh toán thành công. Đồng thời cập nhật booking status → `Confirmed`.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của payment |

**Response 200:**
```json
{
  "id": "7fa85f64-5717-4562-b3fc-2c963f66afa6",
  "bookingId": "6fa85f64-5717-4562-b3fc-2c963f66afa6",
  "amount": 3000000.00,
  "paymentMethod": "CreditCard",
  "status": "Completed",
  "paidAt": "2024-01-25T09:05:00Z"
}
```

---

### PUT `/api/payments/{id}/fail`

Đánh dấu thanh toán thất bại.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `id` | guid | ID của payment |

**Response 200:**
```json
{
  "id": "7fa85f64-5717-4562-b3fc-2c963f66afa6",
  "bookingId": "6fa85f64-5717-4562-b3fc-2c963f66afa6",
  "amount": 3000000.00,
  "paymentMethod": "CreditCard",
  "status": "Failed",
  "paidAt": null
}
```

---

### GET `/api/payments/booking/{bookingId}`

Lấy thông tin payment theo booking ID.

**Auth:** Không cần

**Path Params:**

| Param | Type | Mô tả |
|---|---|---|
| `bookingId` | guid | ID của booking |

**Response 200:** Trả về `PaymentResponseDto`

**Response 404:**
```json
{ "message": "Payment not found." }
```

---

## 9. Quy ước chung

### HTTP Status Codes

| Code | Ý nghĩa |
|------|---------|
| `200` | Thành công |
| `400` | Bad Request – dữ liệu không hợp lệ |
| `401` | Unauthorized – thiếu hoặc sai token |
| `403` | Forbidden – không có quyền thực hiện |
| `404` | Not Found – tài nguyên không tồn tại |
| `500` | Internal Server Error |

### Cấu trúc lỗi thường gặp

```json
{
  "statusCode": 400,
  "message": "Mô tả lỗi cụ thể"
}
```

### Gửi JWT Token

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Định dạng dữ liệu

| Kiểu | Format | Ví dụ |
|------|--------|-------|
| Date | `yyyy-MM-dd` | `"2024-02-01"` |
| DateTime | ISO 8601 UTC | `"2024-01-15T08:30:00Z"` |
| GUID | UUID v4 | `"3fa85f64-5717-4562-b3fc-2c963f66afa6"` |
| Decimal | Số thực | `1500000.00` |

### Luồng đặt tour (Happy Path)

```
1. POST /api/auth/login              → lấy token
2. GET  /api/tour/search             → tìm tour
3. GET  /api/tour/{id}               → xem chi tiết, lấy schedule_id
4. POST /api/cart/items              → thêm vào giỏ (cần token Tourist)
5. GET  /api/cart                    → xem giỏ hàng, lấy cart_id
6. POST /api/bookings                → tạo booking từ cart, lấy booking_id
7. POST /api/payments                → tạo payment
8. PUT  /api/payments/{id}/confirm   → xác nhận thanh toán thành công
```

### Roles

| Role | Mô tả |
|------|-------|
| `Tourist` | Đặt tour, viết review, quản lý giỏ hàng |
| `Guide` | Tạo và quản lý tour của mình |
| `Company` | Quản lý tour công ty |
| `Admin` | Toàn quyền hệ thống |
