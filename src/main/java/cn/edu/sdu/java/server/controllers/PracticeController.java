package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.PracticeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/practice")
public class PracticeController {
    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    // ===== 学生端 =====

    @PostMapping("/getMyProjectList")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getMyProjectList(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.getMyProjectList(dataRequest);
    }

    @PostMapping("/getAvailableKeyTopics")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getAvailableKeyTopics(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.getAvailableKeyTopics(dataRequest);
    }

    @PostMapping("/getProjectDetail")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getProjectDetail(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.getProjectDetail(dataRequest);
    }

    @PostMapping("/saveProject")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse saveProject(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.saveProject(dataRequest);
    }

    @PostMapping("/deleteProject")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse deleteProject(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.deleteProject(dataRequest);
    }

    @PostMapping("/submitSummary")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse submitSummary(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.submitSummary(dataRequest);
    }

    // ===== 教师/管理员端 =====

    @PostMapping("/getReviewProjectList")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getReviewProjectList(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.getReviewProjectList(dataRequest);
    }

    @PostMapping("/reviewProject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse reviewProject(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.reviewProject(dataRequest);
    }

    @PostMapping("/reviewSummary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse reviewSummary(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.reviewSummary(dataRequest);
    }

    @PostMapping("/updateProjectStatus")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse updateProjectStatus(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.updateProjectStatus(dataRequest);
    }

    @PostMapping("/getKeyTopicList")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getKeyTopicList(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.getKeyTopicList(dataRequest);
    }

    @PostMapping("/saveKeyTopic")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse saveKeyTopic(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.saveKeyTopic(dataRequest);
    }

    @PostMapping("/withdrawKeyTopic")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse withdrawKeyTopic(@Valid @RequestBody DataRequest dataRequest) {
        return practiceService.withdrawKeyTopic(dataRequest);
    }
}
