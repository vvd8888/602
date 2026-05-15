package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Integer> {

    /**
     * 根据学生ID查询选课记录
     */
    List<Score> findByStudentPersonId(Integer personId);

    /**
     * 根据学生ID和课程ID查询选课记录
     */
    @Query("from Score where (?1=0 or student.personId=?1) and (?2=0 or course.courseId=?2)")
    List<Score> findByStudentCourse(Integer personId, Integer courseId);

    /**
     * 根据学生ID和课程名称模糊查询
     */
    @Query("from Score where student.personId=?1 and (?2=0 or course.name like %?2%)")
    List<Score> findByStudentCourse(Integer personId, String courseName);

    /**
     * 获取学生统计信息
     */
    @Query("select s.student.personId, count(s.scoreId), sum(s.mark), sum(s.course.credit), sum(s.course.credit * s.mark) from Score s where s.student.personId in ?1 group by s.student.personId")
    List<?> getStudentStatisticsList(List<Integer> personId);

    /**
     * 根据选课状态查询（用于管理员审核）
     */
    List<Score> findBySelectionStatus(String selectionStatus);

    /**
     * 根据学生ID和课程ID精确查询（用于检查是否已选课）
     */
    @Query("SELECT s FROM Score s WHERE s.student.personId = :personId AND s.course.courseId = :courseId")
    Optional<Score> findByPersonIdAndCourseId(@Param("personId") Integer personId,
                                              @Param("courseId") Integer courseId);

    /**
     * 根据课程ID查询选课记录
     */
    @Query("SELECT s FROM Score s WHERE s.course.courseId = :courseId")
    List<Score> findByCourseId(@Param("courseId") Integer courseId);

    /**
     * 根据学生ID和选课状态查询
     */
    List<Score> findByStudentPersonIdAndSelectionStatus(Integer personId, String selectionStatus);

    /**
     * 根据课程ID和选课状态查询
     */
    List<Score> findByCourseCourseIdAndSelectionStatus(Integer courseId, String selectionStatus);

    /**
     * 根据学生ID和课程ID查询，并且选课状态为已批准
     */
    @Query("SELECT s FROM Score s WHERE s.student.personId = :personId AND s.course.courseId = :courseId AND s.selectionStatus = 'APPROVED'")
    Optional<Score> findApprovedByPersonIdAndCourseId(@Param("personId") Integer personId,
                                                      @Param("courseId") Integer courseId);
}