package com.git.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.git.blog.entity.Comment;
import com.git.blog.mapper.CommentMapper;
import com.git.blog.service.ICommentService;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Null;
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
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final CommentMapper commentMapper;

    //存放迭代找出的所有子代的集合
    private List<Comment> tempReplys = new ArrayList<>();

    private List<Integer> deleteReplys = new ArrayList<>();

    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    /**
     * 按照博文id查询评论
     * @param blogId 博文id
     * @param page 分页查询
     * @return 返回查询评论结果
     */
    @Override
    public List<Comment> getCommentsByBlogId(Integer blogId, Integer page) {
        //查询父节点
        Page<Comment> commentPage = new Page<>(page, 5);
        Page<Comment> commentsFromDB = commentMapper.selectPage(commentPage, new QueryWrapper<Comment>().and(i ->
                i.eq("comment_blog_id", blogId).eq("comment_parent_id", -1))
                .orderByAsc("comment_leave_date"));
        List<Comment> comments = commentsFromDB.getRecords();
        for (Comment comment : comments) {
            Integer commentId = comment.getCommentId();
            String commentUsername = comment.getCommentUsername();
            //递归查询出子评论
            recursively(blogId, commentId, commentUsername);
            comment.setReplyComments(tempReplys);
            tempReplys = new ArrayList<>();
        }
        return comments;
    }

    /**
     * 循环迭代找出子集回复
     * @param parentId 本次递归回复的父回复Id
     * @param parentNickname 本次递归回复的父回复名称
     */
    private void recursively(Integer blogId, Integer parentId, String parentNickname) {
        //根据子一级评论的id找到子二级评论
        List<Comment> childComments = commentMapper.selectList(new QueryWrapper<Comment>().and(i ->
                i.eq("comment_blog_id", blogId).eq("comment_parent_id", parentId))
                .orderByAsc("comment_leave_date"));
        if(childComments.size() > 0) {
            for(Comment childComment : childComments) {
                String commentUsername = childComment.getCommentUsername();
                childComment.setParentNickname(parentNickname);
                tempReplys.add(childComment);
                Integer childId = childComment.getCommentId();
                //递归找出子集回复
                recursively(blogId, childId, commentUsername);
            }
        }
    }

    @Override
    public Boolean deleteComment(Integer commentId) {
        deleteReplys.add(commentId);
        if (commentMapper.deleteBatchIds(deleteReplys) > 0) {
            deleteReplys = new ArrayList<>();
            return true;
        }
        return false;
    }

    private void getChildrenRecursively(Integer parentId) {
        List<Comment> childComments = commentMapper.selectList(new QueryWrapper<Comment>()
                .eq("comment_parent_id", parentId));
        if (childComments.size() > 0) {
            for (Comment childComment : childComments) {
                Integer commentId = childComment.getCommentId();
                deleteReplys.add(commentId);
                getChildrenRecursively(commentId);
            }
        }
    }
}
