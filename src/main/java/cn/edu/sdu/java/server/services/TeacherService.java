package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;

    public TeacherService(TeacherRepository teacherRepository,
                          PersonRepository personRepository,
                          UserRepository userRepository,
                          UserTypeRepository userTypeRepository) {
        this.teacherRepository = teacherRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
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

    public DataResponse teacherEditSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Map<String, Object> form = (Map<String, Object>) dataRequest.get("form");

        Person person;
        Teacher teacher;

        if (personId != null && personId > 0) {
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
            person = new Person();
            teacher = new Teacher();
        }

        person.setNum((String) form.get("num"));
        person.setName((String) form.get("name"));
        person.setDept((String) form.get("dept"));
        person.setGender((String) form.get("gender"));
        person.setCard((String) form.get("card"));
        person.setPhone((String) form.get("phone"));
        person.setEmail((String) form.get("email"));
        person.setAddress((String) form.get("address"));

        person = personRepository.save(person);

        if (personId == null || personId <= 0) {
            UserType teacherType = userTypeRepository.findByName("ROLE_TEACHER");
            if (teacherType == null) {
                teacherType = userTypeRepository.findByName("TEACHER");
                if (teacherType == null) {
                    DataResponse response = new DataResponse();
                    response.setCode(1);
                    response.setMsg("未找到教师角色类型，请联系管理员");
                    return response;
                }
            }

            User user = new User();
            user.setPersonId(person.getPersonId());
            user.setPerson(person);
            user.setUserType(teacherType);
            user.setUserName(person.getNum());
            user.setPassword("123456");
            user.setLoginCount(0);
            userRepository.save(user);
        }

        teacher.setPersonId(person.getPersonId());
        teacher.setPerson(person);
        teacher.setTitle((String) form.get("title"));
        teacher.setDegree((String) form.get("degree"));

        teacherRepository.save(teacher);

        DataResponse response = new DataResponse();
        response.setCode(0);
        response.setData(person.getPersonId());
        return response;
    }
}