package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/course/getCourseList")
    public DataResponse getCourseList(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getCourseList(dataRequest);
    }

    @PostMapping("/course/courseSave")
    public DataResponse courseSave(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.courseSave(dataRequest);
    }

    @PostMapping("/course/courseDelete")
    public DataResponse courseDelete(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.courseDelete(dataRequest);
    }

    @PostMapping("/course/teacher/openCourse")
    public DataResponse openCourse(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.openCourse(dataRequest);
    }

    @PostMapping("/course/updateCourseStatus")
    public DataResponse updateCourseStatus(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.updateCourseStatus(dataRequest);
    }

    @PostMapping("/student/selectCourse")
    public DataResponse selectCourse(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.applyForCourse(dataRequest);
    }

    @PostMapping("/student/submitSelections")
    public DataResponse submitSelections(@Valid @RequestBody DataRequest dataRequest) {
        // 这里应该处理批量选课提交
        return courseService.submitSelections(dataRequest);
    }

    @PostMapping("/student/availableCourses")
    public DataResponse getAvailableCourses(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getAvailableCourses(dataRequest);
    }

    @PostMapping("/student/mySelections")
    public DataResponse getMySelections(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getMySelections(dataRequest);
    }

    @PostMapping("/student/getMySelections")
    public DataResponse getMySelectionsLegacy(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getMySelections(dataRequest);
    }

    @PostMapping("/student/getAllSelections")
    public DataResponse getAllSelections(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getAllSelections(dataRequest);
    }

    @PostMapping("/student/updateSelectionMark")
    public DataResponse updateSelectionMark(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.updateSelectionMark(dataRequest);
    }

    @PostMapping("/student/getCourses")
    public DataResponse getCourses(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getAvailableCourses(dataRequest);
    }

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

    @PostMapping("/student/drop")
    public DataResponse dropCourse(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.dropCourse(dataRequest);
    }

    @PostMapping("/student/cancelCourse")
    public DataResponse cancelCourse(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.cancelCourse(dataRequest);
    }

    @PostMapping("/course/detail")
    public DataResponse getCourseDetail(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getCourseDetail(dataRequest);
    }
}