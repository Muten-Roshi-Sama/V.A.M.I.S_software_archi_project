package be.ecam.server.services

//DAO
import be.ecam.server.models.Person
import be.ecam.server.models.Student
import be.ecam.server.models.StudentTable
import be.ecam.server.models.PersonTable
import be.ecam.server.models.EvaluationTable

//DTO
import be.ecam.common.api.StudentDTO
import be.ecam.common.api.Evaluation
import be.ecam.common.api.StudentBulletin

//SeedManager
import be.ecam.server.db.SeedResult
import be.ecam.server.db.seedFromResourceIfMissing

// util
import be.ecam.server.util.requireValidEmail
import be.ecam.server.util.requireValidPassword
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

//Exposed
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.dao.id.EntityID
import java.io.File

// ----------------------------

//DTOs used only to read JSON at startup (seed)
@Serializable
data class StudentCreateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    //
    val studentId: String? = null,
    val studyYear: String? = null,
    val optionCode: String? = null
)

@Serializable
data class StudentUpdateDTO(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    //
    val studentId: String? = null,
    val studyYear: String? = null,
    val optionCode: String? = null
)

@Serializable
data class StudentSeedDTO(
    val studentEmail: String,
    val firstName: String,
    val lastName: String,
    val matricule: String,
    val year: String,
    val option: String?,
    val evaluations: List<EvaluationSeedDTO> = emptyList()
)

@Serializable
data class EvaluationSeedDTO(
    val activityName: String,
    val session: String,
    val score: Int,
    val maxScore: Int
)

@Serializable
data class CourseGradeRow(
    val studentEmail: String,
    val firstName: String?,
    val lastName: String?,
    val matricule: String?,
    val activityName: String,
    val session: String,
    val score: Int,
    val maxScore: Int
)

class StudentService(private val personService: PersonService = PersonService()) {

    private val ops = RoleService<StudentDTO, Student>(
        personService = personService,
        findAll = { Student.all() },
        findById = { id -> Student.findById(id) },
        deleteById = { id -> Student.findById(id)?.delete() != null },
        createForPerson = { person -> Student.createForPerson(person) },
        toDto = { student -> student.toDto() },
        dtoToPersonCreate = { dto ->
            PersonCreateDTO(
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                password = dto.password ?: throw IllegalArgumentException("password required for creation"),
                createdAt = dto.createdAt
            )
        },
        dtoEmail = { dto -> dto.email }
    )
    
    fun getAll(): List<StudentDTO> = ops.getAll()
    fun getById(studentId: Int): StudentDTO? = ops.getById(studentId)
    fun delete(studentId: Int): Boolean = ops.delete(studentId)
    fun count(): Long = ops.count()

    fun create(createDto: StudentCreateDTO): StudentDTO = transaction {
        val emailField = createDto.email
        val passwordField = createDto.password

        requireValidEmail(emailField)
        requireValidPassword(passwordField, minLength = 6)

        if (personService.existsByEmail(emailField)) {
            throw IllegalArgumentException("Email '$emailField' is already registered")
        }

        try {
            val person = personService.create(
                PersonCreateDTO(
                    firstName = createDto.firstName,
                    lastName = createDto.lastName,
                    email = emailField,
                    password = passwordField
                )
            )

            val student = Student.createForPerson(person)
            student.studentId = createDto.studentId
            student.studyYear = createDto.studyYear
            student.optionCode = createDto.optionCode

            student.toDto()
        } catch (ex: ExposedSQLException) {
            val msg = ex.message ?: ""
            if (msg.contains("UNIQUE", true) || msg.contains("constraint", true)) {
                throw IllegalArgumentException("Email '$emailField' is already registered (unique constraint).")
            }
            throw ex
        }
    }

    fun getGradesByCourse(activityName: String): List<CourseGradeRow> = transaction {
        val join = EvaluationTable.innerJoin(StudentTable).innerJoin(PersonTable)
        join
            .selectAll()
            .where { EvaluationTable.activityName eq activityName }
            .map { row ->
                CourseGradeRow(
                    studentEmail = row[PersonTable.email],
                    firstName = row[PersonTable.firstName],
                    lastName = row[PersonTable.lastName],
                    matricule = row[StudentTable.studentId],
                    activityName = row[EvaluationTable.activityName],
                    session = row[EvaluationTable.session],
                    score = row[EvaluationTable.score],
                    maxScore = row[EvaluationTable.maxScore]
                )
            }
            .sortedWith(compareBy<CourseGradeRow>({ it.matricule ?: "" }, { it.session }))
    }

    fun updateEvaluationByMatricule(matricule: String, activityName: String, session: String, newScore: Int) {
        transaction {
            val student = Student.find { StudentTable.studentId eq matricule }.firstOrNull()
                ?: throw IllegalArgumentException("Student not found")
            val updated = EvaluationTable.update({
                (EvaluationTable.student eq student.id.value) and
                        (EvaluationTable.activityName eq activityName) and
                        (EvaluationTable.session eq session)
            }) { r ->
                r[score] = newScore
            }
            if (updated == 0) throw IllegalArgumentException("Evaluation not found")
        }
    }

