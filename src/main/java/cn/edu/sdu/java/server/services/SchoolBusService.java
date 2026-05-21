package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.SchoolBusScheduleRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.SchoolBusCampusResponse;
import cn.edu.sdu.java.server.payload.response.SchoolBusReservationResponse;
import cn.edu.sdu.java.server.payload.response.SchoolBusScheduleResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchoolBusService {
    private final SchoolBusScheduleRepository scheduleRepository;
    private final SchoolBusReservationRepository reservationRepository;
    private final SchoolBusCampusRepository campusRepository;
    private final PersonRepository personRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public SchoolBusService(SchoolBusScheduleRepository scheduleRepository,
                           SchoolBusReservationRepository reservationRepository,
                           SchoolBusCampusRepository campusRepository,
                           PersonRepository personRepository,
                           StudentRepository studentRepository,
                           TeacherRepository teacherRepository) {
        this.scheduleRepository = scheduleRepository;
        this.reservationRepository = reservationRepository;
        this.campusRepository = campusRepository;
        this.personRepository = personRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    /**
     * 获取所有校区列表
     */
    public DataResponse getCampusList() {
        List<SchoolBusCampus> campusList = campusRepository.findAllByOrderBySortOrder();
        List<SchoolBusCampusResponse> responseList = campusList.stream().map(campus -> {
            SchoolBusCampusResponse response = new SchoolBusCampusResponse();
            response.setId(campus.getId());
            response.setCampusCode(campus.getCampusCode());
            response.setCampusName(campus.getCampusName());
            response.setAddress(campus.getAddress());
            response.setSortOrder(campus.getSortOrder());
            response.setEnabled(campus.getEnabled());
            response.setRemark(campus.getRemark());
            return response;
        }).collect(Collectors.toList());
        
        return CommonMethod.getReturnData(responseList);
    }

    /**
     * 获取所有班次列表（管理员）
     */
    public DataResponse getAllSchedules() {
        List<SchoolBusSchedule> scheduleList = scheduleRepository.findAll();
        List<SchoolBusScheduleResponse> responseList = scheduleList.stream().map(this::convertToResponse).collect(Collectors.toList());
        return CommonMethod.getReturnData(responseList);
    }

    /**
     * 根据起点终点查询班次
     */
    public DataResponse getSchedulesByRoute(String departureCampus, String arrivalCampus) {
        List<SchoolBusSchedule> scheduleList = scheduleRepository.findByDepartureCampusAndArrivalCampus(departureCampus, arrivalCampus);
        List<SchoolBusScheduleResponse> responseList = scheduleList.stream().map(this::convertToResponse).collect(Collectors.toList());
        return CommonMethod.getReturnData(responseList);
    }

    /**
     * 获取正常运营的班次列表（学生/教师使用）
     */
    public DataResponse getActiveSchedules() {
        List<SchoolBusSchedule> scheduleList = scheduleRepository.findByStatusOrderByDepartureTime(0);
        List<SchoolBusScheduleResponse> responseList = scheduleList.stream().map(this::convertToResponse).collect(Collectors.toList());
        return CommonMethod.getReturnData(responseList);
    }

    /**
     * 保存班次（管理员）
     */
    public DataResponse saveSchedule(SchoolBusScheduleRequest request) {
        try {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            SchoolBusSchedule schedule;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());

            if (request.getId() != null) {
                // 编辑模式
                Optional<SchoolBusSchedule> optional = scheduleRepository.findById(request.getId());
                if (optional.isEmpty()) {
                    return CommonMethod.getReturnMessageError("班次不存在");
                }
                schedule = optional.get();
                schedule.setUpdateTime(currentTime);
                schedule.setUpdaterId(personId);
            } else {
                // 新增模式
                schedule = new SchoolBusSchedule();
                schedule.setCreateTime(currentTime);
                schedule.setCreatorId(personId);
                schedule.setReservedSeats(0);  // 初始已预约人数为0
                if (request.getStatus() == null) {
                    schedule.setStatus(0);  // 默认正常运营
                }
            }

            // 设置属性
            schedule.setDepartureCampus(request.getDepartureCampus());
            schedule.setArrivalCampus(request.getArrivalCampus());
            schedule.setDepartureTime(request.getDepartureTime());
            schedule.setArrivalTime(request.getArrivalTime());
            schedule.setTotalSeats(request.getTotalSeats());
            schedule.setRemark(request.getRemark());
            if (request.getStatus() != null) {
                schedule.setStatus(request.getStatus());
            }

            scheduleRepository.save(schedule);
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("保存班次失败", e);
            return CommonMethod.getReturnMessageError("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除班次（管理员）
     */
    public DataResponse deleteSchedule(Integer id) {
        try {
            Optional<SchoolBusSchedule> optional = scheduleRepository.findById(id);
            if (optional.isEmpty()) {
                return CommonMethod.getReturnMessageError("班次不存在");
            }
            
            // 检查是否有预约记录
            List<SchoolBusReservation> reservations = reservationRepository.findByScheduleId(id);
            if (!reservations.isEmpty()) {
                return CommonMethod.getReturnMessageError("该班次已有预约记录，无法删除");
            }
            
            scheduleRepository.deleteById(id);
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("删除班次失败", e);
            return CommonMethod.getReturnMessageError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 预约校车（学生/教师）
     */
    public DataResponse reserveSeat(Integer scheduleId, String remark) {
        try {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            // 检查班次是否存在
            Optional<SchoolBusSchedule> scheduleOptional = scheduleRepository.findById(scheduleId);
            if (scheduleOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("班次不存在");
            }

            SchoolBusSchedule schedule = scheduleOptional.get();
            
            // 检查班次状态
            // 0-正常运营 1-停运 2-取消
            if (schedule.getStatus() == null || schedule.getStatus() != 0) {
                return CommonMethod.getReturnMessageError("该班次已停运或取消");
            }

            // 检查是否已经预约
            int count = reservationRepository.countByScheduleIdAndPersonIdAndActiveStatus(scheduleId, personId);
            if (count > 0) {
                return CommonMethod.getReturnMessageError("您已经预约了该班次");
            }

            // 检查座位是否已满
            if (schedule.getReservedSeats() >= schedule.getTotalSeats()) {
                return CommonMethod.getReturnMessageError("该班次座位已满");
            }

            // 获取用户类型
            Optional<Person> personOptional = personRepository.findById(personId);
            if (personOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("用户信息不存在");
            }
            
            Person person = personOptional.get();
            Integer userType = "1".equals(person.getType()) ? 1 : 2;  // 1-学生 2-教师

            // 创建预约记录
            SchoolBusReservation reservation = new SchoolBusReservation();
            reservation.setScheduleId(scheduleId);
            reservation.setPersonId(personId);
            reservation.setUserType(userType);
            reservation.setStatus(1);  // 默认已确认
            reservation.setRemark(remark);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());
            reservation.setReservationTime(currentTime);
            reservation.setCreateTime(currentTime);
            reservation.setUpdateTime(currentTime);

            reservationRepository.save(reservation);

            // 更新班次的已预约人数
            schedule.setReservedSeats(schedule.getReservedSeats() + 1);
            scheduleRepository.save(schedule);

            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("预约失败", e);
            return CommonMethod.getReturnMessageError("预约失败：" + e.getMessage());
        }
    }

    /**
     * 取消预约
     */
    public DataResponse cancelReservation(Integer reservationId, String cancelReason) {
        try {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            Optional<SchoolBusReservation> optional = reservationRepository.findById(reservationId);
            if (optional.isEmpty()) {
                return CommonMethod.getReturnMessageError("预约记录不存在");
            }

            SchoolBusReservation reservation = optional.get();
            
            // 验证是否是本人的预约
            if (!reservation.getPersonId().equals(personId)) {
                return CommonMethod.getReturnMessageError("无权取消此预约");
            }

            // 检查预约状态
            if (reservation.getStatus() == 2) {
                return CommonMethod.getReturnMessageError("该预约已取消");
            }
            
            if (reservation.getStatus() == 3) {
                return CommonMethod.getReturnMessageError("该预约已完成，无法取消");
            }

            // 更新预约状态
            reservation.setStatus(2);  // 已取消
            reservation.setCancelReason(cancelReason);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());
            reservation.setCancelTime(currentTime);
            reservation.setUpdateTime(currentTime);
            
            reservationRepository.save(reservation);

            // 减少班次的已预约人数
            Optional<SchoolBusSchedule> scheduleOptional = scheduleRepository.findById(reservation.getScheduleId());
            if (scheduleOptional.isPresent()) {
                SchoolBusSchedule schedule = scheduleOptional.get();
                if (schedule.getReservedSeats() > 0) {
                    schedule.setReservedSeats(schedule.getReservedSeats() - 1);
                    scheduleRepository.save(schedule);
                }
            }

            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("取消预约失败", e);
            return CommonMethod.getReturnMessageError("取消失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的预约列表
     */
    public DataResponse getMyReservations() {
        try {
            Integer personId = CommonMethod.getPersonId();
            if (personId == null) {
                return CommonMethod.getReturnMessageError("用户未登录");
            }

            List<SchoolBusReservation> reservations = reservationRepository.findByPersonId(personId);
            List<SchoolBusReservationResponse> responseList = reservations.stream()
                .map(reservation -> convertToReservationResponse(reservation, personId))
                .collect(Collectors.toList());
            
            return CommonMethod.getReturnData(responseList);
        } catch (Exception e) {
            log.error("获取预约列表失败", e);
            return CommonMethod.getReturnMessageError("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取某个班次的预约列表（管理员）
     */
    public DataResponse getScheduleReservations(Integer scheduleId) {
        try {
            List<SchoolBusReservation> reservations = reservationRepository.findByScheduleId(scheduleId);
            List<SchoolBusReservationResponse> responseList = reservations.stream()
                .map(reservation -> convertToReservationResponse(reservation, null))
                .collect(Collectors.toList());
            
            return CommonMethod.getReturnData(responseList);
        } catch (Exception e) {
            log.error("获取预约列表失败", e);
            return CommonMethod.getReturnMessageError("获取失败：" + e.getMessage());
        }
    }

    /**
     * 将实体转换为响应对象
     */
    private SchoolBusScheduleResponse convertToResponse(SchoolBusSchedule schedule) {
        SchoolBusScheduleResponse response = new SchoolBusScheduleResponse();
        response.setId(schedule.getId());
        response.setDepartureCampus(schedule.getDepartureCampus());
        response.setArrivalCampus(schedule.getArrivalCampus());
        response.setDepartureTime(schedule.getDepartureTime());
        response.setArrivalTime(schedule.getArrivalTime());
        response.setTotalSeats(schedule.getTotalSeats());
        response.setReservedSeats(schedule.getReservedSeats());
        response.setStatus(schedule.getStatus());
        response.setRemark(schedule.getRemark());
        response.setCreateTime(schedule.getCreateTime());
        
        // 设置状态文本
        switch (schedule.getStatus()) {
            case 0:
                response.setStatusText("正常运营");
                break;
            case 1:
                response.setStatusText("停运");
                break;
            case 2:
                response.setStatusText("取消");
                break;
            default:
                response.setStatusText("未知");
        }
        
        return response;
    }

    /**
     * 将预约实体转换为响应对象
     */
    private SchoolBusReservationResponse convertToReservationResponse(SchoolBusReservation reservation, Integer currentPersonId) {
        SchoolBusReservationResponse response = new SchoolBusReservationResponse();
        response.setId(reservation.getId());
        response.setScheduleId(reservation.getScheduleId());
        response.setPersonId(reservation.getPersonId());
        response.setUserType(reservation.getUserType());
        response.setStatus(reservation.getStatus());
        response.setReservationTime(reservation.getReservationTime());
        response.setCancelTime(reservation.getCancelTime());
        response.setCancelReason(reservation.getCancelReason());
        response.setRemark(reservation.getRemark());
        response.setCreateTime(reservation.getCreateTime());
        
        // 设置用户类型文本
        response.setUserTypeText(reservation.getUserType() == 1 ? "学生" : "教师");
        
        // 设置状态文本
        switch (reservation.getStatus()) {
            case 0:
                response.setStatusText("待确认");
                break;
            case 1:
                response.setStatusText("已确认");
                break;
            case 2:
                response.setStatusText("已取消");
                break;
            case 3:
                response.setStatusText("已完成");
                break;
            default:
                response.setStatusText("未知");
        }
        
        // 获取人员信息
        Optional<Person> personOptional = personRepository.findById(reservation.getPersonId());
        if (personOptional.isPresent()) {
            Person person = personOptional.get();
            response.setPersonName(person.getName());
            response.setPersonNum(person.getNum());
        }
        
        // 获取班次信息
        Optional<SchoolBusSchedule> scheduleOptional = scheduleRepository.findById(reservation.getScheduleId());
        if (scheduleOptional.isPresent()) {
            SchoolBusSchedule schedule = scheduleOptional.get();
            response.setDepartureCampus(schedule.getDepartureCampus());
            response.setArrivalCampus(schedule.getArrivalCampus());
            response.setDepartureTime(schedule.getDepartureTime());
        }
        
        return response;
    }
}
