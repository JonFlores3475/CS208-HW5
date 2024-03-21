package cs208;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;


@RestController
public class StudentsController {

    /**
     * GET /students
     *
     * @return a list of students (extracted from the students table in the database) as JSON
     */
    // TODO: implement this route
    @GetMapping(value = "/students", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Student> listAllStudents() {
        List<Student> listOfStudents = Main.database.listAllStudents();
        return listOfStudents;
    }


    /**
     * GET /students/{id}
     *
     * @return the student with id = {id} (extracted from the students table in the database) as JSON
     * @throws ResponseStatusException: a 404 status code if the student with id = {id} does not exist
     */
    // TODO: implement this route
    @GetMapping(value = "/students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Student getStudent(@PathVariable("id") int id) {
        System.out.println("id = " + id);
        Student student = Main.database.getStudentById(id);
        if (student == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "failed to retrieve student with id = " + id + " in the database because it does not exist"
            );
        }
        return student;
    }


    /**
     * POST /students
     * with the following form parameters:
     * firstName
     * lastName
     * birthDate (in ISO format: yyyy-mm-dd)
     * <p>
     * The parameters passed in the body of the POST request are used to create a new student.
     * The new student is inserted into the students table in the database.
     *
     * @return the created student (which was inserted into the database), as JSON
     */
    // TODO: implement this route
    @PostMapping("/students")
    Student addNewStudent(
            @RequestParam("first_name") String first_name,
            @RequestParam("last_name") String last_name,
            @RequestParam("birth_date") String birth_date
    ) {
        System.out.println("first_name      = " + first_name);
        System.out.println("last_name       = " + last_name);
        System.out.println("birth_date      = " + birth_date);

        Student createdStudent = new Student(first_name, last_name, Date.valueOf(birth_date.trim()));
        Main.database.addNewStudent(createdStudent);
        return createdStudent;
    }


    /**
     * PUT /students/{id}
     * with the following form parameters:
     * firstName
     * lastName
     * birthDate
     * <p>
     * The parameters passed in the body of the PUT request are used to
     * update the existing student with id = {id} in the students table in the database.
     *
     * @return the updated student as JSON
     * @throws ResponseStatusException: a 404 status code if the student with id = {id} does not exist
     */
    // TODO: implement this route
    @PutMapping(value = "/students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Student update(
            @PathVariable("id") int id,
            @RequestParam("first_name") String first_name,
            @RequestParam("last_name") String last_name,
            @RequestParam("birth_date") String birth_date
    ) {
        System.out.println("id   = " + id);
        System.out.println("first_name   = " + first_name);
        System.out.println("last_name   = " + last_name);
        System.out.println("birth_date   = " + birth_date);

        Student student = new Student(id, first_name, last_name, Date.valueOf(birth_date));
        Student student1 = Main.database.UpdateExistingStudentInformation(student);
        if (student1 == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "failed to update the student with id = " + id + " in the database because it does not exist"
            );
        }
        return student;
    }




    /**
     * PATCH /students/{id}
     * with the following optional form parameters:
     *      firstName
     *      lastName
     *      birthDate
     *
     * The optional parameters passed in the body of the PATCH request are used to
     * update the existing student with id = {id} in the students table in the database.
     *
     * @return the updated student as JSON
     *
     * @throws ResponseStatusException: a 404 status code if the student with id = {id} does not exist
     */
    // TODO: implement this route



    /**
     * DELETE /students/{id}
     *
     * Deletes the student with id = {id} from the students table in the database.
     *
     * @throws ResponseStatusException: a 404 status code if the student with id = {id} does not exist
     */
    // TODO: implement this route
    @DeleteMapping(value = "/students/{id}")
    String delete(@PathVariable("id") int id) {
        System.out.println("id = " + id);
        try {
            Student studentToDelete = Main.database.getStudentById(id);
            if (studentToDelete == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "failed to delete the student with id = " + id + " from the database because it does not exist"
                );
            }
            Main.database.deleteExistingStudent(id);
        } catch (SQLException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "failed to delete the student with id = " + id + " from the database"
            );
        }
                    return "Student with id = " + id +" successfully deleted.";
    }
}
