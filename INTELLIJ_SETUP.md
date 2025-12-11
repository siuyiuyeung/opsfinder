# IntelliJ IDEA Setup Guide

## Running OpsFinder Backend in IntelliJ IDEA

This guide shows how to configure IntelliJ IDEA to run the Spring Boot backend with environment variables from `.env` file or run configuration.

---

## 📌 Option 1: IntelliJ Run Configuration (Recommended)

**Pros**: Native IntelliJ support, no extra dependencies, works out of the box
**Cons**: Must manually copy values from .env file

### Step-by-Step Instructions:

#### 1. Create/Edit Run Configuration

1. Open IntelliJ IDEA
2. Go to **Run** → **Edit Configurations...**
3. Click **+** (Add New Configuration) → **Spring Boot**
4. Configure:
   - **Name**: `OpsFinder Backend`
   - **Main class**: `com.igsl.opsfinder.OpsFinderApplication`
   - **Module**: `OpsFinder.main`

#### 2. Set Environment Variables

In the run configuration dialog:

1. Find **Environment variables** field
2. Click the **folder icon** (📁) to open environment variables editor
3. Add these variables from your `.env` file:

```properties
DB_HOST=192.168.31.107
DB_PORT=5432
DB_NAME=opsfinder
DB_USER=opsuser
DB_PASSWORD=Apis@2023!
JWT_SECRET=2wsxcft6yhnji9olvaofjapsoidj245h4ohahjgasjgfi45uj935hbnshodjhasdjf4554jokjfglkfjg
JWT_EXPIRATION=604800000
SPRING_PROFILES_ACTIVE=prod
```

#### 3. Alternative: Use "Paste from File"

1. Click **Paste** button in the environment variables dialog
2. Select your `.env` file
3. IntelliJ will automatically parse and import the variables

#### 4. Run the Application

1. Click **Apply** → **OK**
2. Click the **Run** button (▶️) or press **Shift+F10**
3. Application will start with your environment variables

---

## 📌 Option 2: EnvFile Plugin (Easiest)

**Pros**: Automatically loads `.env` files, no code changes
**Cons**: Requires plugin installation

### Step-by-Step Instructions:

#### 1. Install EnvFile Plugin

1. Go to **File** → **Settings** (Windows/Linux) or **IntelliJ IDEA** → **Preferences** (Mac)
2. Navigate to **Plugins**
3. Search for **"EnvFile"**
4. Click **Install** and restart IntelliJ

#### 2. Configure Run Configuration

1. Go to **Run** → **Edit Configurations...**
2. Select your Spring Boot configuration
3. Find **EnvFile** tab (added by the plugin)
4. Click **+** → **Add**
5. Select your `.env` file: `C:\Users\IGS\IdeaProjects\OpsFinder\.env`
6. Check **Enable EnvFile**

#### 3. Run the Application

The application will automatically load variables from `.env` file on every run.

---

## 📌 Option 3: spring-dotenv Library

**Pros**: Works everywhere (IDE, CLI, Docker), code-based
**Cons**: Adds a dependency

### Step-by-Step Instructions:

#### 1. Add Dependency

Add to `build.gradle`:

```gradle
dependencies {
    // Existing dependencies...

    // .env file support
    implementation 'me.paulschwarz:spring-dotenv:4.0.0'
}
```

#### 2. Update application.yml

The library automatically loads `.env` file from project root. No code changes needed!

#### 3. Refresh Gradle

1. Click **Gradle** tab (right side)
2. Click **Reload All Gradle Projects** (🔄)

#### 4. Run Application

Simply run the application - `.env` file will be loaded automatically.

---

## 🎯 Recommended Approach

**For Development**: Use **Option 2 (EnvFile Plugin)**
- Easiest to set up
- Automatically syncs with `.env` file
- No dependency added to project

**For Production/Docker**: Use **Option 3 (spring-dotenv)**
- Works consistently across all environments
- Explicit dependency management
- Best for team collaboration

**Quick Start**: Use **Option 1** if you want immediate results without plugins or dependencies.

---

## 🔧 Current Environment Variables

Your `.env` file contains:

```properties
# Database Configuration
DB_HOST=192.168.31.107
DB_PORT=5432
DB_NAME=opsfinder
DB_USER=opsuser
DB_PASSWORD=Apis@2023!

# JWT Configuration
JWT_SECRET=2wsxcft6yhnji9olvaofjapsoidj245h4ohahjgasjgfi45uj935hbnshodjhasdjf4554jokjfglkfjg
JWT_EXPIRATION=604800000

# Spring Configuration
SPRING_PROFILES_ACTIVE=prod

# Logging
LOG_LEVEL=INFO
APP_LOG_LEVEL=INFO
```

---

## 🏃 Running the Application

### Using IntelliJ UI

1. Select your run configuration from dropdown
2. Click **Run** (▶️) or **Debug** (🐛)

### Using Gradle

```bash
# Load .env manually, then run
./gradlew bootRun
```

### Keyboard Shortcuts

- **Run**: `Shift + F10` (Windows/Linux) or `Ctrl + R` (Mac)
- **Debug**: `Shift + F9` (Windows/Linux) or `Ctrl + D` (Mac)
- **Stop**: `Ctrl + F2` (Windows/Linux) or `Cmd + F2` (Mac)

---

## 🐛 Troubleshooting

### Variables Not Loading

**Check**:
1. Environment variables are correctly set in run configuration
2. No typos in variable names
3. `.env` file is in project root (not `frontend/` directory)

**Verify**:
```java
// Add to OpsFinderApplication.java to debug
System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
System.out.println("DB_PASSWORD: " + System.getenv("DB_PASSWORD"));
```

### Database Connection Issues

1. Verify PostgreSQL is running: `192.168.31.107:5432`
2. Check credentials match `.env` file
3. Ensure database `opsfinder` exists
4. Review application logs for connection errors

### Application Won't Start

1. Check Java version: **Java 21** required
2. Rebuild project: **Build** → **Rebuild Project**
3. Invalidate caches: **File** → **Invalidate Caches / Restart**
4. Check Gradle sync: **Gradle** tab → **Reload All Gradle Projects**

---

## 📝 Best Practices

### Security

- ✅ **DO**: Keep `.env` in `.gitignore` (already configured)
- ✅ **DO**: Use `.env.example` for sharing safe defaults
- ❌ **DON'T**: Commit `.env` with real credentials to Git
- ❌ **DON'T**: Share `.env` file publicly

### Development

- Default `application.yml` is configured for development
- Use `SPRING_PROFILES_ACTIVE=prod` to switch to production profile

### Team Collaboration

- Keep `.env.example` updated with all required variables
- Document any new environment variables in this guide
- Use consistent variable naming across environments

---

## 🆘 Need Help?

- Check Spring Boot logs in IntelliJ console
- Enable debug logging: Set `LOG_LEVEL=DEBUG` in `.env`
- Review `application.yml` for configuration issues
- Consult: https://docs.spring.io/spring-boot/reference/

---

**Last Updated**: 2025-12-10
