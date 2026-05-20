package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.QuestionnaireService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/questionnaire")
public class QuestionnaireController {
    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @PostMapping("/getQuestionnaireList")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getQuestionnaireList(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.getQuestionnaireList(dataRequest);
    }

    @PostMapping("/getQuestionnaireDetail")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getQuestionnaireDetail(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.getQuestionnaireDetail(dataRequest);
    }

    @PostMapping("/submitResponse")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse submitResponse(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.submitResponse(dataRequest);
    }

    @PostMapping("/getMyResponse")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getMyResponse(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.getMyResponse(dataRequest);
    }

    @PostMapping("/deleteMyResponse")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse deleteMyResponse(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.deleteMyResponse(dataRequest);
    }

    @PostMapping("/getMyQuestionnaireList")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getMyQuestionnaireList(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.getMyQuestionnaireList(dataRequest);
    }

    @PostMapping("/saveQuestionnaire")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse saveQuestionnaire(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.saveQuestionnaire(dataRequest);
    }

    @PostMapping("/withdrawQuestionnaire")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse withdrawQuestionnaire(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.withdrawQuestionnaire(dataRequest);
    }

    @PostMapping("/getStatistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getStatistics(@Valid @RequestBody DataRequest dataRequest) {
        return questionnaireService.getStatistics(dataRequest);
    }
}
