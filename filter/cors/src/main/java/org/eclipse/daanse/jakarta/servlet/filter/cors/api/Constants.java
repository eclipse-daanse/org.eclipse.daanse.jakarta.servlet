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
package org.eclipse.daanse.jakarta.servlet.filter.cors.api;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for CORS filter configuration and HTTP headers.
 *
 * This class contains all the constants used by the CORS filter implementation,
 * including HTTP header names, configuration property names, and default values.
 */
public class Constants {

    /**
     * OSGi configuration PID for the CORS filter component.
     */
    public static final String PID_FILTER_CORS = "daanse.jakarta.servlet.filter.cors.CorsFilter";

    // CORS request headers
    public static final String HEADER_REQUEST_ORIGIN = "Origin";
    public static final String HEADER_REQUEST_ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String HEADER_REQUEST_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    // CORS response headers
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String HEADER_RESPONSE_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String HEADER_RESPONSE_TIMING_ALLOW_ORIGIN = "Timing-Allow-Origin";

    // Configuration property names
    public static final String PROPERTY_ALLOWED_ORIGINS_PARAM = "allowedOrigins";
    public static final String PROPERTY_ALLOWED_TIMING_ORIGINS_PARAM = "allowedTimingOrigins";
    public static final String PROPERTY_ALLOWED_METHODS_PARAM = "allowedMethods";
    public static final String PROPERTY_ALLOWED_HEADERS_PARAM = "allowedHeaders";
    public static final String PROPERTY_PREFLIGHT_MAX_AGE_PARAM = "preflightMaxAge";
    public static final String PROPERTY_ALLOW_CREDENTIALS_PARAM = "allowCredentials";
    public static final String PROPERTY_EXPOSED_HEADERS_PARAM = "exposedHeaders";
    public static final String PROPERTY_CHAIN_PREFLIGHT_PARAM = "chainPreflight";

    // Default values and constants
    /** Wildcard value indicating any origin is allowed */
    public static final String ANY_ORIGIN = "*";
    /** Default allowed origins configuration */
    public static final String[] DEFAULT_ALLOWED_ORIGINS = { ANY_ORIGIN };
    /** Default allowed timing origins (empty by default) */
    public static final String[] DEFAULT_ALLOWED_TIMING_ORIGINS = {};
    /** HTTP methods that are considered "simple" according to CORS specification */
    public static final List<String> SIMPLE_HTTP_METHODS = Arrays.asList("GET", "POST", "HEAD");
    /** Default allowed HTTP methods */
    public static final List<String> DEFAULT_ALLOWED_METHODS = Arrays.asList("GET", "POST", "HEAD");
    /** Default allowed request headers */
    public static final List<String> DEFAULT_ALLOWED_HEADERS = Arrays.asList("X-Requested-With", "Content-Type",
            "Accept", "Origin");
}
