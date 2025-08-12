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
package org.eclipse.daanse.jakarta.servlet.filter.jetty.impl.cors;

import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;

@ObjectClassDefinition(name = CorsFilterConfig.L10N_OCD_CORS_FILTER_NAME, description = CorsFilterConfig.L10N_OCD_CORS_FILTER_DESCRIPTION, localization = CorsFilterConfig.OCD_LOCALIZATION)
public interface CorsFilterConfig {

    public static final String PREFIX_ = HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_INIT_PARAM_PREFIX;

    String OCD_LOCALIZATION = "OSGI-INF/l10n/org.eclipse.daanse.jakarta.servlet.filter.jetty.ocd";
    String L10N_PREFIX = "%";
    String L10N_POSTFIX_DESCRIPTION = ".description";
    String L10N_POSTFIX_NAME = ".name";

    String L10N_ALLOWED_ORIGINS_NAME = L10N_PREFIX + CrossOriginFilter.ALLOWED_ORIGINS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_ORIGINS_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.ALLOWED_ORIGINS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOWED_TIMING_ORIGINS_NAME = L10N_PREFIX + CrossOriginFilter.ALLOWED_TIMING_ORIGINS_PARAM
            + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_TIMING_ORIGINS_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.ALLOWED_TIMING_ORIGINS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOWED_METHODS_NAME = L10N_PREFIX + CrossOriginFilter.ALLOWED_METHODS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_METHODS_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.ALLOWED_METHODS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOWED_HEADERS_NAME = L10N_PREFIX + CrossOriginFilter.ALLOWED_HEADERS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOWED_HEADERS_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.ALLOWED_HEADERS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_PREFLIGHT_MAX_AGE_NAME = L10N_PREFIX + CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM + L10N_POSTFIX_NAME;
    String L10N_PREFLIGHT_MAX_AGE_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_ALLOW_CREDENTIALS_NAME = L10N_PREFIX + CrossOriginFilter.ALLOW_CREDENTIALS_PARAM + L10N_POSTFIX_NAME;
    String L10N_ALLOW_CREDENTIALS_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.ALLOW_CREDENTIALS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_EXPOSED_HEADERS_NAME = L10N_PREFIX + CrossOriginFilter.EXPOSED_HEADERS_PARAM + L10N_POSTFIX_NAME;
    String L10N_EXPOSED_HEADERS_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.EXPOSED_HEADERS_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String L10N_CHAIN_PREFLIGHT_NAME = L10N_PREFIX + CrossOriginFilter.CHAIN_PREFLIGHT_PARAM + L10N_POSTFIX_NAME;
    String L10N_CHAIN_PREFLIGHT_DESCRIPTION = L10N_PREFIX + CrossOriginFilter.CHAIN_PREFLIGHT_PARAM
            + L10N_POSTFIX_DESCRIPTION;

    String DEFAULT_ALLOWED_ORIGINS = "*";
    String DEFAULT_ALLOWED_TIMING_ORIGINS = "";
    String DEFAULT_ALLOWED_METHODS = "GET,POST,HEAD";
    String DEFAULT_ALLOWED_HEADERS = "X-Requested-With,Content-Type,Accept,Origin";
    String DEFAULT_PREFLIGHT_MAX_AGE = "1800";
    String DEFAULT_ALLOW_CREDENTIALS = "true";
    String DEFAULT_EXPOSED_HEADERS = "";
    String DEFAULT_CHAIN_PREFLIGHT = "true";

    @AttributeDefinition(name = L10N_ALLOWED_ORIGINS_NAME, description = L10N_ALLOWED_ORIGINS_DESCRIPTION, defaultValue = DEFAULT_ALLOWED_ORIGINS)
    default String allowedOrigins() {
        return DEFAULT_ALLOWED_ORIGINS;
    }

    @AttributeDefinition(name = L10N_ALLOWED_TIMING_ORIGINS_NAME, description = L10N_ALLOWED_TIMING_ORIGINS_DESCRIPTION, defaultValue = DEFAULT_ALLOWED_TIMING_ORIGINS)
    default String allowedTimingOrigins() {
        return DEFAULT_ALLOWED_TIMING_ORIGINS;
    }

    @AttributeDefinition(name = L10N_ALLOWED_METHODS_NAME, description = L10N_ALLOWED_METHODS_DESCRIPTION, defaultValue = DEFAULT_ALLOWED_METHODS)
    default String allowedMethods() {
        return DEFAULT_ALLOWED_METHODS;
    }

    @AttributeDefinition(name = L10N_ALLOWED_HEADERS_NAME, description = L10N_ALLOWED_HEADERS_DESCRIPTION, defaultValue = DEFAULT_ALLOWED_HEADERS)
    default String allowedHeaders() {
        return DEFAULT_ALLOWED_HEADERS;
    }

    @AttributeDefinition(name = L10N_PREFLIGHT_MAX_AGE_NAME, description = L10N_PREFLIGHT_MAX_AGE_DESCRIPTION, defaultValue = DEFAULT_PREFLIGHT_MAX_AGE
            + "")
    default String preflightMaxAge() {
        return DEFAULT_PREFLIGHT_MAX_AGE;
    }

    @AttributeDefinition(name = L10N_ALLOW_CREDENTIALS_NAME, description = L10N_ALLOW_CREDENTIALS_DESCRIPTION, defaultValue = DEFAULT_ALLOW_CREDENTIALS
            + "")
    default String allowCredentials() {
        return DEFAULT_ALLOW_CREDENTIALS;
    }

    @AttributeDefinition(name = L10N_EXPOSED_HEADERS_NAME, description = L10N_EXPOSED_HEADERS_DESCRIPTION, defaultValue = DEFAULT_EXPOSED_HEADERS)
    default String exposedHeaders() {
        return DEFAULT_EXPOSED_HEADERS;
    }

    @AttributeDefinition(name = L10N_CHAIN_PREFLIGHT_NAME, description = L10N_CHAIN_PREFLIGHT_DESCRIPTION, defaultValue = DEFAULT_CHAIN_PREFLIGHT
            + "")
    default String chainPreflight() {
        return DEFAULT_CHAIN_PREFLIGHT;
    }

    String L10N_OCD_CORS_FILTER_NAME = L10N_PREFIX + "ocd" + ".corsFilter" + L10N_POSTFIX_NAME;
    String L10N_OCD_CORS_FILTER_DESCRIPTION = L10N_PREFIX + "ocd" + ".corsFilter" + L10N_POSTFIX_DESCRIPTION;
}
