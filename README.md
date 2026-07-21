# Currency Exchange Service

REST API для работы с валютами и обменными курсами. Приложение позволяет хранить валюты и курсы, редактировать их и рассчитывать конвертацию.

Проект создан как pet-проект для практики Java Backend: Servlet API, JDBC, SQL и многослойная архитектура.

## Возможности

- просмотр списка валют и поиск валюты по коду;
- добавление валют;
- просмотр, добавление, редактирование и удаление обменных курсов;
- поиск курса по валютной паре;
- конвертация суммы по прямому, обратному или кросс-курсу через USD;
- валидация входных данных;
- централизованная обработка ошибок с JSON-ответами;
- простой web-интерфейс на HTML, CSS и JavaScript;
- логирование работы приложения и доступа к базе данных.

## Технологии

- Java 17;
- Jakarta Servlet API;
- PostgreSQL;
- JDBC и HikariCP;
- Maven и сборка в WAR;
- Apache Tomcat 11;
- Jackson для JSON;
- Lombok;
- SLF4J Simple;
- HTML, CSS и JavaScript без frontend-фреймворков.

## Архитектура

Приложение разделено на слои:

```text
controller/  — сервлеты и HTTP-запросы
service/     — бизнес-логика и расчёт обмена
dao/         — JDBC-запросы к PostgreSQL
entity/      — сущности, получаемые из базы данных
dto/         — данные запросов и ответов API
mapper/      — преобразование entity в DTO
filter/      — кодировка и обработка исключений
config/      — контекст приложения и пул соединений
```

Для расчёта конвертации последовательно применяются три стратегии:

1. прямой курс `A → B`;
2. обратный курс `B → A`;
3. кросс-курс через USD: `USD → B / USD → A`.

## REST API

Все успешные ответы возвращаются в JSON. Ошибки имеют формат:

```json
{
  "message": "Error message"
}
```

### Валюты

| Метод | Endpoint | Описание |
| --- | --- | --- |
| `GET` | `/currencies` | Получить список валют |
| `GET` | `/currency/{code}` | Получить валюту по коду |
| `POST` | `/currencies` | Добавить новую валюту |

### Обменные курсы

| Метод | Endpoint | Описание |
| --- | --- | --- |
| `GET` | `/exchangeRates` | Получить список курсов |
| `GET` | `/exchangeRate/{pair}` | Получить курс, например `/exchangeRate/USDRUB` |
| `POST` | `/exchangeRates` | Добавить курс |
| `PATCH` | `/exchangeRate/{pair}` | Обновить курс |
| `DELETE` | `/exchangeRate/{pair}` | Удалить курс |

### Конвертация

| Метод | Endpoint | Описание |
| --- | --- | --- |
| `GET` | `/exchange?from=USD&to=RUB&amount=10` | Рассчитать обмен валюты |

Данные для `POST` и `PATCH` передаются как `application/x-www-form-urlencoded`. Готовые запросы для проверки находятся в [requests.http](requests.http).

## Быстрый локальный запуск

Нужны JDK 17+, Maven, PostgreSQL и Tomcat 11.

1. Создай базу и таблицы SQL-скриптами: [create_database.sql](database/create_database.sql), [schema.sql](database/schema.sql), [data.sql](database/data.sql).
2. Скопируй `src/main/resources/application.properties.example` в `src/main/resources/application.properties` и укажи свои параметры PostgreSQL.
3. Собери приложение:

```bash
mvn clean package
```

4. Скопируй `target/currency-exchange.war` в папку `webapps` Tomcat и запусти Tomcat.
5. Открой `http://localhost:8080/currency-exchange/`.

`application.properties` содержит пароль от базы и намеренно исключён из Git.
