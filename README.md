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

## Диаграммы
- `docs/uml/class-diagram.puml`
- `docs/erd/er-diagram.puml`
- `docs/UML_ER.md`
