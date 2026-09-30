# QuickPick Core

The backend behind [QuickPick](https://github.com/quickpickapp/quickpick-app). It handles sign-up, friendships and push notifications, and passes encrypted picks from one phone to the next without being able to read them.

Spring Boot on Java 25, Postgres, Twilio for the SMS codes, Firebase for push.

![build](https://github.com/quickpickapp/quickpick-core/actions/workflows/gradle.yml/badge.svg)

## Running it locally

You need JDK 25 and Docker.

```sh
docker compose up -d postgres
./gradlew bootRun
```

Before that, core wants two things that aren't in the repo:

**`configurations/config.ini`**

```ini
[database]
hostname = localhost
port = 5432
username = postgres
password = change-me
database = quickpick

[api]
port = 8080
; HS256 keys, at least 32 characters each
verification_key =
authentication_key =
refresh_key =
allowed_origins = http://localhost:5173

[twilio]
account_sid =
auth_token =
phone_number =
verify_service_sid =

; optional: fixed codes for test numbers, no SMS is sent
[twilio.test.1]
phone_number = +4915100000000
verification_code = 123456

[firebase]
; service account JSON, placed in configurations/notification/
configuration = service-account.json
project_id =

[google]
client_id =
client_secret =

[statistic]
key =
```

**`geo/GeoLite2-City.mmdb`**, free from [MaxMind](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data) after signing up.

## License

[AGPL-3.0](LICENSE.md)
