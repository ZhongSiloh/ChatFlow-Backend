package com.zhongxilin.ChatFlow.service;

import com.zhongxilin.ChatFlow.pojo.ChatMessageRecord;
import java.util.List;

public interface MessageRecordService {
    List<ChatMessageRecord> getChatRecordLists(Integer conversationType, Long conversationId, Long senderId, Long recipientId);
}
