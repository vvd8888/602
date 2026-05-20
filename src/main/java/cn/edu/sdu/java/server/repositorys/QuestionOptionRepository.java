package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Integer> {

    List<QuestionOption> findByQuestionIdOrderBySortOrder(Integer questionId);

    void deleteByQuestionId(Integer questionId);
}
