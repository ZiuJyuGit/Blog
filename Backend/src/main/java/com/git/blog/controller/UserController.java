package com.git.blog.controller;


import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.git.blog.common.response.Result;
import com.git.blog.entity.User;
import com.git.blog.service.IUserService;
import com.git.blog.utils.JwtUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@RestController
public class UserController {

    private final IUserService iUserService;

    private final JwtUtils jwtUtils;

    public UserController(IUserService iUserService, JwtUtils jwtUtils) {
        this.iUserService = iUserService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 登录成功后推送未读消息数量（来源评论&留言）
     * @param user 登录信息
     * @param response 响应头，用于放置jwt
     * @return 返回登录结果
     */
    @PostMapping(value = "/login")
    public Result login(@Validated @RequestBody User user, HttpServletResponse response) {
        User userFromDB = iUserService.getOne(new QueryWrapper<User>().eq("user_name", user.getUserName()));
        if (userFromDB == null || !userFromDB.getUserPassword().equals(SecureUtil.md5(user.getUserPassword()))) {
            return Result.fail();
        }
        //登录验证通过，响应头放置jwt
        String jwt = jwtUtils.generateToken(user.getUserName());
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-control-Expose-Headers", "Authorization");
        return Result.success();
    }
}
