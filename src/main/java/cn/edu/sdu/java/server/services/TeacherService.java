package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeacherService {
    private static final Logger log = LoggerFactory.getLogger(TeacherService.class);

    private final PersonRepository personRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder encoder;
    private final SystemService systemService;

    public TeacherService(PersonRepository personRepository,
                          TeacherRepository teacherRepository,
                          UserRepository userRepository,
                          UserTypeRepository userTypeRepository,
                          PasswordEncoder encoder,
                          SystemService systemService) {
        this.personRepository = personRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.encoder = encoder;
        this.systemService = systemService;
    }

    /**
     * 将 Teacher 对象转换为 Map（用于前端显示）
     */
    public Map<String, Object> getMapFromTeacher(Teacher t) {
        Map<String, Object> m = new HashMap<>();
        Person p;
        if (t == null)
            return m;
        m.put("title", t.getTitle());
        m.put("degree", t.getDegree());
        p = t.getPerson();
        if (p == null)
            return m;
        m.put("personId", t.getPersonId());
        m.put("num", p.getNum());
        m.put("name", p.getName());
        m.put("dept", p.getDept());
        m.put("card", p.getCard());
        m.put("gender", p.getGender());
        m.put("birthday", p.getBirthday());
        m.put("email", p.getEmail());
        m.put("phone", p.getPhone());
        m.put("address", p.getAddress());
        return m;
    }

    /**
     * 获取教师列表（支持按工号/姓名查询）
     */
    public List<Map<String, Object>> getTeacherMapList(String numName) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Teacher> tList = teacherRepository.findTeacherListByNumName(numName);
        if (tList == null || tList.isEmpty())
            return dataList;
        for (Teacher teacher : tList) {
            dataList.add(getMapFromTeacher(teacher));
        }
        return dataList;
    }

    public DataResponse getTeacherList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        List<Map<String, Object>> dataList = getTeacherMapList(numName);
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse getTeacherInfo(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Teacher t = null;
        Optional<Teacher> op;
        if (personId != null) {
            op = teacherRepository.findById(personId);
            if (op.isPresent()) {
                t = op.get();
            }
        }
        return CommonMethod.getReturnData(getMapFromTeacher(t));
    }

    public DataResponse teacherDelete(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Teacher t = null;
        Optional<Teacher> op;
        if (personId != null && personId > 0) {
            op = teacherRepository.findById(personId);
            if (op.isPresent()) {
                t = op.get();
                Optional<User> uOp = userRepository.findById(personId);
                uOp.ifPresent(userRepository::delete);
                Person p = t.getPerson();
                teacherRepository.delete(t);
                personRepository.delete(p);
            }
        }
        return CommonMethod.getReturnMessageOK();
    }

    /**
     * 保存教师信息（参考 StudentService.studentEditSave）
     */
    public DataResponse teacherEditSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Map<String, Object> form = dataRequest.getMap("form");
        String num = CommonMethod.getString(form, "num");

        Teacher t = null;
        Person p;
        User u;
        Optional<Teacher> op;
        boolean isNew = false;

        // 1. 查询是否存在该教师
        if (personId != null) {
            op = teacherRepository.findById(personId);
            if (op.isPresent()) {
                t = op.get();
            }
        }

        // 2. 检查工号是否已被其他人员使用
        Optional<Person> nOp = personRepository.findByNum(num);
        if (nOp.isPresent()) {
            if (t == null || !t.getPerson().getNum().equals(num)) {
                return CommonMethod.getReturnMessageError("新工号已经存在，不能添加或修改！");
            }
        }

        // 3. 新增模式
        if (t == null) {
            // 创建 Person
            p = new Person();
            p.setNum(num);
            p.setType("2");  // 教师类型（根据你的项目设置）
            personRepository.saveAndFlush(p);
            personId = p.getPersonId();

            // 创建 User 账号
            String password = encoder.encode("123456");
            u = new User();
            u.setPersonId(personId);
            u.setUserName(num);
            u.setPassword(password);
            u.setUserType(userTypeRepository.findByName("ROLE_TEACHER"));
            u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
            u.setCreatorId(CommonMethod.getPersonId());
            userRepository.saveAndFlush(u);

            // 创建 Teacher
            t = new Teacher();
            t.setPersonId(personId);
            teacherRepository.saveAndFlush(t);
            isNew = true;
        } else {
            p = t.getPerson();
        }

        personId = p.getPersonId();

        // 4. 如果工号变化，更新 Person 的 num 和 User 的 userName
        if (!num.equals(p.getNum())) {
            Optional<User> uOp = userRepository.findByPersonPersonId(personId);
            if (uOp.isPresent()) {
                u = uOp.get();
                u.setUserName(num);
                userRepository.saveAndFlush(u);
            }
            p.setNum(num);
        }

        // 5. 更新 Person 信息
        p.setName(CommonMethod.getString(form, "name"));
        p.setDept(CommonMethod.getString(form, "dept"));
        p.setCard(CommonMethod.getString(form, "card"));
        p.setGender(CommonMethod.getString(form, "gender"));
        p.setBirthday(CommonMethod.getString(form, "birthday"));
        p.setEmail(CommonMethod.getString(form, "email"));
        p.setPhone(CommonMethod.getString(form, "phone"));
        p.setAddress(CommonMethod.getString(form, "address"));
        personRepository.save(p);

        // 6. 更新 Teacher 信息
        t.setTitle(CommonMethod.getString(form, "title"));
        t.setDegree(CommonMethod.getString(form, "degree"));
        teacherRepository.save(t);

        // 7. 记录修改日志（如果有 SystemService）
        // systemService.modifyLog(t, isNew);

        return CommonMethod.getReturnData(t.getPersonId());
    }
}