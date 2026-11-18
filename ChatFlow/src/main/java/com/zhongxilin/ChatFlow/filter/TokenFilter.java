package com.zhongxilin.ChatFlow.filter;

import com.zhongxilin.ChatFlow.utils.CurrentHolder;
import com.zhongxilin.ChatFlow.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

//AI SSE 请求使用 SseEmitter，所以过滤器需要声明 Servlet 异步支持；过滤器必须显式声明支持异步派发。
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class TokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


//        获取请求路径
        String requestURI = request.getRequestURI();
//        判断是否是登录请求
        if (requestURI.contains("/login") || requestURI.contains("/auth/register")) {

            filterChain.doFilter(request, response);
            return;
        }

        //获取请求头中的token
        String token = request.getHeader("token");
        //判断token是否存在，不存在则响应401
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            return;
        }
        //token存在,校验合法性，失败则响应401
        try {
            Claims claims = JwtUtils.parseToken(token);
            Integer userId = Integer.valueOf(claims.get("id").toString());
            CurrentHolder.setCurrentId(userId);//存入ThreadLocal
        } catch (Exception e) {
            response.setStatus(401);
            return;
        }
        // 合法则放行。无论同步请求还是异步请求，均确保清理当前工作线程的登录上下文。
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Servlet 异步任务可能切换线程；原请求线程仍需清理 ThreadLocal。
            CurrentHolder.remove();
        }


    }
}
