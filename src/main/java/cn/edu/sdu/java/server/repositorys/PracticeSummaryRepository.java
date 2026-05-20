package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PracticeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeSummaryRepository extends JpaRepository<PracticeSummary, Integer> {

    List<PracticeSummary> findByProjectId(Integer projectId);

    List<PracticeSummary> findByProjectIdAndPersonId(Integer projectId, Integer personId);

    PracticeSummary findByProjectIdAndSummaryType(Integer projectId, String summaryType);

    void deleteByProjectId(Integer projectId);
}
