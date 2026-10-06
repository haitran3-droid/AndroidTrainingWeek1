# Bài 7 — StateFlow / SharedFlow

Stop collector, phát nhiều state/event rồi Start. StateFlow trả giá trị cuối; SharedFlow replay=0 và extraBufferCapacity=64 không giữ event khi vắng subscriber; replay=1 phát lại event cuối mỗi lần Start.
