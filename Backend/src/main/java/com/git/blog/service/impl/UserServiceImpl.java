package com.git.blog.service.impl;

import com.git.blog.entity.User;
import com.git.blog.mapper.UserMapper;
import com.git.blog.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
