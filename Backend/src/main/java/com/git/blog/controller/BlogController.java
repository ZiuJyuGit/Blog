package com.git.blog.controller;


import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.git.blog.common.function.CommonFunction;
import com.git.blog.common.response.Result;
import com.git.blog.entity.Blog;
import com.git.blog.service.IBlogService;
import com.git.blog.utils.ESUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Git
 * @since 2021-02-09
 */
@RestController
public class BlogController {

    private final IBlogService iBlogService;

    private final ESUtils esUtils;

    private final CommonFunction commonFunction;

    public BlogController(IBlogService iBlogService, ESUtils esUtils, CommonFunction commonFunction) {
        this.iBlogService = iBlogService;
        this.esUtils = esUtils;
        this.commonFunction = commonFunction;
    }

    /**
     * 分页查询文章
     * @param page 查询文章页数，默认为1，非博主不出现隐私类型(文章类型的visibility属性为0 & blog_state为非1)的文章
     * @param request 用于权限验证，博主可以查看未完成/回收站文章
     * @return 返回分页查询的文章
     */
    @GetMapping("/blog")
    public Result getBlogs(@RequestParam(defaultValue = "1") Integer page, HttpServletRequest request) {
        Page<Blog> blogPage = new Page<>(page, 6);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<Blog>().eq("blog_state", 1).orderByDesc("blog_release_date");
        if (!commonFunction.checkPermission(request)) {
            //查询不可见的类型id
            List<Object> invisibleTypes = commonFunction.getInvisibilityTypes();
            queryWrapper.notIn("blog_type", invisibleTypes);
        }
        Page<Blog> blogs = iBlogService.page(blogPage, queryWrapper);
        return Result.success(
                MapUtil.builder()
                .put("records", blogs.getRecords())
                .put("total", blogs.getTotal())
                .map()
        );
    }

    /**
     * 获取热门文章，默认不按文章类型获取,查询6条,不提供分页查询
     * @param type 未通过认证不允许访问隐私类型的文章
     * @return 返回6条热门文章
     */
    @GetMapping("/hotBlog")
    public Result getBlogsByPageViews(@RequestParam(defaultValue = "0") Integer type, HttpServletRequest request) {
        List<Object> invisibleTypes = commonFunction.getInvisibilityTypes();
        if (!commonFunction.checkPermission(request) && invisibleTypes.contains(type)) {
            throw new RuntimeException("无权限操作");
        }
        Page<Blog> blogPage = new Page<>(1, 6);
        QueryWrapper<Blog> queryWrapper = new QueryWrapper<Blog>().eq("blog_state", 1).orderByDesc("blog_page_views");
        if (type != 0) {
            queryWrapper.eq("blog_type", type);
        }
        Page<Blog> blogs = iBlogService.page(blogPage, queryWrapper);
        return Result.success(
                MapUtil.builder()
                .put("records", blogs.getRecords())
                .put("pages", blogs.getPages())
                .map()
        );
    }

    /**
     * 按照分类查询文章，权限认证通过后可查询所有博文，非博主不可查询“隐私”类型的文章和未公开的文章
     * @param page 分页查询
     * @param type 未通过权限认证不可访问
     * @param request 权限验证
     * @return 分页查询文章结果
     */
    @GetMapping("/blogByType")
    public Result getBlogsByType(@RequestParam(defaultValue = "1") Integer page, @RequestParam Integer type,
                                 HttpServletRequest request) {
        Page<Blog> blogPage = new Page<>(page, 6);
        List<Object> invisibilityTypes = commonFunction.getInvisibilityTypes();
        if (!commonFunction.checkPermission(request) && invisibilityTypes.contains(type)) {
            throw new RuntimeException("查询错误");
        }
        Page<Blog> blogs = iBlogService.page(blogPage, new QueryWrapper<Blog>().and(i -> i.eq("blog_type", type)
                            .eq("blog_state", 1)).orderByDesc("blog_release_date"));
        return Result.success(
            MapUtil.builder()
            .put("records", blogs.getRecords())
            .put("total", blogs.getTotal())
            .map()
        );
    }

    /**
     * 查询不可见的文章---回收站/未完成
     * @param page 分页查询
     * @param state -1 未完成 0 回收站
     * @param request 权限验证
     * @return 返回查询结果
     */
    @GetMapping("/unseenBlog")
    public Result getUndoneBlog(@RequestParam(defaultValue = "1") Integer page, @RequestParam Integer state,
                                HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        Page<Blog> blogPage = new Page<>(page, 6);
        Page<Blog> blogs = iBlogService.page(blogPage, new QueryWrapper<Blog>().eq("blog_state", state).orderByDesc("blog_release_date"));
        return Result.success(
                MapUtil.builder()
                .put("records", blogs.getRecords())
                .put("total", blogs.getTotal())
                .map()
        );
    }

