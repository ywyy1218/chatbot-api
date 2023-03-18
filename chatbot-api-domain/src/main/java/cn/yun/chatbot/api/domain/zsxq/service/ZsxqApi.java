package cn.yun.chatbot.api.domain.zsxq.service;

import cn.yun.chatbot.api.domain.zsxq.IZsxqApi;
import cn.yun.chatbot.api.domain.zsxq.model.aggregates.UnAnsweredQuestionsAggregates;
import cn.yun.chatbot.api.domain.zsxq.model.req.AnswerReq;
import cn.yun.chatbot.api.domain.zsxq.model.req.ReqData;
import cn.yun.chatbot.api.domain.zsxq.model.res.AnswerRes;
import com.alibaba.fastjson.JSON;
import net.sf.json.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ZsxqApi implements IZsxqApi {

    private Logger logger = LoggerFactory.getLogger(ZsxqApi.class);

    @Override
    public UnAnsweredQuestionsAggregates queryUnAnsweredQuestionsTopicId(String groupId, String cookie) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        //要爬取的网站
        HttpGet get = new HttpGet("https://api.zsxq.com/v2/groups/"+groupId+"/topics?scope=all&count=20");
        //cookie信息
        get.addHeader("cookie", cookie);
        //类型 accept
        get.addHeader("Content-Type","application/json;charset=utf8");
        //执行get请求
        CloseableHttpResponse response = httpClient.execute(get);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            String jsonStr = EntityUtils.toString(response.getEntity());
            logger.info("拉取问题数据。groupId:{} jsonStr{}", groupId, jsonStr);
            return JSON.parseObject(jsonStr,UnAnsweredQuestionsAggregates.class);
        }else{
            throw new RuntimeException("queryUnAnsweredQuestionsTopicId Err Code is"+response.getStatusLine().getStatusCode());
        }
    }

    @Override
    public boolean answer(String groupId, String cookie, String topicId, String test, boolean silenced) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        //回答的地址
        HttpPost post = new HttpPost("https://api.zsxq.com/v2/topics/"+topicId+"/answer");
        //cookie信息
        post.addHeader("cookie", cookie);
        //类型 accept
        post.addHeader("Content-Type","application/json;charset=utf8");
        //我这个信息是从浏览器过去的  标头里面
        post.addHeader("user-agent","");

        //组装数据信息，才能去回答我们的数据
        //载荷
        /*String paramJson = "{\n" +
                "  \"req_data\": {\n" +
                "    \"text\": \"自己去百度！\\n\",\n" +
                "    \"image_ids\": [],\n" +
                "    \"silenced\": false\n" +
                "  }\n" +
                "}";*/

        AnswerReq answerReq = new AnswerReq(new ReqData(test,silenced));
        String paramJson = JSONObject.fromObject(answerReq).toString();
        //封装入参对象
        StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json","utf-8"));
        //把参数加进来，实体对象
        post.setEntity(stringEntity);

        CloseableHttpResponse response = httpClient.execute(post);
        //处理这个回馈信息
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            String jsonStr = EntityUtils.toString(response.getEntity());
            //日志
            logger.info("回答问题结果。groupId:{} topicId{} jsonStr{}", groupId, topicId, jsonStr);
            AnswerRes answerRes = JSON.parseObject(jsonStr, AnswerRes.class);
            return answerRes.isSucceed();
        }else{
            throw new RuntimeException("answer Err Code is"+response.getStatusLine().getStatusCode());
        }
    }
}
