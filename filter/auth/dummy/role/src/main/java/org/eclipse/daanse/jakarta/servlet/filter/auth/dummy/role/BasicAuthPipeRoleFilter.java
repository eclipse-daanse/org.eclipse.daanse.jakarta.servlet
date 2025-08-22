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
package org.eclipse.daanse.jakarta.servlet.filter.auth.dummy.role;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterAsyncSupported;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterDispatcher;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardFilterName;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@HttpWhiteboardFilterName("BasicAuthPipeRoleFilter")
@HttpWhiteboardFilterAsyncSupported
@HttpWhiteboardFilterDispatcher
@Component(service = Filter.class)
public class BasicAuthPipeRoleFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(BasicAuthPipeRoleFilter.class);
    private static final String REALM = "daanse";

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("Initializing BasicAuthPipeRoleFilter with realm: {}", REALM);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest request) || !(res instanceof HttpServletResponse response)) {
            logger.debug("Non-HTTP request/response encountered, passing through filter chain");
            chain.doFilter(req, res);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.regionMatches(true, 0, "Basic ", 0, 6)) {
            logger.debug("Missing or invalid Authorization header from {}", request.getRemoteAddr());
            unauthorized(response);
            return;
        }

        String base64 = auth.substring(6).trim();
        String userPass;
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            userPass = new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid Base64 encoding in Authorization header from {}: {}", request.getRemoteAddr(),
                    e.getMessage());
            unauthorized(response);
            return;
        }

        int sep = userPass.indexOf(':');
        if (sep < 0) {
            logger.debug("Missing colon separator in credentials from {}", request.getRemoteAddr());
            unauthorized(response);
            return;
        }

        String userPart = userPass.substring(0, sep);
        String passPart = userPass.substring(sep + 1);

        // Password MUST be empty
        if (passPart.length() != 0) {
            String logUser = userPart.isEmpty() ? "<empty>" : userPart.split("\\|")[0];
            logger.debug("Non-empty password provided for user '{}' from {}", logUser, request.getRemoteAddr());
            unauthorized(response);
            return;
        }

        // Split username & roles: UserName|Role1|Role2|...
        // Handle case where userPart might be empty (only colon provided)
        final String userName;
        final Set<String> roles = new HashSet<>();

        if (userPart.isEmpty()) {
            // Empty username is allowed
            userName = "";
            logger.debug("Empty username authenticated from {}", request.getRemoteAddr());
        } else {
            String[] pieces = userPart.split("\\|");
            userName = pieces[0]; // Can be empty string

            // Extract roles from remaining pieces
            for (int i = 1; i < pieces.length; i++) {
                if (!pieces[i].isEmpty()) {
                    roles.add(pieces[i].trim());
                }
            }
        }

        final UserRolePrincipal principal = new UserRolePrincipal(userName, roles);
        logger.debug("Authenticated user '{}' with roles {} from {}", userName, roles, request.getRemoteAddr());

        HttpServletRequest authRequest = new AuthHttpServletRequest(request, principal);

        chain.doFilter(authRequest, res);
    }

    @Override
    public void destroy() {
        logger.info("Destroying BasicAuthPipeRoleFilter");
    }

    private static void unauthorized(HttpServletResponse response) throws IOException {
        logger.debug("Sending 401 Unauthorized response");
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
