package com.zhongxilin.ChatFlow.service.Impl;

import com.zhongxilin.ChatFlow.mapper.MessageRecordMapper;
import com.zhongxilin.ChatFlow.service.MessageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zhongxilin.ChatFlow.pojo.ChatMessageRecord;
import java.util.List;

@Service
public class MessageRecordServiceImpl implements MessageRecordService {

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Override
    public List<ChatMessageRecord> getChatRecordLists(Integer conversationType, Long conversationId, Long senderId, Long recipientId) {
        return messageRecordMapper.selectPrivateMessages(conversationType, conversationId, senderId, recipientId);
    }
}
