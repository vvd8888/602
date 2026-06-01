package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.repositorys.*;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class StudentResumeService {

    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ScoreRepository scoreRepository;

    public StudentResumeService(StudentRepository studentRepository,
                                PersonRepository personRepository,
                                FamilyMemberRepository familyMemberRepository,
                                ScoreRepository scoreRepository) {
        this.studentRepository = studentRepository;
        this.personRepository = personRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.scoreRepository = scoreRepository;
    }

    /**
     * 生成学生简历PDF
     * @param personId 学生personId
     * @return PDF字节数组
     */
    public byte[] generateStudentResume(Integer personId) throws IOException {
        // 获取学生信息
        Optional<Student> studentOpt = studentRepository.findById(personId);
        if (studentOpt.isEmpty()) {
            throw new RuntimeException("学生不存在");
        }
        Student student = studentOpt.get();
        Person person = student.getPerson();

        // 获取家庭成员信息
        List<FamilyMember> familyMembers = familyMemberRepository.findByStudentPersonId(personId);

        // 获取成绩信息
        List<Score> scores = scoreRepository.findByStudentPersonId(personId);

        // 构建HTML内容
        String htmlContent = buildResumeHtml(person, student, familyMembers, scores);

        // 生成PDF
        return convertHtmlToPdf(htmlContent);
    }

    /**
     * 构建简历HTML内容
     */
    private String buildResumeHtml(Person person, Student student, 
                                   List<FamilyMember> familyMembers, 
                                   List<Score> scores) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html lang='zh-CN'>")
            .append("<head>")
            .append("<meta charset='UTF-8'>")
            .append("<style>")
            .append("body { font-family: 'SimSun', 'STSong-Light', serif; margin: 40px; line-height: 1.8; }")
            .append("h1 { text-align: center; color: #333; border-bottom: 3px solid #333; padding-bottom: 10px; }")
            .append("h2 { color: #555; border-left: 4px solid #333; padding-left: 10px; margin-top: 30px; }")
            .append(".info-table { width: 100%; border-collapse: collapse; margin: 20px 0; }")
            .append(".info-table td { padding: 8px; border: 1px solid #ddd; }")
            .append(".info-table td:first-child { width: 120px; background-color: #f5f5f5; font-weight: bold; }")
            .append(".section { margin-bottom: 30px; }")
            .append("</style>")
            .append("</head>")
            .append("<body>");

        // 标题
        html.append("<h1>个人简历</h1>");

        // 基本信息
        html.append("<div class='section'>")
            .append("<h2>基本信息</h2>")
            .append("<table class='info-table'>")
            .append("<tr><td>姓名</td><td>").append(nullSafe(person.getName())).append("</td>")
            .append("<td>学号</td><td>").append(nullSafe(person.getNum())).append("</td></tr>")
            .append("<tr><td>性别</td><td>").append("1".equals(person.getGender()) ? "男" : "女").append("</td>")
            .append("<td>出生日期</td><td>").append(nullSafe(person.getBirthday())).append("</td></tr>")
            .append("<tr><td>民族</td><td>").append(nullSafe(student.getNation())).append("</td>")
            .append("<td>身份证号</td><td>").append(nullSafe(person.getCard())).append("</td></tr>")
            .append("<tr><td>学院</td><td>").append(nullSafe(person.getDept())).append("</td>")
            .append("<td>专业</td><td>").append(nullSafe(student.getMajor())).append("</td></tr>")
            .append("<tr><td>班级</td><td>").append(nullSafe(student.getClassName())).append("</td>")
            .append("<td>年级</td><td>").append(nullSafe(student.getGrade())).append("</td></tr>")
            .append("<tr><td>电话</td><td>").append(nullSafe(person.getPhone())).append("</td>")
            .append("<td>邮箱</td><td>").append(nullSafe(person.getEmail())).append("</td></tr>")
            .append("<tr><td>地址</td><td colspan='3'>").append(nullSafe(person.getAddress())).append("</td></tr>")
            .append("</table>")
            .append("</div>");

        // 个人简介
        if (person.getIntroduce() != null && !person.getIntroduce().isEmpty()) {
            html.append("<div class='section'>")
                .append("<h2>个人简介</h2>")
                .append("<p>").append(nullSafe(person.getIntroduce())).append("</p>")
                .append("</div>");
        }

        // 家庭成员
        if (familyMembers != null && !familyMembers.isEmpty()) {
            html.append("<div class='section'>")
                .append("<h2>家庭成员</h2>")
                .append("<table class='info-table'>")
                .append("<tr style='background-color: #f5f5f5;'>")
                .append("<td>关系</td><td>姓名</td><td>性别</td><td>年龄</td><td>工作单位</td>")
                .append("</tr>");
            
            for (FamilyMember member : familyMembers) {
                html.append("<tr>")
                    .append("<td>").append(nullSafe(member.getRelation())).append("</td>")
                    .append("<td>").append(nullSafe(member.getName())).append("</td>")
                    .append("<td>").append("1".equals(member.getGender()) ? "男" : "女").append("</td>")
                    .append("<td>").append(member.getAge() != null ? member.getAge().toString() : "").append("</td>")
                    .append("<td>").append(nullSafe(member.getUnit())).append("</td>")
                    .append("</tr>");
            }
            html.append("</table></div>");
        }

        // 学习成绩
        if (scores != null && !scores.isEmpty()) {
            html.append("<div class='section'>")
                .append("<h2>学习成绩</h2>")
                .append("<table class='info-table'>")
                .append("<tr style='background-color: #f5f5f5;'>")
                .append("<td>课程名称</td><td>成绩</td><td>学分</td>")
                .append("</tr>");
            
            for (Score score : scores) {
                html.append("<tr>")
                    .append("<td>").append(nullSafe(score.getCourse() != null ? score.getCourse().getName() : "")).append("</td>")
                    .append("<td>").append(score.getMark() != null ? score.getMark().toString() : "").append("</td>")
                    .append("<td>").append(score.getCourse() != null && score.getCourse().getCredit() != null ? score.getCourse().getCredit().toString() : "").append("</td>")
                    .append("</tr>");
            }
            html.append("</table></div>");
        }

        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * 将HTML转换为PDF
     */
    private byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            
            ConverterProperties properties = new ConverterProperties();
            
            // 尝试加载中文字体
            try {
                ClassPathResource fontResource = new ClassPathResource("font/SourceHanSansSC-Regular.ttf");
                com.itextpdf.layout.font.FontProvider fontProvider = new com.itextpdf.layout.font.FontProvider();
                fontProvider.addFont(fontResource.getInputStream().readAllBytes());
                properties.setFontProvider(fontProvider);
            } catch (Exception e) {
                System.err.println("加载中文字体失败，使用默认字体: " + e.getMessage());
            }
            
            HtmlConverter.convertToPdf(htmlContent, pdfDocument, properties);
            pdfDocument.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("PDF生成失败: " + e.getMessage());
        }
        
        return baos.toByteArray();
    }

    /**
     * 安全处理null值
     */
    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
