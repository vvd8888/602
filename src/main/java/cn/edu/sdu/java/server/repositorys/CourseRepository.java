package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    // ==== 查询方法 ====

    /**
     * 根据课程编号或名称模糊查询
     */
    @Query(value = "from Course where ?1='' or num like %?1% or name like %?1%")
    List<Course> findCourseListByNumName(String numName);

    /**
     * 根据课程编号查询
     */
    Optional<Course> findByNum(String num);

    /**
     * 根据课程名称查询
     */
    List<Course> findByName(String name);

    /**
     * 根据课程状态查询
     */
    List<Course> findByStatus(String status);

    /**
     * 根据教师姓名查询
     */
    List<Course> findByTeacher(String teacher);

    /**
     * 根据课程状态和名称查询
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.name LIKE %:name%")
    List<Course> findByStatusAndName(@Param("status") String status,
                                     @Param("name") String name);

    /**
     * 根据课程编号、名称、教师、状态组合查询
     */
    @Query("SELECT c FROM Course c WHERE " +
            "(:num = '' OR c.num LIKE %:num%) AND " +
            "(:name = '' OR c.name LIKE %:name%) AND " +
            "(:teacher = '' OR c.teacher LIKE %:teacher%) AND " +
            "(:status = '' OR c.status = :status)")
    List<Course> findByConditions(@Param("num") String num,
                                  @Param("name") String name,
                                  @Param("teacher") String teacher,
                                  @Param("status") String status);

    // ==== 开放课程查询 ====

    /**
     * 获取所有开放选课的课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'OPEN'")
    List<Course> findOpenCourses();

    /**
     * 根据关键词查询开放课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'OPEN' AND (c.num LIKE %:keyword% OR c.name LIKE %:keyword%)")
    List<Course> findOpenCoursesByKeyword(@Param("keyword") String keyword);

    /**
     * 根据学分范围查询开放课程
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'OPEN' AND c.credit BETWEEN :minCredit AND :maxCredit")
    List<Course> findOpenCoursesByCreditRange(@Param("minCredit") Integer minCredit,
                                              @Param("maxCredit") Integer maxCredit);

    // ==== 教师相关查询 ====

    /**
     * 根据教师姓名查询开放课程
     */
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher AND c.status = 'OPEN'")
    List<Course> findOpenCoursesByTeacher(@Param("teacher") String teacher);

    /**
     * 查询教师开设的所有课程
     */
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher")
    List<Course> findAllCoursesByTeacher(@Param("teacher") String teacher);

    /**
     * 根据教师和状态查询课程
     */
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher AND c.status = :status")
    List<Course> findByTeacherAndStatus(@Param("teacher") String teacher,
                                        @Param("status") String status);

    // ==== 前置课程相关查询 ====

    /**
     * 查询没有前置课程的课程
     */
    @Query("SELECT c FROM Course c WHERE c.preCourse IS NULL")
    List<Course> findCoursesWithoutPreCourse();

    /**
     * 查询有前置课程的课程
     */
    @Query("SELECT c FROM Course c WHERE c.preCourse IS NOT NULL")
    List<Course> findCoursesWithPreCourse();

    /**
     * 根据前置课程ID查询课程
     */
    @Query("SELECT c FROM Course c WHERE c.preCourse.courseId = :preCourseId")
    List<Course> findByPreCourseId(@Param("preCourseId") Integer preCourseId);

    /**
     * 查询可以作为前置课程的课程
     */
    @Query("SELECT c FROM Course c WHERE c.courseId NOT IN (SELECT c2.courseId FROM Course c2 WHERE c2.preCourse IS NOT NULL)")
    List<Course> findAvailablePreCourses();

    // ==== 学分相关查询 ====

    /**
     * 根据学分查询课程
     */
    List<Course> findByCredit(Integer credit);

    /**
     * 查询指定学分范围的课程
     */
    @Query("SELECT c FROM Course c WHERE c.credit BETWEEN :minCredit AND :maxCredit")
    List<Course> findByCreditRange(@Param("minCredit") Integer minCredit,
                                   @Param("maxCredit") Integer maxCredit);

    /**
     * 根据课程状态和学分查询
     */
    @Query("SELECT c FROM Course c WHERE c.status = :status AND c.credit = :credit")
    List<Course> findByStatusAndCredit(@Param("status") String status,
                                       @Param("credit") Integer credit);

    // ==== 统计查询 ====

    /**
     * 统计各状态课程数量
     */
    @Query("SELECT c.status, COUNT(c) FROM Course c GROUP BY c.status")
    List<Object[]> countCoursesByStatus();

    /**
     * 统计教师开设的课程数量
     */
    @Query("SELECT c.teacher, COUNT(c) FROM Course c GROUP BY c.teacher")
    List<Object[]> countCoursesByTeacher();

    /**
     * 统计各学分的课程数量
     */
    @Query("SELECT c.credit, COUNT(c) FROM Course c GROUP BY c.credit")
    List<Object[]> countCoursesByCredit();

    // ==== 排序查询 ====

    /**
     * 按课程编号排序查询
     */
    List<Course> findAllByOrderByNumAsc();

    /**
     * 按课程名称排序查询
     */
    List<Course> findAllByOrderByNameAsc();

    /**
     * 按学分升序排序查询
     */
    List<Course> findAllByOrderByCreditAsc();

    /**
     * 按学分降序排序查询
     */
    List<Course> findAllByOrderByCreditDesc();

    /**
     * 按创建时间倒序查询
     */
    @Query("SELECT c FROM Course c ORDER BY c.courseId DESC")
    List<Course> findAllOrderByIdDesc();

    // ==== 分页和限制查询 ====

    /**
     * 查询最新的课程
     */
    @Query("SELECT c FROM Course c ORDER BY c.courseId DESC")
    List<Course> findLatestCourses();

    /**
     * 查询最新的N门课程
     */
    @Query(value = "SELECT * FROM course ORDER BY course_id DESC LIMIT :limit", nativeQuery = true)
    List<Course> findLatestCourses(@Param("limit") int limit);

    /**
     * 查询学分最高的课程
     */
    @Query("SELECT c FROM Course c ORDER BY c.credit DESC")
    List<Course> findCoursesByHighestCredit();

    // ==== 复杂查询 ====

    /**
     * 查询可以选择的课程（排除已选课程）
     */
    @Query("SELECT c FROM Course c WHERE c.status = 'OPEN' AND c.courseId NOT IN " +
            "(SELECT s.course.courseId FROM Score s WHERE s.student.personId = :studentId)")
    List<Course> findAvailableCoursesForStudent(@Param("studentId") Integer studentId);

    /**
     * 查询学生已选的课程
     */
    @Query("SELECT c FROM Course c WHERE c.courseId IN " +
            "(SELECT s.course.courseId FROM Score s WHERE s.student.personId = :studentId)")
    List<Course> findSelectedCoursesByStudent(@Param("studentId") Integer studentId);

    /**
     * 查询特定学期的课程
     */
    @Query("SELECT c FROM Course c WHERE c.time LIKE %:semester%")
    List<Course> findCoursesBySemester(@Param("semester") String semester);

    // ==== 存在性检查 ====

    /**
     * 检查课程编号是否存在
     */
    boolean existsByNum(String num);

    /**
     * 检查课程名称是否存在
     */
    boolean existsByName(String name);
}