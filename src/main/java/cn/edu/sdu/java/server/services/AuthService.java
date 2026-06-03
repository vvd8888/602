package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.request.LoginRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.JwtResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import cn.edu.sdu.java.server.util.LoginControlUtil;
import jakarta.validation.Valid;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    public AuthService(PersonRepository personRepository, UserRepository userRepository, UserTypeRepository userTypeRepository, StudentRepository studentRepository, TeacherRepository teacherRepository, AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder encoder, ResourceLoader resourceLoader) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Optional<User> op= userRepository.findByUserName(loginRequest.getUsername());
        if(op.isPresent()) {
            User user= op.get();
            user.setLastLoginTime(DateTimeTool.parseDateTime(new Date()));
            Integer count = user.getLoginCount();
            if (count == null)
                count = 1;
            else count += 1;
            user.setLoginCount(count);
            userRepository.save(user);
        }
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getPerName(),
                roles.getFirst()));
    }
    public DataResponse getValidateCode(DataRequest dataRequest) {
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }

    public DataResponse testValidateInfo( DataRequest dataRequest) {
        Integer validateCodeId = dataRequest.getInteger("validateCodeId");
        String validateCode = dataRequest.getString("validateCode");
        LoginControlUtil li =  LoginControlUtil.getInstance();
        if(validateCodeId == null || validateCode== null || validateCode.isEmpty()) {
            return CommonMethod.getReturnMessageError("验证码为空！");
        }
        String value = li.getValidateCode(validateCodeId);
        if(!validateCode.equals(value))
            return CommonMethod.getReturnMessageError("验证码错位！");
        return CommonMethod.getReturnMessageOK();
    }
    
    /**
     * 注册教师用户(专用方法)
     * 用于恢复被删除的教师用户(person_id=3, user_name="3")
     * 
     * @param dataRequest 包含 password 字段
     * @return 注册结果
     */
    @PostMapping("/registerTeacher")
    public DataResponse registerTeacher(@Valid @RequestBody DataRequest dataRequest) {
        String password = dataRequest.getString("password");
            
        if (password == null || password.isEmpty()) {
            return CommonMethod.getReturnMessageError("密码不能为空!");
        }
    
        // 检查 person_id=3 是否已存在
        Optional<Person> personOp = personRepository.findById(3);
        if (personOp.isPresent()) {
            return CommonMethod.getReturnMessageError("人员信息已存在(person_id=3),无法重复创建!");
        }
    
        // 检查 user_name="3" 是否已存在
        Optional<User> userOp = userRepository.findByUserName("3");
        if (userOp.isPresent()) {
            return CommonMethod.getReturnMessageError("用户名已存在(user_name=3),无法重复创建!");
        }
    
        try {
            // 1. 创建 Person 记录
            Person person = new Person();
            person.setPersonId(3);  // 手动设置 person_id=3
            person.setNum("3");     // 编号也设置为 "3"
            person.setName("教师3"); // 默认名称
            person.setType("2");    // 类型:2 表示教师
            personRepository.saveAndFlush(person);
    
            // 2. 获取教师用户类型
            UserType userType = userTypeRepository.findByName(EUserType.ROLE_TEACHER.name());
            if (userType == null) {
                return CommonMethod.getReturnMessageError("教师用户类型不存在,请先创建用户类型!");
            }
    
            // 3. 创建 User 记录(使用 BCrypt 加密密码)
            User user = new User();
            user.setPersonId(3);
            user.setPerson(person);
            user.setUserType(userType);
            user.setUserName("3");
            user.setPassword(encoder.encode(password));  // 使用 BCryptPasswordEncoder 加密
            user.setCreateTime(DateTimeTool.parseDateTime(new Date()));
            user.setCreatorId(1);  // 假设由管理员创建
            user.setLoginCount(0);
            userRepository.saveAndFlush(user);
    
            // 4. 创建 Teacher 记录
            Teacher teacher = new Teacher();
            teacher.setPersonId(3);
            teacher.setPerson(person);
            teacher.setTitle("教师");
            teacher.setDegree("硕士");
            teacherRepository.saveAndFlush(teacher);
    
            return CommonMethod.getReturnMessageOK("教师用户注册成功!person_id=3, user_name=3");
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("注册失败:" + e.getMessage());
        }
    }
    /*
     *  注册用户示例，我们项目暂时不用， 所有用户通过管理员添加，这里注册，没有考虑关联人员信息的创建，使用时参加学生添加功能的实现
     */
    @PostMapping("/registerUser")
    public DataResponse registerUser(@Valid @RequestBody DataRequest dataRequest) {
        String username = dataRequest.getString("username");
        String password = dataRequest.getString("password");
        String perName = dataRequest.getString("perName");
        String email = dataRequest.getString("email");
        String role = dataRequest.getString("role");
        UserType ut = null;
        Optional<User> uOp = userRepository.findByUserName(username);
        if(uOp.isPresent()) {
            return CommonMethod.getReturnMessageError("用户已经存在，不能注册！");
        }
        Person p = new Person();
        p.setNum(username);
        p.setName(perName);
        p.setEmail(email);
        if("ADMIN".equals(role)) {
            p.setType("0");
            ut = userTypeRepository.findByName(EUserType.ROLE_ADMIN.name());
        }else if("STUDENT".equals(role)) {
            p.setType("1");
            ut = userTypeRepository.findByName(EUserType.ROLE_STUDENT.name());
        }else if("TEACHER".equals(role)) {
            p.setType("2");
            ut = userTypeRepository.findByName(EUserType.ROLE_TEACHER.name());
        }
        personRepository.saveAndFlush(p);
        User u = new User();
        u.setPerson(p);
        u.setUserType(ut);
        u.setUserName(username);
        u.setPassword(encoder.encode(password));
        u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        u.setCreatorId(p.getPersonId());
        u.setLoginCount(0);
        userRepository.saveAndFlush(u);
        if("STUDENT".equals(role)) {
            Student s = new Student();   // 创建实体对象
            s.setPerson(p);
            studentRepository.saveAndFlush(s);  //插入新的Student记录
        }
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }

}
