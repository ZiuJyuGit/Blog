package com.git.blog.service;

import com.git.blog.entity.Comment;
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
public interface ICommentService extends IService<Comment> {

    List<Comment> getCommentsByBlogId(Integer blogId, Integer page);

    Boolean deleteComment(Integer commentId);

}
