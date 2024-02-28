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

import java.sql.SQLException;
import java.util.List;

/**
 * All GET routes (paths) should be accessible from the browser at
 *    http://localhost:8080/<ROUTE_NAME>
 *
 * For example, the route
 *   GET /issues/new
 * will be available at
 *   http://localhost:8080/issues/new
 */
@RestController
public class ClassesController
{

    /**
     * http://localhost:8080/classes
     * GET /classes
     *
     * @return a list of classes (extracted from the classes table in the database) as JSON
     */
    @GetMapping(value = "/classes", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Class> index()
    {
        List<Class> listOfClasses = Main.database.listAllClasses();

        // because we used the following annotation:
        //      produces = MediaType.APPLICATION_JSON_VALUE
        // in the method declaration, when we return the listOfClasses object,
        // the internal libraries of the Spring framework will automatically
        // convert the listOfClasses object to a JSON array of class objects
        // and return it to the client
        return listOfClasses;
    }

    @GetMapping(value = "/students",produces = MediaType.APPLICATION_JSON_VALUE)
    List<Student> listAllStudents(){
        List<Student> listOfStudents = Main.database.listAllStudents();
        return listOfStudents;
    }

    @GetMapping(value = "/students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Student getStudent(@PathVariable("id") int id) {
        System.out.println("id = " + id);
        return Main.database.getStudentById(id);
    }

    /**
     * GET /classes/{id}
     *
     * @return the class with id = {id} (extracted from the classes table in the database) as JSON
     *
     * @throws ResponseStatusException: a 404 status code if the class with id = {id} does not exist
     */
    @GetMapping(value = "/classes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Class show(@PathVariable("id") int id)
    {
        System.out.println("id = " + id);

        Class classWithID = Main.database.getClassWithId(id);
        if (classWithID == null)
        {
            System.out.println("No class with id " + id + " exists.");

            // return 404 status code (i.e., error that the class was not found)
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "class with id " + id + " not found"
            );
        }

        return classWithID;
    }


    /**
     * POST /classes
     * with the following form parameters:
     *      code
     *      title
     *      description
     *      maxStudents
     *
     * The parameters passed in the body of the POST request are used to create a new class.
     * The new class is inserted into the classes table in the database.
     *
     * @return the created class (which was inserted into the database), as JSON
     */
    @PostMapping("/classes")
    Class create(
            @RequestParam("code") String code,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("maxStudents") int maxStudents
    )
    {
        System.out.println("code        = " + code);
        System.out.println("title       = " + title);
        System.out.println("description = " + description);
        System.out.println("maxStudents = " + maxStudents);

        // we can perform additional validation on the parameters, for example:
        if (code.length() > 10)
        {
            System.out.println("Detected a code length greater than 10 characters. Throwing an error...");
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "class code should be less than 10 characters"
            );
        }

        try
        {
            Class createdClass = new Class(code, title, description, maxStudents);
            Main.database.addNewClass(createdClass);
            return createdClass;
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "failed to add new class to the database"
            );
        }
    }


    /**
     * PUT /classes/{id}
     * with the following form parameters:
     *      code
     *      title
     *      description
     *      maxStudents
     *
     * The parameters passed in the body of the PUT request are used to
     * update the existing class with id = {id} in the classes table in the database.
     *
     * @return the updated class as JSON
     *
     * @throws ResponseStatusException: a 404 status code if the class with id = {id} does not exist
     */
    @PutMapping(value = "/classes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Class update(
            @PathVariable("id") int id,
            @RequestParam("code") String code,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("maxStudents") Integer maxStudents
    )
    {
        System.out.println("id          = " + id);
        System.out.println("code        = " + code);
        System.out.println("title       = " + title);
        System.out.println("description = " + description);
        System.out.println("maxStudents = " + maxStudents);

        try
        {
            Class classToUpdate = Main.database.getClassWithId(id);
            if (classToUpdate == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "failed to update the class with id = " + id + " in the database because it does not exist"
                );
            }

            // override the values of all the fields from classToUpdate with the values from the parameters
            classToUpdate.setCode(code);
            classToUpdate.setTitle(title);
            classToUpdate.setDescription(description);
            classToUpdate.setMaxStudents(maxStudents);

            Main.database.updateExistingClassInformation(classToUpdate);
            return classToUpdate;
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "failed to update the class with id = " + id + " in the database"
            );
        }
    }


    /**
     * PATCH /classes/{id}
     * with the following optional form parameters:
     *      code
     *      title
     *      description
     *      maxStudents
     *
     * The optional parameters passed in the body of the PATCH request are used to
     * update the existing class with id = {id} in the classes table in the database.
     *
     * @return the updated class as JSON
     *
     * @throws ResponseStatusException: a 404 status code if the class with id = {id} does not exist
     */
    @PatchMapping(value = "/classes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Class updatePatch(
            @PathVariable("id") int id,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "maxStudents", required = false) Integer maxStudents
    )
    {
        System.out.println("id          = " + id);
        System.out.println("code        = " + code);
        System.out.println("title       = " + title);
        System.out.println("description = " + description);
        System.out.println("maxStudents = " + maxStudents);

        try
        {
            Class classToUpdate = Main.database.getClassWithId(id);
            if (classToUpdate == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "failed to update the class with id = " + id + " in the database because it does not exist"
                );
            }

            if (code != null)
            {
                classToUpdate.setCode(code);
            }

            if (title != null)
            {
                classToUpdate.setTitle(title);
            }

            if (description != null)
            {
                classToUpdate.setDescription(description);
            }

            if (maxStudents != null)
            {
                classToUpdate.setMaxStudents(maxStudents);
            }

            Main.database.updateExistingClassInformation(classToUpdate);
            return classToUpdate;
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "failed to update the class with id = " + id + " in the database"
            );
        }
    }


    /**
     * DELETE /classes/{id}
     *
     * Deletes the class with id = {id} from the classes table in the database.
     *
     * @throws ResponseStatusException: a 404 status code if the class with id = {id} does not exist
     */
    @DeleteMapping(value = "/classes/{id}")
    void delete(@PathVariable("id") int id)
    {
        System.out.println("id = " + id);

        try
        {
            Class classToDelete = Main.database.getClassWithId(id);
            if (classToDelete == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "failed to delete the class with id = " + id + " from the database because it does not exist"
                );
            }

            Main.database.deleteExistingClass(id);
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "failed to delete the class with id = " + id + " from the database"
            );
        }
    }



