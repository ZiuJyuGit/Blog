package com.git.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名字
     */
    @NotBlank
    private String userName;

    /**
     * 用户密码
     */
    @NotBlank
    private String userPassword;


}
