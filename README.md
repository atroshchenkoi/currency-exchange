# Currency Exchange REST API

Учебный REST API для хранения валют, обменных курсов и расчёта конвертации.

Проект запускается как WAR-приложение в Apache Tomcat и использует PostgreSQL.

## Что нужно установить

- JDK 17 или новее;
- Maven 3.9 или новее;
- PostgreSQL;
- Apache Tomcat 11.

Проверить Java и Maven можно командами:

```powershell
java -version
mvn -version
```

## Самое важное: как запустить базу данных

База данных не хранится в GitHub как готовый файл. Вместо этого в репозитории лежат SQL-скрипты, которые создают одинаковую структуру и начальные данные:

- [database/create_database.sql](database/create_database.sql) — создаёт пользователя и базу `currency_exchange`;
- [src/main/resources/database/schema.sql](src/main/resources/database/schema.sql) — создаёт таблицы;
- [src/main/resources/database/data.sql](src/main/resources/database/data.sql) — добавляет начальные валюты и курсы.

Другому человеку достаточно установить PostgreSQL и выполнить эти три скрипта. Передавать свою локальную базу или пароль не нужно.

### Вариант 1. Через PowerShell и `psql`

1. Установи PostgreSQL и убедись, что его сервер запущен.

2. Открой PowerShell в корне проекта.

3. Укажи путь к `psql.exe`. Вместо `16` подставь установленную версию PostgreSQL:

```powershell
$psqlPath = "C:\Program Files\PostgreSQL\16\bin\psql.exe"
```

4. Открой [database/create_database.sql](database/create_database.sql), замени пароль `change_me` на свой и выполни скрипт. PostgreSQL попросит пароль администратора `postgres`:

```powershell
& $psqlPath -U postgres -d postgres -f database/create_database.sql
```

5. Создай таблицы:

```powershell
& $psqlPath -U currency_exchange_user -d currency_exchange -f src/main/resources/database/schema.sql
```

6. Добавь начальные данные:

```powershell
& $psqlPath -U currency_exchange_user -d currency_exchange -f src/main/resources/database/data.sql
```

После этих команд база готова. Скрипты рассчитаны на новую пустую базу; повторный запуск `schema.sql` или `data.sql` приведёт к ошибкам существующих таблиц или записей.

### Вариант 2. Через pgAdmin

1. Открой pgAdmin и подключись к локальному серверу PostgreSQL.
2. Выбери базу `postgres`, открой **Query Tool** и выполни содержимое [database/create_database.sql](database/create_database.sql). Перед выполнением замени `change_me` на свой пароль.
3. Обнови список баз, выбери созданную базу `currency_exchange` и снова открой **Query Tool**.
4. Последовательно выполни содержимое `schema.sql`, а затем `data.sql`.

## Настройка приложения

1. Создай локальный файл конфигурации из примера:

```powershell
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
```

2. В созданном файле `src/main/resources/application.properties` укажи тот же пароль, который записал в `create_database.sql`:

```properties
database.url=jdbc:postgresql://localhost:5432/currency_exchange
database.username=currency_exchange_user
database.password=your_password
database.driver-class-name=org.postgresql.Driver
database.pool.maximum-size=10
```

`application.properties` добавлен в `.gitignore`: его нельзя публиковать в GitHub, потому что там находится пароль от БД.

## Сборка и запуск в Tomcat

1. Собери WAR:

```powershell
mvn clean package
```

После успешной сборки появится файл:

```text
target/currency-exchange.war
```

2. Укажи путь к Tomcat и скопируй WAR в папку `webapps`:

```powershell
$tomcatPath = "D:\java_dev\apache-tomcat-11.0.24"
Copy-Item target/currency-exchange.war "$tomcatPath\webapps\currency-exchange.war" -Force
```

3. Запусти Tomcat:

```powershell
& "$tomcatPath\bin\startup.bat"
```

4. Открой в браузере:

```text
http://localhost:8080/currency-exchange/
```

Проверить API можно по адресу:

```text
http://localhost:8080/currency-exchange/currencies
```

Остановить Tomcat:

```powershell
& "$tomcatPath\bin\shutdown.bat"
```

## REST API

Все успешные ответы возвращаются в JSON. Ошибки имеют формат:

```json
{
  "message": "Error message"
}
```

| Метод | URL | Назначение |
| --- | --- | --- |
| `GET` | `/currencies` | Получить все валюты |
| `GET` | `/currency/{code}` | Получить валюту по коду, например `/currency/USD` |
| `POST` | `/currencies` | Добавить валюту: `name`, `code`, `sign` |
| `GET` | `/exchangeRates` | Получить все обменные курсы |
| `GET` | `/exchangeRate/{pair}` | Получить курс, например `/exchangeRate/USDRUB` |
| `POST` | `/exchangeRates` | Добавить курс: `baseCurrencyCode`, `targetCurrencyCode`, `rate` |
| `PATCH` | `/exchangeRate/{pair}` | Обновить курс: `rate` |
| `DELETE` | `/exchangeRate/{pair}` | Удалить курс |
| `GET` | `/exchange?from=USD&to=RUB&amount=10` | Рассчитать обмен |

Данные для `POST` и `PATCH` передаются в формате `application/x-www-form-urlencoded`.

При расчёте обмена используются три стратегии:

1. Прямой курс `A → B`.
2. Обратный курс `B → A`.
3. Кросс-курс через USD: `USD → B / USD → A`.

Сумма результата округляется до двух знаков после запятой.

Для ручной проверки запросов используй [requests.http](requests.http) в IntelliJ IDEA или другом HTTP-клиенте.

## Использованные инструменты

- Java 17;
- Jakarta Servlet API;
- Maven и сборка WAR;
- Apache Tomcat 11;
- PostgreSQL;
- JDBC и HikariCP для пула соединений;
- Jackson для JSON;
- Lombok;
- SLF4J Simple для логирования;
- JUnit 5;
- HTML, CSS и JavaScript без frontend-фреймворков.

## Структура проекта

```text
src/main/java/com/example/currencyexchange/
├── controller/  # Servlets
├── service/     # Business logic
├── dao/         # JDBC access to PostgreSQL
├── entity/      # Database entities
├── dto/         # Request and response objects
├── filter/      # Encoding and exception handling
└── config/      # Application context and connection pool
```

## Частые проблемы

**`Connection refused` или `Database is unavailable`**

Проверь, что сервер PostgreSQL запущен, имя базы, пользователь и пароль в `application.properties` совпадают с созданными в PostgreSQL.

**Tomcat отдаёт старую версию приложения**

Останови Tomcat, скопируй свежий `target/currency-exchange.war` в `webapps` и запусти Tomcat снова.

**`role "currency_exchange_user" does not exist`**

Выполни [database/create_database.sql](database/create_database.sql) от имени пользователя `postgres`.
