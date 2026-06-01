package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.CoursewareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/courseware")
public class CoursewareController {

    private final CoursewareService coursewareService;

    public CoursewareController(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    @PostMapping("/getCoursewareList")
    public DataResponse getCoursewareList(@RequestBody DataRequest dataRequest) {
        return coursewareService.getCoursewareList(dataRequest);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public DataResponse uploadCourseware(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "courseName", required = false) String courseName) {

        // Spring Boot 会自动处理UTF-8编码，不需要手动转换
        return coursewareService.uploadCourseware(file, title, description, courseId, courseName);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public DataResponse deleteCourseware(@RequestBody DataRequest dataRequest) {
        return coursewareService.deleteCourseware(dataRequest);
    }

    @GetMapping("/download/{coursewareId}")
    public ResponseEntity<byte[]> downloadCourseware(@PathVariable Integer coursewareId) {
        return coursewareService.downloadCourseware(coursewareId, false);
    }

    @GetMapping("/view/{coursewareId}")
    public ResponseEntity<byte[]> viewCourseware(@PathVariable Integer coursewareId) {
        return coursewareService.downloadCourseware(coursewareId, true);
    }
}
