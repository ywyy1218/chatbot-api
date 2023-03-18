package cn.yun.chatbot.api.domain.zsxq;

import cn.yun.chatbot.api.domain.zsxq.model.aggregates.UnAnsweredQuestionsAggregates;

import java.io.IOException;

//知识星球 API 接口
public interface IZsxqApi {
    UnAnsweredQuestionsAggregates queryUnAnsweredQuestionsTopicId(String groupId, String cookie) throws IOException;

    boolean answer(String groupId, String cookie, String topicId, String test, boolean silenced)throws IOException;
}
