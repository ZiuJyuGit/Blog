package com.git.blog.service.impl;

import com.git.blog.entity.Type;
import com.git.blog.mapper.TypeMapper;
import com.git.blog.service.ITypeService;
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
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements ITypeService {

}
