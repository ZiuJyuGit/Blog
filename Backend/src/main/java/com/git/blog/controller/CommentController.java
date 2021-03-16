package com.git.blog.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.git.blog.common.function.CommonFunction;
import com.git.blog.common.response.Result;
import com.git.blog.common.socket.WebSocket;
import com.git.blog.entity.Blog;
import com.git.blog.entity.Comment;
import com.git.blog.entity.Notification;
import com.git.blog.service.IBlogService;
import com.git.blog.service.ICommentService;
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
public class CommentController {

    private final ICommentService iCommentService;

    private final IBlogService iBlogService;

    private final IPUtils ipUtils;

    private final INotificationService iNotificationService;

    private final CommonFunction commonFunction;

    public CommentController(ICommentService iCommentService, IBlogService iBlogService, IPUtils ipUtils,
                             INotificationService iNotificationService, CommonFunction commonFunction) {
        this.iCommentService = iCommentService;
        this.iBlogService = iBlogService;
        this.ipUtils = ipUtils;
        this.iNotificationService = iNotificationService;
        this.commonFunction = commonFunction;
    }

    /**
     * 获取具体博文的评论
     * @param blogId 查询评论所属的博文
     * @param page 分页查询
     * @param request 权限校验
     * @return 返回查询评论
     */
    @GetMapping("/comment/{blogId}")
    public Result getCommentByBlogId(@PathVariable("blogId") Integer blogId, @RequestParam(defaultValue = "1") Integer page,
                                     HttpServletRequest request) {
        Blog blog = iBlogService.getById(blogId);
        if (blog == null) {
            return Result.fail("查询失败，不存在此博客");
        }
        if (!commonFunction.checkPermission(request)) {
            List<Object> invisibilityTypes = commonFunction.getInvisibilityTypes();
            if (blog.getBlogState() != 1 || invisibilityTypes.contains(blog.getBlogType())) {
                throw new RuntimeException("无权限操作");
            }
        }
        //查询具体文章所有评论
        List<Comment> comments = iCommentService.getCommentsByBlogId(blogId, page);
        return Result.success(comments);
    }

    /**
     * 查询文章评论总数，用于前端分页展示时控制页数显示
     * @return 返回文章评论总数
     */
    @GetMapping("/commentCount/{blogId}")
    public Result getCommentCountByBlogId(@PathVariable("blogId") Integer blogId) {
        int count = iCommentService.count(new QueryWrapper<Comment>().and( i ->
                i.eq("comment_parent_id", -1).eq("comment_blog_id", blogId)));
        return Result.success(count);
    }

    /**
     * 新增一条评论
     * @param comment 评论内容
     * @param request 判断评论来源，通过权限校验来源为博主
     * @return 返回新增结果
     */
    @PostMapping("/comment")
    public Result addAComment(@Validated @RequestBody Comment comment, HttpServletRequest request) {
        //若插入评论为子评论，检验评论所属文章是否存在其父评论，防止调用接口存入不属于此文章的评论
        //若插入评论为父评论，检验评论插入文章是否存在，防止调用接口插入不存在文章的评论
        Integer commentParentId = comment.getCommentParentId();
        Integer commentBlogId = comment.getCommentBlogId();
        if (commentParentId != -1) {
            Comment parentComment = iCommentService.getById(commentParentId);
            if (parentComment == null || !parentComment.getCommentBlogId().equals(commentBlogId)) {
                throw new RuntimeException("添加评论出现了错误");
            }
        } else {
            Blog blog = iBlogService.getById(commentBlogId);
            if (blog == null) {
                throw new RuntimeException("添加评论出现了错误");
            }
        }
        //若请求头带有jwt，该评论来自博主
        if (commonFunction.checkPermission(request)) {
            comment.setCommentFromBlogger(1);
        } else {
            comment.setCommentFromBlogger(0);
        }
        comment.setCommentLeaveDate(LocalDateTime.now().withNano(0));
        //设置ip，先从请求头获取ip，再按照ip查询其地址
        String ip = ipUtils.getIpFromRequest(request);
        String address = ipUtils.ip2Address(ip);
        comment.setCommentAddress(address);
        if (iCommentService.save(comment)) {
            //当评论为非博主评论&初次评论/回复博主评论时，该条评论需通知博主并将此消息存入通知表中
            if (comment.getCommentFromBlogger() == 0 && (commentParentId == -1 ||
                    (iCommentService.getById(commentParentId).getCommentFromBlogger() == 1))) {
                Notification notification = new Notification();
                notification.setNotificationUsername(comment.getCommentUsername());
                notification.setNotificationFrom(comment.getCommentBlogId());
                notification.setNotificationContent(comment.getCommentContent());
                notification.setNotificationAvatar(comment.getCommentAvatar());
                notification.setNotificationLeaveDate(comment.getCommentLeaveDate());
                notification.setNotificationAddress(comment.getCommentAddress());
                notification.setNotificationFromId(comment.getCommentId());
                if (!iNotificationService.save(notification)) {
                    throw new RuntimeException("保存失败");
                }
                if (WebSocket.session != null) {
                    Blog blog = iBlogService.getOne(new QueryWrapper<Blog>().eq("blog_id", notification.getNotificationFrom())
                            .select("blog_title"));
                    notification.setNotificationBlogName(blog.getBlogTitle());
                    WebSocket.sendInfo(JSONUtil.toJsonStr(notification));
                }

            }
            return Result.success(comment);
        } else {
            return Result.fail();
        }
    }

    /**
     * 删除一条评论
     * @param commentId 待删除评论id
     * @param request 权限验证
     * @return 返回删除结果
     */
    @DeleteMapping("/comment/{commentId}")
    public Result deleteAComment(@PathVariable("commentId") Integer commentId, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        if (iCommentService.deleteComment(commentId)) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

}
