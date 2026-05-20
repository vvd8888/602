package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Response;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResponseRepository extends JpaRepository<Response, Integer> {

    Optional<Response> findByQuestionnaireIdAndPersonId(Integer questionnaireId, Integer personId);

    List<Response> findByQuestionnaireId(Integer questionnaireId);

    long countByQuestionnaireId(Integer questionnaireId);

    void deleteByQuestionnaireId(Integer questionnaireId);
}
