package cs208;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * The Database class contains helper functions to
 * create a connection and
 * test your connection
 * with the SQLite database.
 */
public class Database {
    private final String sqliteFileName;

    public Database(String sqliteFileName) {
        this.sqliteFileName = sqliteFileName;
    }

    /**
     * Creates a connection to the SQLite database file specified in the {@link #Database(String) constructor}
     *
     * @return a connection to the database, which can be used to execute SQL statements against in the database
     * @throws SQLException if we cannot connect to the database (e.g., missing driver)
     */
    public Connection getDatabaseConnection() throws SQLException {
        // NOTE:
        // 'jdbc' is the protocol or API for connecting from a Java application to a database (SQLite, PostgreSQL, etc.)
        // 'sqlite' is the format of the database (for PostgreSQL, we would use the 'postgresql' format)
        String databaseConnectionURL = "jdbc:sqlite:" + sqliteFileName;
        System.out.println("databaseConnectionURL = " + databaseConnectionURL);

        Connection connection;
        try {
            SQLiteConfig sqLiteConfig = new SQLiteConfig();
            // Enables enforcement of foreign keys constraints in the SQLite database every time we start the application
            sqLiteConfig.enforceForeignKeys(true);

            connection = DriverManager.getConnection(databaseConnectionURL, sqLiteConfig.toProperties());
            return connection;
        } catch (SQLException sqlException) {
            System.err.println("SQLException was thrown while trying to connect using the '" + databaseConnectionURL + "' connection URL");
            System.err.println(sqlException.getMessage());
            throw sqlException;
        }
    }

    /**
     * Tests the connection to the database by running a simple SQL SELECT statement
     * to return the driver version used to connect to the database
     *
     * @return the version of the driver used to connect to the database (e.g., "3.43.0")
     */
    public String testConnection() {
        // this SELECT statement will retrieve one row with one column containing the
        // version of the driver used to connect to the SQLite database
        String sql = "SELECT sqlite_version();";

        try
                (
                        Connection connection = getDatabaseConnection();
                        Statement sqlStatement = connection.createStatement();
                        ResultSet resultSet = sqlStatement.executeQuery(sql);
                ) {
            // get to the first (and only) returned record (row)
            resultSet.next();

            // get the results of the first column in the row which contains the driver version
            String driverVersionToConnectToTheDatabase = resultSet.getString(1);
            System.out.println("Connection to Database Successful!");
            System.out.println("Driver version used to connect to the database: " + driverVersionToConnectToTheDatabase);
            return driverVersionToConnectToTheDatabase;
        } catch (SQLException sqlException) {
            System.err.println("SQLException: failed to query the database");
            System.err.println(sqlException.getMessage());
        }
        return sql;
    }

    public List<Class> listAllClasses() {
        String sql =
                "SELECT id, code, title, description, max_students\n" +
                        "FROM classes;";

        ArrayList<Class> listOfClasses = new ArrayList<>();
        try
                (
                        Connection connection = getDatabaseConnection();
                        Statement sqlStatement = connection.createStatement();
                        ResultSet resultSet = sqlStatement.executeQuery(sql);
                ) {
            //print table header
            printTableHeader(new String[]{"id", "code", "title", "description", "max_students"});

            // resultSet.next() either
            // advances to the next returned record (row)
            // or
            // returns false if there are no more records
            while (resultSet.next()) {
                // extract the values from the current row
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                int maxStudents = resultSet.getInt("max_students");

                // print the results of the current row
                System.out.printf("| %d | %s | %s | %s | %d |%n", id, code, title, description, maxStudents);

                Class classForCurrentRow = new Class(id, code, title, description, maxStudents);
                listOfClasses.add(classForCurrentRow);
            }
        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }

        return listOfClasses;
    }

