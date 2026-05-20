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
public class QuestionnaireService {
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;

    public QuestionnaireService(QuestionnaireRepository questionnaireRepository,
                                QuestionRepository questionRepository,
                                QuestionOptionRepository questionOptionRepository,
                                ResponseRepository responseRepository,
                                AnswerRepository answerRepository,
                                StudentRepository studentRepository,
                                PersonRepository personRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.studentRepository = studentRepository;
        this.personRepository = personRepository;
    }

    /**
     * 学生端：获取问卷列表
     */
    public DataResponse getQuestionnaireList(DataRequest dataRequest) {
        Integer personId = CommonMethod.getPersonId();
        String filter = dataRequest.getString("filter");   // TODO / DONE / ALL

        List<Questionnaire> publishedList = questionnaireRepository.findByStatusOrderByPublishTimeDesc("PUBLISHED");
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Questionnaire q : publishedList) {
            boolean hasSubmitted = responseRepository.findByQuestionnaireIdAndPersonId(q.getQuestionnaireId(), personId).isPresent();
            boolean isExpired = false;
            if (q.getDeadline() != null && !q.getDeadline().isEmpty() && !q.getAllowLate()) {
                String now = DateTimeTool.parseDateTime(new Date());
                isExpired = now.compareTo(q.getDeadline()) > 0;
            }

            if ("DONE".equals(filter) && !hasSubmitted) continue;
            if ("TODO".equals(filter) && hasSubmitted) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("questionnaireId", q.getQuestionnaireId());
            m.put("title", q.getTitle());
            m.put("anonymous", q.getAnonymous());
            m.put("deadline", q.getDeadline());
            m.put("status", q.getStatus());
            m.put("creatorName", q.getCreatorName());
            m.put("questionCount", questionRepository.findByQuestionnaireIdOrderBySortOrder(q.getQuestionnaireId()).size());
            m.put("hasSubmitted", hasSubmitted);
            m.put("isExpired", isExpired);
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 学生端：获取问卷详情（含题目和选项）
     */
    public DataResponse getQuestionnaireDetail(DataRequest dataRequest) {
        Integer questionnaireId = dataRequest.getInteger("questionnaireId");

        Optional<Questionnaire> op = questionnaireRepository.findById(questionnaireId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("问卷不存在");
        Questionnaire q = op.get();

        if (!"PUBLISHED".equals(q.getStatus()))
            return CommonMethod.getReturnMessageError("问卷不可查看");

        List<Question> questions = questionRepository.findByQuestionnaireIdOrderBySortOrder(questionnaireId);
        List<Map<String, Object>> questionList = new ArrayList<>();

        for (Question question : questions) {
            Map<String, Object> qm = new HashMap<>();
            qm.put("questionId", question.getQuestionId());
            qm.put("questionType", question.getQuestionType());
            qm.put("title", question.getTitle());
            qm.put("required", question.getRequired());
            qm.put("sortOrder", question.getSortOrder());

            if ("SCALE".equals(question.getQuestionType())) {
                qm.put("scaleMin", question.getScaleMin());
                qm.put("scaleMax", question.getScaleMax());
            }

            if ("SINGLE".equals(question.getQuestionType()) || "MULTI".equals(question.getQuestionType())) {
                List<QuestionOption> opts = questionOptionRepository.findByQuestionIdOrderBySortOrder(question.getQuestionId());
                List<Map<String, Object>> optionList = new ArrayList<>();
                for (QuestionOption opt : opts) {
                    Map<String, Object> om = new HashMap<>();
                    om.put("optionId", opt.getOptionId());
                    om.put("content", opt.getContent());
                    om.put("sortOrder", opt.getSortOrder());
                    optionList.add(om);
                }
                qm.put("options", optionList);
            }
            questionList.add(qm);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("questionnaireId", q.getQuestionnaireId());
        data.put("title", q.getTitle());
        data.put("description", q.getDescription());
        data.put("anonymous", q.getAnonymous());
        data.put("deadline", q.getDeadline());
        data.put("allowLate", q.getAllowLate());
        data.put("allowModify", q.getAllowModify());
        data.put("questions", questionList);
        return CommonMethod.getReturnData(data);
    }

    /**
     * 学生端：提交回答
     */
    public DataResponse submitResponse(DataRequest dataRequest) {
        Integer questionnaireId = dataRequest.getInteger("questionnaireId");
        Integer responseId = dataRequest.getInteger("responseId");
        Integer personId = CommonMethod.getPersonId();

        Optional<Questionnaire> op = questionnaireRepository.findById(questionnaireId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("问卷不存在");
        Questionnaire q = op.get();

        if (!"PUBLISHED".equals(q.getStatus()))
            return CommonMethod.getReturnMessageError("问卷不可提交");

        // 检查截止时间
        String now = DateTimeTool.parseDateTime(new Date());
        boolean isLate = false;
        if (q.getDeadline() != null && !q.getDeadline().isEmpty() && now.compareTo(q.getDeadline()) > 0) {
            if (!q.getAllowLate())
                return CommonMethod.getReturnMessageError("问卷已截止");
            isLate = true;
        }

        Response response;
        if (responseId != null && responseId > 0) {
            // 修改已有回答
            Optional<Response> respOp = responseRepository.findById(responseId);
            if (respOp.isEmpty())
                return CommonMethod.getReturnMessageError("回答记录不存在");
            response = respOp.get();
            if (!response.getQuestionnaireId().equals(questionnaireId))
                return CommonMethod.getReturnMessageError("回答记录不匹配");
            if (!q.getAllowModify())
                return CommonMethod.getReturnMessageError("不允许修改回答");
            // 删除旧答案
            answerRepository.deleteByResponseId(responseId);
        } else {
            // 检查是否已提交（不允许重复提交，除非 allowModify）
            Optional<Response> existResp = responseRepository.findByQuestionnaireIdAndPersonId(questionnaireId, personId);
            if (existResp.isPresent()) {
                if (!q.getAllowModify())
                    return CommonMethod.getReturnMessageError("您已提交过该问卷");
                response = existResp.get();
                answerRepository.deleteByResponseId(response.getResponseId());
            } else {
                response = new Response();
                response.setQuestionnaireId(questionnaireId);
                response.setPersonId(q.getAnonymous() ? null : personId);
                response.setSubmitTime(now);
            }
        }

        response.setIsLate(isLate);
        response.setSubmitTime(now);
        responseRepository.save(response);

        // 保存答案
        List<?> rawAnswers = dataRequest.getList("answers");
        for (Object obj : rawAnswers) {
            if (!(obj instanceof Map)) continue;
            Map<String, Object> ansMap = (Map<String, Object>) obj;
            Answer answer = new Answer();
            answer.setResponseId(response.getResponseId());
            answer.setQuestionId(CommonMethod.getInteger(ansMap, "questionId"));
            answer.setAnswerValue(CommonMethod.getString(ansMap, "answerValue"));
            answerRepository.save(answer);
        }

        return CommonMethod.getReturnMessageOK("提交成功");
    }

    /**
     * 学生端：获取我的回答（回显用）
     */
    public DataResponse getMyResponse(DataRequest dataRequest) {
        Integer questionnaireId = dataRequest.getInteger("questionnaireId");
        Integer personId = CommonMethod.getPersonId();

        Optional<Response> op = responseRepository.findByQuestionnaireIdAndPersonId(questionnaireId, personId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("您尚未提交该问卷");

        Response response = op.get();
        List<Answer> answers = answerRepository.findByResponseId(response.getResponseId());

        List<Map<String, Object>> answerList = new ArrayList<>();
        for (Answer ans : answers) {
            Map<String, Object> am = new HashMap<>();
            am.put("questionId", ans.getQuestionId());
            am.put("answerValue", ans.getAnswerValue());

            Optional<Question> qOp = questionRepository.findById(ans.getQuestionId());
            qOp.ifPresent(question -> am.put("questionType", question.getQuestionType()));
            answerList.add(am);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("responseId", response.getResponseId());
        data.put("submitTime", response.getSubmitTime());
        data.put("isLate", response.getIsLate());
        data.put("answers", answerList);
        return CommonMethod.getReturnData(data);
    }

    /**
     * 学生端：撤回我的回答
     */
    public DataResponse deleteMyResponse(DataRequest dataRequest) {
        Integer responseId = dataRequest.getInteger("responseId");
        Integer personId = CommonMethod.getPersonId();

        Optional<Response> op = responseRepository.findById(responseId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("回答记录不存在");
        Response response = op.get();

        // 验证归属
        if (response.getPersonId() != null && !response.getPersonId().equals(personId))
            return CommonMethod.getReturnMessageError("无权操作");

        // 匿名问卷无法验证归属，拒绝删除
        if (response.getPersonId() == null)
            return CommonMethod.getReturnMessageError("匿名问卷不支持撤回");

        Optional<Questionnaire> qOp = questionnaireRepository.findById(response.getQuestionnaireId());
        if (qOp.isEmpty())
            return CommonMethod.getReturnMessageError("问卷不存在");
        if (!qOp.get().getAllowModify())
            return CommonMethod.getReturnMessageError("该问卷不允许撤回");

        answerRepository.deleteByResponseId(responseId);
        responseRepository.delete(response);
        return CommonMethod.getReturnMessageOK("已撤回");
    }

    /**
     * 发布者端：获取我创建的问卷列表
     */
    public DataResponse getMyQuestionnaireList(DataRequest dataRequest) {
        Integer personId = CommonMethod.getPersonId();
        String status = dataRequest.getString("status");
        if (status == null) status = "";

        List<Questionnaire> list = questionnaireRepository.findByCreatorIdAndStatus(personId, status);
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Questionnaire q : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("questionnaireId", q.getQuestionnaireId());
            m.put("title", q.getTitle());
            m.put("status", q.getStatus());
            m.put("deadline", q.getDeadline());
            m.put("answerCount", responseRepository.countByQuestionnaireId(q.getQuestionnaireId()));
            m.put("publishTime", q.getPublishTime());
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    }

    /**
     * 发布者端：保存/发布问卷
     */
    public DataResponse saveQuestionnaire(DataRequest dataRequest) {
        Integer questionnaireId = dataRequest.getInteger("questionnaireId");
        String action = dataRequest.getString("action");        // DRAFT / PUBLISH
        String title = dataRequest.getString("title");
        String description = dataRequest.getString("description");
        Boolean anonymous = dataRequest.getBoolean("anonymous");
        String deadline = dataRequest.getString("deadline");
        Boolean allowLate = dataRequest.getBoolean("allowLate");
        Boolean allowModify = dataRequest.getBoolean("allowModify");
        List<?> rawQuestions = dataRequest.getList("questions");

        if (title == null || title.isEmpty())
            return CommonMethod.getReturnMessageError("标题不能为空");

        Integer personId = CommonMethod.getPersonId();
        String personName = "";
        Optional<Person> personOp = personRepository.findById(personId);
        if (personOp.isPresent())
            personName = personOp.get().getName();

        String now = DateTimeTool.parseDateTime(new Date());

        Questionnaire q;
        boolean isNew = questionnaireId == null || questionnaireId == 0;
        if (isNew) {
            q = new Questionnaire();
            q.setCreatorId(personId);
            q.setCreatorName(personName);
            q.setCreateTime(now);
        } else {
            Optional<Questionnaire> op = questionnaireRepository.findById(questionnaireId);
            if (op.isEmpty())
                return CommonMethod.getReturnMessageError("问卷不存在");
            q = op.get();
            if (!"DRAFT".equals(q.getStatus()))
                return CommonMethod.getReturnMessageError("仅草稿可编辑");
        }

        q.setTitle(title);
        q.setDescription(description != null ? description : "");
        q.setAnonymous(anonymous != null && anonymous);
        q.setDeadline(deadline);
        q.setAllowLate(allowLate != null && allowLate);
        q.setAllowModify(allowModify != null && allowModify);
        q.setStatus("DRAFT".equals(action) ? "DRAFT" : "PUBLISHED");
        if ("PUBLISH".equals(action))
            q.setPublishTime(now);

        questionnaireRepository.save(q);

        // 保存题目（先删旧的再新增）
        List<Question> oldQuestions = questionRepository.findByQuestionnaireIdOrderBySortOrder(q.getQuestionnaireId());
        for (Question oldQ : oldQuestions) {
            questionOptionRepository.deleteByQuestionId(oldQ.getQuestionId());
        }
        questionRepository.deleteByQuestionnaireId(q.getQuestionnaireId());

        if (rawQuestions != null) {
            for (Object obj : rawQuestions) {
                if (!(obj instanceof Map)) continue;
                Map<String, Object> qm = (Map<String, Object>) obj;

                Question question = new Question();
                question.setQuestionnaireId(q.getQuestionnaireId());
                question.setQuestionType(CommonMethod.getString(qm, "questionType"));
                question.setTitle(CommonMethod.getString(qm, "title"));
                question.setRequired(CommonMethod.getBoolean(qm, "required"));
                question.setSortOrder(CommonMethod.getInteger(qm, "sortOrder"));
                question.setScaleMin(CommonMethod.getInteger(qm, "scaleMin"));
                question.setScaleMax(CommonMethod.getInteger(qm, "scaleMax"));
                questionRepository.save(question);

                // 保存选项
                List<?> rawOptions = CommonMethod.getList(qm, "options");
                for (Object optObj : rawOptions) {
                    if (!(optObj instanceof Map)) continue;
                    Map<String, Object> om = (Map<String, Object>) optObj;
                    QuestionOption option = new QuestionOption();
                    option.setQuestionId(question.getQuestionId());
                    option.setContent(CommonMethod.getString(om, "content"));
                    option.setSortOrder(CommonMethod.getInteger(om, "sortOrder"));
                    questionOptionRepository.save(option);
                }
            }
        }

        return CommonMethod.getReturnData(q.getQuestionnaireId(), isNew ? "发布成功" : "更新成功");
    }

    /**
     * 发布者端：撤回问卷
     */
    public DataResponse withdrawQuestionnaire(DataRequest dataRequest) {
        Integer questionnaireId = dataRequest.getInteger("questionnaireId");
        Optional<Questionnaire> op = questionnaireRepository.findById(questionnaireId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("问卷不存在");
        Questionnaire q = op.get();
        if (!"PUBLISHED".equals(q.getStatus()))
            return CommonMethod.getReturnMessageError("仅已发布的问卷可撤回");

        q.setStatus("WITHDRAWN");
        questionnaireRepository.save(q);

        // 删除所有回答
        List<Response> responses = responseRepository.findByQuestionnaireId(questionnaireId);
        for (Response r : responses) {
            answerRepository.deleteByResponseId(r.getResponseId());
        }
        responseRepository.deleteByQuestionnaireId(questionnaireId);

        return CommonMethod.getReturnMessageOK("已撤回");
    }

    /**
     * 发布者端：获取问卷统计
     */
    public DataResponse getStatistics(DataRequest dataRequest) {
        Integer questionnaireId = dataRequest.getInteger("questionnaireId");

        Optional<Questionnaire> op = questionnaireRepository.findById(questionnaireId);
        if (op.isEmpty())
            return CommonMethod.getReturnMessageError("问卷不存在");

        List<Student> allStudents = studentRepository.findAll();
        long totalTarget = allStudents.size();
        long submitCount = responseRepository.countByQuestionnaireId(questionnaireId);
        String submitRate = totalTarget > 0
                ? String.format("%.1f%%", submitCount * 100.0 / totalTarget)
                : "0%";

        List<Question> questions = questionRepository.findByQuestionnaireIdOrderBySortOrder(questionnaireId);
        List<Map<String, Object>> questionStatsList = new ArrayList<>();

        for (Question question : questions) {
            Map<String, Object> qs = new HashMap<>();
            qs.put("questionId", question.getQuestionId());
            qs.put("questionType", question.getQuestionType());
            qs.put("title", question.getTitle());

            switch (question.getQuestionType()) {
                case "SINGLE":
                case "MULTI":
                    qs.put("stats", buildOptionStats(question));
                    break;
                case "SCALE":
                    qs.put("stats", buildScaleStats(question, questionnaireId));
                    break;
                case "TEXT":
                    qs.put("stats", buildTextStats(question, questionnaireId));
                    break;
            }
            questionStatsList.add(qs);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalTarget", totalTarget);
        data.put("submitCount", submitCount);
        data.put("submitRate", submitRate);
        data.put("questions", questionStatsList);
        return CommonMethod.getReturnData(data);
    }

    // ========== 统计辅助方法 ==========

    private List<Map<String, Object>> buildOptionStats(Question question) {
        List<QuestionOption> opts = questionOptionRepository.findByQuestionIdOrderBySortOrder(question.getQuestionId());
        List<Response> allResponses = responseRepository.findByQuestionnaireId(question.getQuestionnaireId());
        int total = allResponses.size();

        // 统计每个选项（单选按精确匹配，多选按逗号分隔包含）
        List<Map<String, Object>> result = new ArrayList<>();
        for (QuestionOption opt : opts) {
            int count = 0;
            for (Response r : allResponses) {
                List<Answer> answers = answerRepository.findByResponseId(r.getResponseId());
                for (Answer ans : answers) {
                    if (!ans.getQuestionId().equals(question.getQuestionId())) continue;
                    if ("MULTI".equals(question.getQuestionType())) {
                        String[] parts = ans.getAnswerValue().split(",");
                        for (String p : parts) {
                            if (p.trim().equals(String.valueOf(opt.getOptionId()))) {
                                count++;
                                break;
                            }
                        }
                    } else {
                        if (ans.getAnswerValue().trim().equals(String.valueOf(opt.getOptionId())))
                            count++;
                    }
                }
            }
            Map<String, Object> m = new HashMap<>();
            m.put("content", opt.getContent());
            m.put("count", count);
            m.put("rate", total > 0 ? String.format("%.1f%%", count * 100.0 / total) : "0%");
            result.add(m);
        }
        return result;
    }

    private Map<String, Object> buildScaleStats(Question question, Integer questionnaireId) {
        List<Response> responses = responseRepository.findByQuestionnaireId(questionnaireId);
        List<Integer> values = new ArrayList<>();
        for (Response r : responses) {
            List<Answer> answers = answerRepository.findByResponseId(r.getResponseId());
            for (Answer ans : answers) {
                if (!ans.getQuestionId().equals(question.getQuestionId())) continue;
                try {
                    values.add(Integer.parseInt(ans.getAnswerValue().trim()));
                } catch (NumberFormatException ignored) {}
            }
        }

        double avg = values.stream().mapToInt(Integer::intValue).average().orElse(0);
        int min = values.stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = values.stream().mapToInt(Integer::intValue).max().orElse(0);

        Map<String, Integer> distribution = new LinkedHashMap<>();
        int scaleMin = question.getScaleMin() != null ? question.getScaleMin() : 0;
        int scaleMax = question.getScaleMax() != null ? question.getScaleMax() : 10;
        for (int i = scaleMin; i <= scaleMax; i++) {
            final int v = i;
            distribution.put(String.valueOf(i), (int) values.stream().filter(x -> x == v).count());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("avg", Math.round(avg * 10.0) / 10.0);
        result.put("min", min);
        result.put("max", max);
        result.put("distribution", distribution);
        return result;
    }

    private Map<String, Object> buildTextStats(Question question, Integer questionnaireId) {
        List<Response> responses = responseRepository.findByQuestionnaireId(questionnaireId);
        List<String> textAnswers = new ArrayList<>();
        for (Response r : responses) {
            List<Answer> answers = answerRepository.findByResponseId(r.getResponseId());
            for (Answer ans : answers) {
                if (!ans.getQuestionId().equals(question.getQuestionId())) continue;
                if (ans.getAnswerValue() != null && !ans.getAnswerValue().isEmpty())
                    textAnswers.add(ans.getAnswerValue());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("answerCount", textAnswers.size());
        result.put("answers", textAnswers);
        return result;
    }
}
