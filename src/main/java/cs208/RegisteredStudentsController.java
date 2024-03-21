package cs208;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;


@RestController
public class RegisteredStudentsController {

    /**
     * GET /registered_students
     *
     * @return a list of registered students (extracted from a join between
     * registered_students, students and classes tables in the database) as JSON
     */
    @GetMapping(value = "/registered_students", produces = MediaType.APPLICATION_JSON_VALUE)
    ArrayList<RegisteredStudentJoinResult> registered_students() {
        ArrayList<RegisteredStudentJoinResult> listOfRegisteredStudentJoinResults = Main.database.listAllRegisteredStudents();

        return listOfRegisteredStudentJoinResults;
    }


    /**
     * POST /add_student_to_class
     * with the following form parameters:
     * studentId
     * classId
     * <p>
     * The parameters passed in the body of the POST request will be inserted
     * into the registered_students table in the database.
     */
    // TODO: implement this route
    @PostMapping(value = "/registered_students/add", produces = MediaType.APPLICATION_JSON_VALUE)
    ArrayList<RegisteredStudentJoinResult> registered_students(
            @RequestParam("idOfStudentToAdd") int idOfStudentToAdd,
            @RequestParam("idOfClassToAddTo") int idOfClassToAddTo
    ) {
        System.out.println("idOfStudentToADD = " + idOfStudentToAdd);
        System.out.println("idOfClassToAddTo = " + idOfClassToAddTo);
        Main.database.addStudentToClass(idOfStudentToAdd, idOfClassToAddTo);
        return Main.database.listAllRegisteredStudents();
    }


    /**
     * DELETE /drop_student_from_class
     * with the following form parameters:
     * studentId
     * classId
     * <p>
     * Deletes the student with id = {studentId} from the class with id = {classId}
     * from the registered_students in the database.
     *
     * @throws ResponseStatusException: a 404 status code if the student with id = {studentId} does not exist
     * @throws ResponseStatusException: a 404 status code if the class with id = {classId} does not exist
     */
    // TODO: implement this route
    @DeleteMapping(value = "/registered_students/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    ArrayList<RegisteredStudentJoinResult> registered_students_new(
            @RequestParam("idOfStudentToDelete") int idOfStudentToDelete,
            @RequestParam("idOfClassToDeleteFrom") int idOfClassToDeleteFrom
    ) {
        System.out.println("idOfStudentToADD = " + idOfStudentToDelete);
        System.out.println("idOfClassToAddTo = " + idOfClassToDeleteFrom);
        Student deletedStudent = Main.database.getStudentById(idOfStudentToDelete);
        Class classStudentDeletedFrom = Main.database.getClassWithId(idOfClassToDeleteFrom);
        if(deletedStudent  == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unable to find student with student id = " + idOfStudentToDelete + " because it does not exist."
            );
        }
        if(classStudentDeletedFrom == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unable to find class with class id = " + idOfClassToDeleteFrom + " because it does not exist."
            );
        }
        Main.database.deleteStudentFromClass(idOfStudentToDelete, idOfClassToDeleteFrom);
        return Main.database.listAllRegisteredStudents();
    }



    /**
     * GET /students_taking_class/{classCode}
     *
     * @return a list of registered students (extracted from a join between
     * registered_students, students and classes tables in the database) as JSON
     * that are taking the class {classCode}
     */
    // TODO: implement this route



    /**
     * GET /classes_in_which_student_is_enrolled/{studentId}
     *
     * @return a list of all classes (extracted from a join between
     * registered_students, students and classes tables in the database) as JSON
     * in which the student with id = {studentId} is enrolled
     *
     * @throws ResponseStatusException: a 404 status code if the student with id = {studentId} does not exist
     */
    // TODO: implement this route

}
