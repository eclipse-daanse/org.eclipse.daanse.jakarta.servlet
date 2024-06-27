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

import org.eclipse.daanse.jakarta.servlet.filter.jetty.api.Constants;
import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import jakarta.servlet.Filter;

@Component(service = Filter.class, configurationPid = Constants.PID_FILTER_CORS)
@Designate(ocd = CorsFilterConfig.class)
@RequireHttpWhiteboard
public class CorsFilter extends CrossOriginFilter {


}
