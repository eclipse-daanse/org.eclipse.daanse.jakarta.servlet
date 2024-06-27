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
package org.eclipse.daanse.jakarta.servlet.filter.jetty.api;

import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;

/**
 * All Constants for PIDs and there configuration Properties.
 */
public class Constants {

    private static final String PREFIX_FILTER_INIT = HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_INIT_PARAM_PREFIX;

    /**
     * PID of the CrossOriginFilter
     */
    public static final String PID_FILTER_CORS = "org.eclipse.daanse.jakarta.servlet.filter.jetty.CorsFilter";

    /**
     * {@link CrossOriginFilter#ALLOWED_ORIGINS_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_ALLOWED_ORIGINS_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.ALLOWED_ORIGINS_PARAM;
    /**
     * {@link CrossOriginFilter#ALLOWED_TIMING_ORIGINS_PARAM} Configuration Property
     * for the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_ALLOWED_TIMING_ORIGINS_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.ALLOWED_TIMING_ORIGINS_PARAM;
    /**
     * {@link CrossOriginFilter#ALLOWED_METHODS_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_ALLOWED_METHODS_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.ALLOWED_METHODS_PARAM;
    /**
     * {@link CrossOriginFilter#ALLOWED_HEADERS_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_ALLOWED_HEADERS_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.ALLOWED_HEADERS_PARAM;
    /**
     * {@link CrossOriginFilter#PREFLIGHT_MAX_AGE_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_PREFLIGHT_MAX_AGE_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.PREFLIGHT_MAX_AGE_PARAM;
    /**
     * {@link CrossOriginFilter#ALLOW_CREDENTIALS_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_ALLOW_CREDENTIALS_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.ALLOW_CREDENTIALS_PARAM;
    /**
     * {@link CrossOriginFilter#EXPOSED_HEADERS_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_EXPOSED_HEADERS_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.EXPOSED_HEADERS_PARAM;
    /**
     * {@link CrossOriginFilter#CHAIN_PREFLIGHT_PARAM} Configuration Property for
     * the of the CrossOriginFilter
     */
    public static final String FILTER_CORS_PROP_INIT_CHAIN_PREFLIGHT_PARAM = PREFIX_FILTER_INIT
            + CrossOriginFilter.CHAIN_PREFLIGHT_PARAM;

}
