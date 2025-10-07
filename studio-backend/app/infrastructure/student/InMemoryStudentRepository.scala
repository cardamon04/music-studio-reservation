package infrastructure.student

import domain.student.{
  Student,
  StudentNumber,
  StudentName,
  StudentEmail,
  StudentRepository
}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.concurrent.TrieMap

/** InMemory学生リポジトリ実装
  *
  * DDDのインフラストラクチャ層として、学生のInMemory永続化を実装
  */
@Singleton
class InMemoryStudentRepository @Inject() ()(implicit ec: ExecutionContext)
    extends StudentRepository {

  // スレッドセーフなマップで学生を管理
  private val studentMap = TrieMap[StudentNumber, Student]()

  // 初期データを設定
  initializeDefaultStudents()

  override def findAllActive(): Future[List[Student]] = {
    Future {
      studentMap.values.filter(_.isActive).toList.sortBy(_.name.value)
    }
  }

  override def findAll(): Future[List[Student]] = {
    Future {
      studentMap.values.toList.sortBy(_.name.value)
    }
  }

  override def findByStudentNumber(
      studentNumber: StudentNumber
  ): Future[Option[Student]] = {
    Future {
      studentMap.get(studentNumber)
    }
  }

  override def searchByName(name: String): Future[List[Student]] = {
    Future {
      val searchTerm = name.toLowerCase.trim
      if (searchTerm.isEmpty) {
        List.empty
      } else {
        studentMap.values
          .filter(_.isActive)
          .filter(student =>
            student.name.value.toLowerCase.contains(searchTerm)
          )
          .toList
          .sortBy(_.name.value)
      }
    }
  }

  override def save(student: Student): Future[Student] = {
    Future {
      studentMap.put(student.studentNumber, student)
      student
    }
  }

  override def delete(studentNumber: StudentNumber): Future[Boolean] = {
    Future {
      studentMap.remove(studentNumber).isDefined
    }
  }

  /** デフォルトの学生データを初期化
    */
  private def initializeDefaultStudents(): Unit = {
    val now = java.time.LocalDateTime.now()

    val defaultStudents = List(
      Student(
        studentNumber = StudentNumber("AB12345678"),
        name = StudentName("田中太郎"),
        email = Some(StudentEmail("tanaka@example.com")),
        isActive = true,
        createdAt = now,
        updatedAt = now
      ),
      Student(
        studentNumber = StudentNumber("CD78901234"),
        name = StudentName("佐藤花子"),
        email = Some(StudentEmail("sato@example.com")),
        isActive = true,
        createdAt = now,
        updatedAt = now
      ),
      Student(
        studentNumber = StudentNumber("EF34567890"),
        name = StudentName("鈴木一郎"),
        email = Some(StudentEmail("suzuki@example.com")),
        isActive = true,
        createdAt = now,
        updatedAt = now
      ),
      Student(
        studentNumber = StudentNumber("GH90123456"),
        name = StudentName("高橋美咲"),
        email = Some(StudentEmail("takahashi@example.com")),
        isActive = true,
        createdAt = now,
        updatedAt = now
      ),
      Student(
        studentNumber = StudentNumber("IJ56789012"),
        name = StudentName("山田健太"),
        email = Some(StudentEmail("yamada@example.com")),
        isActive = true,
        createdAt = now,
        updatedAt = now
      )
    )

    defaultStudents.foreach { student =>
      studentMap.put(student.studentNumber, student)
    }
  }
}
