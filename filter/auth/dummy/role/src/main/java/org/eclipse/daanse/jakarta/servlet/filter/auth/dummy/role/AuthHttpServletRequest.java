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

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class AuthHttpServletRequest extends HttpServletRequestWrapper {

    private final UserRolePrincipal principal;

    AuthHttpServletRequest(HttpServletRequest request, UserRolePrincipal principal) {
        super(request);
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public String getRemoteUser() {
        return principal.getName();
    }

    @Override
    public boolean isUserInRole(String role) {
        return principal.roles().contains(role);
    }

    @Override
    public String getAuthType() {
        return HttpServletRequest.BASIC_AUTH;
    }
}
