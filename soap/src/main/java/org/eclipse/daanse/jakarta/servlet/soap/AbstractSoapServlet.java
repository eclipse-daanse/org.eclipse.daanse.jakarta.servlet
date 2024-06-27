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

import static org.eclipse.daanse.jakarta.servlet.soap.SoapServletHelper.setMimeHeadersToResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

/**
 * The {@link AbstractSoapServlet}. is an abstract {@link Servlet} that others
 * could use to:
 * <ul>
 * <li>mapping {@link HttpServletRequest} to {@link SOAPMessage} using
 * {@link MessageFactory}</li>
 * <li>handling of {@link HttpServletRequest#getHeaderNames} Headers to store
 * {@link MimeHeaders} into generated {@link SOAPMessage}</li>
 * <li>map response {@link SOAPMessage} to {@link HttpServletResponse}</li>
 * </ul>
 *
 */
public abstract class AbstractSoapServlet extends HttpServlet {

    /** The Constant EXCEPTION_MSG_MESSAGE_FACTORY. */
    private static final String EXCEPTION_MSG_MESSAGE_FACTORY = "Unable to get/create MessageFactory.";

    /** The Constant EXCEPTION_MSG_MESSAGE_SOAP_CONNECTION. */
    private static final String EXCEPTION_MSG_MESSAGE_SOAP_CONNECTION = "Unable to create SOAPConnection.";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSoapServlet.class);

    /** The Constant EXCEPTION_MSG_POST. */
    private static final String EXCEPTION_MSG_POST = "doPost in SAAJServlet failed.";

    /** The soap connection. */
    protected transient SOAPConnection soapConnection = null;

    /** The message factory. */
    protected transient MessageFactory messageFactory = null;

    /**
     * {@inheritDoc}
     *
     * Inits the {@link Servlet}
     *
     * @param servletConfig the servlet config
     * @throws ServletException the servlet exception
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        try {
            messageFactory = getMessageFactory();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getMessageFactory: {}", messageFactory);
            }
        } catch (SOAPException ex) {
            LOGGER.error(EXCEPTION_MSG_MESSAGE_FACTORY, ex);
            throw new ServletException(EXCEPTION_MSG_MESSAGE_FACTORY);
        }

        try {
            soapConnection = createSOAPConnection();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("createSOAPConnection: {}", soapConnection);
            }
        } catch (SOAPException ex) {
            LOGGER.error(EXCEPTION_MSG_MESSAGE_SOAP_CONNECTION, ex);
            throw new ServletException(EXCEPTION_MSG_MESSAGE_SOAP_CONNECTION);
        }

    }

    /**
     * Gets the message factory.
     *
     * @return the message factory
     * @throws SOAPException the SOAP exception
     */
    protected MessageFactory getMessageFactory() throws SOAPException {
        return MessageFactory.newInstance();
    }

    /**
     * Creates the SOAP connection.
     *
     * @return the SOAP connection
     * @throws SOAPException the SOAP exception
     */
    protected SOAPConnection createSOAPConnection() throws SOAPException {
        return SOAPConnectionFactory.newInstance().createConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws ServletException, IOException {
        try {
            SOAPMessage requestMessage = createSoapMessageRequest(servletRequest);

            if (LOGGER.isDebugEnabled()) {
                ByteArrayOutputStream baos = getOutputStreamOfSoapMessage(requestMessage);
                LOGGER.debug("SOAPMessage in: {}", baos);
            }

            SOAPMessage responseMessage = onMessage(requestMessage);

            if (LOGGER.isDebugEnabled()) {
                ByteArrayOutputStream baos = getOutputStreamOfSoapMessage(responseMessage);
                LOGGER.debug("SOAPMessage out: {}", baos);
            }

            writeSoapMessageToServletResponse(servletResponse, responseMessage);
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_MSG_POST, ex);
            servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private ByteArrayOutputStream getOutputStreamOfSoapMessage(SOAPMessage responseMessage)
            throws SOAPException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        responseMessage.writeTo(baos);
        return baos;
    }

    private void writeSoapMessageToServletResponse(HttpServletResponse servletResponse, SOAPMessage responseMessage)
            throws SOAPException, IOException {
        if (responseMessage != null) {
            // While saving MimeHeaders are generated.
            if (responseMessage.saveRequired()) {
                responseMessage.saveChanges();
            }

            servletResponse.setStatus(HttpServletResponse.SC_OK);
            setMimeHeadersToResponse(servletResponse, responseMessage.getMimeHeaders());
            OutputStream responseOutputStream = servletResponse.getOutputStream();
            responseMessage.writeTo(responseOutputStream);
            responseOutputStream.flush();

        } else {
            servletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private SOAPMessage createSoapMessageRequest(HttpServletRequest servletRequest) throws IOException, SOAPException {
        MimeHeaders headers = SoapServletHelper.getMimeHeadersFromRequest(servletRequest);
        InputStream requestInptStream = servletRequest.getInputStream();
        return messageFactory.createMessage(headers, requestInptStream);
    }

    /**
     * On message.
     *
     * @param soapRequestMessage the {@link SOAPMessage} of the request
     * @return the response {@link SOAPMessage}
     */
    protected abstract SOAPMessage onMessage(SOAPMessage soapRequestMessage);

}
