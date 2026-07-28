package com.alibaba.ticketsystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AIService {

    @Autowired
    private ChatClient chatClient;

    // 调用AI接口
   public String callAI(String promt){
       log.info("[AI-Agent] 发送请求，prompt长度: {}", promt.length());
       String result = chatClient.prompt().user(promt).call().content();
       log.info("[AI-Agent] 收到回复，response长度: {}", result.length());
       return result;
   }

   //根据description来判断工单分类
    public String classify(String description){
        String promt = """
                你是一个意图识别助手。请根据用户的消息判断用户的意图类别。
                可选类别：REFUND(退款退货), LOGISTICS(物流异常), DAMAGE(商品破损), INVOICE(发票问题), CONSULT(咨询), COMPLAINT(投诉)
                请只返回类别英文标识，不要返回其他内容。
                用户消息：""" + description;

        try{
            log.info("[AI-Agent] 发送请求，prompt长度: {}", promt.length());
            String result = chatClient.prompt().user(promt).call().content();
            log.info("[AI-Agent] 收到回复，response长度: {}", result.length());
            return result;
        } catch (Exception e) {
//            throw new RuntimeException(e);
            return "CONSULT";
        }
    }

    //核心业务 ：根据工单描述，用AI来回复消息
    public String processTicket(Long ticketId, String description, Long userId){
        // 构造Agent模式的Prompt，包含工单上下文和用户消息
        String agentPrompt = String.format("""
                当前工单信息：
                - 工单ID: %d
                - 用户ID: %d

                用户消息: %s

                请分析用户诉求，根据需要调用工具查询相关信息（订单、用户信誉、售后策略等），
                然后给出专业的处理建议和回复。

                注意：
                - 如果是退货退款类问题，请先查询订单和售后策略
                - 根据订单金额和用户信誉分判断是否可自动处理
                - 金额<50元且信誉≥80分可建议自动通过
                - 金额≥50元建议转人工审核
                - 如果用户要求加急，可自主调用工具更新优先级为HIGH
                """, ticketId, userId, description);
        try{
            String reply = callAI(agentPrompt);
            return reply;
        }catch (Exception e){
            return "抱歉，AI服务暂时不可用，已为您转交人工处理。";
        }

    }
}
