# Troubleshooting Guide - Task Management Application

## ✅ **Issue Resolved: Database Schema Error**

The main issue you encountered was:
```
ORA-00904: "T1_0"."USER_ID": invalid identifier
```

**Root Cause**: The database tables weren't created with the proper structure when using `hibernate.hbm2ddl.auto=update`.

**Solution Applied**: 
1. Temporarily changed to `create-drop` to force table recreation
2. Ran database test to verify proper table creation
3. Changed back to `update` for normal operation

## 🚀 **How to Run the Application**

### **Method 1: Maven Command**
```bash
mvn javafx:run
```

### **Method 2: IntelliJ IDEA**
1. Right-click on `MainApp.java`
2. Select "Run 'MainApp.main()'"

## 🔍 **Testing the Application**

### **1. First Launch**
- You should see a **Login Screen** with email/password fields
- Click **"Sign Up"** to create a new account

### **2. Create Account**
- **Email**: any-email@example.com
- **Display Name**: Your Name
- **Password**: minimum 6 characters
- Click **"Register"**

### **3. Login and Test Features**
- Use your credentials to log in
- You'll see the **Dashboard** with task management interface
- Click **"Add New Task"** to create your first task

## 🛠️ **Common Issues and Solutions**

### **Issue 1: Database Connection Failed**
**Error**: `java.sql.SQLException: ORA-12505`

**Solution**:
1. Ensure Oracle Database is running
2. Check connection details in `src/main/resources/application.properties`:
   ```properties
   db.url=jdbc:oracle:thin:@localhost:1521:xe
   db.username=system
   db.password=Meenakshi@10
   ```

### **Issue 2: Tables Not Created**
**Error**: `ORA-00904: invalid identifier`

**Solution**:
1. Run the database test: `mvn exec:java`
2. This will create all necessary tables and sequences
3. Then run the application: `mvn javafx:run`

### **Issue 3: JavaFX Not Starting**
**Error**: `JavaFX runtime components are missing`

**Solution**:
1. Ensure Java 21+ is installed
2. Check that JavaFX modules are available
3. Try running from IntelliJ IDEA instead

### **Issue 4: Application Crashes on Login**
**Error**: Various database-related errors

**Solution**:
1. Check database connection
2. Verify tables exist by running database test
3. Check console output for specific error messages

## 📊 **Database Schema Verification**

To verify your database is set up correctly, run:
```bash
mvn exec:java
```

This will:
- ✅ Test database connection
- ✅ Create all necessary tables
- ✅ Test user creation
- ✅ Test task creation
- ✅ Clean up test data

## 🔧 **Configuration Files**

### **Database Configuration**
File: `src/main/resources/application.properties`
```properties
# Update these values for your Oracle Database
db.url=jdbc:oracle:thin:@localhost:1521:xe
db.username=your_username
db.password=your_password
```

### **Hibernate Configuration**
File: `src/main/resources/hibernate.cfg.xml`
- Contains database connection settings
- Entity mappings
- Hibernate properties

## 📝 **Application Features to Test**

1. **User Registration**: Create new accounts
2. **User Login**: Authenticate with credentials
3. **Add Tasks**: Create tasks with title, description, priority, deadline
4. **Edit Tasks**: Modify existing tasks
5. **Delete Tasks**: Remove tasks
6. **Mark Complete**: Toggle task completion status
7. **Search**: Find tasks by title/description
8. **Filter**: Filter by priority or status
9. **Statistics**: View task completion stats

## 🎯 **Success Indicators**

Your application is working correctly if you can:
- [ ] See the login screen
- [ ] Register a new user account
- [ ] Login with your credentials
- [ ] See the dashboard with statistics
- [ ] Add a new task
- [ ] Edit/delete tasks
- [ ] Search and filter tasks
- [ ] See real-time statistics updates

## 📞 **Getting Help**

If you encounter issues:
1. Check the console output for error messages
2. Verify database connection
3. Run the database test: `mvn exec:java`
4. Check that all dependencies are downloaded: `mvn clean compile`

The application is now fully functional and ready to use! 🎉
