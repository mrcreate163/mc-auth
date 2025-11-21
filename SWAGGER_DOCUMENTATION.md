# Swagger UI Documentation Guide

## Обзор

Микросервис MC-AUTH имеет полную документацию API через SpringDoc OpenAPI 3.0 (Swagger UI).

## Как получить доступ

После запуска приложения:

```bash
./mvnw spring-boot:run
```

Откройте в браузере: **http://localhost:8081/swagger-ui.html**

## Основные возможности

### 1. Информация о API

При открытии Swagger UI вы увидите:
- **Название**: MC-Auth Microservice API
- **Версия**: 1.0.0
- **Описание**: Полное описание микросервиса аутентификации
- **Контакты**: support@socialnetwork.com
- **Лицензия**: MIT License

### 2. Группировка эндпоинтов

Все эндпоинты сгруппированы под тегом **"Аутентификация"**:
- POST /api/v1/auth/register - Регистрация нового пользователя
- POST /api/v1/auth/login - Вход в систему
- GET /api/v1/auth/validate - Проверка валидности JWT токена
- POST /api/v1/auth/refresh - Обновление access токена
- POST /api/v1/auth/logout - Выход из системы
- GET /api/v1/auth/captcha - Генерация капчи
- POST /api/v1/auth/password/recovery/ - Запрос на восстановление пароля
- POST /api/v1/auth/change-password-link - Смена пароля по токену
- POST /api/v1/auth/change-email-link - Запрос на изменение email
- GET /api/v1/auth/confirm-email-change - Подтверждение изменения email

### 3. Детальная документация каждого эндпоинта

Для каждого эндпоинта доступно:

#### Описание
- Краткое резюме операции
- Подробное описание функционала
- Требования к данным

#### Параметры
- **Path parameters**: параметры в URL
- **Query parameters**: параметры в строке запроса
- **Headers**: требуемые заголовки (например, X-User-Id, Authorization)
- **Request Body**: схема тела запроса с примерами

#### Примеры Request Body
```json
{
  "email": "user@example.com",
  "password": "mySecurePassword123"
}
```

#### Возможные ответы
- **200 OK**: Успешный ответ с примером
- **400 Bad Request**: Ошибка валидации с описанием
- **401 Unauthorized**: Неверные учетные данные
- **409 Conflict**: Конфликт данных (например, email уже существует)
- **500 Internal Server Error**: Внутренняя ошибка сервера

### 4. Модели данных (Schemas)

Swagger UI показывает все DTO модели:

#### Request Models:
- **AuthenticateRq**: Данные для входа
- **RegistrationDto**: Данные для регистрации
- **RefreshTokenRequest**: Запрос обновления токена
- **ChangePasswordRequest**: Запрос смены пароля
- **ChangeEmailRequest**: Запрос смены email
- **RecoveryPasswordLinkRq**: Запрос восстановления пароля

#### Response Models:
- **TokenResponse**: JWT токены (access + refresh)
- **ErrorResponse**: Информация об ошибке
- **CaptchaDto**: Данные капчи
- **ValidationResponse**: Результат валидации токена

Каждое поле модели содержит:
- Тип данных
- Описание
- Пример значения
- Validation правила (required, minLength, format)

### 5. Аутентификация

Swagger UI поддерживает JWT Bearer аутентификацию:

1. Нажмите кнопку **"Authorize"** вверху справа
2. Введите JWT токен в формате: `Bearer {your_token}`
3. Нажмите **"Authorize"**
4. Теперь можно тестировать защищённые эндпоинты

### 6. Интерактивное тестирование

Для каждого эндпоинта:

1. Нажмите на эндпоинт для раскрытия
2. Нажмите **"Try it out"**
3. Заполните параметры/тело запроса
4. Нажмите **"Execute"**
5. Увидите:
   - Запрос (Request URL, headers, body)
   - Ответ (Response code, headers, body)
   - cURL команду для воспроизведения запроса

## Примеры использования

### Пример 1: Тестирование регистрации

1. Сначала получите капчу:
   ```
   GET /api/v1/auth/captcha
   ```
   
2. Используйте полученный код в регистрации:
   ```
   POST /api/v1/auth/register
   {
     "email": "test@example.com",
     "password1": "Test123!",
     "password2": "Test123!",
     "firstName": "Тест",
     "lastName": "Тестов",
     "captchaCode": "{код из капчи}"
   }
   ```

### Пример 2: Вход и использование токенов

1. Войдите:
   ```
   POST /api/v1/auth/login
   {
     "email": "test@example.com",
     "password": "Test123!"
   }
   ```

2. Скопируйте полученный accessToken

3. Авторизуйтесь в Swagger UI:
   - Нажмите "Authorize"
   - Вставьте: `Bearer {accessToken}`

4. Теперь можете тестировать защищённые эндпоинты

### Пример 3: Обновление токена

```
POST /api/v1/auth/refresh
{
  "refreshToken": "{ваш refresh токен}"
}
```

## Конфигурация

Настройки Swagger UI находятся в `application.yaml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    display-request-duration: true
    doc-expansion: none
  show-actuator: false
```

## Экспорт документации

### Получение OpenAPI спецификации

**JSON формат:**
```bash
curl http://localhost:8081/v3/api-docs > openapi.json
```

**YAML формат:**
```bash
curl http://localhost:8081/v3/api-docs.yaml > openapi.yaml
```

### Использование с другими инструментами

OpenAPI спецификацию можно импортировать в:
- **Postman**: File → Import → openapi.json
- **Insomnia**: Import/Export → Import Data → openapi.json
- **API Platform**: Для автоматической генерации клиентов
- **Swagger Editor**: Для редактирования и валидации

## Преимущества документации

✅ **Всегда актуальная**: Генерируется автоматически из кода
✅ **Интерактивная**: Можно тестировать API прямо из браузера
✅ **Полная**: Все endpoints, параметры, модели документированы
✅ **На русском языке**: Все описания на понятном русском
✅ **Профессиональная**: Соответствует стандартам OpenAPI 3.0
✅ **Расширяемая**: Легко добавлять новые endpoints и модели

## Качественное оформление

Все элементы документации содержат:

### Эндпоинты:
- ✓ Краткое резюме (summary)
- ✓ Подробное описание (description)
- ✓ Все возможные HTTP статусы
- ✓ Примеры успешных и ошибочных ответов
- ✓ Описание security требований

### DTOs:
- ✓ Описание класса
- ✓ Описание каждого поля
- ✓ Примеры значений
- ✓ Validation правила (minLength, pattern, etc.)
- ✓ Указание required/optional полей

### Параметры:
- ✓ Описание каждого параметра
- ✓ Примеры значений
- ✓ Указание типа данных
- ✓ Указание required/optional

## Заключение

Swagger UI документация MC-Auth микросервиса является:
- **Полной** - охватывает все 10 endpoints
- **Точной** - соответствует реальной реализации
- **Понятной** - с подробными описаниями на русском
- **Интерактивной** - позволяет тестировать API
- **Профессиональной** - следует best practices OpenAPI

Используйте её для:
- Изучения API
- Тестирования функционала
- Интеграции с фронтендом
- Генерации клиентских SDK
- Обучения новых разработчиков
