package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Score;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.ScoreRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScoreService {
    private final CourseRepository courseRepository;
    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public ScoreService(CourseRepository courseRepository,
                        ScoreRepository scoreRepository,
                        StudentRepository studentRepository,
                        UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return null;
    }

    private User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) return null;
        Optional<User> userOpt = userRepository.findByUserName(username);
        return userOpt.orElse(null);
    }

    private Integer getCurrentStudentPersonId() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return null;
        if (currentUser.getUserType() != null && "ROLE_STUDENT".equals(currentUser.getUserType().getName())) {
            Optional<Student> studentOpt = studentRepository.findById(currentUser.getPersonId());
            if (studentOpt.isPresent()) {
                return studentOpt.get().getPersonId();
            }
        }
        return null;
    }

    public OptionItemList getStudentItemOptionList(DataRequest dataRequest) {
        List<Student> sList = studentRepository.findStudentListByNumName("");
        List<OptionItem> itemList = new ArrayList<>();
        for (Student s : sList) {
            itemList.add(new OptionItem(s.getPersonId(), s.getPersonId() + "", s.getPerson().getNum() + "-" + s.getPerson().getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public OptionItemList getCourseItemOptionList(DataRequest dataRequest) {
        List<Course> sList = courseRepository.findAll();
        List<OptionItem> itemList = new ArrayList<>();
        for (Course c : sList) {
            itemList.add(new OptionItem(c.getCourseId(), c.getCourseId() + "", c.getNum() + "-" + c.getName()));
        }
        return new OptionItemList(0, itemList);
    }

    public DataResponse getScoreList(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");

        Integer currentStudentPersonId = getCurrentStudentPersonId();
        if (currentStudentPersonId != null) {
            personId = currentStudentPersonId;
        }

        if (personId == null) personId = 0;

        Integer courseId = dataRequest.getInteger("courseId");
        if (courseId == null) courseId = 0;

        List<Score> sList = scoreRepository.findByStudentCourse(personId, courseId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> m;
        for (Score s : sList) {
            m = new HashMap<>();
            m.put("scoreId", s.getScoreId() + "");
            m.put("personId", s.getStudent().getPersonId() + "");
            m.put("courseId", s.getCourse().getCourseId() + "");
            m.put("studentNum", s.getStudent().getPerson().getNum());
            m.put("studentName", s.getStudent().getPerson().getName());
            m.put("className", s.getStudent().getClassName());
            m.put("courseNum", s.getCourse().getNum());
            m.put("courseName", s.getCourse().getName());
            m.put("credit", "" + s.getCourse().getCredit());
            m.put("mark", "" + s.getMark());
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse scoreSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Integer courseId = dataRequest.getInteger("courseId");
        Integer mark = dataRequest.getInteger("mark");
        Integer scoreId = dataRequest.getInteger("scoreId");
        Optional<Score> op;
        Score s = null;
        if (scoreId != null) {
            op = scoreRepository.findById(scoreId);
            if (op.isPresent()) s = op.get();
        }
        if (s == null) {
            s = new Score();
            s.setStudent(studentRepository.findById(personId).get());
            s.setCourse(courseRepository.findById(courseId).get());
        }
        s.setMark(mark);
        scoreRepository.save(s);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse scoreDelete(DataRequest dataRequest) {
        Integer scoreId = dataRequest.getInteger("scoreId");
        Optional<Score> op;
        Score s = null;
        if (scoreId != null) {
            op = scoreRepository.findById(scoreId);
            if (op.isPresent()) {
                s = op.get();
                scoreRepository.delete(s);
            }
        }
        return CommonMethod.getReturnMessageOK();
    }
}
