package com.git.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

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
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 留言id--主键
     */
    @Null(message = "不能自定义留言Id，必须为“”")
    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;

    /**
     * 该留言是否来自博主--0 false 1 true
     */
    private Integer messageFromBlogger;

    /**
     * 记录二级留言的父级留言，-1为无父留言
     */
    @Min(value = -1, message = "插入父评论id范围有误")
    private Integer messageParentId;

    /**
     * 留言用户昵称
     */
    @NotBlank(message = "留言用户昵称必须含实际内容，内容不能为空/只含空格")
    private String messageUsername;

    /**
     * 留言内容
     */
    @NotBlank(message = "用户留言必须含实际内容，内容不能为空/只含空格")
    private String messageContent;

    /**
     * 留言头像
     */
    @NotNull(message = "必须选一个头像")
    private Integer messageAvatar;

    /**
     * 留言时间
     */
    private LocalDateTime messageLeaveDate;

    /**
     * 留言地点
     */
    private String messageAddress;

    /**
     * 用于查询子级留言
     */
    @TableField(exist = false)
    private List<Message> replyMessages = new ArrayList<>();

    /**
     * 用于显示该留言的回复对象
     */
    @TableField(exist = false)
    private String parentNickname;

}
