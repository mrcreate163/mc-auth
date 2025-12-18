# === Этап 1: Сборка проекта ===
# Используем образ с Maven и Java 21, как указано в вашем pom.xml
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем файл pom.xml для скачивания зависимостей
COPY pom.xml .

# Скачиваем все зависимости, чтобы кешировать этот слой
RUN mvn dependency:go-offline

# Копируем все исходные коды вашего проекта
COPY src ./src

# Запускаем сборку проекта. Пропускаем тесты, так как они не нужны в production-образе
RUN mvn clean package -DskipTests


# === Этап 2: Создание финального образа ===
# Используем легковесный образ только с Java 21
FROM eclipse-temurin:21-jre-jammy

# Указываем рабочую директорию
WORKDIR /app

# Копируем собранный .jar файл из этапа сборки
COPY --from=build /app/target/*.jar app.jar

# Указываем порт, который слушает ваше приложение
EXPOSE 8081

# Команда для запуска приложения с оптимизированными JVM параметрами
# Параметры памяти:
# -Xms128m -Xmx256m: ограничение heap памяти
# -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m: ограничение metaspace
# -XX:+UseSerialGC: экономный сборщик мусора для микросервисов
# -XX:TieredStopAtLevel=1: отключение агрессивной JIT-компиляции
# -Xss256k: уменьшение стека потоков
# -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0: поддержка контейнеров
ENTRYPOINT ["java", \
    "-Xms128m", \
    "-Xmx256m", \
    "-XX:MetaspaceSize=64m", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:+UseSerialGC", \
    "-XX:TieredStopAtLevel=1", \
    "-Xss256k", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