    fun update(studentId: Int, dto: StudentUpdateDTO): StudentDTO = transaction {
        val student = Student.findById(studentId) ?: throw IllegalArgumentException("not found")
        val person = student.person

        dto.email?.let { newEmail ->
            if (newEmail != person.email) {
                requireValidEmail(newEmail)
                val existing = personService.findByEmail(newEmail)
                if (existing != null && existing.id.value != person.id.value) {
                    throw IllegalArgumentException("Email '$newEmail' is already registered")
                }
                person.email = newEmail
            }
        }

        dto.firstName?.let { person.firstName = it }
        dto.lastName?.let { person.lastName = it }
        dto.password?.let { pw ->
            requireValidPassword(pw, minLength = 6)
            person.password = pw
        }

        dto.studentId?.let { student.studentId = it }
        dto.studyYear?.let { student.studyYear = it }
        dto.optionCode?.let { student.optionCode = it }

        student.toDto()
    }

    fun existsByEmail(email: String): Boolean = transaction {
        val person = personService.findByEmail(email) ?: return@transaction false
        Student.find { StudentTable.person eq person.id }.firstOrNull() != null
    }

    fun getByPersonId(personId: Int): StudentDTO? = transaction {
        Student.find { StudentTable.person eq EntityID(personId, PersonTable) }
            .firstOrNull()
            ?.toDto()
    }

    fun createStudentFromDto(dto: StudentDTO) {
        val createDto = StudentCreateDTO(
            firstName = dto.firstName,
            lastName = dto.lastName,
            email = dto.email,
            password = dto.password ?: "",
            studentId = dto.studentId,
            studyYear = dto.studyYear,
            optionCode = dto.optionCode
        )
        create(createDto)
    }

    fun seedFromResource(resourcePath: String = "data/students.json"): SeedResult {
        return seedFromResourceIfMissing<StudentDTO>(
            name = "students",
            resourcePath = resourcePath,
            exists = { dto: StudentDTO -> existsByEmail(dto.email) },
            create = { dto: StudentDTO -> createStudentFromDto(dto) },
            legacyMapper = { map ->
                fun getString(vararg keys: String): String? {
                    for (k in keys) {
                        val v = map[k] ?: map[k.lowercase()]
                        if (v is String) return v
                        if (v != null) return v.toString()
                    }
                    return null
                }

                val firstName = getString("firstName") ?: ""
                val lastName = getString("lastName") ?: ""
                val email = getString("email") ?: ""
                val password = getString("password")
                val createdAt = getString("createdAt") ?: ""
                val studentId = getString("studentId", "student_id")
                val studyYear = getString("studyYear", "study_year")
                val optionCode = getString("optionCode", "option_code")

                StudentDTO(
                    id = null,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    createdAt = createdAt,
                    studentId = studentId,
                    studyYear = studyYear,
                    optionCode = optionCode
                )
            }
        )
    }

    fun getAllStudentsWithBulletins(): List<StudentBulletin> {
        return transaction {
            Student.all().map { student ->
                //retrieval of assessments for each student
                val evaluations = EvaluationTable
                    .selectAll()
                    .where { EvaluationTable.student eq student.id.value }
                    .map { evalRow ->
                        Evaluation(  //using the DTO from /shared/
                            activityName = evalRow[EvaluationTable.activityName],
                            session = evalRow[EvaluationTable.session],
                            score = evalRow[EvaluationTable.score],
                            maxScore = evalRow[EvaluationTable.maxScore]
                        )
                    }

                //Construction of the StudentBulletin object
                StudentBulletin(  //using the DTO from /shared/
                    studentEmail = student.email,
                    firstName = student.firstName ?: "",
                    lastName = student.lastName ?: "",
                    matricule = student.studentId ?: "",
                    year = student.studyYear ?: "",
                    option = student.optionCode ?: "",
                    evaluations = evaluations
                )
            }
        }
    }

    //Getting one student by his email
    fun getStudentByEmail(emailParam: String): StudentBulletin? {
        return transaction {
            val person = Person.find { PersonTable.email eq emailParam }.firstOrNull() ?: return@transaction null
            val student = Student.find { StudentTable.person eq person.id }.firstOrNull() ?: return@transaction null

            val evaluations = EvaluationTable
                .selectAll()
                .where { EvaluationTable.student eq student.id.value }
                .map { evalRow ->
                    Evaluation(
                        activityName = evalRow[EvaluationTable.activityName],
                        session = evalRow[EvaluationTable.session],
                        score = evalRow[EvaluationTable.score],
                        maxScore = evalRow[EvaluationTable.maxScore]
                    )
                }

            StudentBulletin(
                studentEmail = student.email,
                firstName = student.firstName ?: "",
                lastName = student.lastName ?: "",
                matricule = student.studentId ?: "",
                year = student.studyYear ?: "",
                option = student.optionCode ?: "",
                evaluations = evaluations
            )
        }
    }

