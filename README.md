# ZIoVPO Server

Серверная часть проекта на Spring Boot с поддержкой JWT, HTTPS и PostgreSQL.

## Функции
- JWT-аутентификация с access/refresh токенами
- Ролевая авторизация (`USER`, `ADMIN`)
- HTTPS через `server.ssl` и PKCS12 хранилище
- Подключение к PostgreSQL через `spring.datasource`
- CI-пайплайн GitHub Actions с `test` и `build`

## Переменные окружения
- `PG_URL` — URL базы данных PostgreSQL
- `PG_USER` — пользователь базы данных
- `PG_PASS` — пароль базы данных
- `JWT_SECRET` — секрет для подписи JWT
- `TLS_KEYSTORE_PASSWORD` — пароль для `hotel-1bib23427.p12`

## Запуск
```bash
mvn clean package
java -jar target/*.jar
```

## Postman
Импортируйте `postman/ziovpo.postman_collection.json` в Postman.

1. Установите переменную `base_url`:
   - `https://localhost:8080`
2. Отключите проверку SSL, если используется самоподписанный сертификат.
3. Выполните `POST /api/auth/login` с JSON:
   ```json
   {
     "username": "user@example.com",
     "password": "yourpassword",
     "deviceId": "postman"
   }
   ```
4. Скопируйте `accessToken` из ответа и добавьте заголовок:
   - `Authorization: Bearer <accessToken>`
5. Выполните защищённые запросы, например `GET /whoami`.

Доступные запросы в коллекции:
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/public/ping`
- `GET /whoami`
- `POST /api/user/license/create`
- `POST /api/user/license/activate`
- `GET /api/user/license/check`
- `POST /api/user/license/extend`

## Диаграммы
- `docs/uml/class-diagram.puml`
- `docs/erd/er-diagram.puml`
- `docs/UML_ER.md`
