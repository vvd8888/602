package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PracticeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PracticeProjectRepository extends JpaRepository<PracticeProject, Integer> {

    @Query("SELECT p FROM PracticeProject p WHERE p.leaderId = :leaderId AND (:projectType = '' OR p.projectType = :projectType) ORDER BY p.createTime DESC")
    List<PracticeProject> findByLeaderIdAndProjectType(@Param("leaderId") Integer leaderId, @Param("projectType") String projectType);

    @Query("SELECT p FROM PracticeProject p WHERE (:status = '' OR p.status = :status) AND (:projectType = '' OR p.projectType = :projectType) ORDER BY p.createTime DESC")
    List<PracticeProject> findByStatusAndProjectType(@Param("status") String status, @Param("projectType") String projectType);

    @Query("SELECT p FROM PracticeProject p WHERE (:status = '' OR p.status = :status) ORDER BY p.createTime DESC")
    List<PracticeProject> findByStatus(@Param("status") String status);

    List<PracticeProject> findByKeyTopicId(Integer keyTopicId);
}
