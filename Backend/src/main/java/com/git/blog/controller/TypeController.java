package com.git.blog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.git.blog.common.function.CommonFunction;
import com.git.blog.common.response.Result;
import com.git.blog.entity.Type;
import com.git.blog.service.ITypeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@RestController
public class TypeController {

    private final ITypeService iTypeService;

    private final CommonFunction commonFunction;

    public TypeController(ITypeService iTypeService, CommonFunction commonFunction) {
        this.iTypeService = iTypeService;
        this.commonFunction = commonFunction;
    }

    /**
     * 查询所有标签类型
     * @param request 权限验证
     * @return 返回查询结果
     */
    @GetMapping("/type")
    public Result getTypes(HttpServletRequest request) {
        List<Type> types;
        if (!commonFunction.checkPermission(request)) {
            types = iTypeService.list(new QueryWrapper<Type>().eq("type_visibility", 1));
        } else {
            types = iTypeService.list();
        }
        return Result.success(types);
    }

    /**
     * 新增一种标签类型
     * @param type 新增标签具体信息
     * @param request 权限验证
     * @return 返回新增标签结果
     */
    @PostMapping("/type")
    public Result addAType(@RequestBody Type type, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        Type typeFromDB = iTypeService.getOne(new QueryWrapper<Type>().eq("type_name", type.getTypeName()));
        if (typeFromDB != null) {
            throw new RuntimeException("该标签已存在");
        }
        if (iTypeService.save(type)) {
            return Result.success(type);
        } else {
            return Result.fail();
        }
    }

    @PutMapping("/type/{typeId}")
    public Result updateAType(@Validated @RequestBody Type type, HttpServletRequest request,
                              @PathVariable("typeId") Integer typeId) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        Type typeFromDB = iTypeService.getOne(new QueryWrapper<Type>().and(i ->
                i.eq("type_name", type.getTypeName()).eq("type_visibility", type.getTypeVisibility())));
        if (typeFromDB != null) {
            throw new RuntimeException("该标签已存在");
        }
        type.setTypeId(typeId);
        if (iTypeService.updateById(type)) {
            return Result.success();
        } else {
            return Result.fail();
        }
    }

}
