package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/course")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // 原有接口
    @PostMapping("/getCourseList")
    public DataResponse getCourseList(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getCourseList(dataRequest);
    }

    @PostMapping("/courseSave")
    public DataResponse courseSave(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.courseSave(dataRequest);
    }

    @PostMapping("/courseDelete")
    public DataResponse courseDelete(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.courseDelete(dataRequest);
    }

    @PostMapping("/teacher/openCourse")
    public DataResponse openCourse(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.openCourse(dataRequest);
    }

    // ✅ 新增：更新课程状态接口
    @PostMapping("/updateCourseStatus")
    public DataResponse updateCourseStatus(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.updateCourseStatus(dataRequest);
    }

    // 学生选课相关接口
    @PostMapping("/student/availableCourses")
    public DataResponse getAvailableCourses(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getAvailableCourses(dataRequest);
    }

    @PostMapping("/student/apply")
    public DataResponse applyForCourse(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.applyForCourse(dataRequest);
    }

    @PostMapping("/student/mySelections")
    public DataResponse getMySelections(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getMySelections(dataRequest);
    }

    // 管理员审核相关接口
    @PostMapping("/admin/pendingSelections")
    public DataResponse getPendingSelections(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getPendingSelections(dataRequest);
    }

    @PostMapping("/admin/approve")
    public DataResponse approveSelection(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.approveSelection(dataRequest);
    }

    @PostMapping("/admin/reject")
    public DataResponse rejectSelection(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.rejectSelection(dataRequest);
    }
}