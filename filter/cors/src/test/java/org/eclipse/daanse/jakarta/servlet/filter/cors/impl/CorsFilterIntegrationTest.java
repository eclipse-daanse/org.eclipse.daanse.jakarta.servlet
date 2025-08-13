/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.jakarta.servlet.filter.cors.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.daanse.jakarta.servlet.filter.cors.api.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Integration tests for CORS filter using mock implementations to test real CORS scenarios without
 * requiring a full servlet container.
 */
@DisplayName("CORS Filter Integration Tests")
class CorsFilterIntegrationTest {

    private CorsFilter corsFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        corsFilter = new CorsFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    @DisplayName("Complete CORS workflow - Simple request with allowed origin")
    void completeWorkflowSimpleRequestAllowedOrigin() throws ServletException, IOException {
        // Given - Configure filter for basic CORS
        CorsFilterConfig config = createConfig().allowedOrigins("https://example.com").allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type", "Authorization").allowCredentials(true)
                .exposedHeaders("X-Custom-Header").build();

        corsFilter.activate(config);

        // Setup request
        request.setMethod("GET");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://example.com");
        request.setRequestURI("/api/data");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader("Vary")).isEqualTo(Constants.HEADER_REQUEST_ORIGIN);
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("https://example.com");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS)).isEqualTo("true");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_EXPOSE_HEADERS))
                .isEqualTo("X-Custom-Header");
        assertThat(filterChain.wasChainCalled()).isTrue();
    }

    @Test
    @DisplayName("Complete CORS workflow - Preflight request")
    void completeWorkflowPreflightRequest() throws ServletException, IOException {
        // Given - Configure filter for preflight handling
        CorsFilterConfig config = createConfig().allowedOrigins("https://app.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With").preflightMaxAge(3600)
                .allowCredentials(true).chainPreflight(false).build();

        corsFilter.activate(config);

        // Setup preflight request
        request.setMethod("OPTIONS");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://app.example.com");
        request.setHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD, "PUT");
        request.setHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type,Authorization");
        request.setRequestURI("/api/users/123");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("https://app.example.com");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS)).isEqualTo("true");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_MAX_AGE)).isEqualTo("3600");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_METHODS))
                .isEqualTo("GET,POST,PUT,DELETE");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS))
                .isEqualTo("Content-Type,Authorization,X-Requested-With");

        // Preflight should not be chained when chainPreflight=false
        assertThat(filterChain.wasChainCalled()).isFalse();
    }

    @Test
    @DisplayName("Wildcard origin with credentials disabled")
    void wildcardOriginWithCredentialsDisabled() throws ServletException, IOException {
        // Given
        CorsFilterConfig config = createConfig().allowedOrigins("*").allowedMethods("GET", "POST")
                .allowCredentials(false).build();

        corsFilter.activate(config);

        request.setMethod("POST");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://anywhere.com");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("https://anywhere.com");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS)).isNull();
    }

    @Test
    @DisplayName("Wildcard pattern matching")
    void wildcardPatternMatching() throws ServletException, IOException {
        // Given
        CorsFilterConfig config = createConfig().allowedOrigins("https://*.example.com", "http://localhost:*")
                .allowedMethods("GET").build();

        corsFilter.activate(config);

        // Test subdomain matching
        request.setMethod("GET");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://api.example.com");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("https://api.example.com");

        // Reset for second test
        response = new MockHttpServletResponse();
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "http://localhost:3000");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("http://localhost:3000");
    }

    @Test
    @DisplayName("Timing origin control")
    void timingOriginControl() throws ServletException, IOException {
        // Given
        CorsFilterConfig config = createConfig().allowedOrigins("https://example.com", "https://partner.com")
                .allowedTimingOrigins("https://example.com") // Only example.com can access timing
                .allowedMethods("GET").build();

        corsFilter.activate(config);

        // Test with timing-allowed origin
        request.setMethod("GET");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://example.com");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_TIMING_ALLOW_ORIGIN)).isEqualTo("https://example.com");

        // Reset for second test with different origin
        response = new MockHttpServletResponse();
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://partner.com");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("https://partner.com");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_TIMING_ALLOW_ORIGIN)).isNull(); // Not allowed for
                                                                                                // timing access
    }

    @Test
    @DisplayName("Complex request scenario")
    void complexRequestScenario() throws ServletException, IOException {
        // Given
        CorsFilterConfig config = createConfig().allowedOrigins("https://admin.example.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE").allowedHeaders("*")
                .exposedHeaders("X-Total-Count", "X-RateLimit-Remaining").allowCredentials(true).build();

        corsFilter.activate(config);

        // Test complex request (not simple due to custom headers)
        request.setMethod("POST");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://admin.example.com");
        request.setHeader("X-Custom-Auth", "token123");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN))
                .isEqualTo("https://admin.example.com");
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_EXPOSE_HEADERS))
                .isEqualTo("X-Total-Count,X-RateLimit-Remaining");
        assertThat(filterChain.wasChainCalled()).isTrue();
    }

    @Test
    @DisplayName("Rejected request scenarios")
    void rejectedRequestScenarios() throws ServletException, IOException {
        // Given
        CorsFilterConfig config = createConfig().allowedOrigins("https://allowed.com").allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type").chainPreflight(false).build();

        corsFilter.activate(config);

        // Test disallowed origin
        request.setMethod("GET");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://malicious.com");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
        assertThat(filterChain.wasChainCalled()).isTrue(); // Request still proceeds

        // Test preflight with disallowed method
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        request.setMethod("OPTIONS");
        request.setHeader(Constants.HEADER_REQUEST_ORIGIN, "https://allowed.com");
        request.setHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD, "DELETE");

        // When
        corsFilter.doFilter(request, response, filterChain);

        // Then
        assertThat(response.getHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN)).isNull();
        assertThat(filterChain.wasChainCalled()).isTrue();
    }

    // Helper methods for creating test configurations
    private ConfigBuilder createConfig() {
        return new ConfigBuilder();
    }

    private static class ConfigBuilder {
        private String[] allowedOrigins = {};
        private String[] allowedTimingOrigins = {};
        private String[] allowedMethods = { "GET", "POST", "HEAD" };
        private String[] allowedHeaders = { "X-Requested-With", "Content-Type", "Accept" };
        private String[] exposedHeaders = {};
        private int preflightMaxAge = 1800;
        private boolean allowCredentials = true;
        private boolean chainPreflight = true;

        public ConfigBuilder allowedOrigins(String... origins) {
            this.allowedOrigins = origins;
            return this;
        }

        public ConfigBuilder allowedTimingOrigins(String... origins) {
            this.allowedTimingOrigins = origins;
            return this;
        }

        public ConfigBuilder allowedMethods(String... methods) {
            this.allowedMethods = methods;
            return this;
        }

        public ConfigBuilder allowedHeaders(String... headers) {
            this.allowedHeaders = headers;
            return this;
        }

        public ConfigBuilder exposedHeaders(String... headers) {
            this.exposedHeaders = headers;
            return this;
        }

        public ConfigBuilder preflightMaxAge(int maxAge) {
            this.preflightMaxAge = maxAge;
            return this;
        }

        public ConfigBuilder allowCredentials(boolean allow) {
            this.allowCredentials = allow;
            return this;
        }

        public ConfigBuilder chainPreflight(boolean chain) {
            this.chainPreflight = chain;
            return this;
        }

        public CorsFilterConfig build() {
            return new CorsFilterConfig() {
                @Override
                public String[] allowedOrigins() {
                    return allowedOrigins;
                }

                @Override
                public String[] allowedOriginsPatterns() {
                    return new String[] {};
                }

                @Override
                public String[] allowedTimingOrigins() {
                    return allowedTimingOrigins;
                }

                @Override
                public String[] allowedMethods() {
                    return allowedMethods;
                }

                @Override
                public String[] allowedHeaders() {
                    return allowedHeaders;
                }

                @Override
                public int preflightMaxAge() {
                    return preflightMaxAge;
                }

                @Override
                public boolean allowCredentials() {
                    return allowCredentials;
                }

                @Override
                public String[] exposedHeaders() {
                    return exposedHeaders;
                }

                @Override
                public boolean chainPreflight() {
                    return chainPreflight;
                }

                @Override
                public Class<? extends java.lang.annotation.Annotation> annotationType() {
                    return CorsFilterConfig.class;
                }
            };
        }
    }

    // Mock implementations for testing
    private static class MockHttpServletRequest implements HttpServletRequest {
        private final Map<String, String> headers = new HashMap<>();
        private String method = "GET";
        private String requestURI = "/";

        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public void setRequestURI(String uri) {
            this.requestURI = uri;
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        // Minimal implementations for unused methods
        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public jakarta.servlet.http.Cookie[] getCookies() {
            return new jakarta.servlet.http.Cookie[0];
        }

        @Override
        public long getDateHeader(String name) {
            return -1;
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return Collections.enumeration(Collections.emptyList());
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }

        @Override
        public int getIntHeader(String name) {
            return -1;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return "";
        }

        @Override
        public String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public java.security.Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer("http://localhost" + requestURI);
        }

        @Override
        public String getServletPath() {
            return "";
        }

        @Override
        public jakarta.servlet.http.HttpSession getSession(boolean create) {
            return null;
        }

        @Override
        public jakarta.servlet.http.HttpSession getSession() {
            return null;
        }

        @Override
        public String changeSessionId() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse response) {
            return false;
        }

        @Override
        public void login(String username, String password) {
        }

        @Override
        public void logout() {
        }

        @Override
        public java.util.Collection<jakarta.servlet.http.Part> getParts() {
            return Collections.emptyList();
        }

        @Override
        public jakarta.servlet.http.Part getPart(String name) {
            return null;
        }

        @Override
        public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) {
            return null;
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(Collections.emptyList());
        }

        @Override
        public String getCharacterEncoding() {
            return null;
        }

        @Override
        public void setCharacterEncoding(String env) {
        }

        @Override
        public int getContentLength() {
            return -1;
        }

        @Override
        public long getContentLengthLong() {
            return -1;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public jakarta.servlet.ServletInputStream getInputStream() {
            return null;
        }

        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(Collections.emptyList());
        }

        @Override
        public String[] getParameterValues(String name) {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.emptyMap();
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String getServerName() {
            return "localhost";
        }

        @Override
        public int getServerPort() {
            return 8080;
        }

        @Override
        public java.io.BufferedReader getReader() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return "127.0.0.1";
        }

        @Override
        public String getRemoteHost() {
            return "localhost";
        }

        @Override
        public void setAttribute(String name, Object o) {
        }

        @Override
        public void removeAttribute(String name) {
        }

        @Override
        public java.util.Locale getLocale() {
            return java.util.Locale.getDefault();
        }

        @Override
        public Enumeration<java.util.Locale> getLocales() {
            return Collections.enumeration(Collections.singletonList(java.util.Locale.getDefault()));
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return "localhost";
        }

        @Override
        public String getLocalAddr() {
            return "127.0.0.1";
        }

        @Override
        public int getLocalPort() {
            return 8080;
        }

        @Override
        public jakarta.servlet.ServletContext getServletContext() {
            return null;
        }

        @Override
        public jakarta.servlet.AsyncContext startAsync() {
            return null;
        }

        @Override
        public jakarta.servlet.AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public jakarta.servlet.AsyncContext getAsyncContext() {
            return null;
        }

        @Override
        public jakarta.servlet.DispatcherType getDispatcherType() {
            return jakarta.servlet.DispatcherType.REQUEST;
        }

        @Override
        public String getRequestId() {
            return null;
        }

        @Override
        public String getProtocolRequestId() {
            return null;
        }

        @Override
        public jakarta.servlet.ServletConnection getServletConnection() {
            return null;
        }
    }

    private static class MockHttpServletResponse implements HttpServletResponse {
        private final Map<String, String> headers = new HashMap<>();
        private int status = 200;

        @Override
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            headers.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
        }

        @Override
        public int getStatus() {
            return status;
        }

        // Minimal implementations for unused methods
        @Override
        public void addCookie(jakarta.servlet.http.Cookie cookie) {
        }

        @Override
        public boolean containsHeader(String name) {
            return headers.containsKey(name);
        }

        @Override
        public String encodeURL(String url) {
            return url;
        }

        @Override
        public String encodeRedirectURL(String url) {
            return url;
        }

        @Override
        public void sendError(int sc, String msg) {
        }

        @Override
        public void sendError(int sc) {
        }

        @Override
        public void sendRedirect(String location) {
        }

        @Override
        public void setDateHeader(String name, long date) {
        }

        @Override
        public void addDateHeader(String name, long date) {
        }

        @Override
        public void setIntHeader(String name, int value) {
        }

        @Override
        public void addIntHeader(String name, int value) {
        }

        @Override
        public java.util.Collection<String> getHeaders(String name) {
            return Collections.emptyList();
        }

        @Override
        public java.util.Collection<String> getHeaderNames() {
            return headers.keySet();
        }

        @Override
        public String getCharacterEncoding() {
            return "UTF-8";
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public jakarta.servlet.ServletOutputStream getOutputStream() {
            return null;
        }

        @Override
        public java.io.PrintWriter getWriter() {
            return null;
        }

        @Override
        public void setCharacterEncoding(String charset) {
        }

        @Override
        public void setContentLength(int len) {
        }

        @Override
        public void setContentLengthLong(long length) {
        }

        @Override
        public void setContentType(String type) {
        }

        @Override
        public void setBufferSize(int size) {
        }

        @Override
        public int getBufferSize() {
            return 8192;
        }

        @Override
        public void flushBuffer() {
        }

        @Override
        public void resetBuffer() {
        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public void reset() {
        }

        @Override
        public void setLocale(java.util.Locale loc) {
        }

        @Override
        public java.util.Locale getLocale() {
            return java.util.Locale.getDefault();
        }
    }

    private static class MockFilterChain implements FilterChain {
        private boolean chainCalled = false;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            chainCalled = true;
        }

        public boolean wasChainCalled() {
            return chainCalled;
        }
    }
}
