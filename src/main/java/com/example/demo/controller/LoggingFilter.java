package com.example.demo.controller;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Component
@WebFilter("/*")
public class LoggingFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(LoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic, if needed
    }
    /*
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        boolean isMultipart = httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().toLowerCase().startsWith("multipart/");

        if (isMultipart) {
            logger.info("Incoming Multipart Request: {} {}", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
            chain.doFilter(request, response); // Skip body logging for multipart requests
        } else {
            // Wrap the request to read its body
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);

            // Log Request Information
            logger.info("Incoming Request: {} {}", wrappedRequest.getMethod(), wrappedRequest.getRequestURI());
            logger.info("Request Body: {}", wrappedRequest.getRequestBody());

            // Wrap the response to capture its body
            CharArrayWriter responseWriter = new CharArrayWriter();
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpServletResponse) {
                @Override
                public PrintWriter getWriter() {
                    return new PrintWriter(responseWriter);
                }
            };

            // Proceed with the filter chain
            chain.doFilter(wrappedRequest, wrappedResponse);

            // Log Response Information
            logger.info("Response Status: {}", wrappedResponse.getStatus());
            logger.info("Response Body: {}", responseWriter.toString());
        }
    }*/
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        boolean isMultipart = httpServletRequest.getContentType() != null &&
                httpServletRequest.getContentType().toLowerCase().startsWith("multipart/");

        if (isMultipart) {
            logger.info("Incoming Multipart Request: {} {}", httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
            chain.doFilter(request, response); // Skip body logging for multipart requests
        } else {
            // Wrap the request to read its body for POST/PUT
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);

            // Log Request Information
            logger.info("Incoming Request: {} {}", wrappedRequest.getMethod(), wrappedRequest.getRequestURI());

            if ("GET".equalsIgnoreCase(wrappedRequest.getMethod())) {
                // Log query parameters for GET requests
                String queryString = wrappedRequest.getQueryString();
                logger.info("Query String: {}", queryString != null ? queryString : "None");
                wrappedRequest.getParameterMap().forEach((key, value) ->
                        logger.info("Query Parameter: {} = {}", key, String.join(",", value))
                );
            } else {
                // Log body for POST/PUT requests
                logger.info("Request Body: {}", wrappedRequest.getRequestBody());
            }

            // Wrap the response to capture its body
            CharArrayWriter responseWriter = new CharArrayWriter();
            HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpServletResponse) {
                @Override
                public PrintWriter getWriter() {
                    return new PrintWriter(responseWriter);
                }
            };

            // Proceed with the filter chain
            chain.doFilter(wrappedRequest, wrappedResponse);

            // Log Response Information
            logger.info("Response Status: {}", wrappedResponse.getStatus());
            logger.info("Response Body: {}", responseWriter.toString());
        }
    }

    @Override
    public void destroy() {
        // Cleanup logic, if needed
    }

    /**
     * Custom wrapper to cache the request body for logging purposes.
     */
    private static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final String requestBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            try (var reader = request.getReader()) {
                this.requestBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }

        public String getRequestBody() {
            return this.requestBody;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final byte[] bytes = this.requestBody.getBytes(StandardCharsets.UTF_8);
            return new CachedServletInputStream(bytes);
        }
    }

    /**
     * Custom input stream for the request body.
     */
    private static class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        public CachedServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // Not implemented
        }
    }
}
