# GuideGo - Tourism Tour Booking Platform

## Giới thiệu

**GuideGo** là một nền tảng đặt tour du lịch backend được xây dựng bằng **ASP.NET Core 8.0**. Hệ thống hỗ trợ kết nối giữa khách du lịch (Tourist), hướng dẫn viên (Guide), công ty du lịch (Company) và quản trị viên (Admin).

---

## Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Framework | ASP.NET Core 8.0 (Web API) |
| Ngôn ngữ | C# |
| Database | PostgreSQL |
| ORM | Entity Framework Core 8.0 |
| Authentication | JWT Bearer + BCrypt |
| API Docs | Swagger / OpenAPI |
| Naming Convention | Snake_case (EFCore.NamingConventions) |

---

## Kiến trúc dự án

Dự án theo kiến trúc **3 lớp (Three-Layer Architecture)**:

```
Controllers (API Layer)
    ↓
Services (Business Logic Layer)
    ↓
Repositories (Data Access Layer)
    ↓
Entity Framework Core
    ↓
PostgreSQL Database
```

### Cấu trúc thư mục

```
GuideGo/
├── PRMGuideGo/                    # Web API project
│   ├── Controllers/               # 8 API controllers
│   ├── Program.cs                 # Startup & Dependency Injection
│   ├── appsettings.json           # Cấu hình runtime
│   ├── appsettings.example.json   # Template cấu hình
│   └── PRMGuideGo.http            # REST client test file
│
├── GuideGo-Service/               # Business Logic layer
│   ├── Services/                  # 8 service classes
│   ├── Interfaces/                # 8 service interfaces
│   └── Dtos/                      # Data Transfer Objects
│       ├── Auth/
│       ├── Cart/
│       ├── Guide/
│       ├── Review/
│       ├── Tour/
│       ├── User/
│       └── Booking/
│
├── GuideGo-Repository/            # Data Access layer
│   ├── Data/
│   │   └── AppDbContext.cs        # EF Core DbContext
│   ├── Entities/                  # 14 domain entities
│   ├── Repositories/              # 7 repository implementations
│   ├── Interfaces/                # 5 repository interfaces
│   ├── Migrations/                # EF Core migrations
│   └── Enums/                     # 3 enumerations
│
└── PRMGuideGo.slnx                # Solution file
```

---

## Cài đặt & Chạy dự án

### Yêu cầu

- .NET 8.0 SDK
- PostgreSQL 9.6+
- Visual Studio 2022 hoặc VS Code với C# extension

### Cấu hình

1. Copy file cấu hình mẫu:
   ```bash
   cp PRMGuideGo/appsettings.example.json PRMGuideGo/appsettings.json
   ```

2. Chỉnh sửa `appsettings.json`:
   ```json
   {
     "ConnectionStrings": {
       "DefaultConnection": "Host=localhost;Port=5432;Database=guidego;Username=postgres;Password=YOUR_PASSWORD"
     },
     "Jwt": {
       "Key": "YOUR_SECRET_KEY_MIN_32_CHARACTERS_LONG",
       "Issuer": "PRMGuideGo",
       "Audience": "PRMGuideGoClient",
       "ExpiresInMinutes": 60
     }
   }
   ```

3. Chạy migration để tạo database:
   ```bash
   dotnet ef database update --project GuideGo-Repository --startup-project PRMGuideGo
   ```

4. Chạy ứng dụng:
   ```bash
   dotnet run --project PRMGuideGo
   ```

5. Truy cập Swagger UI tại: `https://localhost:{port}/swagger`

> **Tài khoản Admin mặc định:**
> - Email: `admin@gmail.com`
> - Password: `123456` *(cần đổi trong production)*

---

## API Endpoints

### Auth
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| POST | `/api/auth/register` | Đăng ký tài khoản | Không |
| POST | `/api/auth/login` | Đăng nhập, nhận JWT | Không |

### User
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/user` | Danh sách users | Admin |
| GET | `/api/user/{id}` | Thông tin user | Có |
| PUT | `/api/user/{id}` | Cập nhật user | Có |
| DELETE | `/api/user/{id}` | Xóa user | Admin |

### Tour
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/tour` | Danh sách tours | Không |
| GET | `/api/tour/{id}` | Chi tiết tour | Không |
| POST | `/api/tour` | Tạo tour mới | Guide/Admin |
| PUT | `/api/tour/{id}` | Cập nhật tour | Guide/Admin |
| DELETE | `/api/tour/{id}` | Xóa tour | Guide/Admin |
| GET | `/api/tour/search` | Tìm kiếm tour | Không |

### Guide
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/guides` | Danh sách hướng dẫn viên | Không |
| GET | `/api/guides/{id}` | Chi tiết hướng dẫn viên | Không |
| POST | `/api/guides` | Tạo profile guide | Guide |
| PUT | `/api/guides/{id}` | Cập nhật profile | Guide |

### Review
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/review/tour/{tourId}` | Reviews của tour | Không |
| POST | `/api/review` | Tạo review | Tourist |
| PUT | `/api/review/{id}` | Cập nhật review | Tourist |
| DELETE | `/api/review/{id}` | Xóa review | Tourist/Admin |

