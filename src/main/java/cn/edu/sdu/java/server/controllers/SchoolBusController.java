package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.request.SchoolBusReservationRequest;
import cn.edu.sdu.java.server.payload.request.SchoolBusScheduleRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.SchoolBusService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 校车管理控制器
 * 提供校车的班次管理和预约功能
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/schoolbus")
@Slf4j
public class SchoolBusController {
    
    private final SchoolBusService schoolBusService;

    public SchoolBusController(SchoolBusService schoolBusService) {
        this.schoolBusService = schoolBusService;
    }

    /**
     * 获取校区列表（所有用户）
     */
    @PostMapping("/getCampusList")
    public DataResponse getCampusList(@Valid @RequestBody DataRequest dataRequest) {
        return schoolBusService.getCampusList();
    }

    /**
     * 获取所有班次列表（管理员）
     */
    @PostMapping("/getAllSchedules")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getAllSchedules(@Valid @RequestBody DataRequest dataRequest) {
        return schoolBusService.getAllSchedules();
    }

    /**
     * 根据起点终点查询班次（所有用户）
     */
    @PostMapping("/getSchedulesByRoute")
    public DataResponse getSchedulesByRoute(@Valid @RequestBody DataRequest dataRequest) {
        String departureCampus = dataRequest.getString("departureCampus");
        String arrivalCampus = dataRequest.getString("arrivalCampus");
        return schoolBusService.getSchedulesByRoute(departureCampus, arrivalCampus);
    }

    /**
     * 获取正常运营的班次列表（学生/教师）
     */
    @PostMapping("/getActiveSchedules")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public DataResponse getActiveSchedules(@Valid @RequestBody DataRequest dataRequest) {
        return schoolBusService.getActiveSchedules();
    }

    /**
     * 保存班次（管理员）
     */
    @PostMapping("/saveSchedule")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse saveSchedule(@Valid @RequestBody SchoolBusScheduleRequest request) {
        return schoolBusService.saveSchedule(request);
    }

    /**
     * 删除班次（管理员）
     */
    @PostMapping("/deleteSchedule")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse deleteSchedule(@Valid @RequestBody DataRequest dataRequest) {
        Integer id = dataRequest.getInteger("id");
        return schoolBusService.deleteSchedule(id);
    }

    /**
     * 预约校车（学生/教师）
     */
    @PostMapping("/reserveSeat")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public DataResponse reserveSeat(@Valid @RequestBody DataRequest dataRequest) {
        Integer scheduleId = dataRequest.getInteger("scheduleId");
        String remark = dataRequest.getString("remark");
        return schoolBusService.reserveSeat(scheduleId, remark);
    }

    /**
     * 取消预约（学生/教师）
     */
    @PostMapping("/cancelReservation")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public DataResponse cancelReservation(@Valid @RequestBody DataRequest dataRequest) {
        Integer reservationId = dataRequest.getInteger("reservationId");
        String cancelReason = dataRequest.getString("cancelReason");
        return schoolBusService.cancelReservation(reservationId, cancelReason);
    }

    /**
     * 获取我的预约列表（学生/教师）
     */
    @PostMapping("/getMyReservations")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER')")
    public DataResponse getMyReservations(@Valid @RequestBody DataRequest dataRequest) {
        return schoolBusService.getMyReservations();
    }

    /**
     * 获取某个班次的预约列表（管理员）
     */
    @PostMapping("/getScheduleReservations")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getScheduleReservations(@Valid @RequestBody DataRequest dataRequest) {
        Integer scheduleId = dataRequest.getInteger("scheduleId");
        return schoolBusService.getScheduleReservations(scheduleId);
    }
}
