package com.git.blog.common.function;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.git.blog.entity.Type;
import com.git.blog.service.ITypeService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 该类存放共用方法
 */
@Component
public class CommonFunction {

    private final ITypeService iTypeService;

    public CommonFunction(ITypeService iTypeService) {
        this.iTypeService = iTypeService;
    }

    /**
     * 从请求头中获取"Authorization"，若不为空则为合法认证（拦截器已过滤非法jwt）
     * @param request 权限操作
     */
    public Boolean checkPermission(HttpServletRequest request) {
        return StringUtils.isNotBlank(request.getHeader("Authorization"));
    }

    /**
     * 查询所有不对外可见的标签id
     * @return 返回不可见的List集合
     */
    public List<Object> getInvisibilityTypes() {
        return iTypeService.listObjs(new QueryWrapper<Type>().eq("type_visibility", 0));
    }
}
