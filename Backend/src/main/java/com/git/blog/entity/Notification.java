package com.git.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知id--主键
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Integer notificationId;

    /**
     * 通知来源用户昵称
     */
    private String notificationUsername;

    /**
     * 通知来源 0 留言板 非0 博文id
     */
    private Integer notificationFrom;

    /**
     * 通知内容
     */
    private String notificationContent;

    /**
     * 通知头像
     */
    private Integer notificationAvatar;

    /**
     * 通知发出时间
     */
    private LocalDateTime notificationLeaveDate;

    /**
     * 通知地点
     */
    private String notificationAddress;

    /**
     * 通知来源id，用于回复
     */
    private Integer notificationFromId;

    /**
     * 用于标识该通知来源博文的名字，用于前端展示，留言板为空
     */
    @TableField(exist = false)
    private String  notificationBlogName;

}
