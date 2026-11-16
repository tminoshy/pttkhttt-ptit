# Restaurant Management System — PTTK Final Project

A Java/JSP web app in [code/restaurant](code/restaurant) with two modules: **Add a new dish** and **View customer statistics by revenue**.

## Run with Docker (recommended)

Requires Docker Engine with Compose v2.

```sh
cd code/restaurant
docker compose up --build -d
```

Open <http://localhost:8080/restaurant/> and log in with the management account declared in [seed.sql](code/restaurant/db/seed.sql).

| Task | Command (in `code/restaurant`) |
| --- | --- |
| Run JUnit tests (on `restaurant_test`) | `docker compose --profile test run --rm test` |
| Follow app logs | `docker compose logs -f app` |
| Stop, keep data | `docker compose down` |
| Stop and delete data and uploaded images | `docker compose down -v` |
| Use other ports | `APP_PORT=8081 DB_PORT=3308 docker compose up -d` |

- The schema and seed data load only when the `db-data` volume is empty. After editing `schema.sql` or `seed.sql`, run `docker compose down -v` and then `up` again.
- After editing Java or JSP files, run `docker compose up --build -d` again.
- Uploaded dish images are kept in the `dish-images` volume.
- From the repository root, add `-f code/restaurant/compose.yaml` to each command instead of `cd`.

## Run without Docker

Requires JDK 17+, Maven 3.9+, MySQL 8 and **Tomcat 10.1** (Jakarta Servlet 6 / JSP 3.1).

1. Create the databases and user as a MySQL administrator:

   ```sql
   CREATE DATABASE restaurant CHARACTER SET utf8mb4;
   CREATE DATABASE restaurant_test CHARACTER SET utf8mb4;
   CREATE USER 'restaurant'@'localhost' IDENTIFIED BY 'restaurant_local';
   GRANT ALL PRIVILEGES ON restaurant.* TO 'restaurant'@'localhost';
   GRANT ALL PRIVILEGES ON restaurant_test.* TO 'restaurant'@'localhost';
   ```

2. Load the schema and seed data **once** into the empty `restaurant` database (from the repository root):

   ```sh
   mysql --default-character-set=utf8mb4 -u restaurant -p restaurant < code/restaurant/db/schema.sql
   mysql --default-character-set=utf8mb4 -u restaurant -p restaurant < code/restaurant/db/seed.sql
   ```

3. Build and test:

   ```sh
   cd code/restaurant
   mvn clean verify
   ```

   JUnit resets only `restaurant_test`. For another port or account, pass `-Dtest.db.url='jdbc:mysql://127.0.0.1:3307/restaurant_test?characterEncoding=UTF-8' -Dtest.db.user=... -Dtest.db.password=...`. The URL must still select `restaurant_test`.

4. Configure the connection in [db.properties](code/restaurant/src/main/resources/db.properties) before building, or pass `-Ddb.config=/absolute/path/db.properties` in Tomcat's `CATALINA_OPTS`.

5. Deploy and start Tomcat:

   ```sh
   # CATALINA_HOME points to your Tomcat 10.1 install
   cp target/restaurant.war "$CATALINA_HOME/webapps/restaurant.war"
   "$CATALINA_HOME/bin/catalina.sh" run
   ```

6. Open <http://localhost:8080/restaurant/> and log in with the management account in [seed.sql](code/restaurant/db/seed.sql). Image upload needs the expanded WAR and a writable `images/dishes` directory.

## Scenario trace check

Needs only Python 3, no database or Tomcat (from the repository root):

```sh
python3 code/restaurant/tools/trace.py                     # both modules
python3 code/restaurant/tools/trace.py --module dish       # or --module statistics
python3 -m unittest discover -s code/restaurant/tools -p 'test_*.py'
```
