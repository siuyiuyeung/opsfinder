# OpsFinder

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-blue.svg)](https://vuejs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

OpsFinder is a modern operations management application built with Spring Boot and Vue.js, designed to help teams manage devices, track operations, and handle error messages efficiently.

## Features

- 🔐 **JWT Authentication** - Secure user authentication with role-based access control
- 📱 **Device Management** - Track and manage devices with full-text search capabilities
- 🚨 **Error Message Tracking** - Pattern-based error detection with actionable recommendations
- 🔄 **Real-time Updates** - WebSocket support for live data synchronization
- 📊 **RESTful API** - Well-documented API endpoints
- 🎨 **Modern UI** - Responsive Vuetify 3 interface
- 🐳 **Docker Support** - Containerized deployment with Docker Compose

## Tech Stack

### Backend
- **Java 21** with Spring Boot 4.0.0
- **Spring Security 7.x** for authentication & authorization
- **Spring Data JPA** with Hibernate
- **PostgreSQL 16** database
- **Liquibase** for database migrations
- **JWT** for token-based authentication
- **WebSocket** for real-time communication
- **Caffeine** for caching

### Frontend
- **Vue 3** with Composition API
- **Vuetify 3** UI framework
- **Vite** build tool
- **TypeScript** for type safety
- **Pinia** for state management
- **Axios** for HTTP requests
- **STOMP** for WebSocket communication

## Quick Start

### Prerequisites

- Java 21 or higher
- Node.js 18+ and npm
- PostgreSQL 16
- Docker & Docker Compose (optional)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd OpsFinder
   ```

2. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your database credentials
   ```

3. **Start PostgreSQL**
   ```bash
   docker-compose -f docker-compose.db.yml up -d
   ```

4. **Run the backend**
   ```bash
   ./gradlew bootRun
   ```

5. **Run the frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

6. **Access the application**
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080
   - Default credentials: `admin` / `admin123`

### Docker Deployment

```bash
# Start all services
docker-compose up -d

# Access at http://localhost
```

## Project Structure

```
OpsFinder/
├── docs/                       # Documentation
│   ├── deployment/            # Deployment guides
│   ├── guides/                # Development guides
│   └── *.md                   # Project documentation
├── scripts/                    # Utility scripts
│   ├── build/                 # Build scripts
│   ├── database/              # Database scripts
│   ├── deploy/                # Deployment scripts
│   └── setup/                 # Setup scripts
├── src/                       # Backend source code
│   ├── main/
│   │   ├── java/              # Java source files
│   │   └── resources/         # Configuration files
│   └── test/                  # Test files
├── frontend/                  # Frontend application
│   ├── src/                   # Vue source files
│   └── public/                # Static assets
└── CLAUDE.md                  # AI assistant context
```

## Documentation

- **[Development Setup](docs/guides/INTELLIJ_SETUP.md)** - IntelliJ IDEA configuration
- **[Password Tool](docs/guides/PASSWORD_TOOL.md)** - BCrypt password encryption utility
- **[Frontend Build (Windows)](docs/guides/WINDOWS_FRONTEND_BUILD.md)** - Windows build instructions
- **[Frontend API Config](docs/guides/FRONTEND_API_CONFIG.md)** - API configuration guide
- **[Deployment Guide](docs/deployment/DEPLOYMENT.md)** - Production deployment
- **[Docker Deployment](docs/deployment/DOCKER_DEPLOYMENT.md)** - Docker setup
- **[External Database](docs/deployment/EXTERNAL_DATABASE.md)** - External PostgreSQL setup

## Development

### Backend Commands

```bash
# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew build

# Encrypt password (BCrypt)
./gradlew encryptPassword --args="yourpassword"
```

### Frontend Commands

```bash
cd frontend

# Development server
npm run dev

# Build for production
npm run build

# Type checking
npm run type-check

# Preview production build
npm run preview
```

### Database Management

```bash
# Reset database
psql -U opsuser -d opsfinder -f scripts/database/reset-database.sql

# Initialize database
psql -U opsuser -d opsfinder -f scripts/database/init-db.sql
```

## API Documentation

The API is RESTful and follows these conventions:

- **Authentication**: `/api/auth/**`
- **Devices**: `/api/devices/**`
- **Error Messages**: `/api/error-messages/**`
- **Users**: `/api/users/**`
- **WebSocket**: `/ws/**`

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `opsfinder` |
| `DB_USER` | Database user | `opsuser` |
| `DB_PASSWORD` | Database password | `opspassword` |
| `JWT_SECRET` | JWT secret key | Generated |
| `ADMIN_PASSWORD` | Default admin password | `admin123` |

### Profiles

- **Default** (development): `application.yml`
- **Production**: `application-prod.yml` (use `SPRING_PROFILES_ACTIVE=prod`)

## Security

- ✅ JWT-based authentication
- ✅ BCrypt password hashing
- ✅ Role-based access control (ADMIN, OPERATOR, VIEWER)
- ✅ CORS configuration
- ✅ SQL injection prevention via JPA
- ✅ XSS protection via content security policy

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Commit Convention

Follow conventional commits:
- `feat:` - New features
- `fix:` - Bug fixes
- `docs:` - Documentation changes
- `refactor:` - Code refactoring
- `test:` - Test additions/changes
- `chore:` - Build process or tooling changes

## License

This project is proprietary software. All rights reserved.

## Support

For issues and questions:
- Create an issue in the repository
- Contact the development team

---

**Built with ❤️ using Spring Boot, Vue.js, and Claude Code**
