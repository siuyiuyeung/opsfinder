# Password Encryption Tool

This tool encrypts passwords using BCrypt, the same encryption method used by the OpsFinder application.

## Usage

### Method 1: Using Gradle Task (Recommended)

```bash
# Windows
gradlew encryptPassword --args="yourpassword"

# Linux/Mac
./gradlew encryptPassword --args="yourpassword"
```

**Example:**
```bash
gradlew encryptPassword --args="admin123"
```

**Output:**
```
╔════════════════════════════════════════════════════════════════╗
║           Password Encrypted Successfully                      ║
╚════════════════════════════════════════════════════════════════╝

Plain Text Password: admin123

BCrypt Hash:
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

Hash Length: 60 characters

You can use this hash in:
  - Liquibase changelog files
  - Direct SQL INSERT/UPDATE statements
  - Application configuration
```

### Method 2: Run Directly from IntelliJ

1. Open `src/main/java/com/igsl/opsfinder/util/PasswordEncryptionTool.java`
2. Right-click on the file → **Run 'PasswordEncryptionTool.main()'**
3. Edit the run configuration to add program arguments:
   - Run → Edit Configurations
   - Add your password in "Program arguments" field
   - Click OK and run again

### Method 3: Command Line (After Build)

```bash
# Build the project first
gradlew build

# Run the tool
java -cp build/classes/java/main;build/libs/* com.igsl.opsfinder.util.PasswordEncryptionTool yourpassword
```

## Common Use Cases

### 1. Update Admin Password in Changelog

Edit `src/main/resources/db/changelog/changelog-001-users.yaml`:

```yaml
- column:
    name: password
    value: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

Then reset the database:
```sql
DROP TABLE users CASCADE;
DROP TABLE databasechangelog;
DROP TABLE databasechangeloglock;
```

Restart the application to rerun migrations.

### 2. Update Existing User Password in Database

```sql
UPDATE users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'admin';
```

### 3. Create New Users via SQL

```sql
INSERT INTO users (username, password, full_name, role, active)
VALUES (
    'newuser',
    '$2a$10$HASH_GENERATED_FROM_TOOL',
    'New User',
    'OPERATOR',
    true
);
```

## Important Notes

1. **BCrypt hashes are always 60 characters long** - if your hash is shorter, it's invalid
2. **BCrypt generates different hashes for the same password** - this is normal and secure
3. **Never store plain text passwords** - always use BCrypt hashes
4. **The tool uses the same BCryptPasswordEncoder** as the application for consistency

## Troubleshooting

### "No such property: args"
Run without arguments first to see usage:
```bash
gradlew encryptPassword
```

### "Class not found"
Build the project first:
```bash
gradlew build
```

### Hash doesn't work for login
- Verify the hash is exactly 60 characters
- Check that you copied the entire hash without line breaks
- Ensure the user account is active in the database
- Check database password column can store 255 characters

## Security Notes

- BCrypt is a one-way hash - you cannot decrypt it back to the original password
- Each time you encrypt the same password, you get a different hash (this is intentional)
- The algorithm automatically includes salt for security
- The `$2a$10$` prefix indicates BCrypt version and work factor
