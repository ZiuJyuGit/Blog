package com.git.blog.service.impl;

import com.git.blog.entity.Blog;
import com.git.blog.mapper.BlogMapper;
import com.git.blog.service.IBlogService;
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
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
