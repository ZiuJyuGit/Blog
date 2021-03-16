package com.git.blog.service;

import com.git.blog.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
public interface IMessageService extends IService<Message> {

    List<Message> getMessages(Integer page);
    
    Boolean deleteMessage(Integer messageId);

}
