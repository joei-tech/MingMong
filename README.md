# Setup Guide (README)
## Prerequisites
Please make sure that the following is installed:
- JDK 26 (`java -version`)
- Netbeans IDE
- Oracle Database XE 21c
- Oracle JDBC driver ojdbc11.jar
- Oracle SQL developer running on localhost:1521

## Database Setup
1. Run Oracle SQL Developer and create a new database connection.
2. Under Name, enter Mingmong. Then, under User Info, enter the default SYSTEM account and the password that was set during installation.
3. Open the provided schema.sql file. Select your active database connection from the top-right menu in the SQL worksheet, then click Run Script to create the required tables.
4. Run the command `COMMIT;` to save the changes.
5. To verify that all three tables were created, please run:
```
SELECT table_name FROM user_tables WHERE table_name IN ('PLAYERS', 'MATCHES', 'ROUNDS');.
```

## Configuration
Open the JDBC.java file and write:
   ```
private static final String URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
private static final String USER = "system";
private static final String PASSWORD = "password";
   ```

Note: Replace “password” with your actual password in Oracle.

# Build and run 
- In GitHub, click Code then download ZIP file, then extract the downloaded folder on your computer. 
- Then in Apache NetBeans, go to MingMong, then right-click on the project and select: Clean and Build.  
   Expected result: 
   BUILD SUCCESS

- Then right-click on the project and select: Run 

Make sure to use this main class: 
Package florante.mingmong; 

Expected result: 
The MingMong welcome screen opens.
