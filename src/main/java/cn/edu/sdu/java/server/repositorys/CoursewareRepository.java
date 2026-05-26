package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Courseware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CoursewareRepository extends JpaRepository<Courseware, Integer> {
    List<Courseware> findByTeacherId(Integer teacherId);

    @Query("SELECT c FROM Courseware c ORDER BY c.coursewareId DESC")
    List<Courseware> findAllOrderByIdDesc();
}
