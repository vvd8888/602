package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByQuestionnaireIdOrderBySortOrder(Integer questionnaireId);

    void deleteByQuestionnaireId(Integer questionnaireId);
}
