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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
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

    private final String dummyUserName;
    private final Set<String> dummyRoles;

    @Activate
    public NoAuthDummyFilter(OCD config) {
        this.dummyUserName = config.dummyUserName();
        this.dummyRoles = Set.of(config.dummyRoles());
    }

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

        // If a Basic Authorization header is present, take the username from it without
        // checking the password. Otherwise fall back to the configurable dummy user.
        // This filter never rejects a request (no 401).
        UserRolePrincipal principal = principalFromBasicAuth(request)
                .orElseGet(() -> new UserRolePrincipal(dummyUserName, dummyRoles));

        HttpServletRequest authRequest = new AuthHttpServletRequest(request, principal);

        chain.doFilter(authRequest, res);
    }

    private static Optional<UserRolePrincipal> principalFromBasicAuth(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.regionMatches(true, 0, "Basic ", 0, 6)) {
            return Optional.empty();
        }

        String base64 = auth.substring(6).trim();
        String userPass;
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            userPass = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid Base64 encoding in Authorization header from {}: {}", request.getRemoteAddr(),
                    e.getMessage());
            return Optional.empty();
        }

        int sep = userPass.indexOf(':');
        // No password validation: only the username part is used.
        String userPart = sep < 0 ? userPass : userPass.substring(0, sep);
        if (userPart.isEmpty()) {
            return Optional.empty();
        }

        logger.debug("Authenticated user '{}' (password ignored) from {}", userPart, request.getRemoteAddr());
        // username = role
        return Optional.of(new UserRolePrincipal(userPart, Set.of(userPart)));
    }

    @Override
    public void destroy() {
        logger.info("Destroying NoAuthDummyFilter");
    }

}