// =====================================================================================================================
// Alternative examples
// ---------------------------------------------------------------------------------------------------------------------
// NOTES:
//   The routes below are alternatives to some of the routes define above.
//
//   However, these routes below are for demo purposes only, and
//   should not be used in production they are not considered best practices.
// =====================================================================================================================


    /**
     * http://localhost:8080/get_array_of_classes_as_json_using_our_custom_code_for_creating_json
     * GET /get_array_of_classes_as_json_using_our_custom_code_for_creating_json
     *
     * NOTE:
     *  - this is not the preferred way to return JSON, this is just for demonstration purposes
     *  - see method {@link #index()} for the preferred way, which uses the
     *    internal libraries of the Spring framework to automatically convert the returned object
     *    to JSON format and return it to the client
     *
     * @return an array of classes as JSON
     */
    @GetMapping(value = "/get_array_of_classes_as_json_using_our_custom_code_for_creating_json", produces = MediaType.APPLICATION_JSON_VALUE)
    String getArrayOfClassesAsJSONUsingOurCustomCodeForCreatingJSON()
    {
        List<Class> listOfClasses = Main.database.listAllClasses();
        if (listOfClasses.isEmpty())
        {
            // returns an empty array in JSON format
            return "[]";
        }

        String listOfClassesAsJSON = "";

        // we need to add a comma after each element, except for the last element
        for (int i = 0; i < listOfClasses.size() - 1; i++)
        {
            Class currentClass = listOfClasses.get(i);
            listOfClassesAsJSON += currentClass.toJSON();
            listOfClassesAsJSON += ", ";
        }

        // we do not need to add a comma after the last element, otherwise the JSON will be invalid
        Class lastClass = listOfClasses.get(listOfClasses.size() - 1);
        listOfClassesAsJSON += lastClass.toJSON();

        // wrap the list of classes in a JSON object
        listOfClassesAsJSON = "[" + listOfClassesAsJSON + "]";

        return listOfClassesAsJSON;
    }


    /**
     * http://localhost:8080/create_class_using_query_parameters_in_url?code=CS+101&title=Intro+to+CS+Principles&description=Intro+to+the+impact+of+CS+...&maxStudents=40
     * GET /create_class_using_query_parameters_in_url
     *
     * The query parameters passed in the URL create a new class.
     * The new class is inserted into the classes table in the database.
     *
     * NOTE:
     *  - This is not the preferred way to create a new class.
     *  - The values of the parameters should not be passed as query parameters in the URL.
     *  - Instead, they should be passed in the body of a POST request
     *  - see method {@link #create(String, String, String, int)} for the preferred way, which uses a POST request
     *
     * @return the created class (which was inserted into the database), as JSON
     */
    @GetMapping("/create_class_using_query_parameters_in_url")
    Class getCreateClassWithParametersInURLThisIsNotThePreferredWay(
            @RequestParam("code") String code,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("maxStudents") int maxStudents
    )
    {
        System.out.println("code        = " + code);
        System.out.println("title       = " + title);
        System.out.println("description = " + description);
        System.out.println("maxStudents = " + maxStudents);

        try
        {
            Class createdClass = new Class(code, title, description, maxStudents);
            Main.database.addNewClass(createdClass);
            return createdClass;
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY, // 422 error code
                    "failed to add new class to the database"
            );
        }

    }

}
