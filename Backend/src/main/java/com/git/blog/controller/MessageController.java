package com.git.blog.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.git.blog.common.function.CommonFunction;
import com.git.blog.common.response.Result;
import com.git.blog.common.socket.WebSocket;
import com.git.blog.entity.Message;
import com.git.blog.entity.Notification;
import com.git.blog.service.IMessageService;
import com.git.blog.service.INotificationService;
import com.git.blog.utils.IPUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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
public class MessageController {

    private final IMessageService iMessageService;

    private final IPUtils ipUtils;

    private final INotificationService iNotificationService;

    private final CommonFunction commonFunction;

    public MessageController(IMessageService iMessageService, IPUtils ipUtils, INotificationService iNotificationService,
                             CommonFunction commonFunction) {
        this.iMessageService = iMessageService;
        this.ipUtils = ipUtils;
        this.iNotificationService = iNotificationService;
        this.commonFunction = commonFunction;
    }

    /**
     * 查询留言内容
     * @param page 分页查询，默认查询页数为1
     * @return 返回查询留言结果
     */
    @GetMapping("/message")
    public Result getMessages(@RequestParam(defaultValue = "1") Integer page) {
        List<Message> messages = iMessageService.getMessages(page);
            return Result.success(messages);
    }

    /**
     * 查询首次留言总数，用于前端分页展示时控制页数显示
     * @return 返回首次留言总数
     */
    @GetMapping("/messageCount")
    public Result getMessageCount() {
        int count = iMessageService.count(new QueryWrapper<Message>().eq("message_parent_id", -1));
        return Result.success(count);
    }

    /**
     * 查询所有留言数量，页面展示
     * @return 返回留言总数
     */
    @GetMapping("/allMessageCount")
    public Result getAllMessageCount() {
        int count = iMessageService.count();
        return Result.success(count);
    }

    /**
     * 新增一条留言
     * @param message 留言具体内容
     * @param request 判断留言来源，通过权限校验来源为博主
     * @return 返回新增结果
     */
    @PostMapping("/message")
    public Result addAMessage(@Validated @RequestBody Message message, HttpServletRequest request) {
        //若插入评论为子评论，检验留言板是否存在其父评论，防止调用接口存入不存在留言的子留言
        Integer messageParentId = message.getMessageParentId();
        if (messageParentId != -1) {
            Message parentMessage = iMessageService.getById(messageParentId);
            if (parentMessage == null) {
                throw new RuntimeException("添加留言出现了错误");
            }
        }
        //若请求头带有jwt，该留言来自博主,且非博主不可用博主专属头像
        if (commonFunction.checkPermission(request)) {
            message.setMessageFromBlogger(1);
        } else {
            if (message.getMessageAvatar() == 7) {
                throw new RuntimeException("非法头像");
            }
            message.setMessageFromBlogger(0);
        }
        message.setMessageLeaveDate(LocalDateTime.now().withNano(0));
        //设置ip，先从请求头获取ip，再按照ip查询其地址
        String ip = ipUtils.getIpFromRequest(request);
        String address = ipUtils.ip2Address(ip);
        message.setMessageAddress(address);
        if (iMessageService.save(message)) {
            //当留言为非博主留言&初次留言/回复博主留言时，该条评论需通知博主并将此消息存入通知表中
            if (message.getMessageFromBlogger() == 0 && (messageParentId == -1 ||
                    (iMessageService.getById(messageParentId).getMessageFromBlogger() == 1))) {
                Notification notification = new Notification();
                notification.setNotificationUsername(message.getMessageUsername());
                notification.setNotificationFrom(0);
                notification.setNotificationContent(message.getMessageContent());
                notification.setNotificationAvatar(message.getMessageAvatar());
                notification.setNotificationAddress(message.getMessageAddress());
                notification.setNotificationLeaveDate(message.getMessageLeaveDate());
                notification.setNotificationFromId(message.getMessageId());
                if (!iNotificationService.save(notification)) {
                    throw new RuntimeException("保存失败");
                }
                if (WebSocket.session != null) {
                    WebSocket.sendInfo(JSONUtil.toJsonStr(notification));
                }
            }
            return Result.success(message);
        } else {
            return Result.fail();
        }
    }

    /**
     * 删除一条留言
     * @param messageId 待删除留言id
     * @param request 权限验证
     * @return 返回删除留言结果
     */
    @DeleteMapping("/message/{messageId}")
    public Result deleteAMessage(@PathVariable("messageId") Integer messageId, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        if (iMessageService.deleteMessage(messageId)) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

}
