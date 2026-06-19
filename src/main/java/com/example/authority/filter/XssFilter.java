package com.example.authority.filter;



import com.example.authority.config.properties.XssProperties;
import com.example.authority.config.wrapper.XssRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;


public class XssFilter implements Filter {

    private final XssProperties xssProperties;

    public XssFilter(XssProperties xssProperties) {
        this.xssProperties = xssProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        if (!xssProperties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String uri = req.getRequestURI();
        for (String exclude : xssProperties.getExcludes()) {
            if (uri.startsWith(exclude)) {
                chain.doFilter(request, response);
                return;
            }
        }

        chain.doFilter(new XssRequestWrapper(req), response);
    }
}
