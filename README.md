# Image API

Aplikacja Spring Boot do generowania obrazów przy użyciu AI (DALL-E 3) i wysyłania ich pocztą elektroniczną.

## Wymagania

- Java 17 lub nowsza
- Maven
- Klucz API OpenAI
- Klucz API SendGrid

## Konfiguracja

1. Sklonuj repozytorium
2. Ustaw zmienne środowiskowe:

### Windows (PowerShell)
```powershell
$env:OPENAI_API_KEY="twój_klucz_openai"
$env:SENDGRID_API_KEY="twój_klucz_sendgrid"
$env:SENDGRID_FROM_EMAIL="twój_email"
```

### Linux/Mac
```bash
export OPENAI_API_KEY="twój_klucz_openai"
export SENDGRID_API_KEY="twój_klucz_sendgrid"
export SENDGRID_FROM_EMAIL="twój_email"
```

### IntelliJ IDEA
1. Kliknij prawym przyciskiem myszy na projekt
2. Wybierz "Modify Run Configuration"
3. W sekcji "Environment variables" dodaj:
```
OPENAI_API_KEY=twój_klucz_openai
SENDGRID_API_KEY=twój_klucz_sendgrid
SENDGRID_FROM_EMAIL=twój_email
```

## Uruchomienie

1. Zbuduj projekt:
```bash
mvn clean install
```

2. Uruchom aplikację:
```bash
mvn spring:boot run
```

3. Otwórz przeglądarkę i przejdź do:
```
http://localhost:8080
```

## Funkcje

- Generowanie obrazów przy użyciu DALL-E 3
- Generowanie żartów na wybrany temat
- Wysyłanie wygenerowanych obrazów pocztą elektroniczną
- Galeria wygenerowanych obrazów
- Konsola H2 dostępna pod adresem: `http://localhost:8080/h2-console`

## Baza danych H2

- URL: `jdbc:h2:mem:imagedb`
- Username: `sa`
- Password: `password`
- Konsola H2: `http://localhost:8080/h2-console`

## API Endpoints

- `POST /api/images/generate` - generowanie obrazu
- `POST /api/images/email` - wysyłanie obrazu pocztą
- `GET /api/images` - pobieranie wszystkich obrazów
- `GET /api/jokes` - generowanie żartu
- `GET /api/jokes/{topic}` - generowanie żartu na temat 



DODATKOWE FUNKCJONALNOSCI W TRAKCIE DOKUMENTOWANIA
