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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "%role.filter.name",
    description = "%role.filter.description",
    localization = "OSGI-INF/l10n/bundle"
)
public @interface OCD {

    @AttributeDefinition(name = "%dummy.user.name", description = "%dummy.user.description")
    String dummyUserName() default "NoAuthDummyUser";

    @AttributeDefinition(name = "%dummy.roles.name", description = "%dummy.roles.description")
    String[] dummyRoles() default {};

}
