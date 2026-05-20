package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Integer> {

    List<Questionnaire> findByStatusOrderByPublishTimeDesc(String status);

    @Query("SELECT q FROM Questionnaire q WHERE q.creatorId = :creatorId AND (:status = '' OR q.status = :status) ORDER BY q.createTime DESC")
    List<Questionnaire> findByCreatorIdAndStatus(@Param("creatorId") Integer creatorId, @Param("status") String status);
}
