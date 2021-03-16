package com.git.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论id--主键
     */
    @Null(message = "不能自定义评论Id，必须为“”")
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Integer commentId;

    /**
     * 该评论是否来自博主--0 false 1 true
     */
    private Integer commentFromBlogger;

    /**
     * 记录二级评论的父级评论，-1为无父评论
     */
    @Min(value = -1, message = "插入父评论id范围有误")
    private Integer commentParentId;

    /**
     * 评论用户昵称
     */
    @NotBlank(message = "用户昵称必须含实际内容，内容不能为空/只含空格")
    private String commentUsername;

    /**
     * 评论所属博文
     */
    private Integer commentBlogId;

    /**
     * 评论内容
     */
    @NotBlank(message = "用户评论必须含实际内容，内容不能为空/只含空格")
    private String commentContent;

    /**
     * 评论头像
     */
    @NotNull(message = "必须选一个头像")
    private Integer commentAvatar;

    /**
     * 评论时间
     */
    private LocalDateTime commentLeaveDate;

    /**
     * 评论地点
     */
    private String commentAddress;

    /**
     * 用于查询子级评论
     */
    @TableField(exist = false)
    private List<Comment> replyComments = new ArrayList<>();

    /**
     * 用于显示该评论的回复对象
     */
    @TableField(exist = false)
    private String parentNickname;

}
