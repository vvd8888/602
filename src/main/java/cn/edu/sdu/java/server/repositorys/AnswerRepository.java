package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

    List<Answer> findByResponseId(Integer responseId);

    void deleteByResponseId(Integer responseId);
}
