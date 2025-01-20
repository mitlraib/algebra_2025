package com.ashcollege;

import com.ashcollege.service.Persist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class RestExceptionHandler extends DefaultHandlerExceptionResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Persist.class);

    @ExceptionHandler(Throwable.class)
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        LOGGER.info(String.format("Received exception in REST controller, in path=%s, params: %s",
                request.getRequestURI(), request.getQueryString()));
        return super.doResolveException(request, response, handler, e);
    }
}