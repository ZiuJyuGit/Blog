package com.git.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.git.blog.entity.Message;
import com.git.blog.mapper.MessageMapper;
import com.git.blog.service.IMessageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    private final MessageMapper messageMapper;

    //存放迭代找出的所有子代的集合
    private List<Message> tempReplys = new ArrayList<>();

    private List<Integer> deleteReplys = new ArrayList<>();

    public MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public List<Message> getMessages(Integer page) {
        Page<Message> messagePage = new Page<>(page, 10);
        Page<Message> messageFromDB = messageMapper.selectPage(messagePage, new QueryWrapper<Message>()
                .eq("message_parent_id", -1).orderByAsc("message_leave_date"));
        List<Message> messages = messageFromDB.getRecords();
        for (Message message : messages) {
            Integer messageId = message.getMessageId();
            String messageUsername = message.getMessageUsername();
            //递归查询出子评论
            recursively(messageId, messageUsername);
            message.setReplyMessages(tempReplys);
            tempReplys = new ArrayList<>();
        }
        return messages;
    }

    private void recursively(Integer parentId, String parentNickname) {
        //根据子一级留言的id找到子二级留言
        List<Message> childMessages = messageMapper.selectList(new QueryWrapper<Message>()
                .eq("message_parent_id", parentId).orderByAsc("message_leave_date"));
        if (childMessages.size() > 0) {
            for (Message ChildMessage : childMessages) {
                String messageUsername = ChildMessage.getMessageUsername();
                ChildMessage.setParentNickname(parentNickname);
                tempReplys.add(ChildMessage);
                Integer messageId = ChildMessage.getMessageId();
                recursively(messageId, messageUsername);
            }
        }
    }

    @Override
    public Boolean deleteMessage(Integer messageId) {
        deleteReplys.add(messageId);
        getChildrenRecursively(messageId);
        if (messageMapper.deleteBatchIds(deleteReplys) > 0) {
            deleteReplys = new ArrayList<>();
            return true;
        }
        return false;
    }

    private void getChildrenRecursively(Integer parentId) {
        List<Message> childMessages = messageMapper.selectList(new QueryWrapper<Message>()
                .eq("message_parent_id", parentId));
        if (childMessages.size() > 0) {
            for (Message childMessage : childMessages) {
                Integer messageId = childMessage.getMessageId();
                deleteReplys.add(messageId);
                getChildrenRecursively(messageId);
            }
        }
    }
}
