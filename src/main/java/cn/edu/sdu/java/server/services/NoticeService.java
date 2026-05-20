package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeTargetRepository noticeTargetRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    public NoticeService(NoticeRepository noticeRepository, NoticeTargetRepository noticeTargetRepository,
                         NoticeReadRepository noticeReadRepository, StudentRepository studentRepository,
                         PersonRepository personRepository, UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.noticeTargetRepository = noticeTargetRepository;
        this.noticeReadRepository = noticeReadRepository;
        this.studentRepository = studentRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
    }

    /**
     * 学生端：获取通知列表
     */
    public DataResponse getNoticeList(DataRequest dataRequest) {
        Integer personId = CommonMethod.getPersonId();
        String filter = dataRequest.getString("filter");         // ALL / UNREAD / READ
        String noticeType = dataRequest.getString("noticeType");

        List<Notice> publishedList = noticeRepository.findByStatusOrderByPublishTimeDesc("PUBLISHED");
        List<Map<String, Object>> dataList = new ArrayList<>();

        // 获取所有目标为ALL的通知ID + 当前学生所在班级的通知ID + 指定该学生的通知ID
        Student student = studentRepository.findByPersonPersonId(personId).orElse(null);
        String className = student != null ? student.getClassName() : "";

        for (Notice notice : publishedList) {
            // 过滤通知类型
            if (noticeType != null && !noticeType.isEmpty() && !noticeType.equals(notice.getNoticeType()))
                continue;

            // 检查该学生是否在目标范围内
            if (!isTargetStudent(notice.getNoticeId(), personId, className))
                continue;

            boolean isRead = noticeReadRepository.findByNoticeIdAndPersonId(notice.getNoticeId(), personId).isPresent();

            // 过滤已读/未读
            if ("READ".equals(filter) && !isRead)
                continue;
            if ("UNREAD".equals(filter) && isRead)
                continue;

            long readCount = noticeReadRepository.countByNoticeId(notice.getNoticeId());
            long targetCount = getTargetCount(notice.getNoticeId());

            Map<String, Object> m = new HashMap<>();
            m.put("noticeId", notice.getNoticeId());
            m.put("title", notice.getTitle());
            m.put("noticeType", notice.getNoticeType());
            m.put("status", notice.getStatus());
            m.put("creatorName", notice.getCreatorName());
            m.put("publishTime", notice.getPublishTime());
            m.put("isRead", isRead);
            m.put("readCount", readCount);
            m.put("targetCount", targetCount);
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 学生端：获取通知详情，点开即标记已读
     */
    public DataResponse getNoticeDetail(DataRequest dataRequest) {
        Integer noticeId = dataRequest.getInteger("noticeId");
        Integer personId = CommonMethod.getPersonId();

        Optional<Notice> op = noticeRepository.findById(noticeId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("通知不存在");
        Notice notice = op.get();

        if (!"PUBLISHED".equals(notice.getStatus()))
            return CommonMethod.getReturnMessageError("通知不可查看");

        // 标记已读
        if (noticeReadRepository.findByNoticeIdAndPersonId(noticeId, personId).isEmpty()) {
            NoticeRead nr = new NoticeRead();
            nr.setNoticeId(noticeId);
            nr.setPersonId(personId);
            nr.setReadTime(DateTimeTool.parseDateTime(new Date()));
            noticeReadRepository.save(nr);
        }

        long readCount = noticeReadRepository.countByNoticeId(noticeId);
        long targetCount = getTargetCount(noticeId);
        String readRate = targetCount > 0
                ? String.format("%.1f%%", readCount * 100.0 / targetCount)
                : "0%";

        Map<String, Object> m = new HashMap<>();
        m.put("noticeId", notice.getNoticeId());
        m.put("title", notice.getTitle());
        m.put("content", notice.getContent());
        m.put("noticeType", notice.getNoticeType());
        m.put("creatorName", notice.getCreatorName());
        m.put("publishTime", notice.getPublishTime());
        m.put("readCount", readCount);
        m.put("targetCount", targetCount);
        m.put("readRate", readRate);
        return CommonMethod.getReturnData(m);
    }

    /**
     * 发布者端：获取通知已读详情
     */
    public DataResponse getReadStatus(DataRequest dataRequest) {
        Integer noticeId = dataRequest.getInteger("noticeId");

        Optional<Notice> op = noticeRepository.findById(noticeId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("通知不存在");

        List<NoticeRead> readList = noticeReadRepository.findByNoticeId(noticeId);
        List<Integer> readPersonIds = noticeReadRepository.findPersonIdListByNoticeId(noticeId);

        // 获取该通知的所有目标学生
        Set<Integer> targetPersonIds = getTargetPersonIds(noticeId);
        long targetCount = targetPersonIds.size();
        long readCount = readList.size();
        long unreadCount = targetCount - readCount;
        String readRate = targetCount > 0
                ? String.format("%.1f%%", readCount * 100.0 / targetCount)
                : "0%";

        // 已读列表
        List<Map<String, Object>> readMapList = new ArrayList<>();
        for (NoticeRead nr : readList) {
            Map<String, Object> item = new HashMap<>();
            item.put("personId", nr.getPersonId());
            item.put("readTime", nr.getReadTime());
            Optional<Person> pOp = personRepository.findById(nr.getPersonId());
            if (pOp.isPresent()) {
                item.put("name", pOp.get().getName());
                Student s = studentRepository.findByPersonPersonId(nr.getPersonId()).orElse(null);
                item.put("className", s != null ? s.getClassName() : "");
            }
            readMapList.add(item);
        }

        // 未读列表
        List<Map<String, Object>> unreadMapList = new ArrayList<>();
        for (Integer pid : targetPersonIds) {
            if (!readPersonIds.contains(pid)) {
                Map<String, Object> item = new HashMap<>();
                item.put("personId", pid);
                Optional<Person> pOp = personRepository.findById(pid);
                if (pOp.isPresent()) {
                    item.put("name", pOp.get().getName());
                    Student s = studentRepository.findByPersonPersonId(pid).orElse(null);
                    item.put("className", s != null ? s.getClassName() : "");
                }
                unreadMapList.add(item);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("readCount", readCount);
        data.put("unreadCount", unreadCount);
        data.put("readRate", readRate);
        data.put("readList", readMapList);
        data.put("unreadList", unreadMapList);
        return CommonMethod.getReturnData(data);
    }

    /**
     * 发布者端：发布/保存通知
     */
    public DataResponse saveNotice(DataRequest dataRequest) {
        Integer noticeId = dataRequest.getInteger("noticeId");
        String action = dataRequest.getString("action");       // DRAFT / PUBLISH
        String title = dataRequest.getString("title");
        String content = dataRequest.getString("content");
        String noticeType = dataRequest.getString("noticeType");
        List<?> rawList = dataRequest.getList("targets");
        List<Map<String, Object>> targets = new ArrayList<>();
        for (Object obj : rawList) {
            if (obj instanceof Map) {
                targets.add((Map<String, Object>) obj);
            }
        }

        if (title == null || title.isEmpty())
            return CommonMethod.getReturnMessageError("标题不能为空");

        Integer personId = CommonMethod.getPersonId();
        String personName = "";
        Optional<Person> personOp = personRepository.findById(personId);
        if (personOp.isPresent())
            personName = personOp.get().getName();

        String now = DateTimeTool.parseDateTime(new Date());

        Notice notice;
        boolean isNew = noticeId == null || noticeId == 0;
        if (isNew) {
            notice = new Notice();
            notice.setCreatorId(personId);
            notice.setCreatorName(personName);
            notice.setCreateTime(now);
        } else {
            Optional<Notice> op = noticeRepository.findById(noticeId);
            if (op.isEmpty())
                return CommonMethod.getReturnMessageError("通知不存在");
            notice = op.get();
            if (!"DRAFT".equals(notice.getStatus()))
                return CommonMethod.getReturnMessageError("仅草稿可编辑");
        }

        notice.setTitle(title);
        notice.setContent(content != null ? content : "");
        notice.setNoticeType(noticeType != null ? noticeType : "SYSTEM");
        notice.setStatus("DRAFT".equals(action) ? "DRAFT" : "PUBLISHED");
        if ("PUBLISH".equals(action))
            notice.setPublishTime(now);

        noticeRepository.save(notice);

        // 保存目标（先删旧的再新增）
        noticeTargetRepository.deleteByNoticeId(notice.getNoticeId());
        if (targets != null) {
            for (Map<String, Object> t : targets) {
                NoticeTarget nt = new NoticeTarget();
                nt.setNoticeId(notice.getNoticeId());
                nt.setTargetType(CommonMethod.getString(t, "targetType"));
                nt.setTargetValue(CommonMethod.getString(t, "targetValue"));
                noticeTargetRepository.save(nt);
            }
        }

        return CommonMethod.getReturnMessageOK(isNew ? "发布成功" : "更新成功");
    }

    /**
     * 发布者端：撤回通知
     */
    public DataResponse withdrawNotice(DataRequest dataRequest) {
        Integer noticeId = dataRequest.getInteger("noticeId");
        Optional<Notice> op = noticeRepository.findById(noticeId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("通知不存在");
        Notice notice = op.get();
        if (!"PUBLISHED".equals(notice.getStatus()))
            return CommonMethod.getReturnMessageError("仅已发布的通知可撤回");

        notice.setStatus("WITHDRAWN");
        noticeRepository.save(notice);
        return CommonMethod.getReturnMessageOK();
    }

    /**
     * 发布者端：获取我发布的通知列表
     */
    public DataResponse getMyNoticeList(DataRequest dataRequest) {
        Integer personId = CommonMethod.getPersonId();
        String status = dataRequest.getString("status");
        if (status == null) status = "";

        List<Notice> list = noticeRepository.findByCreatorIdAndStatus(personId, status);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Notice notice : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("noticeId", notice.getNoticeId());
            m.put("title", notice.getTitle());
            m.put("noticeType", notice.getNoticeType());
            m.put("status", notice.getStatus());
            m.put("publishTime", notice.getPublishTime());
            m.put("createTime", notice.getCreateTime());
            m.put("readCount", noticeReadRepository.countByNoticeId(notice.getNoticeId()));
            m.put("targetCount", getTargetCount(notice.getNoticeId()));
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    // ========== 内部辅助方法 ==========

    /**
     * 判断学生是否在通知的目标范围内
     */
    private boolean isTargetStudent(Integer noticeId, Integer personId, String className) {
        List<NoticeTarget> targets = noticeTargetRepository.findByNoticeId(noticeId);
        if (targets.isEmpty()) return true; // 无目标限制 = 全体

        for (NoticeTarget nt : targets) {
            switch (nt.getTargetType()) {
                case "ALL":
                    return true;
                case "CLASS":
                    if (nt.getTargetValue() != null && nt.getTargetValue().equals(className))
                        return true;
                    break;
                case "PERSON":
                    if (String.valueOf(personId).equals(nt.getTargetValue()))
                        return true;
                    break;
            }
        }
        return false;
    }

    /**
     * 获取通知的目标学生人数
     */
    private long getTargetCount(Integer noticeId) {
        return getTargetPersonIds(noticeId).size();
    }

    /**
     * 获取通知的目标学生 personId 集合
     */
    private Set<Integer> getTargetPersonIds(Integer noticeId) {
        List<NoticeTarget> targets = noticeTargetRepository.findByNoticeId(noticeId);
        Set<Integer> personIds = new HashSet<>();
        boolean hasAll = false;

        for (NoticeTarget nt : targets) {
            switch (nt.getTargetType()) {
                case "ALL":
                    hasAll = true;
                    break;
                case "CLASS":
                    List<Student> classStudents = studentRepository.findByClassName(nt.getTargetValue());
                    for (Student s : classStudents)
                        personIds.add(s.getPersonId());
                    break;
                case "PERSON":
                    personIds.add(Integer.parseInt(nt.getTargetValue()));
                    break;
            }
        }

        if (hasAll || targets.isEmpty()) {
            // 包含所有学生
            List<Student> allStudents = studentRepository.findAll();
            for (Student s : allStudents)
                personIds.add(s.getPersonId());
        }

        return personIds;
    }
}
