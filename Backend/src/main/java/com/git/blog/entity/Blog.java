package com.git.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

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
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 博文唯一id--主键
     */
    @Null(message = "不能自定义文章Id，必须为“”")
    @TableId(value = "blog_id", type = IdType.AUTO)
    private Integer blogId;

    /**
     * 博文标题
     */
    @NotBlank(message = "博文标题必须含实际内容，内容不能为空/只含空格")
    private String blogTitle;

    /**
     * 博文内容
     */
    @NotBlank(message = "博文内容必须含实际内容，内容不能为空/只含空格")
    private String blogContent;

    /**
     * 博文发布时间
     */
    private LocalDateTime blogReleaseDate;

    /**
     * 博文最近修改时间
     */
    private LocalDateTime blogLastModificationDate;

    /**
     * 博文浏览数
     */
    private Integer blogPageViews;

    /**
     * 博文状态---1 未完成 0 回收站 1 公开可见
     */
    @Min(value = -1, message = "权限范围有误")
    @Max(value = 1, message = "权限范围有误")
    private Integer blogState;

    /**
     * 博文首页图片
     */
    @NotEmpty(message = "首图不能缺，缺了不好看")
    private String blogPicture;

    /**
     * 博文分类，数据来源分类id
     */
    @Min(value = 0, message = "所属分类有误")
    private Integer blogType;

    /**
     * 博文简介
     */
    @NotBlank(message = "博文简介不能为空")
    private String blogAbstract;



}
