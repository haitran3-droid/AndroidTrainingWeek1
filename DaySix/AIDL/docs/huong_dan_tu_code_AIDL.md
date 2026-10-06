# Hướng dẫn tự code lại project AIDL bằng Android Studio

Thực hiện theo thứ tự: **tạo project → tạo module → viết contract AIDL → viết Server → viết Client → chạy thử**.

Project mẫu nằm tại `C:\Users\cuong.bui1\IdeaProjects\AIDL`. Bài thực hành dùng thư mục `AIDLPractice` để bạn có thể giữ project mẫu và đối chiếu từng file.

## 1. Tạo project trong Android Studio

Ở màn hình Welcome, chọn **New Project**. Nếu đang mở một project, chọn **File → New → New Project**. Xem [hướng dẫn chính thức](https://developer.android.com/studio/projects/create-project).

Trong nhóm **Phone and Tablet**, chọn **No Activity**, rồi bấm **Next**. Ta sẽ tự tạo Activity và giao diện XML.

Điền:

| Trường | Giá trị |
|---|---|
| Name | `AIDLPractice` |
| Package name | `com.example.client` |
| Save location | `C:\Users\cuong.bui1\IdeaProjects\AIDLPractice` |
| Language | Kotlin |
| Minimum SDK | API 29 — Android 10 |
| Build configuration language, nếu có | Kotlin DSL |

Bấm **Finish**, chờ Gradle Sync hoàn tất.

**Điểm kiểm tra:** bên trái xuất hiện module `app` và nhóm **Gradle Scripts**.

## 2. Tạo thêm module Server và contract

Project sẽ có hai ứng dụng và một thư viện chung:

```text
Client ─── gọi qua Binder IPC ───► Server
   │                               │
   └──── cùng sử dụng contract ────┘
```

`contract` định nghĩa dữ liệu và các hàm mà hai bên thống nhất sử dụng.

Tạo Server qua **File → New → New Module → Phone & Tablet Module**:

| Trường | Giá trị |
|---|---|
| Application name | `AIDL Server` |
| Module name | `server` |
| Package name | `com.example.server` |
| Minimum SDK | API 29 |
| Activity template | No Activity / Add No Activity |

Bấm **Finish**.

Tiếp tục chọn **File → New → New Module → Android Library**:

| Trường | Giá trị |
|---|---|
| Module name | `contract` |
| Package name | `com.example.service` |
| Minimum SDK | API 29 |

Bấm **Finish** và chờ Sync. Android Library sẽ được đóng gói vào hai app, không có nút Run riêng.

Nguồn: [tạo app module](https://developer.android.com/studio/projects/add-app-module), [tạo Android Library](https://developer.android.com/studio/projects/android-library).

**Điểm kiểm tra:** project có `app`, `server`, `contract`.

## 3. Đặt tên module Client và cấu hình Gradle

Để tên module giống project mẫu, mở `settings.gradle.kts`. Giữ các khối cấu hình repository, thay phần `include(...)` bằng:

```kotlin
rootProject.name = "AIDLPractice"

include(":client", ":server", ":contract")

// Module có tên client, mã nguồn vẫn nằm trong thư mục app.
project(":client").projectDir = file("app")
```

Như vậy, trong bài thực hành:

- Module `client` dùng thư mục `app`.
- Module `server` dùng thư mục `server`.
- Module `contract` dùng thư mục `contract`.

Trong `build.gradle.kts` **ở cấp project**, cấu hình:

```kotlin
plugins {
    id("com.android.application") version "9.1.1" apply false
    id("com.android.library") version "9.1.1" apply false
}
```

Đây là phiên bản đã dùng để build project mẫu. Nếu dùng cấu hình này, trong `gradle/wrapper/gradle-wrapper.properties`, đặt:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.3.1-bin.zip
distributionSha256Sum=b266d5ff6b90eada6dc3b20cb090e3731302e553a27c5d3e4df1f0d76beaff06
```

Giữ các dòng cấu hình wrapper còn lại. Với AGP 9, Kotlin đã được tích hợp; các cấu hình dưới đây dùng khả năng đó. Xem [tài liệu Kotlin tích hợp](https://developer.android.com/build/migrate-to-built-in-kotlin).

### 3.1. Module contract

Mở `contract/build.gradle.kts`, thay nội dung bằng:

```kotlin
plugins {
    id("com.android.library")
}

android {
    namespace = "com.example.service"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    buildFeatures {
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

Dòng `aidl = true` bật việc sinh mã Java từ các file `.aidl`.

### 3.2. Module Client

Trong `app/build.gradle.kts`, cấu hình:

```kotlin
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.client"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.client"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":contract"))

    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
```

### 3.3. Module Server

Trong `server/build.gradle.kts`, cấu hình:

```kotlin
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.server"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.server"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":contract"))
}
```

Bấm **Sync Now**. Nếu chưa có SDK 36, cài qua **Tools → SDK Manager**. Chọn Gradle JDK 17 trở lên; có thể dùng JBR đi kèm Android Studio.

**Điểm kiểm tra:** cả Client và Server đều phụ thuộc vào cùng module `contract`.

## 4. Viết contract AIDL

Chuyển bảng bên trái từ **Android** sang **Project** để thấy đúng cấu trúc thư mục.

Trong `contract/src/main`, tạo các thư mục:

```text
aidl/com/example/service
```

Chuột phải thư mục cha → **New → Directory**, tạo lần lượt từng thư mục. Sau đó dùng **New → File** để tạo các file dưới đây.

### 4.1. Book.aidl

```aidl
package com.example.service;

parcelable Book;
```

File này thông báo rằng `Book` là một kiểu dữ liệu có thể truyền qua Parcel. Phần triển khai thực tế nằm trong `Book.kt`.

### 4.2. IOnBookAddedListener.aidl

```aidl
package com.example.service;

import com.example.service.Book;

oneway interface IOnBookAddedListener {
    void onBookAdded(in Book newBook);
}
```

Đây là chiều giao tiếp **Server → Client**: Server thông báo vừa có sách mới.

### 4.3. IBookManager.aidl

```aidl
package com.example.service;

import com.example.service.Book;
import com.example.service.IOnBookAddedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    int getBookCount();
    int getServerPid();

    void registerListener(IOnBookAddedListener listener);
    void unregisterListener(IOnBookAddedListener listener);
}
```

Ý nghĩa từng hàm:

| Hàm | Mục đích |
|---|---|
| `getBookList()` | Client đọc danh sách sách |
| `addBook(in Book book)` | Client gửi một sách sang Server |
| `getBookCount()` | Client hỏi số lượng sách |
| `getServerPid()` | Client hỏi PID để xác nhận hai tiến trình khác nhau |
| `registerListener()` | Client đăng ký nhận callback |
| `unregisterListener()` | Client hủy nhận callback |

## 5. Viết Book.kt

Trong `contract/src/main/java`, tạo package `com.example.service`.

Chuột phải package → **New → Kotlin Class/File → Book**.

Tự gõ lại mã trong [Book.kt của project mẫu](../contract/src/main/java/com/example/service/Book.kt).

Khi viết, tập trung vào ba phần:

```kotlin
data class Book(
    val id: Int,
    val name: String,
    val author: String,
    val price: Double
) : Parcelable
```

Đoạn trên minh họa khai báo dữ liệu; phần triển khai đầy đủ nằm trong file mẫu.

`writeToParcel()` ghi các trường theo thứ tự:

```text
id → name → author → price
```

Constructor nhận `Parcel` phải đọc đúng thứ tự đó. `CREATOR` dùng để tạo lại đối tượng `Book` ở phía nhận.

**Điểm kiểm tra:** mở Terminal trong Android Studio, chạy:

```powershell
.\gradlew.bat :contract:assembleDebug
```

Sau khi build thành công, bạn sẽ dùng được:

```kotlin
IBookManager.Stub
IOnBookAddedListener.Stub
```

Đây là các lớp được sinh từ AIDL; bạn không tự viết chúng.

## 6. Viết Server

Trong package `com.example.server`, tạo `BookManagerService.kt`.

Tự viết theo [BookManagerService.kt mẫu](../server/src/main/java/com/example/server/BookManagerService.kt), theo thứ tự:

1. Khai báo danh sách sách và khóa `lock`.
2. Tạo `RemoteCallbackList`.
3. Implement `IBookManager.Stub`.
4. Viết hàm phát callback.
5. Trả Binder trong `onBind()`.
6. Dọn callback trong `onDestroy()`.

Phần cốt lõi là:

```kotlin
private val binder = object : IBookManager.Stub() {
    // Implement các hàm đã khai báo trong IBookManager.aidl.
}

override fun onBind(intent: Intent): IBinder = binder
```

Đoạn trên là khung giải thích; để biên dịch cần implement đầy đủ các hàm như file mẫu.

**Stub là phía Server:** nó nhận lời gọi IPC và thực thi code của bạn.

Sau đó hoàn thiện các file giao diện Server theo thứ tự:

| File cần tạo | File mẫu |
|---|---|
| `res/values/strings.xml` | [Chuỗi Server](../server/src/main/res/values/strings.xml) |
| `res/values/themes.xml` | [Theme Server](../server/src/main/res/values/themes.xml) |
| `res/layout/activity_server.xml` | [Layout Server](../server/src/main/res/layout/activity_server.xml) |
| `ServerActivity.kt` | [Activity Server](../server/src/main/java/com/example/server/ServerActivity.kt) |
| `AndroidManifest.xml` | [Manifest Server](../server/src/main/AndroidManifest.xml) |

Các đường dẫn `res/...` trong bảng nằm dưới `server/src/main/`.

Trong Manifest, chú ý Service:

```xml
<service
    android:name=".BookManagerService"
    android:exported="true"
    android:permission="com.example.server.permission.BOOK_SERVICE"
    android:process=":remote" />
```

Permission `signature` phải được khai báo ngoài `<application>`, như file mẫu. Hai app debug build trên cùng máy dùng chung debug certificate để có quyền kết nối.

**Điểm kiểm tra:** chọn **Run → Edit Configurations → + → Android App**, chọn module `server`, đặt Launch là **Default Activity**, rồi chạy Server trên emulator.

## 7. Viết Client

Trong thư mục `app/src/main`, tạo giao diện trước để các ID tồn tại khi viết Activity:

| File cần tạo | File mẫu |
|---|---|
| `res/values/strings.xml` | [Chuỗi Client](../client/src/main/res/values/strings.xml) |
| `res/values/themes.xml` | [Theme Client](../client/src/main/res/values/themes.xml) |
| `res/layout/activity_client.xml` | [Layout Client](../client/src/main/res/layout/activity_client.xml) |
| `AndroidManifest.xml` | [Manifest Client](../client/src/main/AndroidManifest.xml) |

Liên kết trỏ vào thư mục `client` của project mẫu. Khi thực hành, bạn tạo các file tương ứng trong thư mục `app` đã ánh xạ thành module `client` ở bước 3.

Sau đó tạo `ClientActivity.kt` trong package `com.example.client`, viết theo [ClientActivity.kt mẫu](../client/src/main/java/com/example/client/ClientActivity.kt).

File này khá dài, nên viết thành từng phần theo thứ tự:

1. **Các biến giao diện:** trạng thái, danh sách, tên sách, tác giả và các nút.
2. **Worker:** dùng `Executors.newSingleThreadExecutor()` để thực hiện lời gọi remote.
3. **Session:** giữ remote interface và listener của một phiên kết nối.
4. **Binding:** implement `ServiceConnection`.
5. **onCreate():** lấy các View và gắn sự kiện cho nút.
6. **Bind và gọi API:** kết nối, thêm sách, đọc danh sách.
7. **Dọn kết nối:** unregister, unbind trong `onStop()`.

Khi bind, phải chỉ rõ app và lớp Service:

```kotlin
val intent = Intent().setComponent(
    ComponentName(
        "com.example.server",
        "com.example.server.BookManagerService"
    )
)
```

Khi nhận Binder trong `onServiceConnected()`, lấy interface bằng:

```kotlin
val remote = IBookManager.Stub.asInterface(service)
```

Sau đó Client có thể gọi:

```kotlin
remote.getBookList()
remote.getServerPid()
remote.addBook(book)
```

Các lời gọi này được thực hiện trên worker trong project mẫu. Biến `book` là đối tượng `Book` được tạo từ dữ liệu người dùng nhập.

Callback được nhận qua:

```kotlin
object : IOnBookAddedListener.Stub() {
    override fun onBookAdded(newBook: Book) {
        // Chuyển sang UI thread trước khi cập nhật View.
    }
}
```

Đọc kỹ cách mẫu dùng **listener riêng cho từng Session**. Nó giúp callback từ kết nối cũ không cập nhật màn hình của kết nối mới.

## 8. Chạy và kiểm tra

Tạo cấu hình Run cho module `client`, giống cách đã làm với `server`.

Thực hiện trên **cùng một emulator**:

1. Chạy `server` để cài app.
2. Chạy `client`.
3. Kiểm tra trạng thái đã kết nối và danh sách có hai sách mẫu.
4. So sánh PID Client và PID Service: chúng phải khác nhau.
5. Bấm **Thêm sách qua AIDL**: danh sách tăng lên ba sách và xuất hiện callback.
6. Bấm **Ngắt kết nối**, rồi **Kết nối lại**.
7. Nhấn Home và quay lại để quan sát việc bind/unbind theo vòng đời.

Nếu muốn tự viết lại kiểm thử, tạo `AidlIntegrationTest.kt` trong `app/src/androidTest/java/com/example/client`, theo [bài kiểm thử mẫu](../client/src/androidTest/java/com/example/client/AidlIntegrationTest.kt).

Chạy:

```powershell
.\gradlew.bat :server:installDebug
.\gradlew.bat :client:connectedDebugAndroidTest
```

Khi thực hành, hãy hoàn thành và build được **module contract trước**, rồi mới viết Server và Client. Đây là điểm kiểm tra đầu tiên để xác nhận cấu hình AIDL đã đúng.

Ảnh kết quả của project mẫu: [Client thêm sách và nhận callback](client-demo.png).
