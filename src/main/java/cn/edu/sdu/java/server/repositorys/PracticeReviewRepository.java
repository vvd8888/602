package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PracticeReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeReviewRepository extends JpaRepository<PracticeReview, Integer> {

    List<PracticeReview> findByProjectIdOrderByReviewTimeDesc(Integer projectId);

    List<PracticeReview> findByProjectIdAndReviewTypeOrderByReviewTimeDesc(Integer projectId, String reviewType);
}
