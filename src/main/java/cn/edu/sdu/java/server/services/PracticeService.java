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
public class PracticeService {
    private final PracticeProjectRepository projectRepository;
    private final PracticeMemberRepository memberRepository;
    private final PracticeSummaryRepository summaryRepository;
    private final PracticeReviewRepository reviewRepository;
    private final KeyTopicRepository keyTopicRepository;
    private final PersonRepository personRepository;
    private final StudentRepository studentRepository;

    public PracticeService(PracticeProjectRepository projectRepository,
                           PracticeMemberRepository memberRepository,
                           PracticeSummaryRepository summaryRepository,
                           PracticeReviewRepository reviewRepository,
                           KeyTopicRepository keyTopicRepository,
                           PersonRepository personRepository,
                           StudentRepository studentRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.summaryRepository = summaryRepository;
        this.reviewRepository = reviewRepository;
        this.keyTopicRepository = keyTopicRepository;
        this.personRepository = personRepository;
        this.studentRepository = studentRepository;
    }

    // ==================== 学生端 ====================

    /**
     * 学生端：获取我的项目列表
     */
    public DataResponse getMyProjectList(DataRequest dataRequest) {
        Integer personId = CommonMethod.getPersonId();
        String projectType = dataRequest.getString("projectType"); // NORMAL / KEY / ""
        if (projectType == null) projectType = "";

        // 获取我作为队员的所有项目
        List<PracticeMember> myMemberships = memberRepository.findByPersonId(personId);
        Set<Integer> myProjectIds = new HashSet<>();
        for (PracticeMember pm : myMemberships) {
            myProjectIds.add(pm.getProjectId());
        }

        List<PracticeProject> projects;
        if (projectType.isEmpty()) {
            projects = projectRepository.findByStatus("");
        } else {
            projects = projectRepository.findByStatusAndProjectType("", projectType);
        }

        // 过滤：只看我参与的项目
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (PracticeProject p : projects) {
            if (!myProjectIds.contains(p.getProjectId())) continue;

            List<PracticeMember> members = memberRepository.findByProjectId(p.getProjectId());
            Map<String, Object> m = buildProjectSummary(p, members);
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 学生端：获取可申请的重点选题列表
     */
    public DataResponse getAvailableKeyTopics(DataRequest dataRequest) {
        List<KeyTopic> topics = keyTopicRepository.findByStatusOrderByCreateTimeDesc("PUBLISHED");
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (KeyTopic kt : topics) {
            Map<String, Object> m = new HashMap<>();
            m.put("keyTopicId", kt.getKeyTopicId());
            m.put("title", kt.getTitle());
            m.put("description", kt.getDescription());
            m.put("requirements", kt.getRequirements());
            m.put("creatorName", kt.getCreatorName());
            m.put("createTime", kt.getCreateTime());
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 学生端：获取项目详情
     */
    public DataResponse getProjectDetail(DataRequest dataRequest) {
        Integer projectId = dataRequest.getInteger("projectId");
        Optional<PracticeProject> op = projectRepository.findById(projectId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("项目不存在");
        PracticeProject p = op.get();

        List<PracticeMember> members = memberRepository.findByProjectId(p.getProjectId());
        List<PracticeSummary> summaries = summaryRepository.findByProjectId(p.getProjectId());
        List<PracticeReview> reviews = reviewRepository.findByProjectIdOrderByReviewTimeDesc(p.getProjectId());

        Map<String, Object> data = buildProjectSummary(p, members);
        data.put("description", p.getDescription());
        data.put("projectType", p.getProjectType());
        data.put("keyTopicId", p.getKeyTopicId());
        data.put("mentor", p.getMentor());
        data.put("startDate", p.getStartDate());
        data.put("endDate", p.getEndDate());
        data.put("createTime", p.getCreateTime());
        data.put("submitTime", p.getSubmitTime());
        data.put("approveTime", p.getApproveTime());
        data.put("rejectReason", p.getRejectReason());

        List<Map<String, Object>> memberList = new ArrayList<>();
        for (PracticeMember pm : members) {
            Map<String, Object> mm = new HashMap<>();
            mm.put("memberId", pm.getMemberId());
            mm.put("personId", pm.getPersonId());
            mm.put("personName", pm.getPersonName());
            mm.put("className", pm.getClassName());
            mm.put("role", pm.getRole());
            memberList.add(mm);
        }
        data.put("members", memberList);

        List<Map<String, Object>> summaryList = new ArrayList<>();
        for (PracticeSummary ps : summaries) {
            Map<String, Object> sm = new HashMap<>();
            sm.put("summaryId", ps.getSummaryId());
            sm.put("personId", ps.getPersonId());
            sm.put("personName", ps.getPersonName());
            sm.put("summaryType", ps.getSummaryType());
            sm.put("title", ps.getTitle());
            sm.put("content", ps.getContent());
            sm.put("submitTime", ps.getSubmitTime());
            sm.put("status", ps.getStatus());
            summaryList.add(sm);
        }
        data.put("summaries", summaryList);

        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (PracticeReview pr : reviews) {
            Map<String, Object> rm = new HashMap<>();
            rm.put("reviewId", pr.getReviewId());
            rm.put("reviewerName", pr.getReviewerName());
            rm.put("reviewType", pr.getReviewType());
            rm.put("reviewResult", pr.getReviewResult());
            rm.put("comment", pr.getComment());
            rm.put("reviewTime", pr.getReviewTime());
            reviewList.add(rm);
        }
        data.put("reviews", reviewList);

        return CommonMethod.getReturnData(data);
    }

    /**
     * 学生端：保存/提交项目
     */
    public DataResponse saveProject(DataRequest dataRequest) {
        Integer projectId = dataRequest.getInteger("projectId");
        String action = dataRequest.getString("action"); // DRAFT / SUBMIT
        String title = dataRequest.getString("title");
        String description = dataRequest.getString("description");
        String projectType = dataRequest.getString("projectType"); // NORMAL / KEY
        Integer keyTopicId = dataRequest.getInteger("keyTopicId");
        String mentor = dataRequest.getString("mentor");
        String startDate = dataRequest.getString("startDate");
        String endDate = dataRequest.getString("endDate");
        List<?> rawMembers = dataRequest.getList("members");

        if (title == null || title.isEmpty())
            return CommonMethod.getReturnMessageError("标题不能为空");
        if (projectType == null || (!"NORMAL".equals(projectType) && !"KEY".equals(projectType)))
            return CommonMethod.getReturnMessageError("项目类型无效");

        Integer personId = CommonMethod.getPersonId();
        String now = DateTimeTool.parseDateTime(new Date());

        PracticeProject p;
        boolean isNew = projectId == null || projectId == 0;
        if (isNew) {
            p = new PracticeProject();
            p.setLeaderId(personId);
            p.setCreateTime(now);
            // 获取队长信息
            String leaderName = "";
            String className = "";
            Optional<Person> pop = personRepository.findById(personId);
            if (pop.isPresent()) {
                leaderName = pop.get().getName();
                Student s = studentRepository.findByPersonPersonId(personId).orElse(null);
                if (s != null) className = s.getClassName();
            }
            p.setLeaderName(leaderName);
        } else {
            Optional<PracticeProject> op = projectRepository.findById(projectId);
            if (op.isEmpty())
                return CommonMethod.getReturnMessageError("项目不存在");
            p = op.get();
            if (!"DRAFT".equals(p.getStatus()) && !"REJECTED".equals(p.getStatus()))
                return CommonMethod.getReturnMessageError("仅草稿或已驳回状态可编辑");
            if (!p.getLeaderId().equals(personId))
                return CommonMethod.getReturnMessageError("仅队长可编辑");
        }

        p.setTitle(title);
        p.setDescription(description != null ? description : "");
        p.setProjectType(projectType);
        p.setKeyTopicId(keyTopicId);
        p.setMentor(mentor);
        p.setStartDate(startDate);
        p.setEndDate(endDate);

        if ("SUBMIT".equals(action)) {
            p.setStatus("SUBMITTED");
            p.setSubmitTime(now);
        } else {
            p.setStatus("DRAFT");
        }
        p.setRejectReason(null);

        projectRepository.save(p);

        // 保存队员（先删旧的再新增，自动过滤队长）
        if (isNew) {
            saveMembersFromList(p.getProjectId(), rawMembers, personId);
            addLeaderToProject(p.getProjectId(), personId);
        } else {
            memberRepository.deleteByProjectId(p.getProjectId());
            saveMembersFromList(p.getProjectId(), rawMembers, personId);
            addLeaderToProject(p.getProjectId(), personId);
        }

        return CommonMethod.getReturnData(p.getProjectId(), isNew ? "创建成功" : "更新成功");
    }

    private void addLeaderToProject(Integer projectId, Integer personId) {
        PracticeMember leader = new PracticeMember();
        leader.setProjectId(projectId);
        leader.setPersonId(personId);
        leader.setRole("LEADER");
        Optional<Person> pop = personRepository.findById(personId);
        if (pop.isPresent()) {
            leader.setPersonName(pop.get().getName());
            Student s = studentRepository.findByPersonPersonId(personId).orElse(null);
            if (s != null) leader.setClassName(s.getClassName());
        }
        memberRepository.save(leader);
    }

    private void saveMembersFromList(Integer projectId, List<?> rawMembers, Integer leaderId) {
        if (rawMembers == null) return;
        for (Object obj : rawMembers) {
            if (!(obj instanceof Map)) continue;
            Map<String, Object> mm = (Map<String, Object>) obj;
            Integer memberPersonId = CommonMethod.getInteger(mm, "personId");
            if (memberPersonId == null || memberPersonId.equals(leaderId)) continue;
            PracticeMember pm = new PracticeMember();
            pm.setProjectId(projectId);
            pm.setPersonId(memberPersonId);
            pm.setPersonName(CommonMethod.getString(mm, "personName"));
            pm.setClassName(CommonMethod.getString(mm, "className"));
            pm.setRole("MEMBER");
            memberRepository.save(pm);
        }
    }

    /**
     * 学生端：删除草稿项目
     */
    public DataResponse deleteProject(DataRequest dataRequest) {
        Integer projectId = dataRequest.getInteger("projectId");
        Integer personId = CommonMethod.getPersonId();

        Optional<PracticeProject> op = projectRepository.findById(projectId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("项目不存在");
        PracticeProject p = op.get();

        if (!p.getLeaderId().equals(personId))
            return CommonMethod.getReturnMessageError("仅队长可删除");
        if (!"DRAFT".equals(p.getStatus()))
            return CommonMethod.getReturnMessageError("仅草稿可删除");

        memberRepository.deleteByProjectId(projectId);
        summaryRepository.deleteByProjectId(projectId);
        projectRepository.delete(p);
        return CommonMethod.getReturnMessageOK("已删除");
    }

    /**
     * 学生端：提交总结
     */
    public DataResponse submitSummary(DataRequest dataRequest) {
        Integer projectId = dataRequest.getInteger("projectId");
        Integer summaryId = dataRequest.getInteger("summaryId");
        String action = dataRequest.getString("action"); // DRAFT / SUBMIT
        String summaryType = dataRequest.getString("summaryType"); // PERSONAL / TEAM
        String title = dataRequest.getString("title");
        String content = dataRequest.getString("content");
        Integer personId = CommonMethod.getPersonId();

        Optional<PracticeProject> op = projectRepository.findById(projectId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("项目不存在");
        PracticeProject p = op.get();

        if (!"COMPLETED".equals(p.getStatus()) && !"SUMMARY_SUBMITTED".equals(p.getStatus()))
            return CommonMethod.getReturnMessageError("项目尚未完成，无法提交总结");

        // 团队总结仅队长可提交
        if ("TEAM".equals(summaryType) && !p.getLeaderId().equals(personId))
            return CommonMethod.getReturnMessageError("仅队长可提交团队总结");

        String now = DateTimeTool.parseDateTime(new Date());

        PracticeSummary ps;
        if (summaryId != null && summaryId > 0) {
            Optional<PracticeSummary> sop = summaryRepository.findById(summaryId);
            if (sop.isEmpty())
                return CommonMethod.getReturnMessageError("总结不存在");
            ps = sop.get();
            if (!"DRAFT".equals(ps.getStatus()) && !"REJECTED".equals(ps.getStatus()))
                return CommonMethod.getReturnMessageError("仅草稿或已驳回可编辑");
        } else {
            // 检查是否已有同类总结
            if ("TEAM".equals(summaryType)) {
                PracticeSummary existing = summaryRepository.findByProjectIdAndSummaryType(projectId, "TEAM");
                if (existing != null)
                    return CommonMethod.getReturnMessageError("团队总结已存在");
            }
            ps = new PracticeSummary();
            ps.setProjectId(projectId);
            ps.setSummaryType(summaryType);
        }

        ps.setPersonId("PERSONAL".equals(summaryType) ? personId : null);
        String personName = "";
        Optional<Person> pop = personRepository.findById(personId);
        if (pop.isPresent()) personName = pop.get().getName();
        ps.setPersonName(personName);
        ps.setTitle(title);
        ps.setContent(content != null ? content : "");
        ps.setSubmitTime(now);
        ps.setStatus("SUBMIT".equals(action) ? "SUBMITTED" : "DRAFT");

        summaryRepository.save(ps);
        return CommonMethod.getReturnMessageOK("SUBMIT".equals(action) ? "总结已提交" : "草稿已保存");
    }

    /**
     * 学生端：获取项目状态摘要（用于列表页快速显示）
     */
    private Map<String, Object> buildProjectSummary(PracticeProject p, List<PracticeMember> members) {
        Map<String, Object> m = new HashMap<>();
        m.put("projectId", p.getProjectId());
        m.put("title", p.getTitle());
        m.put("projectType", p.getProjectType());
        m.put("status", p.getStatus());
        m.put("leaderName", p.getLeaderName());
        m.put("mentor", p.getMentor());
        m.put("startDate", p.getStartDate());
        m.put("endDate", p.getEndDate());
        m.put("createTime", p.getCreateTime());
        m.put("submitTime", p.getSubmitTime());
        m.put("memberCount", members.size());

        List<String> memberNames = new ArrayList<>();
        for (PracticeMember pm : members) {
            memberNames.add(pm.getPersonName());
        }
        m.put("memberNames", String.join("、", memberNames));

        String statusDisplay;
        switch (p.getStatus()) {
            case "DRAFT" -> statusDisplay = "草稿";
            case "SUBMITTED" -> statusDisplay = "待审核";
            case "APPROVED" -> statusDisplay = "已通过";
            case "REJECTED" -> statusDisplay = "已驳回";
            case "IN_PROGRESS" -> statusDisplay = "进行中";
            case "COMPLETED" -> statusDisplay = "已完成";
            case "SUMMARY_SUBMITTED" -> statusDisplay = "待审核总结";
            case "FINISHED" -> statusDisplay = "已结束";
            default -> statusDisplay = p.getStatus();
        }
        m.put("statusDisplay", statusDisplay);
        return m;
    }

    // ==================== 教师/管理员端 ====================

    /**
     * 教师/管理员端：获取审核项目列表
     */
    public DataResponse getReviewProjectList(DataRequest dataRequest) {
        String status = dataRequest.getString("status");
        String projectType = dataRequest.getString("projectType");
        if (status == null) status = "";
        if (projectType == null) projectType = "";

        List<PracticeProject> projects;
        if (projectType.isEmpty()) {
            projects = projectRepository.findByStatus(status);
        } else {
            projects = projectRepository.findByStatusAndProjectType(status, projectType);
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (PracticeProject p : projects) {
            List<PracticeMember> members = memberRepository.findByProjectId(p.getProjectId());
            Map<String, Object> m = buildProjectSummary(p, members);
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 教师/管理员端：审核项目
     */
    public DataResponse reviewProject(DataRequest dataRequest) {
        Integer projectId = dataRequest.getInteger("projectId");
        String reviewResult = dataRequest.getString("reviewResult"); // APPROVED / REJECTED
        String comment = dataRequest.getString("comment");
        Integer reviewerId = CommonMethod.getPersonId();

        Optional<PracticeProject> op = projectRepository.findById(projectId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("项目不存在");
        PracticeProject p = op.get();

        if (!"SUBMITTED".equals(p.getStatus()))
            return CommonMethod.getReturnMessageError("项目状态不可审核");

        String now = DateTimeTool.parseDateTime(new Date());
        String reviewerName = "";
        Optional<Person> pop = personRepository.findById(reviewerId);
        if (pop.isPresent()) reviewerName = pop.get().getName();

        PracticeReview review = new PracticeReview();
        review.setProjectId(projectId);
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setReviewType("PROJECT");
        review.setReviewResult(reviewResult);
        review.setComment(comment);
        review.setReviewTime(now);
        reviewRepository.save(review);

        if ("APPROVED".equals(reviewResult)) {
            p.setStatus("APPROVED");
            p.setApproveTime(now);
            p.setRejectReason(null);
        } else {
            p.setStatus("REJECTED");
            p.setRejectReason(comment);
        }
        projectRepository.save(p);

        return CommonMethod.getReturnMessageOK("APPROVED".equals(reviewResult) ? "已通过" : "已驳回");
    }

    /**
     * 教师/管理员端：审核总结
     */
    public DataResponse reviewSummary(DataRequest dataRequest) {
        Integer summaryId = dataRequest.getInteger("summaryId");
        String reviewResult = dataRequest.getString("reviewResult"); // APPROVED / REJECTED
        String comment = dataRequest.getString("comment");
        Integer reviewerId = CommonMethod.getPersonId();

        Optional<PracticeSummary> sop = summaryRepository.findById(summaryId);
        if (sop.isEmpty())
            return CommonMethod.getReturnMessageError("总结不存在");
        PracticeSummary ps = sop.get();

        if (!"SUBMITTED".equals(ps.getStatus()))
            return CommonMethod.getReturnMessageError("总结状态不可审核");

        Optional<PracticeProject> op = projectRepository.findById(ps.getProjectId());
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("项目不存在");
        PracticeProject p = op.get();

        String now = DateTimeTool.parseDateTime(new Date());
        String reviewerName = "";
        Optional<Person> pop = personRepository.findById(reviewerId);
        if (pop.isPresent()) reviewerName = pop.get().getName();

        PracticeReview review = new PracticeReview();
        review.setProjectId(ps.getProjectId());
        review.setSummaryId(summaryId);
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setReviewType("SUMMARY");
        review.setReviewResult(reviewResult);
        review.setComment(comment);
        review.setReviewTime(now);
        reviewRepository.save(review);

        ps.setStatus(reviewResult);
        summaryRepository.save(ps);

        // 检查是否所有总结都已审核通过
        if ("APPROVED".equals(reviewResult)) {
            List<PracticeSummary> allSummaries = summaryRepository.findByProjectId(p.getProjectId());
            boolean allApproved = allSummaries.stream().allMatch(s -> "APPROVED".equals(s.getStatus()));
            if (allApproved && allSummaries.size() >= 2) { // 至少个人+团队
                p.setStatus("FINISHED");
                projectRepository.save(p);
            }
        }

        return CommonMethod.getReturnMessageOK("APPROVED".equals(reviewResult) ? "已通过" : "已驳回");
    }

    /**
     * 教师/管理员端：更新项目状态（进行中→已完成）
     */
    public DataResponse updateProjectStatus(DataRequest dataRequest) {
        Integer projectId = dataRequest.getInteger("projectId");
        String newStatus = dataRequest.getString("status"); // IN_PROGRESS / COMPLETED

        Optional<PracticeProject> op = projectRepository.findById(projectId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("项目不存在");
        PracticeProject p = op.get();

        if ("IN_PROGRESS".equals(newStatus) && "APPROVED".equals(p.getStatus())) {
            p.setStatus("IN_PROGRESS");
            projectRepository.save(p);
            return CommonMethod.getReturnMessageOK("已设为进行中");
        }
        if ("COMPLETED".equals(newStatus) && "IN_PROGRESS".equals(p.getStatus())) {
            p.setStatus("COMPLETED");
            projectRepository.save(p);
            return CommonMethod.getReturnMessageOK("已设为已完成");
        }
        return CommonMethod.getReturnMessageError("状态流转不合法");
    }

    // ==================== 重点选题管理（教师/管理员） ====================

    /**
     * 教师/管理员端：获取重点选题列表
     */
    public DataResponse getKeyTopicList(DataRequest dataRequest) {
        Integer personId = CommonMethod.getPersonId();
        String status = dataRequest.getString("status");
        if (status == null) status = "";

        List<KeyTopic> list = keyTopicRepository.findByCreatorIdAndStatus(personId, status);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (KeyTopic kt : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("keyTopicId", kt.getKeyTopicId());
            m.put("title", kt.getTitle());
            m.put("description", kt.getDescription());
            m.put("requirements", kt.getRequirements());
            m.put("status", kt.getStatus());
            m.put("createTime", kt.getCreateTime());
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 教师/管理员端：保存/发布重点选题
     */
    public DataResponse saveKeyTopic(DataRequest dataRequest) {
        Integer keyTopicId = dataRequest.getInteger("keyTopicId");
        String action = dataRequest.getString("action"); // DRAFT / PUBLISH
        String title = dataRequest.getString("title");
        String description = dataRequest.getString("description");
        String requirements = dataRequest.getString("requirements");

        if (title == null || title.isEmpty())
            return CommonMethod.getReturnMessageError("标题不能为空");

        Integer personId = CommonMethod.getPersonId();
        String now = DateTimeTool.parseDateTime(new Date());
        String personName = "";
        Optional<Person> pop = personRepository.findById(personId);
        if (pop.isPresent()) personName = pop.get().getName();

        KeyTopic kt;
        boolean isNew = keyTopicId == null || keyTopicId == 0;
        if (isNew) {
            kt = new KeyTopic();
            kt.setCreatorId(personId);
            kt.setCreatorName(personName);
            kt.setCreateTime(now);
        } else {
            Optional<KeyTopic> op = keyTopicRepository.findById(keyTopicId);
            if (op.isEmpty())
                return CommonMethod.getReturnMessageError("选题不存在");
            kt = op.get();
            if (!"DRAFT".equals(kt.getStatus()))
                return CommonMethod.getReturnMessageError("仅草稿可编辑");
        }

        kt.setTitle(title);
        kt.setDescription(description != null ? description : "");
        kt.setRequirements(requirements != null ? requirements : "");
        kt.setStatus("PUBLISH".equals(action) ? "PUBLISHED" : "DRAFT");
        keyTopicRepository.save(kt);

        return CommonMethod.getReturnData(kt.getKeyTopicId(), isNew ? "创建成功" : "更新成功");
    }

    /**
     * 教师/管理员端：撤回重点选题
     */
    public DataResponse withdrawKeyTopic(DataRequest dataRequest) {
        Integer keyTopicId = dataRequest.getInteger("keyTopicId");
        Optional<KeyTopic> op = keyTopicRepository.findById(keyTopicId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("选题不存在");
        KeyTopic kt = op.get();
        if (!"PUBLISHED".equals(kt.getStatus()))
            return CommonMethod.getReturnMessageError("仅已发布的选题可撤回");

        kt.setStatus("WITHDRAWN");
        keyTopicRepository.save(kt);
        return CommonMethod.getReturnMessageOK("已撤回");
    }
}
