package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
//xiugai
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;//xiugai

public interface TeacherRepository extends JpaRepository<Teacher,Integer> {

        // 根据工号或姓名模糊查询教师
        @Query("SELECT t FROM Teacher t WHERE t.person.num LIKE %:numName% OR t.person.name LIKE %:numName%")
        List<Teacher> findByPersonNumContainingOrPersonNameContaining(@Param("numName") String numName);
    }


