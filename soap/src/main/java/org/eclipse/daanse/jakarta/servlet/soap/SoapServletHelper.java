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
package org.eclipse.daanse.jakarta.servlet.soap;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;

class SoapServletHelper {

    private static final String HEADER_DELIMITER = ",";

    private SoapServletHelper() {
    }

    static MimeHeaders getMimeHeadersFromRequest(HttpServletRequest request) {
        Enumeration<String> headerNamesEnum = request.getHeaderNames();
        MimeHeaders mimeHeaders = new MimeHeaders();

        while (headerNamesEnum.hasMoreElements()) {
            String headerName = headerNamesEnum.nextElement();
            String headerValue = request.getHeader(headerName);

            StringTokenizer values = new StringTokenizer(headerValue, HEADER_DELIMITER);

            while (values.hasMoreTokens()) {
                mimeHeaders.addHeader(headerName, values.nextToken().trim());
            }
        }

        return mimeHeaders;
    }

    static void setMimeHeadersToResponse(final HttpServletResponse response, final MimeHeaders mimeHeaders) {
        final Iterator<MimeHeader> it = mimeHeaders.getAllHeaders();

        while (it.hasNext()) {
            final MimeHeader header = it.next();

            final String[] values = mimeHeaders.getHeader(header.getName());

            if (values.length == 1) {
                response.setHeader(header.getName(), header.getValue());
            } else {
                final StringBuilder sb = new StringBuilder();

                boolean first = true;
                for (String value : values) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(',');
                    }
                    sb.append(value);
                }
                response.setHeader(header.getName(), sb.toString());
            }
        }
    }

}
