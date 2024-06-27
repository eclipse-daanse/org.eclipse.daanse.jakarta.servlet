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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;

@ObjectClassDefinition()
public @interface CorsFilterConfig {

    public static final String PREFIX_ = HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_INIT_PARAM_PREFIX;

    @AttributeDefinition
    public String allowedOrigins();

    @AttributeDefinition
    public String allowedTimingOrigins();

    @AttributeDefinition
    public String allowedMethods();

    @AttributeDefinition
    public String allowedHeaders();

    @AttributeDefinition
    public String preflightMaxAge();

    @AttributeDefinition
    public String allowCredentials();

    @AttributeDefinition
    public String exposedHeaders();

    @AttributeDefinition
    public String forwardPreflight();

    @AttributeDefinition
    public String chainPreflight();
}
