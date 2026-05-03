# Stage 1: Build môi trường Flutter
FROM ubuntu:20.04 AS build

# Cài đặt các dependencies cần thiết
RUN apt-get update && apt-get install -y curl git wget unzip libgconf-2-4 gdb libstdc++6 libglu1-mesa fonts-droid-fallback lib32stdc++6 python3
RUN apt-get clean

# Tải Flutter SDK
RUN git clone https://github.com/flutter/flutter.git /usr/local/flutter
ENV PATH="/usr/local/flutter/bin:/usr/local/flutter/bin/cache/dart-sdk/bin:${PATH}"

# Đổi channel sang stable và chạy flutter doctor
RUN flutter channel stable
RUN flutter upgrade
RUN flutter doctor -v

# Copy source code của mày vào container
WORKDIR /app
COPY . .

# Build app sang Web
RUN flutter clean
RUN flutter pub get
RUN flutter build web

# Stage 2: Serve app bằng Nginx (Cực nhẹ)
FROM nginx:alpine
COPY --from=build /app/build/web /usr/share/nginx/html
EXPOSE 80