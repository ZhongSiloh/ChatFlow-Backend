package com.zhongxilin.ChatFlow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.zhongxilin.ChatFlow.pojo.ChatMessageRecord;
import java.util.List;

@Mapper
public interface MessageRecordMapper {
    List<ChatMessageRecord> selectPrivateMessages(@Param("conversationType") Integer conversationType,
                                                   @Param("conversationId") Long conversationId,
                                                   @Param("senderId") Long senderId,
                                                   @Param("recipientId") Long recipientId);
}
