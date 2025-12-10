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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.daanse.jakarta.servlet.filter.cors.api.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A servlet filter that implements Cross-Origin Resource Sharing (CORS) functionality.
 *
 * This filter handles CORS requests by:
 * - Processing simple CORS requests by setting appropriate response headers
 * - Handling preflight OPTIONS requests for complex CORS scenarios
 * - Validating origins, methods, and headers against configured allowed lists
 * - Supporting wildcard patterns for flexible origin matching
 * - Managing timing information access through Timing-Allow-Origin header
 *
 * The filter can be configured through OSGi Configuration Admin to customize:
 * - Allowed origins (with wildcard support)
 * - Allowed HTTP methods
 * - Allowed request headers
 * - Exposed response headers
 * - Preflight request cache duration
 * - Credential support
 * - Request chaining behavior
 */
@Component(service = Filter.class, configurationPid = Constants.PID_FILTER_CORS)
@Designate(ocd = CorsFilterConfig.class, factory = true)
@RequireHttpWhiteboard
public class CorsFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsFilter.class);

    // Origin configuration flags
    private boolean anyOriginAllowed;
    private boolean anyTimingOriginAllowed;
    private boolean anyHeadersAllowed;

    // Collections for storing allowed origins and patterns
    private final Set<String> allowedOrigins = new HashSet<>();
    private final List<Pattern> allowedOriginPatterns = new ArrayList<>();
    private final Set<String> allowedTimingOrigins = new HashSet<>();
    private final List<Pattern> allowedTimingOriginPatterns = new ArrayList<>();

    // CORS policy configuration
    private final List<String> allowedMethods = new ArrayList<>();
    private final List<String> allowedHeaders = new ArrayList<>();
    private final List<String> exposedHeaders = new ArrayList<>();
    private int preflightMaxAge;
    private boolean allowCredentials;
    private boolean chainPreflight;

    /**
     * Activates the CORS filter with the provided configuration.
     *
     * @param config the OSGi configuration containing CORS settings
     * @throws ServletException if configuration is invalid
     */
    @Activate
    public void activate(CorsFilterConfig config) throws ServletException {

        anyOriginAllowed = calculateAllowedOrigins(allowedOrigins, allowedOriginPatterns, config.allowedOrigins());

        anyTimingOriginAllowed = calculateAllowedOrigins(allowedTimingOrigins, allowedTimingOriginPatterns,
                config.allowedTimingOrigins());

        allowedMethods.addAll(List.of(config.allowedMethods()));

        List<String> tmpAllowedHeaders = List.of(config.allowedHeaders());
        if (tmpAllowedHeaders.contains("*")) {
            anyHeadersAllowed = true;
        } else {
            allowedHeaders.addAll(tmpAllowedHeaders);
        }

        preflightMaxAge = config.preflightMaxAge();
        allowCredentials = config.allowCredentials();
        exposedHeaders.addAll(List.of(config.exposedHeaders()));
        chainPreflight = config.chainPreflight();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("CORS Filter successfully activated with configuration:");
            LOGGER.info("  {} = {}", Constants.PROPERTY_ALLOWED_ORIGINS_PARAM, anyOriginAllowed ? "[Any origin allowed]" : allowedOrigins);
            LOGGER.info("  {} = {}", Constants.PROPERTY_ALLOWED_TIMING_ORIGINS_PARAM, anyTimingOriginAllowed ? "[Any timing origin allowed]" : allowedTimingOrigins);
            LOGGER.info("  {} = {}", Constants.PROPERTY_ALLOWED_METHODS_PARAM, allowedMethods);
            LOGGER.info("  {} = {}", Constants.PROPERTY_ALLOWED_HEADERS_PARAM, anyHeadersAllowed ? "[Any headers allowed]" : allowedHeaders);
            LOGGER.info("  {} = {} seconds", Constants.PROPERTY_PREFLIGHT_MAX_AGE_PARAM, preflightMaxAge);
            LOGGER.info("  {} = {}", Constants.PROPERTY_ALLOW_CREDENTIALS_PARAM, allowCredentials);
            LOGGER.info("  {} = {}", Constants.PROPERTY_EXPOSED_HEADERS_PARAM, exposedHeaders);
            LOGGER.info("  {} = {}", Constants.PROPERTY_CHAIN_PREFLIGHT_PARAM, chainPreflight);
        }
    }

    /**
     * Processes origin configuration and populates origin collections.
     *
     * @param origins the set to store exact origin matches
     * @param originPatterns the list to store compiled regex patterns for wildcard origins
     * @param originsConfig array of configured origin strings
     * @return true if any origin is allowed (wildcard "*" found), false otherwise
     */
    private boolean calculateAllowedOrigins(Set<String> origins, List<Pattern> originPatterns, String[] originsConfig) {

        for (String origin : originsConfig) {
            if (!origin.isBlank()) {
                if (Constants.ANY_ORIGIN.equals(origin)) {
                    origins.clear();
                    originPatterns.clear();
                    return true;
                } else if (origin.contains("*")) {
                    originPatterns.add(Pattern.compile(parseAllowedWildcardOriginToRegex(origin)));
                } else {
                    origins.add(origin);
                }
            }
        }
        return false;
    }

    /**
     * Main filter entry point that processes all incoming requests.
     *
     * @param request the servlet request
     * @param response the servlet response
     * @param chain the filter chain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    /**
     * Core CORS processing logic that handles different types of CORS requests.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param chain the filter chain for request forwarding
     */
    private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        response.addHeader("Vary", Constants.HEADER_REQUEST_ORIGIN);
        String origin = request.getHeader(Constants.HEADER_REQUEST_ORIGIN);

        if (origin != null) {
            if (anyOriginAllowed || originMatches(allowedOrigins, allowedOriginPatterns, origin)) {
                if (isSimpleRequest(request)) {
                    LOGGER.debug("Processing simple CORS request to {}",
                            request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                } else if (isPreflightRequest(request)) {
                    LOGGER.debug("Processing preflight CORS request to {}",
                            request.getRequestURI());
                    handlePreflightResponse(request, response, origin);
                    if (chainPreflight)
                        LOGGER.debug("Preflight request to {} chained to application",
                                request.getRequestURI());
                    else
                        return;
                } else {
                    LOGGER.debug("Processing complex CORS request to {}",
                            request.getRequestURI());
                    handleSimpleResponse(request, response, origin);
                }

                if (anyTimingOriginAllowed
                        || originMatches(allowedTimingOrigins, allowedTimingOriginPatterns, origin)) {
                    response.setHeader(Constants.HEADER_RESPONSE_TIMING_ALLOW_ORIGIN, origin);
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Origin '{}' for request to {} not permitted for timing headers. Allowed timing origins: {}",
                            origin, request.getRequestURI(), allowedTimingOrigins);
                }
            } else if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("CORS request to {} rejected: origin '{}' not in allowed origins list: {}",
                        request.getRequestURI(), origin, allowedOrigins);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Checks if the provided origin(s) match any of the allowed origins or patterns.
     *
     * @param allowedOrigins set of exact origin matches
     * @param allowedOriginPatterns list of regex patterns for wildcard matching
     * @param originList space-separated list of origins to validate
     * @return true if any origin matches the allowed configuration
     */
    private boolean originMatches(Set<String> allowedOrigins, List<Pattern> allowedOriginPatterns, String originList) {
        if (originList.trim().length() == 0)
            return false;

        String[] origins = originList.split(" ");
        for (String origin : origins) {
            if (origin.trim().length() == 0)
                continue;

            if (allowedOrigins.contains(origin))
                return true;

            for (Pattern allowedOrigin : allowedOriginPatterns) {
                if (allowedOrigin.matcher(origin).matches())
                    return true;
            }
        }
        return false;
    }

    /**
     * Converts a wildcard origin pattern to a regular expression.
     * Escapes dots and converts asterisks to regex wildcards.
     *
     * @param allowedOrigin origin pattern with wildcards
     * @return regex pattern string
     */
    private String parseAllowedWildcardOriginToRegex(String allowedOrigin) {
        String regex = allowedOrigin.replace(".", "\\.");
        return regex.replace("*", ".*");
    }

    /**
     * Determines if the request is a simple CORS request according to CORS specification.
     * Simple requests use standard HTTP methods (GET, POST, HEAD) and don't require preflight.
     *
     * Note: This implementation uses a simplified header check by looking for the absence
     * of Access-Control-Request-Method header, which is required for preflight requests.
     *
     * @param request the HTTP request to evaluate
     * @return true if this is a simple CORS request
     */
    private boolean isSimpleRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (Constants.SIMPLE_HTTP_METHODS.contains(method)) {
            // Simplified detection: preflight requests must include Access-Control-Request-Method
            return request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD) == null;
        }
        return false;
    }

    /**
     * Determines if the request is a CORS preflight request.
     * Preflight requests are OPTIONS requests that include the Access-Control-Request-Method header.
     *
     * @param request the HTTP request to evaluate
     * @return true if this is a preflight CORS request
     */
    private boolean isPreflightRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (!"OPTIONS".equalsIgnoreCase(method))
            return false;
        return request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD) != null;
    }

    /**
     * Sets response headers for simple CORS requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response to modify
     * @param origin the validated origin to include in response headers
     */
    private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        if (allowCredentials)
            response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        if (!exposedHeaders.isEmpty())
            response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_EXPOSE_HEADERS, join(exposedHeaders));
    }

    /**
     * Handles preflight CORS requests by validating the requested method and headers,
     * then setting appropriate response headers if the request is allowed.
     *
     * @param request the preflight HTTP request
     * @param response the HTTP response to configure
     * @param origin the validated origin for the response
     */
    private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
        boolean methodAllowed = isMethodAllowed(request);

        if (!methodAllowed)
            return;
        List<String> headersRequested = getAccessControlRequestHeaders(request);
        boolean headersAllowed = areHeadersAllowed(headersRequested);
        if (!headersAllowed)
            return;
        response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        if (allowCredentials)
            response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        if (preflightMaxAge > 0)
            response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_MAX_AGE, String.valueOf(preflightMaxAge));
        response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_METHODS, join(allowedMethods));
        if (anyHeadersAllowed)
            response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS, join(headersRequested));
        else
            response.setHeader(Constants.HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS, join(allowedHeaders));
    }

    /**
     * Validates if the requested HTTP method is allowed for CORS requests.
     *
     * @param request the HTTP request containing the method to validate
     * @return true if the method is in the allowed methods list
     */
    private boolean isMethodAllowed(HttpServletRequest request) {
        String accessControlRequestMethod = request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD);
        LOGGER.debug("Validating requested method: {} = {}", Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD, accessControlRequestMethod);
        boolean result = false;
        if (accessControlRequestMethod != null)
            result = allowedMethods.contains(accessControlRequestMethod);
        if (result) {
            LOGGER.debug("Method '{}' is permitted. Allowed methods: {}", accessControlRequestMethod, allowedMethods);
        } else {
            LOGGER.debug("Method '{}' is not permitted. Allowed methods: {}", accessControlRequestMethod, allowedMethods);
        }
        return result;
    }

    /**
     * Extracts and parses the requested headers from the Access-Control-Request-Headers header.
     *
     * @param request the HTTP request containing headers to parse
     * @return list of individual header names requested by the client
     */
    private List<String> getAccessControlRequestHeaders(HttpServletRequest request) {

        String accessControlRequestHeaders = request.getHeader(Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS);

        LOGGER.debug("Requested headers: {} = {}", Constants.HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS, accessControlRequestHeaders);
        if (accessControlRequestHeaders == null)
            return Collections.emptyList();

        List<String> requestedHeaders = new ArrayList<>();
        String[] headers = accessControlRequestHeaders.split(",");
        for (String header : headers) {
            String h = header.trim();
            if (h.length() > 0)
                requestedHeaders.add(h);
        }
        return requestedHeaders;
    }

    /**
     * Validates if all requested headers are allowed according to the CORS policy.
     *
     * @param requestedHeaders list of headers requested by the client
     * @return true if all headers are allowed, false if any header is forbidden
     */
    private boolean areHeadersAllowed(List<String> requestedHeaders) {
        if (anyHeadersAllowed) {
            LOGGER.debug("All headers are permitted (wildcard configuration active)");
            return true;
        }

        boolean result = true;
        for (String requestedHeader : requestedHeaders) {
            boolean headerAllowed = false;
            for (String allowedHeader : allowedHeaders) {
                if (requestedHeader.equalsIgnoreCase(allowedHeader.trim())) {
                    headerAllowed = true;
                    break;
                }
            }
            if (!headerAllowed) {
                result = false;
                break;
            }
        }
        if (result) {
            LOGGER.debug("All requested headers {} are permitted. Allowed headers: {}", requestedHeaders, allowedHeaders);
        } else {
            LOGGER.debug("One or more requested headers {} are not permitted. Allowed headers: {}", requestedHeaders, allowedHeaders);
        }
        return result;
    }

    /**
     * Joins a list of strings with commas for use in HTTP headers.
     *
     * @param strings list of strings to join
     * @return comma-separated string
     */
    private String join(List<String> strings) {
        return String.join(",", strings);
    }

    /**
     * Cleans up resources when the filter is destroyed.
     * Resets all configuration to default values and clears collections.
     */
    @Override
    public void destroy() {
        anyOriginAllowed = false;
        anyTimingOriginAllowed = false;
        allowedOrigins.clear();
        allowedOriginPatterns.clear();
        allowedTimingOrigins.clear();
        allowedTimingOriginPatterns.clear();
        allowedMethods.clear();
        allowedHeaders.clear();
        preflightMaxAge = 0;
        allowCredentials = false;
    }
}