### Cart
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/cart` | Xem giỏ hàng | Tourist |
| POST | `/api/cart/add` | Thêm tour vào giỏ | Tourist |
| DELETE | `/api/cart/item/{id}` | Xóa item khỏi giỏ | Tourist |

### Booking
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/booking` | Danh sách booking | Có |
| POST | `/api/booking` | Tạo booking | Tourist |
| PUT | `/api/booking/{id}` | Cập nhật booking | Có |

### Payment
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| POST | `/api/payment` | Xử lý thanh toán | Tourist |
| GET | `/api/payment/{bookingId}` | Thông tin payment | Có |

---

## Domain Entities

### Sơ đồ quan hệ

```
User ──────────── Guide (1:1)
  │
  ├──────────────  Company (1:1)
  │
  └──────────────  Cart (1:1)
                     └── CartItem ──── TourSchedule
                                           │
Tour ──────────── TourSchedule            │
  │                                        │
  ├── TourImage                        Booking ──── Payment
  ├── Location
  └── Review

User ──── Chat ──── Message
```

### Mô tả các Entity

| Entity | Mô tả |
|--------|-------|
| `User` | Tài khoản người dùng (Tourist, Guide, Company, Admin) |
| `Guide` | Profile hướng dẫn viên (1:1 với User) |
| `Company` | Profile công ty du lịch (1:1 với User) |
| `Tour` | Tour du lịch với thông tin giá, mô tả |
| `Location` | Địa điểm (thành phố, tọa độ) |
| `TourImage` | Hình ảnh của tour |
| `TourSchedule` | Lịch khởi hành với số slot còn lại |
| `Cart` | Giỏ hàng (1:1 với User) |
| `CartItem` | Từng tour trong giỏ hàng |
| `Booking` | Đơn đặt tour đã xác nhận |
| `Payment` | Thông tin thanh toán (1:1 với Booking) |
| `Review` | Đánh giá và bình luận tour |
| `Chat` | Cuộc trò chuyện giữa users |
| `Message` | Tin nhắn trong cuộc trò chuyện |

---

## Phân quyền

```csharp
public enum UserRole
{
    Tourist,   // Xem tour, đặt tour, viết review, nhắn tin
    Guide,     // Tạo/quản lý tour, xem tin nhắn
    Company,   // Quản lý tour công ty
    Admin      // Toàn quyền truy cập hệ thống
}
```

---

## Enumerations

### BookingStatus
| Giá trị | Mô tả |
|---------|-------|
| `Pending` | Chờ xác nhận |
| `Confirmed` | Đã xác nhận |
| `Completed` | Đã hoàn thành |
| `Cancelled` | Đã hủy |

### PaymentStatus
| Giá trị | Mô tả |
|---------|-------|
| `Pending` | Chờ thanh toán |
| `Completed` | Thanh toán thành công |
| `Failed` | Thanh toán thất bại |
| `Refunded` | Đã hoàn tiền |

---

## Luồng nghiệp vụ chính

### Đặt tour

```
1. Tourist tìm kiếm tour
2. Chọn TourSchedule (ngày khởi hành)
3. Thêm vào Cart
4. Tạo Booking từ CartItem
5. Thanh toán → Payment record
6. Booking status: Pending → Confirmed
```

### Tìm kiếm tour

Hỗ trợ lọc đa tiêu chí:
- Từ khóa (tên tour, mô tả)
- Địa điểm / thành phố
- Khoảng giá (min/max)
- Ngôn ngữ hướng dẫn viên
- Hướng dẫn viên được xác minh
- Phân trang

---

## Database Schema

- **14 bảng** với snake_case naming convention
- UUID primary keys (generated by PostgreSQL)
- Soft delete với `is_active` boolean
- Timestamps: `created_at`, `updated_at`
- Giá: DECIMAL(10,2), Rating: DECIMAL(2,1)

---

## Design Patterns

| Pattern | Áp dụng |
|---------|---------|
| Repository Pattern | Trừu tượng hóa data access |
| Service Layer | Đóng gói business logic |
| Dependency Injection | ASP.NET Core DI container |
| DTO Pattern | Tách biệt internal model với API contract |
| Enum-based Status | BookingStatus, PaymentStatus |

---

## Dependencies

| Package | Version | Mục đích |
|---------|---------|---------|
| `Microsoft.AspNetCore.Authentication.JwtBearer` | 8.0.11 | JWT authentication |
| `Microsoft.EntityFrameworkCore` | 8.0.25 | ORM |
| `Npgsql.EntityFrameworkCore.PostgreSQL` | 8.0.11 | PostgreSQL provider |
| `BCrypt.Net-Next` | 4.0.3 | Hash mật khẩu |
| `System.IdentityModel.Tokens.Jwt` | 8.14.0 | Tạo JWT token |
| `Swashbuckle.AspNetCore` | 6.6.2 | Swagger/OpenAPI |
| `EFCore.NamingConventions` | 8.0.3 | Snake_case DB naming |

---

## Thống kê dự án

| Layer | Files |
|-------|-------|
| PRMGuideGo (API) | ~14 files |
| GuideGo-Service | ~40 files |
| GuideGo-Repository | ~44 files |
| **Tổng cộng** | **~98 files C#** |

---

## Lưu ý Production

- Thay đổi JWT Secret Key thành giá trị an toàn (>= 32 ký tự)
- Đổi mật khẩu admin mặc định (`admin@gmail.com` / `123456`)
- Lưu connection string qua biến môi trường, không hardcode
- Thêm error logging (Serilog, NLog...)
- Cấu hình CORS phù hợp cho môi trường production
