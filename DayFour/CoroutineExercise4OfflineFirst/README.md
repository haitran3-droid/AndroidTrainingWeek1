# Bài 4 — Danh sách user offline-first

UI luôn quan sát Room bằng `Flow`. Nút Refresh lấy dữ liệu từ JSONPlaceholder qua Retrofit rồi thay cache trong transaction; lỗi mạng được hiển thị nhưng dữ liệu cache vẫn còn. Xóa chỉ cập nhật Room sau khi server trả HTTP thành công.
