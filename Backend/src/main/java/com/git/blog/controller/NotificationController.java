package com.git.blog.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.git.blog.common.function.CommonFunction;
import com.git.blog.common.response.Result;
import com.git.blog.entity.Blog;
import com.git.blog.entity.Notification;
import com.git.blog.service.IBlogService;
import com.git.blog.service.INotificationService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@RestController
public class NotificationController {

    private final INotificationService iNotificationService;

    private final IBlogService iBlogService;

    private final CommonFunction commonFunction;

    public NotificationController(INotificationService iNotificationService, CommonFunction commonFunction,
                                  IBlogService iBlogService) {
        this.iNotificationService = iNotificationService;
        this.commonFunction = commonFunction;
        this.iBlogService = iBlogService;
    }

    /**
     * 查询通知消息
     * @param page 分页查询
     * @param request 权限验证
     * @return 返回查询通知结果
     */
    @GetMapping("/notification")
    public Result getNotifications(@RequestParam(defaultValue = "1") Integer page, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        Page<Notification> notificationPage = new Page<>(page, 10);
        Page<Notification> notificationsPage = iNotificationService.page(notificationPage,
                new QueryWrapper<Notification>().orderByDesc("notification_leave_date"));
        List<Notification> notifications = notificationsPage.getRecords();
        for (Notification notification : notifications) {
            if (notification.getNotificationFrom() != 0) {
                Blog blog = iBlogService.getOne(new QueryWrapper<Blog>().eq("blog_id", notification.getNotificationFrom())
                .select("blog_title"));
                notification.setNotificationBlogName(blog.getBlogTitle());
            }
        }
        return Result.success(notifications);
    }

    /**
     * 删除一条通知信息（已读）
     * @param notificationId 待删除通知id
     * @param request 权限验证
     * @return 返回删除结果
     */
    @DeleteMapping("/notification/{notificationId}")
    public Result deleteANotification(@PathVariable("notificationId") Integer notificationId, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        if (iNotificationService.removeById(notificationId)) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    /**
     * 获得通知数量
     * @param request 权限校验
     * @return 返回通知数量
     */
    @GetMapping("/notificationCount")
    public Result getNotificationCount(HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        int count = iNotificationService.count();
        return Result.success(count);
    }
}