    public Class addNewClass(Class newClass) throws SQLException {
        String sql =
                "INSERT INTO classes (code, title, description, max_students)\n" +
                        "VALUES (?, ?, ?, ?);";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ) {
            sqlStatement.setString(1, newClass.getCode());
            sqlStatement.setString(2, newClass.getTitle());
            sqlStatement.setString(3, newClass.getDescription());
            sqlStatement.setInt(4, newClass.getMaxStudents());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0) {
                ResultSet resultSet = sqlStatement.getGeneratedKeys();

                while (resultSet.next()) {
                    // "last_insert_rowid()" is the column name that contains the id of the last inserted row
                    // alternatively, we could have used resultSet.getInt(1); to get the id of the first column returned
                    int generatedIdForTheNewlyInsertedClass = resultSet.getInt("last_insert_rowid()");
                    System.out.println("SUCCESSFULLY inserted a new class with id = " + generatedIdForTheNewlyInsertedClass);

                    // this can be useful if we need to make additional processing on the newClass object
                    newClass.setId(generatedIdForTheNewlyInsertedClass);
                }

                resultSet.close();
            }
        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
            throw sqlException;
        }

        return newClass;
    }

    public void updateExistingClassInformation(Class classToUpdate) throws SQLException {
        String sql =
                "UPDATE classes\n" +
                        "SET code = ?, title = ?, description = ?, max_students = ?\n" +
                        "WHERE id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                ) {
            sqlStatement.setString(1, classToUpdate.getCode());
            sqlStatement.setString(2, classToUpdate.getTitle());
            sqlStatement.setString(3, classToUpdate.getDescription());
            sqlStatement.setInt(4, classToUpdate.getMaxStudents());
            sqlStatement.setInt(5, classToUpdate.getId());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0) {
                System.out.println("SUCCESSFULLY updated the class with id = " + classToUpdate.getId());
            } else {
                System.out.println("!!! WARNING: failed to update the class with id = " + classToUpdate.getId());
            }
        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to update the class with id = " + classToUpdate.getId());
            System.out.println(sqlException.getMessage());
            throw sqlException;
        }
    }

    public void deleteExistingClass(int idOfClassToDelete) throws SQLException {
        String sql =
                "DELETE FROM classes\n" +
                        "WHERE id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                ) {
            sqlStatement.setInt(1, idOfClassToDelete);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0) {
                System.out.println("SUCCESSFULLY deleted the class with id = " + idOfClassToDelete);
            } else {
                System.out.println("!!! WARNING: failed to delete the class with id = " + idOfClassToDelete);
            }
        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to delete the class with id = " + idOfClassToDelete);
            System.out.println(sqlException.getMessage());
            throw sqlException;
        }
    }

    public List<Student> listAllStudents() {
        String sql =
                "SELECT id, first_name, last_name, birth_date\n" +
                        "FROM students;";

        ArrayList<Student> listOfStudents = new ArrayList<>();
        try
                (
                        Connection connection = getDatabaseConnection();
                        Statement sqlStatement = connection.createStatement();
                        ResultSet resultSet = sqlStatement.executeQuery(sql);
                ) {
            printTableHeader(new String[]{"id", "first_name", "last_name", "birth_date"});

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                // the resultSet.getDate() does not work in this case, so we're using the getString() method instead
                String birthDate = resultSet.getString("birth_date");

                System.out.printf("| %d | %s | %s | %s |%n", id, firstName, lastName, birthDate);

                Student studentForCurrentRow = new Student(id, firstName, lastName, Date.valueOf(birthDate));
                listOfStudents.add(studentForCurrentRow);
            }
        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to query the students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }

        return listOfStudents;
    }

    public Student getStudentById(int id)
    {
        String firstName = null;
        String lastName = null;
        String birthDate = null;
        String sql =
                "Select *\n" +
                        "From students\n" +
                        "Where id = ?";
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement sqlStatement = connection.prepareStatement(sql);
        ) {
            sqlStatement.setInt(1, id);

            ResultSet resultSet = sqlStatement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("No student with id = " + id);
                return null;
            }
                printTableHeader(new String[]{"id", "first_name", "last_name", "birth_date"});
                id = resultSet.getInt("id");
                firstName = resultSet.getString("first_name");
                lastName = resultSet.getString("last_name");
                // the resultSet.getDate() does not work in this case, so we're using the getString() method instead
                birthDate = resultSet.getString("birth_date");
                System.out.printf("| %d | %s | %s | %s |%n", id, firstName, lastName, birthDate);
                return new Student(id, firstName, lastName, Date.valueOf(birthDate));
        }catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());

            return null;
        }
    }


    public ArrayList<RegisteredStudentJoinResult> listAllRegisteredStudents()
    {
        String sql =
                "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                "FROM students\n" +
                "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                "ORDER BY students.last_name, students.first_name, classes.code;";

        ArrayList<RegisteredStudentJoinResult> listOfRegisteredStudentJoinResults = new ArrayList<>();
        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int studentId = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", studentId, studentFullName, code, title);

                RegisteredStudentJoinResult registeredStudentJoinResultForCurrentRow = new RegisteredStudentJoinResult(studentId, studentFullName, code, title);
                listOfRegisteredStudentJoinResults.add(registeredStudentJoinResultForCurrentRow);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }

        return listOfRegisteredStudentJoinResults;
    }








    public Class getClassWithId(int id)
    {
        String sql =
                "SELECT id, code, title, description, max_students\n" +
                "FROM classes\n" +
                "WHERE id = ?;";
        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql);
        )
        {
            sqlStatement.setInt(1, id);

            ResultSet resultSet = sqlStatement.executeQuery();

            if (!resultSet.next())
            {
                System.out.println("No class with id = " + id);
                return null;
            }

            int idOfClass = resultSet.getInt("id");
            String code = resultSet.getString("code");
            String title = resultSet.getString("title");
            String description = resultSet.getString("description");
            int maxStudents = resultSet.getInt("max_students");

            return new Class(idOfClass, code, title, description, maxStudents);
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());

            return null;
        }
    }

    public void addNewStudent(Student newStudent)
    {
        // 💡 HINT: in a prepared statement
        // to set the date parameter in the format "YYYY-MM-DD", use the code:
        // sqlStatement.setString(columnIndexTBD, newStudent.getBirthDate().toString());
        //
        // to set the date parameter in the unix format (i.e., milliseconds since 1970), use this code:
        // sqlStatement.setDate(columnIndexTBD, newStudent.getBirthDate());

        // TODO: add your code here
        String sql =
                "INSERT INTO students (id, first_name,last_name,birth_date)\n" +
                        "VALUES (?, ?, ?, ?);";
        String sql1 =
                "SELECT max(students.id)\n" +
                        "FROM students;\n";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        Statement res = connection.createStatement();
                        ResultSet resultSet = res.executeQuery(sql1);
                )
        {
            int id = resultSet.getInt(1)+1;
            newStudent.setId(id);
            sqlStatement.setInt(1, newStudent.getId());
            sqlStatement.setString(2, newStudent.getFirstName());
            sqlStatement.setString(3, newStudent.getLastName());
            sqlStatement.setString(4, newStudent.getBirthDate().toString());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }

    private void printTableHeader(String[] listOfColumnNames)
    {
        System.out.print("| ");
        for (String columnName : listOfColumnNames)
        {
            System.out.print(columnName + " | ");
        }
        System.out.println();
        System.out.println(Utils.characterRepeat('-', 80));
    }
    public Student UpdateExistingStudentInformation(Student studentToUpdate){
        Date birthDate = studentToUpdate.getBirthDate();
        int studentID = studentToUpdate.getId();
        String first_name = studentToUpdate.getFirstName();
        String last_name = studentToUpdate.getLastName();
        String sql1 =
                "UPDATE students\n"+
                        "SET first_name=?, last_name=?, birth_date=?\n"+
                        "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement res = connection.prepareStatement(sql1);
            res.setString(1, first_name);
            res.setString(2, last_name);
            res.setDate(3, birthDate);
            res.setInt(4, studentID);
            res.executeQuery();
        }

        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to alter Students table");
            System.out.println(sqlException.getMessage());
        }
        String sql =
                "SELECT first_name, last_name\n"+
                        "FROM students\n"+
                        "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement res1 = connection.prepareStatement(sql);
            res1.setInt(1, studentID);
            ResultSet ResultSet1 = res1.executeQuery();
            while (ResultSet1.next()) {
                first_name = ResultSet1.getString("first_name");
                last_name = ResultSet1.getString("last_name");
            }
            return new Student(studentID, first_name, last_name, birthDate);
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to alter Students table");
            System.out.println(sqlException.getMessage());
            return null;
        }

    }
    public void deleteExistingStudent(int idOfStudentToDelete) throws SQLException {
        String sql =
                "DELETE FROM students\n" +
                        "WHERE id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                ) {
            sqlStatement.setInt(1, idOfStudentToDelete);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0) {
                System.out.println("SUCCESSFULLY deleted the student with id = " + idOfStudentToDelete);
            } else {
                System.out.println("!!! WARNING: failed to delete the student with id = " + idOfStudentToDelete);
            }
        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to delete the student with id = " + idOfStudentToDelete);
            System.out.println(sqlException.getMessage());
            throw sqlException;
        }
    }
    public void addStudentToClass(int idOfStudentToAdd, int idOfClassToAddTo){
        String sql = "INSERT INTO registered_students (class_id, student_id, signup_date)\n" +
                "VALUES (?,?,?)";
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE id = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, idOfStudentToAdd);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("No such Student ID, please try again here: \n");
                connection.close();
            }
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, idOfClassToAddTo);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                connection.close();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idOfClassToAddTo);
            preparedStatement.setInt(2, idOfStudentToAdd);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date timestamp = new java.sql.Date(utilDate.getTime());
            preparedStatement.setDate(3, timestamp);
            preparedStatement.execute();
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public void deleteStudentFromClass(int idOfStudentToDelete, int idOfClassToDeleteFrom){
        String sql = "DELETE\n" +
                "FROM registered_students\n"+
                "WHERE class_id = ? and student_id = ?";
        String sql1 = "SELECT id\n" +
                "FROM students\n" +
                "WHERE id = ?";
        String sql2 = "SELECT id\n" +
                "FROM classes\n" +
                "WHERE id = ?";
        try {
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setInt(1, idOfStudentToDelete);
            ResultSet res = preparedStatement1.executeQuery();
            if (!res.next()) {
                System.out.println("No such Student ID, please try again here: \n");
                connection.close();
            }
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setInt(1, idOfClassToDeleteFrom);
            ResultSet res2 = preparedStatement2.executeQuery();
            if (!res2.next()) {
                System.out.println("No such class ID, please try again here: \n");
                connection.close();
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idOfClassToDeleteFrom);
            preparedStatement.setInt(2, idOfStudentToDelete);
            preparedStatement.execute();
            connection.close();
        } catch (SQLException sqlException) {
            System.out.println(sqlException.getMessage());
        }
    }
    public ArrayList<RegisteredStudentJoinResult> showAllStudentsInClass(String classCode){
        String sql =
                "SELECT *\n" +
                        "FROM(\n"+
                        "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                        "FROM students\n" +
                        "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                        "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                        "ORDER BY student_id)\n" +
                        "WHERE code = ?;" ;
        ArrayList<RegisteredStudentJoinResult> listOfRegisteredStudentJoinResults = new ArrayList<>();
        try{
            Connection connection = getDatabaseConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, classCode);
            ResultSet resultSet = preparedStatement.executeQuery();
        if(!resultSet.next()) {
            System.out.println("Either no students are enrolled in this class, or this class code does not exist.\n");
            connection.close();
        }
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
                RegisteredStudentJoinResult registeredStudentJoinResultForCurrentRow = new RegisteredStudentJoinResult(id, studentFullName, code, title);
                listOfRegisteredStudentJoinResults.add(registeredStudentJoinResultForCurrentRow);
            }
            connection.close();
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
        return listOfRegisteredStudentJoinResults;
    }
    public ArrayList<RegisteredStudentJoinResult> showAllStudentsClasses(int studentId){
        String First = null;
        String Last = null;
        Boolean proceed = false;
        ResultSet resultSet = null;
        ArrayList<RegisteredStudentJoinResult> studentClasses = new ArrayList<RegisteredStudentJoinResult>();
        String sql =
                "SELECT ALL *\n" +
                        "FROM(\n"+
                        "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                        "FROM students\n" +
                        "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                        "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                        "ORDER BY class_id)\n" +
                        "WHERE student_full_name = ?;" ;
        String sql1 = "SELECT first_name, last_name\n" +
                "FROM students\n" +
                "WHERE students.id = ?";
        try{
            Connection connection = getDatabaseConnection();
            while(!proceed) {
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                preparedStatement1.setInt(1, studentId);
                ResultSet resultSet1 = preparedStatement1.executeQuery();
                if (!resultSet1.next()) {
                    System.out.println("Either this student is not enrolled in any classes or this student does not exist.");
                    connection.close();
                }
                First = resultSet1.getString(1);
                Last = resultSet1.getString(2);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, First + " " + Last);
                resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    System.out.println("Either this student is not enrolled in any classes or this student does not exist.");
                    connection.close();
                }
                proceed = true;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, First + " " + Last);
            resultSet = preparedStatement.executeQuery();
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
                RegisteredStudentJoinResult registeredStudentJoinResultForCurrentRow = new RegisteredStudentJoinResult(id, studentFullName, code, title);
                studentClasses.add(registeredStudentJoinResultForCurrentRow);
            }
            connection.close();
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
        return studentClasses;
    }
}
