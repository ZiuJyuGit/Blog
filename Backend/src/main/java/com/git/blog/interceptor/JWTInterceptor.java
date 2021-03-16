package com.git.blog.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.git.blog.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 权限验证
 */

@Component
public class JWTInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JWTInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        //请求头中无jwt跳过验证，若有需验真伪
        if (StringUtils.isNotBlank(authorization)) {
            Claims claim = jwtUtils.getClaimByToken(authorization);
            if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                throw new RuntimeException("登录无效（虚假token/token已过期），请重新登录");
            } else {
                //权限验证成功后，若jwt剩余时间小于两天，为其续签。
                long expiration = claim.getExpiration().getTime();
                long renewedTime = expiration - 24 * 60 * 60 * 1000 * 2;
                Date renewDate = new Date();
                renewDate.setTime(renewedTime);
                if (jwtUtils.isTokenExpired(renewDate)) {
                    String jwt = jwtUtils.generateToken(claim.getSubject());
                    response.setHeader("Authorization", jwt);
                    response.setHeader("Access-control-Expose-Headers", "Authorization");
                }
            }
        }
        return true;
    }
}
