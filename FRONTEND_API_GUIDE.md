# GuideGo — Tài liệu tích hợp API cho Front-end (Admin & Guide)

> **Phạm vi tài liệu**: Dành cho Front-end team tích hợp API và xây dựng giao diện cho **Role Admin** và **Role Guide**.
> Role Tourist đã được xử lý riêng.
> **Base URL**: cần thay `{{BASE_URL}}` bằng địa chỉ server thực tế (ví dụ: `https://api.guidego.com`).

---

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Authentication — Đăng nhập & JWT](#2-authentication--đăng-nhập--jwt)
3. [Phân quyền chi tiết theo Role](#3-phân-quyền-chi-tiết-theo-role)
4. [FLOW & API — Role ADMIN](#4-flow--api--role-admin)
   - 4.1 [Quản lý User](#41-quản-lý-user)
   - 4.2 [Quản lý Guide (Duyệt/Từ chối)](#42-quản-lý-guide-duyệttừ-chối)
   - 4.3 [Quản lý Tour](#43-quản-lý-tour)
   - 4.4 [Quản lý Tour Schedule](#44-quản-lý-tour-schedule)
   - 4.5 [Quản lý Location](#45-quản-lý-location)
   - 4.6 [Quản lý Review](#46-quản-lý-review)
5. [FLOW & API — Role GUIDE](#5-flow--api--role-guide)
   - 5.1 [Đăng ký trở thành Guide](#51-đăng-ký-trở-thành-guide)
   - 5.2 [Quản lý hồ sơ Guide](#52-quản-lý-hồ-sơ-guide)
   - 5.3 [Quản lý Tour](#53-quản-lý-tour)
   - 5.4 [Upload ảnh Tour](#54-upload-ảnh-tour)
   - 5.5 [Quản lý Tour Schedule](#55-quản-lý-tour-schedule)
   - 5.6 [Chat với Tourist](#56-chat-với-tourist)
6. [Enums & Trạng thái](#6-enums--trạng-thái)
7. [Cấu trúc Response chung](#7-cấu-trúc-response-chung)
8. [Lưu ý quan trọng](#8-lưu-ý-quan-trọng)

---

## 1. Tổng quan hệ thống

| Thành phần | Công nghệ |
|---|---|
| Backend | ASP.NET Core 8 |
| Database | PostgreSQL |
| Authentication | JWT Bearer Token |
| Realtime | SignalR (Chat) |
| Payment | VNPay |
| Image Upload | Cloudinary |

### Các Role trong hệ thống

| Role | Mô tả |
|---|---|
| `Tourist` | Người dùng thông thường — đặt tour, review, chat |
| `Guide` | Hướng dẫn viên — tạo/quản lý tour, lịch trình, chat |
| `Company` | Công ty du lịch |
| `Admin` | Quản trị viên — toàn quyền hệ thống |

---

## 2. Authentication — Đăng nhập & JWT

### 2.1 Đăng ký tài khoản

**`POST {{BASE_URL}}/api/auth/register`**

```json
// Request Body
{
  "full_name": "Nguyen Van A",
  "email": "user@example.com",
  "password": "123456",
  "phone": "0901234567",
  "avatar_url": "https://..." // optional
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Register successfully"
}
```

> **Validation**:
> - `phone`: đúng 10 chữ số
> - `password`: tối thiểu 6 ký tự
> - `email`: đúng định dạng email

---

### 2.2 Đăng nhập

**`POST {{BASE_URL}}/api/auth/login`**

```json
// Request Body
{
  "email": "user@example.com",
  "password": "123456"
}
```

```json
// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

> **Sau khi login**:
> 1. Lưu token vào `localStorage` / `SecureStorage`
> 2. Decode JWT để lấy `id` (User GUID) và `role` (Tourist/Guide/Admin/Company)
> 3. Gắn token vào mọi request tiếp theo qua header: `Authorization: Bearer {token}`
> 4. Dựa vào `role` để điều hướng đến đúng giao diện

---

### 2.3 Cách gắn token vào request

```http
GET /api/user HTTP/1.1
Host: api.guidego.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

---

## 3. Phân quyền chi tiết theo Role

| Tính năng | Tourist | Guide | Admin |
|---|:---:|:---:|:---:|
| Xem danh sách User | ✗ | ✗ | ✓ |
| Tạo/Xóa User | ✗ | ✗ | ✓ |
| Cập nhật thông tin User | Chỉ bản thân | Chỉ bản thân | ✓ (tất cả) |
| Thay đổi Role của User | ✗ | ✗ | ✓ |
| Xem danh sách Guide | ✓ | ✓ | ✓ |
| Duyệt/Từ chối Guide | ✗ | ✗ | ✓ |
| Tạo Tour | ✗ | ✓ | ✓ |
| Sửa/Xóa Tour | ✗ | Chỉ tour của mình | ✓ (tất cả) |
| Upload ảnh Tour | ✗ | Chỉ tour của mình | ✓ (tất cả) |
| Tạo/Sửa/Xóa Tour Schedule | ✗ | Chỉ tour của mình | ✓ (tất cả) |
| Tạo Review | ✓ | ✗ | ✗ |
| Xóa/Sửa Review | Chỉ của mình | ✗ | ✓ (tất cả) |
| Quản lý Location | ✗ | ✗ | ✓ |
| Chat | ✓ | ✓ | ✓ |

---

## 4. FLOW & API — Role ADMIN

### FLOW tổng quan Admin

```
[Admin Login]
     │
     ├──→ [Dashboard Tổng quan]
     │         ├── Thống kê user, tour, booking
     │         └── (Các endpoint thống kê nếu có)
     │
     ├──→ [Quản lý User]
     │         ├── Xem danh sách user
     │         ├── Tạo user mới (gán role)
     │         ├── Sửa thông tin / đổi role
     │         └── Xóa user (soft delete)
     │
     ├──→ [Quản lý Guide]
     │         ├── Xem danh sách guide (lọc chưa verify)
     │         ├── Duyệt guide → IsVerified = true
     │         └── Từ chối guide → xóa guide record
     │
     ├──→ [Quản lý Tour]
     │         ├── Xem tất cả tour
     │         ├── Tạo tour (có thể gán guide_id)
     │         ├── Sửa / Xóa bất kỳ tour nào
     │         └── Upload ảnh cho tour
     │
     ├──→ [Quản lý Tour Schedule]
     │         ├── Xem lịch trình theo tour
     │         ├── Tạo / Sửa / Xóa schedule
     │         └── (Không xóa được nếu có booking active)
     │
     ├──→ [Quản lý Location]
     │         ├── Xem / Tạo / Sửa / Xóa địa điểm
     │         └── Dùng location_id khi tạo tour
     │
     └──→ [Quản lý Review]
               ├── Xem tất cả review
               ├── Sửa review bất kỳ
               └── Xóa review vi phạm
```

---

### 4.1 Quản lý User

#### Lấy danh sách tất cả User

**`GET {{BASE_URL}}/api/user`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin**

```json
// Response 200
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "full_name": "Nguyen Van A",
    "email": "user@example.com",
    "phone": "0901234567",
    "role": "Tourist",
    "avatar_url": "https://...",
    "is_active": true,
    "created_at": "2024-01-15T10:30:00Z"
  }
]
```

---

#### Lấy thông tin 1 User theo ID

**`GET {{BASE_URL}}/api/user/{id}`**
Yêu cầu: Public (không cần token)

```json
// Response 200
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "full_name": "Nguyen Van A",
  "email": "user@example.com",
  "phone": "0901234567",
  "role": "Tourist",
  "avatar_url": "https://...",
  "is_active": true,
  "created_at": "2024-01-15T10:30:00Z"
}
```

---

#### Tạo User mới (Admin)

**`POST {{BASE_URL}}/api/user`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin**

```json
// Request Body
{
  "full_name": "Tran Thi B",
  "email": "guide@example.com",
  "password": "123456",
  "phone": "0912345678",
  "avatar_url": "https://...",   // optional
  "role": "Guide"                // Tourist | Guide | Company | Admin
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "User created successfully"
}
```

---

#### Cập nhật thông tin User

**`PUT {{BASE_URL}}/api/user/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin** (hoặc chính user đó, nhưng không được đổi role)

```json
// Request Body
{
  "full_name": "Tran Thi B Updated",
  "email": "newemail@example.com",
  "phone": "0912345679",
  "password": "newpass123",      // optional, nếu muốn đổi mật khẩu
  "avatar_url": "https://...",   // optional
  "role": "Admin"                // optional — CHỈ Admin mới được đổi role
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "User updated successfully"
}
```

> **Lưu ý**: Khi user thường (không phải Admin) gọi API này, backend sẽ bỏ qua trường `role` dù có truyền lên.

---

#### Xóa User (Soft Delete)

**`DELETE {{BASE_URL}}/api/user/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin**

```json
// Response 200
{
  "message": "User deleted successfully"
}
```

> User bị xóa sẽ có `is_active = false`, không hiển thị trong hệ thống nhưng dữ liệu vẫn còn trong DB.

---

### 4.2 Quản lý Guide (Duyệt/Từ chối)

#### FLOW duyệt Guide

```
Admin vào trang danh sách Guide
         │
         ├── Lọc guide chưa được verify (is_verified = false)
         │        [GET /api/guides]
         │
         ├── Xem chi tiết hồ sơ guide
         │        [GET /api/guides/{id}]
         │
         ├── Duyệt → [PUT /api/guides/{id}/verify]
         │        → is_verified = true
         │        → Guide có thể tạo tour
         │
         └── Từ chối → [DELETE /api/guides/{id}/reject]
                  → Xóa record guide
                  → User vẫn tồn tại nhưng không còn là guide
```

---

#### Lấy danh sách tất cả Guide

**`GET {{BASE_URL}}/api/guides`**
Yêu cầu: Public

```json
// Response 200
{
  "message": "Get all guides successfully",
  "data": [
    {
      "id": "guid",
      "user_id": "guid",
      "experience_years": 5,
      "languages": ["Vietnamese", "English"],
      "description": "Hướng dẫn viên chuyên nghiệp...",
      "rating": 4.8,
      "is_verified": false,
      "user": {
        "full_name": "Le Van Guide",
        "email": "guide@example.com",
        "avatar_url": "https://..."
      }
    }
  ]
}
```

> **Gợi ý UI**: Admin nên có filter để lọc `is_verified = false` (guide đang chờ duyệt) và `is_verified = true` (đã được duyệt).

---

#### Lấy thông tin 1 Guide theo ID

**`GET {{BASE_URL}}/api/guides/{id}`**
Yêu cầu: Public

```json
// Response 200
{
  "message": "Get guide successfully",
  "data": {
    "id": "guid",
    "user_id": "guid",
    "experience_years": 5,
    "languages": ["Vietnamese", "English"],
    "description": "...",
    "rating": 4.8,
    "is_verified": false
  }
}
```

---

#### Duyệt Guide

**`PUT {{BASE_URL}}/api/guides/{id}/verify`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin**

```
// Request Body: (không cần body)

// Response 200
{
  "message": "Guide verified successfully"
}
```

---

#### Từ chối Guide

**`DELETE {{BASE_URL}}/api/guides/{id}/reject`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin**

```
// Request Body: (không cần body)

// Response 200
{
  "message": "Guide rejected successfully"
}
```

---

### 4.3 Quản lý Tour

#### Lấy danh sách tất cả Tour

**`GET {{BASE_URL}}/api/tour`**
Yêu cầu: Public

```json
// Response 200
[
  {
    "id": "guid",
    "title": "Tour Hà Nội 3 ngày 2 đêm",
    "description": "...",
    "location_id": "guid",
    "guide_id": "guid",
    "price_per_person": 1500000,
    "max_people": 20,
    "duration_days": 3,
    "rating": 4.5,
    "is_active": true,
    "created_at": "2024-01-15T10:30:00Z",
    "images": [
      { "id": "guid", "image_url": "https://..." }
    ]
  }
]
```

---

#### Tìm kiếm Tour (có filter & phân trang)

**`GET {{BASE_URL}}/api/tour/search`**
Yêu cầu: Public

**Query Parameters:**

| Param | Type | Mô tả |
|---|---|---|
| `keyword` | string | Tìm theo tiêu đề, mô tả |
| `city` | string | Lọc theo thành phố |
| `location_id` | guid | Lọc theo địa điểm cụ thể |
| `guide_language` | string | Ngôn ngữ hướng dẫn viên |
| `verified_guide_only` | bool | Chỉ tour của guide đã verify (default: false) |
| `min_price` | decimal | Giá tối thiểu |
| `max_price` | decimal | Giá tối đa |
| `start_date` | DateOnly | Lọc từ ngày (yyyy-MM-dd) |
| `end_date` | DateOnly | Lọc đến ngày |
| `sort_by` | string | Sắp xếp (default: `created_at`) |
| `page` | int | Trang hiện tại (default: 1) |
| `page_size` | int | Số item/trang (default: 20) |

```
GET /api/tour/search?keyword=Hà Nội&min_price=500000&page=1&page_size=10
```

```json
// Response 200
{
  "data": [ /* List<TourResponseDto> */ ],
  "totalCount": 50,
  "page": 1
}
```

---

#### Tạo Tour mới (Admin tạo và có thể gán cho bất kỳ Guide)

**`POST {{BASE_URL}}/api/tour`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide, Admin**

```json
// Request Body
{
  "title": "Tour Hội An 2 ngày",
  "description": "Khám phá phố cổ Hội An...",
  "location_id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "guide_id": "guid-of-guide",   // optional — Admin có thể gán guide bất kỳ
  "price_per_person": 1200000,
  "max_people": 15,
  "duration_days": 2
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour created successfully",
  "data": {
    "id": "newly-created-tour-guid"
  }
}
```

---

#### Cập nhật Tour

**`PUT {{BASE_URL}}/api/tour/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**

```json
// Request Body
{
  "title": "Tour Hội An 2 ngày — Cập nhật",
  "description": "Mô tả mới...",
  "location_id": "guid",
  "price_per_person": 1300000,
  "max_people": 20,
  "duration_days": 2
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour updated successfully"
}
```

---

#### Xóa Tour (Soft Delete)

**`DELETE {{BASE_URL}}/api/tour/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour deleted successfully"
}
```

---

#### Upload ảnh cho Tour

**`POST {{BASE_URL}}/api/tour/{id}/images`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**
Content-Type: `multipart/form-data`

```
// Form Data
file: [chọn file ảnh JPG/PNG]
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Image uploaded successfully",
  "data": {
    "image_url": "https://res.cloudinary.com/..."
  }
}
```

---

### 4.4 Quản lý Tour Schedule

#### Lấy danh sách Schedule theo Tour

**`GET {{BASE_URL}}/api/tour-schedules/tour/{tourId}`**
Yêu cầu: Public

**Query Parameters:**

| Param | Type | Mô tả |
|---|---|---|
| `onlyAvailable` | bool | Chỉ lấy schedule còn slot (optional) |

```json
// Response 200
[
  {
    "id": "guid",
    "tour_id": "guid",
    "start_date": "2024-03-15",
    "end_date": "2024-03-17",
    "available_slots": 10,
    "created_at": "2024-01-15T10:30:00Z"
  }
]
```

---

#### Lấy chi tiết 1 Schedule

**`GET {{BASE_URL}}/api/tour-schedules/{id}`**
Yêu cầu: Public

---

#### Tạo Tour Schedule

**`POST {{BASE_URL}}/api/tour-schedules`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**

```json
// Request Body
{
  "tour_id": "guid-of-tour",
  "start_date": "2024-04-01",    // định dạng: yyyy-MM-dd
  "end_date": "2024-04-03",
  "available_slots": 15
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour schedule created successfully",
  "data": {
    "id": "newly-created-schedule-guid"
  }
}
```

> **Validation quan trọng**:
> - `start_date` phải sau ngày hiện tại ít nhất 1 ngày (không được đặt hôm nay hoặc quá khứ)
> - `end_date` phải sau `start_date`
> - `available_slots` phải > 0

---

#### Cập nhật Tour Schedule

**`PUT {{BASE_URL}}/api/tour-schedules/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**

```json
// Request Body
{
  "start_date": "2024-04-05",
  "end_date": "2024-04-07",
  "available_slots": 20
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour schedule updated successfully"
}
```

---

#### Xóa Tour Schedule

**`DELETE {{BASE_URL}}/api/tour-schedules/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour schedule deleted successfully"
}

// Response 400 — Nếu đang có booking active
{
  "statusCode": 400,
  "message": "Cannot delete schedule with active bookings"
}
```

---

### 4.5 Quản lý Location

#### Lấy danh sách Location

**`GET {{BASE_URL}}/api/location`**
Yêu cầu: Public

```json
// Response 200
{
  "message": "Get all locations successfully",
  "data": [
    {
      "id": "guid",
      "name": "Hà Nội",
      "latitude": 21.0285,
      "longitude": 105.8542,
      "address": "Hoàn Kiếm, Hà Nội",
      "city": "Hà Nội",
      "country": "Vietnam"
    }
  ]
}
```

---

#### Lấy thông tin 1 Location

**`GET {{BASE_URL}}/api/location/{id}`**
Yêu cầu: Public

---

#### Tạo Location mới

**`POST {{BASE_URL}}/api/location`**
Yêu cầu: **Admin** (khuyến nghị — hiện tại API chưa enforce role cứng)

```json
// Request Body
{
  "name": "Đà Lạt",
  "latitude": 11.9465,
  "longitude": 108.4419,
  "address": "Phường 1, Đà Lạt",    // optional
  "city": "Đà Lạt",                   // optional
  "country": "Vietnam"                 // optional
}
```

```json
// Response 200
{
  "message": "Location created successfully",
  "data": {
    "id": "newly-created-location-guid"
  }
}
```

---

#### Cập nhật Location

**`PUT {{BASE_URL}}/api/location/{id}`**
Yêu cầu: **Admin** (khuyến nghị)

```json
// Request Body
{
  "name": "Đà Lạt — Updated",
  "latitude": 11.9465,
  "longitude": 108.4419,
  "city": "Đà Lạt",
  "country": "Vietnam"
}
```

---

#### Xóa Location

**`DELETE {{BASE_URL}}/api/location/{id}`**
Yêu cầu: **Admin** (khuyến nghị)

```json
// Response 200
{
  "message": "Location deleted successfully"
}
```

---

### 4.6 Quản lý Review

#### Lấy tất cả Review

**`GET {{BASE_URL}}/api/review`**
Yêu cầu: Public

```json
// Response 200
[
  {
    "id": "guid",
    "tour_id": "guid",
    "user_id": "guid",
    "rating": 5,
    "comment": "Tour rất tuyệt vời!",
    "created_at": "2024-01-20T10:00:00Z"
  }
]
```

---

#### Xóa Review (Admin — xóa review vi phạm)

**`DELETE {{BASE_URL}}/api/review/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Admin** hoặc chính người tạo review

```json
// Response 200
{
  "statusCode": 200,
  "message": "Review deleted successfully"
}
```

---

## 5. FLOW & API — Role GUIDE

### FLOW tổng quan Guide

```
[Guide Login]
     │
     ├──→ [Đăng ký trở thành Guide] (nếu chưa có guide profile)
     │         [POST /api/guides]
     │         → Chờ Admin duyệt (is_verified = false)
     │
     ├──→ [Xem hồ sơ cá nhân Guide]
     │         [GET /api/guides/user/{userId}]
     │
     ├──→ [Cập nhật hồ sơ Guide]
     │         [PUT /api/guides/{id}]
     │
     ├──→ [Quản lý Tour của mình]
     │         ├── Xem danh sách tour → [GET /api/tour]
     │         ├── Tạo tour mới → [POST /api/tour]
     │         ├── Sửa tour → [PUT /api/tour/{id}]
     │         ├── Xóa tour → [DELETE /api/tour/{id}]
     │         └── Upload ảnh → [POST /api/tour/{id}/images]
     │
     ├──→ [Quản lý Tour Schedule]
     │         ├── Xem lịch trình → [GET /api/tour-schedules/tour/{tourId}]
     │         ├── Tạo lịch trình → [POST /api/tour-schedules]
     │         ├── Sửa lịch trình → [PUT /api/tour-schedules/{id}]
     │         └── Xóa lịch trình → [DELETE /api/tour-schedules/{id}]
     │
     └──→ [Chat với Tourist]
               ├── Xem danh sách chat → [GET /api/chats/my-chats]
               ├── Xem tin nhắn → [GET /api/chats/{chatId}/messages]
               └── Gửi tin nhắn → [POST /api/chats/send]
                   (+ Realtime qua SignalR)
```

---

### 5.1 Đăng ký trở thành Guide

> Thực hiện sau khi user đã đăng ký tài khoản. Một user chỉ cần đăng ký guide profile 1 lần.

**`POST {{BASE_URL}}/api/guides`**
Yêu cầu: Public (không bắt buộc token, nhưng nên truyền để backend biết user)

```json
// Request Body
{
  "user_id": "guid-of-current-user",
  "experience_years": 5,
  "languages": ["Vietnamese", "English", "Japanese"],  // optional
  "description": "Tôi là hướng dẫn viên có 5 năm kinh nghiệm tại Hà Nội..."  // optional
}
```

```json
// Response 200
{
  "message": "Guide created successfully",
  "data": {
    "id": "newly-created-guide-id",
    "user_id": "guid",
    "is_verified": false
  }
}
```

> Sau khi tạo xong, guide cần **chờ Admin duyệt** (`is_verified = false`). Cho đến khi được duyệt, guide **không thể tạo tour**.

---

### 5.2 Quản lý hồ sơ Guide

#### Lấy thông tin Guide theo User ID

**`GET {{BASE_URL}}/api/guides/user/{userId}`**
Yêu cầu: Public

```json
// Response 200
{
  "message": "Get guide successfully",
  "data": {
    "id": "guide-guid",
    "user_id": "user-guid",
    "experience_years": 5,
    "languages": ["Vietnamese", "English"],
    "description": "...",
    "rating": 4.8,
    "is_verified": true
  }
}
```

> Guide dùng endpoint này để kiểm tra trạng thái tài khoản (`is_verified`) sau khi đăng ký.

---

#### Cập nhật hồ sơ Guide

**`PUT {{BASE_URL}}/api/guides/{id}`**
Yêu cầu: Token khuyến nghị (hiện API chưa enforce role)

```json
// Request Body
{
  "experience_years": 6,
  "languages": ["Vietnamese", "English", "French"],
  "description": "Mô tả mới về bản thân..."
}
```

```json
// Response 200
{
  "message": "Guide updated successfully"
}
```

---

### 5.3 Quản lý Tour

> **Lưu ý quan trọng**: Guide chỉ được tạo tour khi `is_verified = true`.
> Khi Guide tạo tour, `guide_id` sẽ tự động được gán từ JWT token. Guide **không thể** tạo tour cho người khác.

#### Tạo Tour mới

**`POST {{BASE_URL}}/api/tour`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide, Admin**

```json
// Request Body
{
  "title": "Tour Sapa 3 ngày 2 đêm",
  "description": "Khám phá bản làng H'Mông, ruộng bậc thang Sa Pa...",
  "location_id": "guid-of-sapa-location",
  "price_per_person": 2500000,
  "max_people": 12,
  "duration_days": 3
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour created successfully",
  "data": {
    "id": "new-tour-guid"
  }
}
```

> Sau khi tạo tour, Guide nên:
> 1. Upload ảnh cho tour `[POST /api/tour/{id}/images]`
> 2. Tạo tour schedule (lịch trình) `[POST /api/tour-schedules]`

---

#### Sửa Tour của mình

**`PUT {{BASE_URL}}/api/tour/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour)**

```json
// Request Body
{
  "title": "Tour Sapa 3 ngày — Phiên bản mới",
  "description": "...",
  "location_id": "guid",
  "price_per_person": 2800000,
  "max_people": 15,
  "duration_days": 3
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour updated successfully"
}

// Response 403 — Nếu guide cố sửa tour của người khác
{
  "statusCode": 403,
  "message": "Forbidden"
}
```

---

#### Xóa Tour của mình

**`DELETE {{BASE_URL}}/api/tour/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour)**

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour deleted successfully"
}
```

---

### 5.4 Upload ảnh Tour

**`POST {{BASE_URL}}/api/tour/{id}/images`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour)**
Content-Type: `multipart/form-data`

```
Form Data:
  file: [file ảnh — JPG, PNG, WEBP]
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Image uploaded successfully",
  "data": {
    "image_url": "https://res.cloudinary.com/guidego/..."
  }
}
```

> - Có thể upload nhiều ảnh bằng cách gọi API nhiều lần
> - Ảnh được lưu trên **Cloudinary** và trả về URL

---

### 5.5 Quản lý Tour Schedule

#### Tạo lịch trình cho Tour

**`POST {{BASE_URL}}/api/tour-schedules`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour), Admin**

```json
// Request Body
{
  "tour_id": "guid-of-tour",
  "start_date": "2024-05-01",
  "end_date": "2024-05-03",
  "available_slots": 12
}
```

```json
// Response 200
{
  "statusCode": 200,
  "message": "Tour schedule created successfully",
  "data": {
    "id": "new-schedule-guid"
  }
}

// Response 400 — Ngày đã qua hoặc là hôm nay
{
  "statusCode": 400,
  "message": "Cannot create schedule for today or past dates"
}
```

---

#### Xem lịch trình của Tour

**`GET {{BASE_URL}}/api/tour-schedules/tour/{tourId}?onlyAvailable=true`**
Yêu cầu: Public

```json
// Response 200
[
  {
    "id": "guid",
    "tour_id": "guid",
    "start_date": "2024-05-01",
    "end_date": "2024-05-03",
    "available_slots": 10,
    "created_at": "2024-02-01T08:00:00Z"
  }
]
```

---

#### Cập nhật lịch trình

**`PUT {{BASE_URL}}/api/tour-schedules/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour)**

```json
// Request Body
{
  "start_date": "2024-05-05",
  "end_date": "2024-05-07",
  "available_slots": 15
}
```

---

#### Xóa lịch trình

**`DELETE {{BASE_URL}}/api/tour-schedules/{id}`**
Yêu cầu: `Authorization: Bearer {token}` | Role: **Guide (chủ tour)**

> **Không thể xóa** nếu schedule đang có booking còn hiệu lực.

---

### 5.6 Chat với Tourist

#### Lấy danh sách các cuộc hội thoại

**`GET {{BASE_URL}}/api/chats/my-chats`**
Yêu cầu: `Authorization: Bearer {token}`

```json
// Response 200
[
  {
    "id": "chat-guid",
    "user_id": "tourist-guid",
    "guide_id": "guide-guid",
    "created_at": "2024-01-10T09:00:00Z",
    "last_message": "Xin chào Guide!"
  }
]
```

---

#### Xem tin nhắn trong 1 cuộc hội thoại

**`GET {{BASE_URL}}/api/chats/{chatId}/messages`**
Yêu cầu: `Authorization: Bearer {token}`

```json
// Response 200
[
  {
    "id": "msg-guid",
    "chat_id": "chat-guid",
    "sender_id": "user-guid",
    "content": "Xin chào, tour có bao gồm bữa ăn không ạ?",
    "sent_at": "2024-01-10T09:05:00Z"
  }
]
```

---

#### Gửi tin nhắn

**`POST {{BASE_URL}}/api/chats/send`**
Yêu cầu: `Authorization: Bearer {token}`

```json
// Request Body
{
  "chat_id": "chat-guid",
  "content": "Tour bao gồm 3 bữa sáng và 2 bữa tối nhé!"
}
```

```json
// Response 200
{
  "id": "new-msg-guid",
  "chat_id": "chat-guid",
  "sender_id": "guide-guid",
  "content": "Tour bao gồm 3 bữa sáng và 2 bữa tối nhé!",
  "sent_at": "2024-01-10T09:10:00Z"
}
```

---

#### Kết nối SignalR (Realtime Chat)

**Hub URL**: `{{BASE_URL}}/chatHub?access_token={JWT_TOKEN}`

**Kết nối:**
```javascript
// Ví dụ với @microsoft/signalr
const connection = new signalR.HubConnectionBuilder()
  .withUrl(`${BASE_URL}/chatHub?access_token=${token}`)
  .build();

await connection.start();

// Tham gia room chat
await connection.invoke("JoinChat", chatId);

// Lắng nghe tin nhắn mới
connection.on("ReceiveMessage", (message) => {
  console.log("Tin nhắn mới:", message);
  // { senderId, content, sentAt, ... }
});
```

> **Flow chat**:
> 1. Tourist tạo chat với Guide: `POST /api/chats/get-or-create?guideId={guideId}`
> 2. Cả hai kết nối SignalR Hub với JWT token
> 3. Gửi tin nhắn qua REST API `POST /api/chats/send`
> 4. Backend tự broadcast đến Hub → cả hai nhận realtime qua `ReceiveMessage`

---

## 6. Enums & Trạng thái

### UserRole

| Value | Mô tả |
|---|---|
| `Tourist` | Khách du lịch |
| `Guide` | Hướng dẫn viên |
| `Company` | Công ty du lịch |
| `Admin` | Quản trị viên |

### BookingStatus

| Value | Mô tả | Màu gợi ý |
|---|---|---|
| `Pending` | Đang chờ xử lý | Vàng |
| `Confirmed` | Đã xác nhận | Xanh lá |
| `Cancelled` | Đã hủy | Đỏ |

### PaymentStatus

| Value | Mô tả | Màu gợi ý |
|---|---|---|
| `Pending` | Chờ thanh toán | Vàng |
| `Completed` | Thanh toán thành công | Xanh lá |
| `Failed` | Thanh toán thất bại | Đỏ |
| `Refunded` | Đã hoàn tiền | Xanh dương |

---

## 7. Cấu trúc Response chung

Backend trả về nhiều format response khác nhau tùy controller. Front-end cần xử lý **cả 3 dạng** sau:

### Dạng 1: statusCode + message (+ data)
```json
{
  "statusCode": 200,
  "message": "Success",
  "data": { ... }  // hoặc không có data
}
```

### Dạng 2: message + data
```json
{
  "message": "Get successfully",
  "data": [ ... ]
}
```

### Dạng 3: List trực tiếp
```json
[ { ... }, { ... } ]
```

### Dạng 4: Object trực tiếp
```json
{
  "token": "..."
}
```

### Dạng 5: Error Response
```json
{
  "statusCode": 400,
  "message": "Mô tả lỗi cụ thể"
}
```

> **HTTP Status Codes thường gặp:**
> - `200 OK` — Thành công
> - `400 Bad Request` — Dữ liệu không hợp lệ
> - `401 Unauthorized` — Chưa đăng nhập / token hết hạn
> - `403 Forbidden` — Không có quyền
> - `404 Not Found` — Không tìm thấy resource
> - `500 Internal Server Error` — Lỗi server

---

## 8. Lưu ý quan trọng

### Xác thực & Phân quyền

1. **Token hết hạn**: Khi nhận `401 Unauthorized`, xóa token và điều hướng về trang login.
2. **Lưu trữ token**: Ưu tiên `HttpOnly Cookie` hoặc `SecureStorage` để tránh XSS. Không dùng `localStorage` ở production nếu bảo mật là yêu cầu cao.
3. **Decode JWT**: Dùng thư viện `jwt-decode` để đọc `role` và `id` từ token mà không cần gọi API.
4. **Role trong token**: Token chứa `role` dưới dạng string (`"Tourist"`, `"Guide"`, `"Admin"`).

### Luồng Guide

5. **Guide chưa được duyệt**: Khi `is_verified = false`, nên hiển thị thông báo "Tài khoản đang chờ Admin duyệt" và ẩn/khóa các chức năng tạo tour.
6. **Guide lấy guide_id**: Sau khi login, gọi `GET /api/guides/user/{userId}` để lấy `guide_id` cần cho các thao tác update/verify.

### Tour & Schedule

7. **Thứ tự tạo Tour**: Tạo Tour → Upload ảnh → Tạo Schedule (phải theo thứ tự này vì cần `tour_id`).
8. **Ngày tạo Schedule**: `start_date` phải ít nhất là ngày mai (không thể đặt hôm nay hoặc quá khứ).
9. **Không xóa được Schedule**: Nếu schedule đang có booking `Pending` hoặc `Confirmed`, backend trả lỗi 400.
10. **Format ngày tháng**: Dùng `yyyy-MM-dd` cho `DateOnly` fields (ví dụ: `"2024-05-01"`).

### Location

11. **Cần có Location trước**: Khi tạo Tour, bắt buộc phải có `location_id`. Admin nên tạo sẵn danh sách Location trước khi Guide tạo tour. Gọi `GET /api/location` để lấy danh sách và cho Guide chọn từ dropdown.

### Upload ảnh

12. **Multipart Form**: Khi upload ảnh, request phải dùng `Content-Type: multipart/form-data`, không phải `application/json`.
13. **Không có endpoint xóa ảnh**: Hiện tại chưa có API xóa ảnh tour. Cần backend bổ sung nếu cần.

### Chat & SignalR

14. **Kết nối SignalR**: Token được truyền qua **query parameter** `?access_token=...` (không phải header), vì SignalR WebSocket không hỗ trợ custom header trên một số nền tảng.
15. **Tourist tạo chat trước**: Chỉ Tourist mới gọi `POST /api/chats/get-or-create`. Guide không cần tạo chat — chỉ cần nhận và phản hồi.

### Admin

16. **Location chưa enforce role**: Các API CRUD của `/api/location` và `/api/guides` hiện không bắt buộc role ở tầng attribute. Nên tự hạn chế ở phía UI (chỉ hiển thị chức năng này cho Admin). Cần backend bổ sung `[Authorize(Roles = "Admin")]` cho các endpoint này.
17. **Guide verify/reject**: Endpoint `PUT /api/guides/{id}/verify` và `DELETE /api/guides/{id}/reject` được thiết kế cho Admin, nhưng cần xác nhận backend có enforce role không.

### Xử lý lỗi

18. **Validation messages**: Backend trả message bằng tiếng Anh. Front-end có thể cần map sang tiếng Việt để UX tốt hơn.
19. **Không có pagination mặc định**: Ngoài `/api/tour/search`, phần lớn các GET list API trả về toàn bộ dữ liệu. Cần filter/sort ở phía client hoặc yêu cầu backend bổ sung phân trang.

---
