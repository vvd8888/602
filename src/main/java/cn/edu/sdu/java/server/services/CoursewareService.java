package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Courseware;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CoursewareRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CoursewareService {

    private final CoursewareRepository coursewareRepository;
    private final TeacherRepository teacherRepository;
    private final PersonRepository personRepository;

    @Value("${courseware.upload.path:uploads/courseware}")
    private String uploadPath;

    public CoursewareService(CoursewareRepository coursewareRepository,
                             TeacherRepository teacherRepository,
                             PersonRepository personRepository) {
        this.coursewareRepository = coursewareRepository;
        this.teacherRepository = teacherRepository;
        this.personRepository = personRepository;
    }

    // 获取当前登录用户角色
    private String getCurrentRole() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getAuthorities().iterator().next().getAuthority();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 获取当前登录用户信息（支持教师、管理员、普通用户）
    private Map<String, Object> getCurrentTeacher() {
        Integer personId = CommonMethod.getPersonId();
        String role = getCurrentRole();
        Map<String, Object> result = new HashMap<>();

        System.out.println("getCurrentTeacher - personId = " + personId);
        System.out.println("getCurrentTeacher - role = " + role);

        if (personId != null) {
            // 先尝试查找教师
            Optional<Teacher> teacherOpt = teacherRepository.findById(personId);
            if (teacherOpt.isPresent()) {
                Teacher teacher = teacherOpt.get();
                result.put("teacherId", teacher.getPersonId());
                result.put("teacherName", teacher.getPerson().getName());
                System.out.println("教师上传: " + teacher.getPerson().getName());
                return result;
            }

            // 如果不是教师，查找 Person（适用于管理员或普通用户）
            Optional<Person> personOpt = personRepository.findById(personId);
            if (personOpt.isPresent()) {
                Person person = personOpt.get();
                result.put("teacherId", personId);
                result.put("teacherName", person.getName());
                System.out.println("非教师用户上传: " + person.getName() + ", 角色: " + role);
                return result;
            }

            System.out.println("未找到用户记录，personId=" + personId);
        }
        return result;
    }

    public DataResponse getCoursewareList(DataRequest dataRequest) {
        List<Courseware> list = coursewareRepository.findAllOrderByIdDesc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Courseware c : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("coursewareId", c.getCoursewareId());
            map.put("title", c.getTitle());
            map.put("description", c.getDescription());
            map.put("fileName", c.getFileName());
            map.put("fileSize", c.getFileSize());
            map.put("fileType", c.getFileType());
            map.put("courseId", c.getCourseId());
            map.put("courseName", c.getCourseName());
            map.put("teacherId", c.getTeacherId());
            map.put("teacherName", c.getTeacherName());
            map.put("downloadCount", c.getDownloadCount());
            map.put("createTime", c.getCreateTime());
            result.add(map);
        }

        DataResponse response = new DataResponse();
        response.setCode(0);
        response.setData(result);
        return response;
    }

    public DataResponse uploadCourseware(MultipartFile file, String title, String description,
                                         Integer courseId, String courseName) {
        try {
            System.out.println("=== 开始上传课件 ===");
            System.out.println("title = " + title);
            System.out.println("description = " + description);
            System.out.println("courseId = " + courseId);
            System.out.println("courseName = " + courseName);

            Map<String, Object> teacher = getCurrentTeacher();
            Integer teacherId = (Integer) teacher.get("teacherId");
            String teacherName = (String) teacher.get("teacherName");

            System.out.println("teacherId = " + teacherId);
            System.out.println("teacherName = " + teacherName);

            if (teacherId == null) {
                DataResponse response = new DataResponse();
                response.setCode(1);
                response.setMsg("请先登录");
                return response;
            }

            // 获取项目根目录的绝对路径
            String basePath = System.getProperty("user.dir");
            Path uploadDir = Paths.get(basePath, uploadPath);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("创建目录: " + uploadDir.toAbsolutePath());
            }

            String originalFileName = file.getOriginalFilename();
            String fileExt = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExt;

            Path targetPath = uploadDir.resolve(newFileName);
            System.out.println("保存到: " + targetPath.toAbsolutePath());

            // 保存文件
            file.getInputStream().transferTo(Files.newOutputStream(targetPath));

            Courseware courseware = new Courseware();
            courseware.setTitle(title);
            courseware.setDescription(description);
            courseware.setFileName(originalFileName);
            courseware.setFilePath(targetPath.toString());
            courseware.setFileSize(file.getSize());
            courseware.setFileType(fileExt);
            courseware.setCourseId(courseId);
            courseware.setCourseName(courseName);
            courseware.setTeacherId(teacherId);
            courseware.setTeacherName(teacherName);
            courseware.setDownloadCount(0);
            courseware.setCreateTime(DateTimeTool.parseDateTime(new Date()));

            coursewareRepository.save(courseware);
            System.out.println("数据库保存成功，ID = " + courseware.getCoursewareId());

            DataResponse response = new DataResponse();
            response.setCode(0);
            response.setMsg("上传成功！");
            return response;

        } catch (Exception e) {
            System.err.println("上传失败：");
            e.printStackTrace();
            DataResponse response = new DataResponse();
            response.setCode(1);
            response.setMsg("上传失败：" + e.getMessage());
            return response;
        }
    }

    public DataResponse deleteCourseware(DataRequest dataRequest) {
        Integer coursewareId = dataRequest.getInteger("coursewareId");
        Optional<Courseware> optional = coursewareRepository.findById(coursewareId);
        if (optional.isEmpty()) {
            DataResponse response = new DataResponse();
            response.setCode(1);
            response.setMsg("课件不存在");
            return response;
        }
        Courseware courseware = optional.get();

        try {
            Path filePath = Paths.get(courseware.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        coursewareRepository.delete(courseware);

        DataResponse response = new DataResponse();
        response.setCode(0);
        response.setMsg("删除成功！");
        return response;
    }

    public ResponseEntity<byte[]> downloadCourseware(Integer coursewareId, boolean isOnlineView) {
        Optional<Courseware> optional = coursewareRepository.findById(coursewareId);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Courseware courseware = optional.get();

        // 检查文件是否存在
        Path filePath = Paths.get(courseware.getFilePath());
        if (!Files.exists(filePath)) {
            System.err.println("文件不存在: " + courseware.getFilePath());
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] data = Files.readAllBytes(filePath);

            // 根据文件类型设置Content-Type
            MediaType mediaType = getMediaTypeByFileType(courseware.getFileType());

            // 处理文件名编码，防止中文乱码
            String fileName = courseware.getFileName();
            if (fileName == null || fileName.isEmpty()) {
                fileName = "courseware" + courseware.getFileType();
            }
            
            // URL编码文件名，支持中文
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            
            if (isOnlineView) {
                // 在线查看：使用inline，浏览器会尝试直接打开
                headers.setContentDispositionFormData("inline", encodedFileName);
            } else {
                // 下载：使用attachment，浏览器会下载文件
                headers.setContentDispositionFormData("attachment", encodedFileName);
                // 增加下载次数
                courseware.setDownloadCount(courseware.getDownloadCount() + 1);
                coursewareRepository.save(courseware);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 根据文件扩展名获取MediaType
    private MediaType getMediaTypeByFileType(String fileType) {
        if (fileType == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        
        String ext = fileType.toLowerCase();
        switch (ext) {
            case ".pdf":
                return MediaType.APPLICATION_PDF;
            case ".jpg":
            case ".jpeg":
                return MediaType.IMAGE_JPEG;
            case ".png":
                return MediaType.IMAGE_PNG;
            case ".gif":
                return MediaType.IMAGE_GIF;
            case ".bmp":
                return MediaType.valueOf("image/bmp");
            case ".txt":
                return MediaType.TEXT_PLAIN;
            case ".html":
            case ".htm":
                return MediaType.TEXT_HTML;
            case ".xml":
                return MediaType.APPLICATION_XML;
            case ".json":
                return MediaType.APPLICATION_JSON;
            case ".doc":
                return MediaType.valueOf("application/msword");
            case ".docx":
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case ".xls":
                return MediaType.valueOf("application/vnd.ms-excel");
            case ".xlsx":
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case ".ppt":
                return MediaType.valueOf("application/vnd.ms-powerpoint");
            case ".pptx":
                return MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            case ".zip":
                return MediaType.valueOf("application/zip");
            case ".rar":
                return MediaType.valueOf("application/x-rar-compressed");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
