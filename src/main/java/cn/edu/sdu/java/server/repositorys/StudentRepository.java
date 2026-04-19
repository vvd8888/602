package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;  // 添加这行

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByPersonPersonId(Integer personId);
    Optional<Student> findByPersonNum(String num);
    List<Student> findByPersonName(String name);

    // 修改为使用命名参数
    @Query("SELECT s FROM Student s WHERE (:numName IS NULL OR :numName = '' OR s.person.num LIKE %:numName% OR s.person.name LIKE %:numName%)")
    List<Student> findStudentListByNumName(@Param("numName") String numName);

    // 修改分页查询
    @Query(value = "SELECT s FROM Student s WHERE (:numName IS NULL OR :numName = '' OR s.person.num LIKE %:numName% OR s.person.name LIKE %:numName%)",
            countQuery = "SELECT COUNT(s) FROM Student s WHERE (:numName IS NULL OR :numName = '' OR s.person.num LIKE %:numName% OR s.person.name LIKE %:numName%)")
    Page<Student> findStudentPageByNumName(@Param("numName") String numName, Pageable pageable);
}