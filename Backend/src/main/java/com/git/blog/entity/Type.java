package com.git.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.io.Serializable;

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
public class Type implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型id--主键
     */
    @Null(message = "不能自定义类型Id，必须为“”")
    @TableId(value = "type_id", type = IdType.AUTO)
    private Integer typeId;

    /**
     * 类型名字
     */
    @NotBlank
    private String typeName;

    /**
     * 该类型的文章是否对外可见--0 false 1 true
     */
    @Min(value = 0, message = "类型可见属性设置有误")
    @Max(value = 1, message = "类型可见属性设置有误")
    private Integer typeVisibility;

}
