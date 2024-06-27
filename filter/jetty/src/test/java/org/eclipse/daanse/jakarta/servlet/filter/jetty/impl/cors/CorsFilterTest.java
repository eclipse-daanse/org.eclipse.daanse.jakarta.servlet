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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.daanse.jakarta.servlet.filter.jetty.api.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.servlet.Servlet;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
class CorsFilterTest {

    @InjectBundleContext
    BundleContext bc;

    @InjectService
    ConfigurationAdmin ca;

    @Test
    @WithConfiguration(pid = Constants.PID_FILTER_CORS, location = "?", properties = {
            @Property(key = HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, value = "/*") })
    void testNotConfigures() throws MalformedURLException, IOException, InterruptedException {

        HttpResponse<String> response = registerServletAndConnect();
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isNotEmpty();
        assertThat(response.statusCode()).isEqualTo(200);

    }

    @Test
    @WithConfiguration(pid = Constants.PID_FILTER_CORS, location = "?", properties = {
            @Property(key = HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, value = "/*"),
            @Property(key = Constants.FILTER_CORS_PROP_INIT_ALLOWED_ORIGINS_PARAM, value = "not.here") })
    void testWithConfiguration() throws MalformedURLException, IOException, InterruptedException {

        HttpResponse<String> response = registerServletAndConnect();
        assertThat(response.headers().firstValue("")).isEmpty();
        assertThat(response.statusCode()).isEqualTo(200);
    }

    private HttpResponse<String> registerServletAndConnect()
            throws InterruptedException, IOException, MalformedURLException {

        // register Servlet
        ScOkServlet testServlet = new ScOkServlet();
        Dictionary<String, Object> servletProps = new Hashtable<>();
        servletProps.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "testServlet");
        servletProps.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/*");
        bc.registerService(Servlet.class, testServlet, servletProps);

        Thread.sleep(500);

        // doRequest
        URI uri = URI.create("http://localhost:8080");
        HttpRequest request = HttpRequest.newBuilder(uri).GET().header("Origin", "http://localhost").build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        return response;
    }

}
