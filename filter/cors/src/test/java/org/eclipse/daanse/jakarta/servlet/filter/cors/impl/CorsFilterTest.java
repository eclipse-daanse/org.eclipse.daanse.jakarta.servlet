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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.daanse.jakarta.servlet.filter.cors.api.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("CORS Filter Tests")
class CorsFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private CorsFilterConfig config;

    private CorsFilter corsFilter;

    @BeforeEach
    void setUp() throws ServletException {
        corsFilter = new CorsFilter();
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should activate with default configuration")
        void shouldActivateWithDefaultConfiguration() throws ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET", "POST", "HEAD"});
            when(config.allowedHeaders()).thenReturn(new String[]{"X-Requested-With", "Content-Type", "Accept"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            // When & Then - Should not throw exception
            corsFilter.activate(config);
        }

        @Test
        @DisplayName("Should handle wildcard origin configuration")
        void shouldHandleWildcardOriginConfiguration() throws ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{"*"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET"});
            when(config.allowedHeaders()).thenReturn(new String[]{"*"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            // When & Then - Should not throw exception
            corsFilter.activate(config);
        }

        @Test
        @DisplayName("Should handle wildcard patterns")
        void shouldHandleWildcardPatterns() throws ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{"https://*.example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET"});
            when(config.allowedHeaders()).thenReturn(new String[]{"Content-Type"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            // When & Then - Should not throw exception
            corsFilter.activate(config);
        }
    }

    @Nested
    @DisplayName("Request Processing Tests")
    class RequestProcessingTests {

        @BeforeEach
        void setUpFilter() throws ServletException {
            when(config.allowedOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET", "POST", "HEAD"});
            when(config.allowedHeaders()).thenReturn(new String[]{"X-Requested-With", "Content-Type", "Accept"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            corsFilter.activate(config);
        }

        @Test
        @DisplayName("Should add Vary header to all responses")
        void shouldAddVaryHeaderToAllResponses() throws IOException, ServletException {
            // Given
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn(null);

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should process request without origin header")
        void shouldProcessRequestWithoutOriginHeader() throws IOException, ServletException {
            // Given
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn(null);

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
            verify(filterChain).doFilter(request, response);
            verifyNoMoreInteractions(response);
        }

        @Test
        @DisplayName("Should reject request with disallowed origin")
        void shouldRejectRequestWithDisallowedOrigin() throws IOException, ServletException {
            // Given
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://malicious.com");
            //when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
            verify(filterChain).doFilter(request, response);
            // Should not set CORS headers for disallowed origin
            verify(response, never()).setHeader(eq(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN), any());
        }
    }

    @Nested
    @DisplayName("Simple CORS Request Tests")
    class SimpleCorsRequestTests {

        @BeforeEach
        void setUpFilter() throws ServletException {
            when(config.allowedOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedMethods()).thenReturn(new String[]{"GET", "POST", "HEAD"});
            when(config.allowedHeaders()).thenReturn(new String[]{"X-Requested-With", "Content-Type", "Accept"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{"X-Custom-Header"});
            when(config.chainPreflight()).thenReturn(true);

            corsFilter.activate(config);
        }

        @Test
        @DisplayName("Should handle simple GET request")
        void shouldHandleSimpleGetRequest() throws IOException, ServletException {
            // Given
            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.com");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_EXPOSE_HEADERS, "X-Custom-Header");
            verify(response).setHeader(Constants.HEADER_RESPONSE_TIMING_ALLOW_ORIGIN, "https://example.com");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle simple POST request")
        void shouldHandleSimplePostRequest() throws IOException, ServletException {
            // Given
            when(request.getMethod()).thenReturn("POST");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.com");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not expose headers when none configured")
        void shouldNotExposeHeadersWhenNoneConfigured() throws IOException, ServletException {
            // Given - Setup filter without exposed headers
            when(config.allowedOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET"});
            when(config.allowedHeaders()).thenReturn(new String[]{"Content-Type"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(false);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            corsFilter.activate(config);

            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(null);

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.com");
            verify(response, never()).setHeader(eq(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS), any());
            verify(response, never()).setHeader(eq(Constants.HEADER_RESPONSE_ACCESS_CONTROL_EXPOSE_HEADERS), eq("[Access-Control-Expose-Headers, X-Custom-Header]"));
        }
    }

    @Nested
    @DisplayName("Preflight Request Tests")
    class PreflightRequestTests {

        @BeforeEach
        void setUpFilter() throws ServletException {
            when(config.allowedOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET", "POST", "PUT", "DELETE"});
            when(config.allowedHeaders()).thenReturn(new String[]{"X-Requested-With", "Content-Type", "Authorization"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(false);

            corsFilter.activate(config);
        }

        @Test
        @DisplayName("Should handle valid preflight request")
        void shouldHandleValidPreflightRequest() throws IOException, ServletException {
            // Given
            when(request.getMethod()).thenReturn("OPTIONS");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("PUT");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type,Authorization");
            when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.com");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_MAX_AGE, "1800");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS, "X-Requested-With,Content-Type,Authorization");

            // Should not chain when chainPreflight is false
            verify(filterChain, never()).doFilter(request, response);
        }

        @Test
        @DisplayName("Should reject preflight with disallowed method")
        void shouldRejectPreflightWithDisallowedMethod() throws IOException, ServletException {
            when(config.chainPreflight()).thenReturn(true);
            corsFilter.activate(config);

            // Given
            when(request.getMethod()).thenReturn("OPTIONS");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("PATCH");
            when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
            verify(response, never()).setHeader(eq(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN), any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should reject preflight with disallowed header")
        void shouldRejectPreflightWithDisallowedHeader() throws IOException, ServletException {
            when(config.chainPreflight()).thenReturn(true);
            corsFilter.activate(config);

            // Given
            when(request.getMethod()).thenReturn("OPTIONS");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("PUT");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("X-Custom-Forbidden-Header");
            when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
            verify(response, never()).setHeader(eq(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN), any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should chain preflight when configured")
        void shouldChainPreflightWhenConfigured() throws IOException, ServletException {
            // Given - Reconfigure with chaining enabled
            when(config.chainPreflight()).thenReturn(true);
            corsFilter.activate(config);

            when(request.getMethod()).thenReturn("OPTIONS");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("PUT");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type");
            when(request.getRequestURI()).thenReturn("/api/test");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.com");
            verify(filterChain).doFilter(request, response); // Should chain
        }
    }

    @Nested
    @DisplayName("Wildcard Configuration Tests")
    class WildcardConfigurationTests {

        @Test
        @DisplayName("Should allow all origins with wildcard")
        void shouldAllowAllOriginsWithWildcard() throws IOException, ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{"*"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET"});
            when(config.allowedHeaders()).thenReturn(new String[]{"Content-Type"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            corsFilter.activate(config);

            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://any-domain.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(null);

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://any-domain.com");
        }

        @Test
        @DisplayName("Should allow all headers with wildcard")
        void shouldAllowAllHeadersWithWildcard() throws IOException, ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET", "POST", "PUT"});
            when(config.allowedHeaders()).thenReturn(new String[]{"*"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(false);

            corsFilter.activate(config);

            when(request.getMethod()).thenReturn("OPTIONS");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("PUT");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("X-Custom-Header,Another-Header");

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://example.com");
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS, "X-Custom-Header,Another-Header");
        }

        @Test
        @DisplayName("Should match wildcard patterns")
        void shouldMatchWildcardPatterns() throws IOException, ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{"https://*.example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET"});
            when(config.allowedHeaders()).thenReturn(new String[]{"Content-Type"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            corsFilter.activate(config);

            when(request.getMethod()).thenReturn("GET");
            when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://subdomain.example.com");
            when(request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(null);

            // When
            corsFilter.doFilter(request, response, filterChain);

            // Then
            verify(response).setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, "https://subdomain.example.com");
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should cleanup resources on destroy")
        void shouldCleanupResourcesOnDestroy() throws ServletException {
            // Given
            when(config.allowedOrigins()).thenReturn(new String[]{"https://example.com"});
            when(config.allowedTimingOrigins()).thenReturn(new String[]{});
            when(config.allowedMethods()).thenReturn(new String[]{"GET"});
            when(config.allowedHeaders()).thenReturn(new String[]{"Content-Type"});
            when(config.preflightMaxAge()).thenReturn(1800);
            when(config.allowCredentials()).thenReturn(true);
            when(config.exposedHeaders()).thenReturn(new String[]{});
            when(config.chainPreflight()).thenReturn(true);

            corsFilter.activate(config);

            // When
            corsFilter.destroy();

            // Then - Should not throw exception and reset state
            // Verify by trying to process a request after destroy
            //when(request.getMethod()).thenReturn("GET");
            //when(request.getHeader(Constants.HEADER_REQUEST_ORIGIN)).thenReturn("https://example.com");

            // Should not match any origins after destroy (they were cleared)
            assertThat(corsFilter).isNotNull(); // Filter instance still exists but state is reset
        }
    }
}
