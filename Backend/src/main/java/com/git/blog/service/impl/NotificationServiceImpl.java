package com.git.blog.service.impl;

import com.git.blog.entity.Notification;
import com.git.blog.mapper.NotificationMapper;
import com.git.blog.service.INotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

}
