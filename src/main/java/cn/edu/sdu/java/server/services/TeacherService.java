package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder encoder;  //密码服务自动注入


    public TeacherService(TeacherRepository teacherRepository,
                          PersonRepository personRepository,
                          UserRepository userRepository,
                          UserTypeRepository userTypeRepository,
                          PasswordEncoder encoder) {
        this.teacherRepository = teacherRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.encoder = encoder;
    }

    public DataResponse getTeacherList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        List<Teacher> list;
        if (numName != null && !numName.isEmpty()) {
            list = teacherRepository.findByPersonNumContainingOrPersonNameContaining(numName);
        } else {
            list = teacherRepository.findAll();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Teacher teacher : list) {
            Person person = teacher.getPerson();
            Map<String, Object> map = new HashMap<>();
            map.put("personId", teacher.getPersonId());
            map.put("num", person.getNum());
            map.put("name", person.getName());
            map.put("dept", person.getDept());
            map.put("title", teacher.getTitle());
            map.put("degree", teacher.getDegree());
            map.put("gender", person.getGender());
            map.put("card", person.getCard());
            map.put("phone", person.getPhone());
            map.put("email", person.getEmail());
            map.put("address", person.getAddress());
            result.add(map);
        }
        DataResponse response = new DataResponse();
        response.setCode(0);
        response.setData(result);
        return response;
    }

    public DataResponse getTeacherInfo(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Optional<Teacher> optional = teacherRepository.findById(personId);
        if (optional.isEmpty()) {
            DataResponse response = new DataResponse();
            response.setCode(1);
            response.setMsg("教师不存在");
            return response;
        }
        Teacher teacher = optional.get();
        Person person = teacher.getPerson();
        Map<String, Object> map = new HashMap<>();
        map.put("personId", teacher.getPersonId());
        map.put("num", person.getNum());
        map.put("name", person.getName());
        map.put("dept", person.getDept());
        map.put("title", teacher.getTitle());
        map.put("degree", teacher.getDegree());
        map.put("gender", person.getGender());
        map.put("card", person.getCard());
        map.put("phone", person.getPhone());
        map.put("email", person.getEmail());
        map.put("address", person.getAddress());
        DataResponse response = new DataResponse();
        response.setCode(0);
        response.setData(map);
        return response;
    }

    public DataResponse teacherDelete(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Optional<Teacher> optional = teacherRepository.findById(personId);
        if (optional.isEmpty()) {
            DataResponse response = new DataResponse();
            response.setCode(1);
            response.setMsg("教师不存在");
            return response;
        }
        Teacher teacher = optional.get();
        Person person = teacher.getPerson();

        teacherRepository.delete(teacher);
        Optional<User> userOpt = userRepository.findById(personId);
        if (userOpt.isPresent()) {
            userRepository.delete(userOpt.get());
        }
        personRepository.delete(person);

        DataResponse response = new DataResponse();
        response.setCode(0);
        return response;
    }

    @Transactional
    public DataResponse teacherEditSave(DataRequest dataRequest) {
        try {
            System.out.println("=== teacherEditSave 开始 ===");

            Integer personId = dataRequest.getInteger("personId");
            System.out.println("personId = " + personId);

            Map<String, Object> form = (Map<String, Object>) dataRequest.get("form");
            System.out.println("form = " + form);

            if (form == null) {
                DataResponse response = new DataResponse();
                response.setCode(1);
                response.setMsg("表单数据不能为空");
                return response;
            }

            Person person;
            Teacher teacher;

            if (personId != null && personId > 0) {
                System.out.println("编辑模式");
                Optional<Teacher> optional = teacherRepository.findById(personId);
                if (optional.isEmpty()) {
                    DataResponse response = new DataResponse();
                    response.setCode(1);
                    response.setMsg("教师不存在");
                    return response;
                }
                teacher = optional.get();
                person = teacher.getPerson();
            } else {
                System.out.println("新增模式");
                person = new Person();
                teacher = new Teacher();
            }

            // 设置 Person 信息
            person.setNum((String) form.get("num"));
            person.setName((String) form.get("name"));
            person.setDept((String) form.get("dept"));
            person.setGender((String) form.get("gender"));
            person.setCard((String) form.get("card"));
            person.setPhone((String) form.get("phone"));
            person.setEmail((String) form.get("email"));
            person.setAddress((String) form.get("address"));

            System.out.println("保存 Person 前");
            person = personRepository.save(person);
            System.out.println("保存 Person 后，personId = " + person.getPersonId());

            // 新增模式：创建 User 账号
            if (personId == null || personId <= 0) {
                System.out.println("开始创建 User 账号");

                UserType teacherType = userTypeRepository.findByName("ROLE_TEACHER");
                if (teacherType == null) {
                    teacherType = userTypeRepository.findByName("TEACHER");
                }
                System.out.println("teacherType = " + (teacherType != null ? teacherType.getName() : "null"));

                if (teacherType == null) {
                    DataResponse response = new DataResponse();
                    response.setCode(1);
                    response.setMsg("未找到教师角色类型");
                    return response;
                }

                User user = new User();
                // 注意：不需要 setPersonId！@MapsId 会自动处理
                user.setPerson(person);
                user.setUserType(teacherType);
                user.setUserName(person.getNum());
                String password = encoder.encode("123456");
                user.setPassword(password);
                user.setLoginCount(0);

                System.out.println("保存 User 前");
                userRepository.save(user);
                System.out.println("保存 User 成功");
            }

            // 设置 Teacher 信息
            teacher.setPerson(person);
            teacher.setTitle((String) form.get("title"));
            teacher.setDegree((String) form.get("degree"));

            System.out.println("保存 Teacher 前");
            teacherRepository.save(teacher);
            System.out.println("保存 Teacher 成功");

            DataResponse response = new DataResponse();
            response.setCode(0);
            response.setData(person.getPersonId());
            return response;

        } catch (Exception e) {
            System.err.println("保存失败：");
            e.printStackTrace();
            DataResponse response = new DataResponse();
            response.setCode(1);
            response.setMsg("保存失败：" + e.getMessage());
            return response;
        }
    }
}