package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.StudentLeave;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.StudentLeaveRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.lang.management.PlatformLoggingMXBean;
import java.util.*;

@Service
public class StudentLeaveService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentLeaveRepository studentLeaveRepository;

    public StudentLeaveService(StudentRepository studentRepository, TeacherRepository teacherRepository, StudentLeaveRepository studentLeaveRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.studentLeaveRepository = studentLeaveRepository;
    }

    public OptionItemList getTeacherItemOptionList(DataRequest dataRequest) {
        List<Teacher> sList = teacherRepository.findAll();  //数据库查询操作
        List<OptionItem> itemList = new ArrayList<>();
        for (Teacher t : sList) {
            itemList.add(new OptionItem(t.getPersonId(), t.getPersonId() + "", t.getPerson().getNum() + "-" + t.getPerson().getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getStudentLeaveList(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        String userName = CommonMethod.getUsername();
        Integer state = dataRequest.getInteger("state");
        if(state == null)
            state = -1;
        String search = dataRequest.getString("search");
        assert roleName != null;
        List<StudentLeave> slList = switch (roleName) {
            case "ROLE_STUDENT" -> studentLeaveRepository.getStudentLeaveList(state, search, "", "");
            case "ROLE_TEACHER" -> studentLeaveRepository.getStudentLeaveList(state, search, "","");
            case "ROLE_ADMIN" -> studentLeaveRepository.getStudentLeaveList(state, search, "", "");
            default -> null;
        };
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> map;
        Student s;
        Teacher t;
        ComDataUtil di = ComDataUtil.getInstance();
        if (slList != null && !slList.isEmpty()) {
            for (StudentLeave sl : slList) {
                map = new HashMap<>();
                s = sl.getStudent();
                t = sl.getTeacher();
                map.put("studentLeaveId", sl.getStudentLeaveId());
                map.put("studentNum", s.getPerson().getNum());
                map.put("studentName", s.getPerson().getName());
                map.put("studentId", s.getPersonId());
                map.put("teacherName", t.getPerson().getNum() + t.getPerson().getName());
                map.put("state", sl.getState());
                map.put("stateName", di.getDictionaryLabelByValue("SHZTM", sl.getState()+""));
                map.put("reason", sl.getReason());
                map.put("leaveDate", sl.getLeaveDate());
                map.put("adminComment", sl.getAdminComment());
                map.put("teacherId", t.getPersonId());
                map.put("teacherComment", sl.getTeacherComment());
                map.put("teacherStatus", sl.getTeacherStatus() != null ? sl.getTeacherStatus() : 0);
                map.put("adminStatus", sl.getAdminStatus() != null ? sl.getAdminStatus() : 0);
                dataList.add(map);
            }
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse studentLeaveSave(DataRequest dataRequest) {
        Integer state = dataRequest.getInteger("state");
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        Integer teacherId = dataRequest.getInteger("teacherId");
        String leaveDate = dataRequest.getString("leaveDate");
        String reason = dataRequest.getString("reason");
        // 接收前端传的 学号+姓名
        String studentNum = dataRequest.getString("studentNum");
        String studentName = dataRequest.getString("studentName");
        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isPresent())
                sl = op.get();
        }
        if(sl == null) {
            sl = new StudentLeave();
            sl.setState(0);
            sl.setTeacherStatus(0);  // 教师未审核
            sl.setAdminStatus(0);    // 行政未审核
            sl.setApplyTime(new Date());
            sl.setTeacherComment("");
            sl.setAdminComment("");
            sl.setStudent(studentRepository.findByPersonNum(CommonMethod.getUsername()).get());
            Student student = studentRepository.findByPersonNum(studentNum).orElse(null);
            if(student != null){
                sl.setStudent(student);
            }
        }
        if(teacherId != null && teacherId > 0) {
            Optional<Teacher> op = teacherRepository.findById(teacherId);
            if(op.isPresent())
                sl.setTeacher(op.get());
        }
        sl.setLeaveDate(leaveDate);
        sl.setReason(reason);
        sl.setState(state);
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }
    public DataResponse studentLeaveCheck(DataRequest dataRequest) {
        String roleName = CommonMethod.getRoleName();
        Integer state = dataRequest.getInteger("state"); // 前端传来：1=通过, 2=不通过（或0=不通过）
        Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
        String teacherComment = dataRequest.getString("teacherComment");
        String adminComment = dataRequest.getString("adminComment");
        StudentLeave sl = null;
        if(studentLeaveId != null && studentLeaveId > 0) {
            Optional<StudentLeave> op = studentLeaveRepository.findById(studentLeaveId);
            if(op.isPresent())
                sl = op.get();
        }
        if(sl == null) {
            return CommonMethod.getReturnMessageOK();
        }
        
        // 记录当前审核人的意见、时间和状态
        if("ROLE_ADMIN".equals(roleName)) {
            sl.setAdminComment(adminComment);
            sl.setAdminTime(new Date());
            // 设置行政的审核状态：1表示通过，2表示不通过
            if (state != null && state == 1) {
                sl.setAdminStatus(1);  // 通过
            } else if (state != null && (state == 0 || state == 2)) {
                sl.setAdminStatus(2);  // 不通过
            }
        } else if("ROLE_TEACHER".equals(roleName)) {
            sl.setTeacherComment(teacherComment);
            sl.setTeacherTime(new Date());
            // 设置教师的审核状态：1表示通过，2表示不通过
            if (state != null && state == 1) {
                sl.setTeacherStatus(1);  // 通过
            } else if (state != null && (state == 0 || state == 2)) {
                sl.setTeacherStatus(2);  // 不通过
            }
        }
        
        // 新审核逻辑（仅限请假管理）：
        // - 有一个人审核通过便显示通过 (state=1)
        // - 此时另一个还可以更改
        // - 如果没人审核就是未审核状态 (state=0)
        // - 没人通过但有人不通过时显示不通过 (state=2)
        
        // 获取教师和行政的审核状态
        Integer teacherStatus = sl.getTeacherStatus() != null ? sl.getTeacherStatus() : 0;
        Integer adminStatus = sl.getAdminStatus() != null ? sl.getAdminStatus() : 0;
        
        boolean teacherApproved = (teacherStatus == 1);
        boolean adminApproved = (adminStatus == 1);
        boolean teacherRejected = (teacherStatus == 2);
        boolean adminRejected = (adminStatus == 2);
        
        // 计算最终状态
        int newStatus;
        if (teacherApproved || adminApproved) {
            // 有一个人审核通过便显示通过
            newStatus = 1;
        } else if (teacherRejected || adminRejected) {
            // 没人通过但有人不通过
            newStatus = 2;
        } else {
            // 没人审核就是未审核状态
            newStatus = 0;
        }
        
        sl.setState(newStatus);
        studentLeaveRepository.save(sl);
        return CommonMethod.getReturnMessageOK();
    }
}