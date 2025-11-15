/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.jakarta.servlet.filter.auth.dummy.noauth;

import java.io.IOException;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterAsyncSupported;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterDispatcher;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Designate(ocd = OCD.class, factory = true)
@HttpWhiteboardFilterName("NoAuthFilter")
@HttpWhiteboardFilterAsyncSupported
@HttpWhiteboardFilterDispatcher
@Component(service = Filter.class, configurationPid = NoAuthDummyFilter.PID)
public class NoAuthDummyFilter implements Filter {

    public static final String PID = "daanse.jakarta.servlet.filter.auth.dummy.noauth.NoAuthDummyFilter";

    private static final Logger logger = LoggerFactory.getLogger(NoAuthDummyFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("Initializing NoAuthDummyFilter.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest request) || !(res instanceof HttpServletResponse response)) {
            logger.debug("Non-HTTP request/response encountered, passing through filter chain");
            chain.doFilter(req, res);
            return;
        }

        final UserRolePrincipal principal = new UserRolePrincipal("NoAuthDummyUser", Set.of());

        HttpServletRequest authRequest = new AuthHttpServletRequest(request, principal);

        chain.doFilter(authRequest, res);
    }

    @Override
    public void destroy() {
        logger.info("Destroying NoAuthDummyFilter");
    }

}
