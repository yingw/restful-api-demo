# Restful API Demo

This is a demo project to show how to design restful API through Spring MVC and Swagger.

## Quick Start

Clone and Run

```bash
git clone http://git.wilmar.cn/YinGuowei/restful-api-demo.git && cd restful-api-demo
./mvnw spring-boot:run
```

Visit Swagger at: http://localhost:8080/swagger-ui.html

Visit database at: http://localhost:8080/h2-console/ use:
- JDBC URL: jdbc:h2:mem:api
- Username: sa
- Password: [leave empty]

Use curl to test:
```bash
# get users
curl -X GET http://localhost:8080/api/users?keyword=YIN&page=2&size=1&sort=name,asc&sort=email,desc

# create user
curl -X POST http://localhost:8080/api/users -d "{\"login\": \"test\", \"name\": \"Test User\", \"email\": \"test@example.com\", \"roles\": [ {\"id\": 1} ]}" -H "Content-Type:application/json"

```

## Generate API Docs

1. generate adoc files
```bash
./mvnw swagger2markup:convertSwagger2markup
```

2. generate html and PDF files

```bash
./mvnw asciidoctor:process-asciidoc
```

Or just run

```bash
./mvnw spring-boot:run
./mvnw test (need 8080 running)
```

Then find your document (HTML/PDF) in **target/asciidoc** folder. (TODO: Chinese font in PDF needs to be fixed.)