    /**
     * 查询某一文章（包括该文章的评论），查询一次访问量+1
     * @param blogId 查询的文章id
     * @param request 用于权限验证，博主可以查看隐私/回收站文章
     * @return 返回该文章具体信息
     */
    @GetMapping("/blog/{blogId}")
    public Result getABlog(@PathVariable("blogId") Integer blogId, HttpServletRequest request) {
        Blog blog = iBlogService.getById(blogId);
        if (blog == null) {
            return Result.fail("查询失败，不存在此博客");
        }
        if (!commonFunction.checkPermission(request)) {
            List<Object> invisibilityTypes = commonFunction.getInvisibilityTypes();
            if (blog.getBlogState() != 1 || invisibilityTypes.contains(blog.getBlogType())) {
                throw new RuntimeException("无权限操作");
            }
        }
        blog.setBlogPageViews(blog.getBlogPageViews() + 1);
        if (!iBlogService.updateById(blog)) {
            return Result.fail();
        }
        esUtils.blog2ES(blog);
        return Result.success();
    }

    /**
     * 新增文章
     * @param blog 添加的文章
     * @param request 用于权限验证
     * @return 返回新增结果
     */
    @PostMapping("/blog")
    public Result addABlog(@Validated @RequestBody Blog blog, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        blog.setBlogReleaseDate(LocalDateTime.now().withNano(0));
        blog.setBlogPageViews(1);
        if (iBlogService.save(blog)) {
            esUtils.blog2ES(blog);
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    /**
     * 修改某一文章
     * @param blog 修改的文章
     * @param request 用于权限验证
     * @return 返回修改结果
     */
    @PutMapping("/blog/{blogId}")
    public Result updateABlog(@Validated @RequestBody Blog blog, HttpServletRequest request,
                              @PathVariable("blogId") Integer blogId) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        blog.setBlogId(blogId);
        blog.setBlogLastModificationDate(LocalDateTime.now().withNano(0));
        if (iBlogService.updateById(blog)) {
            Blog newBlog = iBlogService.getById(blogId);
            esUtils.blog2ES(newBlog);
            return Result.success();
        } else {
            return Result.fail();
        }
    }

    /**
     * 删除文章，若文章不为回收站状态，将其放置回收站（仅管理员可见）/若处在回收站，再次删除则从数据库中删除
     * @param blogId 待删除/移至回收站文章id
     * @param request 权限验证
     * @return 返回删除文章结果
     */
    @DeleteMapping("/blog/{blogId}")
    public Result deleteABlog(@PathVariable("blogId") Integer blogId, HttpServletRequest request) {
        if (!commonFunction.checkPermission(request)) {
            throw new RuntimeException("无权限操作");
        }
        Blog blog = iBlogService.getById(blogId);
        if (blog.getBlogState() == 0) {
            if (iBlogService.removeById(blogId)) {
                esUtils.deleteBlogFromES(blogId);
                return Result.success();
            } else {
                return Result.fail();
            }
        } else {
            blog.setBlogState(0);
            if (iBlogService.updateById(blog)) {
                esUtils.blog2ES(blog);
                return Result.success();
            } else {
                return Result.fail();
            }
        }
    }

    /**
     * 按关键词搜索博文，权限验证未通过时，不可搜索出隐私文章 & 非可见文章。
     * 即使权限验证通过，也不需要查询非可见（未完成/回收站）文章
     * @param phrase 搜索字段
     * @param page 分页
     * @return 返回分页查询结果
     */
    @GetMapping("/search")
    public Result getBlogsByPhrase(@RequestParam String phrase, @RequestParam(defaultValue = "0") Integer page,
                                   HttpServletRequest request) {
        Map<Object, Object> blogInfos;
        if (commonFunction.checkPermission(request)) {
            blogInfos = esUtils.searchBlogFromES(phrase, page, null);
        } else {
            List<Object> invisibilityTypes = commonFunction.getInvisibilityTypes();
            blogInfos = esUtils.searchBlogFromES(phrase, page, invisibilityTypes);
        }
        return Result.success(blogInfos);
    }

    /**
     * @return 返回文章数量
     */
    @GetMapping("/allBlogCount")
    public Result getAllBlogCount() {
        int count = iBlogService.count();
        return Result.success(count);
    }

}
