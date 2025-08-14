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

import org.eclipse.daanse.jakarta.servlet.filter.cors.api.Constants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi Metatype configuration interface for the CORS filter.
 *
 * This annotation interface defines all configurable properties for the CORS filter, including
 * localization keys for property names and descriptions. The configuration supports
 * internationalization through OSGi localization bundles.
 */
@ObjectClassDefinition(name = CorsFilterConfig.L10N_OCD_CORS_FILTER_NAME, description = CorsFilterConfig.L10N_OCD_CORS_FILTER_DESCRIPTION, localization = CorsFilterConfig.OCD_LOCALIZATION)
public @interface CorsFilterConfig {

    // Localization configuration
    String OCD_LOCALIZATION = "OSGI-INF/l10n/org.eclipse.daanse.jakarta.servlet.filter.cors.ocd";
    String L10N_PREFIX = "%";
    String L10N_POSTFIX_DESCRIPTION = ".description";
    String L10N_POSTFIX_NAME = ".name";

    // Localization keys for allowed origins
    String L10N_ALLOWED_ORIGINS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWED_ORIGINS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_ORIGINS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWED_ORIGINS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for timing origins
    String L10N_ALLOWED_TIMING_ORIGINS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWED_TIMING_ORIGINS_PARAM
            + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_TIMING_ORIGINS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWED_TIMING_ORIGINS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for allowed methods
    String L10N_ALLOWED_METHODS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWED_METHODS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_METHODS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWED_METHODS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for allowed headers
    String L10N_ALLOWED_HEADERS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOWED_HEADERS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_HEADERS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOWED_HEADERS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for preflight max age
    String L10N_PREFLIGHT_MAX_AGE_NAME = L10N_PREFIX + Constants.PROPERTY_PREFLIGHT_MAX_AGE_PARAM + L10N_POSTFIX_NAME;
    String L10N_PREFLIGHT_MAX_AGE_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_PREFLIGHT_MAX_AGE_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for credentials
    String L10N_ALLOW_CREDENTIALS_NAME = L10N_PREFIX + Constants.PROPERTY_ALLOW_CREDENTIALS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOW_CREDENTIALS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_ALLOW_CREDENTIALS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for exposed headers
    String L10N_EXPOSED_HEADERS_NAME = L10N_PREFIX + Constants.PROPERTY_EXPOSED_HEADERS_PARAM + L10N_POSTFIX_NAME;
    String L10N_EXPOSED_HEADERS_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_EXPOSED_HEADERS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Localization keys for preflight chaining
    String L10N_CHAIN_PREFLIGHT_NAME = L10N_PREFIX + Constants.PROPERTY_CHAIN_PREFLIGHT_PARAM + L10N_POSTFIX_NAME;
    String L10N_CHAIN_PREFLIGHT_DESCRIPTION = L10N_PREFIX + Constants.PROPERTY_CHAIN_PREFLIGHT_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    // Default configuration values
    /** Default preflight cache duration in seconds (30 minutes) */
    int DEFAULT_PREFLIGHT_MAX_AGE = 1800;
    /** Default credential support setting */
    boolean DEFAULT_ALLOW_CREDENTIALS = true;
    /** Default preflight chaining behavior */
    boolean DEFAULT_CHAIN_PREFLIGHT = true;

    /**
     * Configures the allowed origins for CORS requests. Use "*" to allow all origins, or specify exact
     * origins or wildcard patterns.
     */
    @AttributeDefinition(name = L10N_ALLOWED_ORIGINS_NAME, description = L10N_ALLOWED_ORIGINS_DESCRIPTION)
    String[] allowedOrigins() default {};

    /**
     * Configures origins allowed to access timing information via Timing-Allow-Origin header.
     */
    @AttributeDefinition(name = L10N_ALLOWED_TIMING_ORIGINS_NAME, description = L10N_ALLOWED_TIMING_ORIGINS_DESCRIPTION, defaultValue = {})
    String[] allowedTimingOrigins() default {};

    /**
     * Configures the HTTP methods allowed for CORS requests.
     */
    @AttributeDefinition(name = L10N_ALLOWED_METHODS_NAME, description = L10N_ALLOWED_METHODS_DESCRIPTION)
    String[] allowedMethods() default { "GET", "POST", "HEAD" };

    /**
     * Configures the request headers allowed for CORS requests. Use "*" to allow all headers.
     */
    @AttributeDefinition(name = L10N_ALLOWED_HEADERS_NAME, description = L10N_ALLOWED_HEADERS_DESCRIPTION)
    String[] allowedHeaders() default { "X-Requested-With", "Content-Type", "Accept", "Origin" };

    /**
     * Configures how long (in seconds) preflight responses can be cached by browsers.
     */
    @AttributeDefinition(name = L10N_PREFLIGHT_MAX_AGE_NAME, description = L10N_PREFLIGHT_MAX_AGE_DESCRIPTION, defaultValue = DEFAULT_PREFLIGHT_MAX_AGE
            + "")
    int preflightMaxAge() default DEFAULT_PREFLIGHT_MAX_AGE;

    /**
     * Configures whether credentials (cookies, authorization headers) are allowed in CORS requests.
     */
    @AttributeDefinition(name = L10N_ALLOW_CREDENTIALS_NAME, description = L10N_ALLOW_CREDENTIALS_DESCRIPTION, defaultValue = DEFAULT_ALLOW_CREDENTIALS
            + "")
    boolean allowCredentials() default true;

    /**
     * Configures which response headers are exposed to the client in CORS requests.
     */
    @AttributeDefinition(name = L10N_EXPOSED_HEADERS_NAME, description = L10N_EXPOSED_HEADERS_DESCRIPTION)
    String[] exposedHeaders() default {};

    /**
     * Configures whether preflight requests are forwarded to the application or handled entirely by the
     * filter.
     */
    @AttributeDefinition(name = L10N_CHAIN_PREFLIGHT_NAME, description = L10N_CHAIN_PREFLIGHT_DESCRIPTION, defaultValue = DEFAULT_CHAIN_PREFLIGHT
            + "")
    boolean chainPreflight() default DEFAULT_CHAIN_PREFLIGHT;

    // Object class definition localization
    String L10N_OCD_CORS_FILTER_NAME = L10N_PREFIX + "ocd" + ".corsFilter" + L10N_POSTFIX_NAME;
    String L10N_OCD_CORS_FILTER_DESCRIPTION = L10N_PREFIX + "ocd" + ".corsFilter" + L10N_POSTFIX_DESCRIPTION;
}
