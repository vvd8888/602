package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.NoticeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping("/getNoticeList")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getNoticeList(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.getNoticeList(dataRequest);
    }

    @PostMapping("/getNoticeDetail")
    @PreAuthorize("hasRole('STUDENT')")
    public DataResponse getNoticeDetail(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.getNoticeDetail(dataRequest);
    }

    @PostMapping("/getReadStatus")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getReadStatus(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.getReadStatus(dataRequest);
    }

    @PostMapping("/saveNotice")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse saveNotice(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.saveNotice(dataRequest);
    }

    @PostMapping("/withdrawNotice")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse withdrawNotice(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.withdrawNotice(dataRequest);
    }

    @PostMapping("/getMyNoticeList")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getMyNoticeList(@Valid @RequestBody DataRequest dataRequest) {
        return noticeService.getMyNoticeList(dataRequest);
    }
}