    //Getting one student by his matricule
    fun getStudentByMatricule(matriculeParam: String): StudentBulletin? {
        return transaction {
            val student = Student.find { StudentTable.studentId eq matriculeParam }.firstOrNull() 
                ?: return@transaction null

            val evaluations = EvaluationTable
                .selectAll()
                .where { EvaluationTable.student eq student.id.value }
                .map { evalRow ->
                    Evaluation(
                        activityName = evalRow[EvaluationTable.activityName],
                        session = evalRow[EvaluationTable.session],
                        score = evalRow[EvaluationTable.score],
                        maxScore = evalRow[EvaluationTable.maxScore]
                    )
                }

            StudentBulletin(
                studentEmail = student.email,
                firstName = student.firstName ?: "",
                lastName = student.lastName ?: "",
                matricule = student.studentId ?: "",
                year = student.studyYear ?: "",
                option = student.optionCode ?: "",
                evaluations = evaluations
            )
        }
    }

    //// ========== CREATE (change in the database) ==========

    //Create a new student with their assessments
    fun createStudent(student: StudentSeedDTO) {
        transaction {
            //Check if the student already exists
            val existingPerson = Person.find { PersonTable.email eq student.studentEmail }.firstOrNull()
            if (existingPerson != null) {
                throw IllegalArgumentException("Student with this email already exists")
            }
            
            val existingStudent = Student.find { StudentTable.studentId eq student.matricule }.firstOrNull()
            if (existingStudent != null) {
                throw IllegalArgumentException("Student with this matricule already exists")
            }

            val person = Person.new {
                firstName = student.firstName
                lastName = student.lastName
                email = student.studentEmail
                password = ""
                createdAt = java.time.LocalDateTime.now().toString()
            }

            val newStudent = Student.new {
                this.person = person
                studentId = student.matricule
                studyYear = student.year
                optionCode = student.option
            }

            //Insert into evaluations (for each eval)
            student.evaluations.forEach { eval ->
                EvaluationTable.insert { row ->
                    row[this.student] = newStudent.id.value
                    row[activityName] = eval.activityName
                    row[session] = eval.session
                    row[score] = eval.score
                    row[maxScore] = eval.maxScore
                }
            }
        }
    }

    //Add one assessment to an existing student
    fun addEvaluation(studentEmail: String, eval: EvaluationSeedDTO) {
        transaction {
            val person = Person.find { PersonTable.email eq studentEmail }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")
            val student = Student.find { StudentTable.person eq person.id }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")

            EvaluationTable.insert { row ->
                row[this.student] = student.id.value
                row[activityName] = eval.activityName
                row[session] = eval.session
                row[score] = eval.score
                row[maxScore] = eval.maxScore
            }
        }
    }

    // ========== UPDATE change in the database ==========

    //Updates a student's information
    fun updateStudent(emailParam: String, newData: StudentSeedDTO) {
        transaction {
            val person = Person.find { PersonTable.email eq emailParam }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")
            val student = Student.find { StudentTable.person eq person.id }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")

            person.firstName = newData.firstName
            person.lastName = newData.lastName
            
            student.studyYear = newData.year
            student.optionCode = newData.option
        }
    }

    //Updates the score of an existing assessment
    fun updateEvaluation(studentEmail: String, activityName: String, session: String, newScore: Int) {
        transaction {
            val person = Person.find { PersonTable.email eq studentEmail }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")
            val student = Student.find { StudentTable.person eq person.id }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")

            val updated = EvaluationTable.update({
                (EvaluationTable.student eq student.id.value) and
                        (EvaluationTable.activityName eq activityName) and
                        (EvaluationTable.session eq session)
            }) { row ->
                row[score] = newScore
            }

            if (updated == 0) {
                throw IllegalArgumentException("Evaluation not found")
            }
        }
    }

    // ========== DELETE (in the DB) ==========

    //Deletes a student AND all of their assessments
    fun deleteStudent(emailParam: String) {
        transaction {
            val person = Person.find { PersonTable.email eq emailParam }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")
            val student = Student.find { StudentTable.person eq person.id }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")

            EvaluationTable.deleteWhere { EvaluationTable.student eq student.id.value }
            
            student.delete()
        }
    }

    //Delete one specific assessment
    fun deleteEvaluation(studentEmail: String, activityName: String, session: String) {
        transaction {
            val person = Person.find { PersonTable.email eq studentEmail }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")
            val student = Student.find { StudentTable.person eq person.id }.firstOrNull() 
                ?: throw IllegalArgumentException("Student not found")

            val deleted = EvaluationTable.deleteWhere {
                (EvaluationTable.student eq student.id.value) and
                        (EvaluationTable.activityName eq activityName) and
                        (EvaluationTable.session eq session)
            }

            if (deleted == 0) {
                throw IllegalArgumentException("Evaluation not found")
            }
        }
    }
}
