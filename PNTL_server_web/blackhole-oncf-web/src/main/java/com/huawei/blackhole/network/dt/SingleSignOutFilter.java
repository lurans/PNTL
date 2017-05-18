/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.huawei.blackhole.network.dt;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Single Sign Out protocol. It handles registering the session
 * and destroying the session.
 */
public final class SingleSignOutFilter extends AbstractConfigurationFilter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final SingleSignOutHandler HANDLER = new SingleSignOutHandler();

    private AtomicBoolean handlerInitialized = new AtomicBoolean(false);

    public void init(final FilterConfig filterConfig) throws ServletException {
        if (!isIgnoreInitConfiguration()) {
            HANDLER.setArtifactParameterName(getPropertyFromInitParams(filterConfig, "artifactParameterName",
                    SingleSignOutHandler.DEFAULT_ARTIFACT_PARAMETER_NAME));
            HANDLER.setLogoutParameterName(getPropertyFromInitParams(filterConfig, "logoutParameterName",
                    SingleSignOutHandler.DEFAULT_LOGOUT_PARAMETER_NAME));
            HANDLER.setFrontLogoutParameterName(getPropertyFromInitParams(filterConfig, "frontLogoutParameterName",
                    SingleSignOutHandler.DEFAULT_FRONT_LOGOUT_PARAMETER_NAME));
            HANDLER.setRelayStateParameterName(getPropertyFromInitParams(filterConfig, "relayStateParameterName",
                    SingleSignOutHandler.DEFAULT_RELAY_STATE_PARAMETER_NAME));
            HANDLER.setCasServerUrlPrefix(getPropertyFromInitParams(filterConfig, "casServerUrlPrefix", ""));
            HANDLER.setArtifactParameterOverPost(parseBoolean(getPropertyFromInitParams(filterConfig,
                    "artifactParameterOverPost", "false")));
            HANDLER.setEagerlyCreateSessions(parseBoolean(getPropertyFromInitParams(filterConfig,
                    "eagerlyCreateSessions", "true")));
        }
        HANDLER.init();
        handlerInitialized.set(true);
    }

    public void setArtifactParameterName(final String name) {
        HANDLER.setArtifactParameterName(name);
    }

    public void setLogoutParameterName(final String name) {
        HANDLER.setLogoutParameterName(name);
    }

    public void setFrontLogoutParameterName(final String name) {
        HANDLER.setFrontLogoutParameterName(name);
    }

    public void setRelayStateParameterName(final String name) {
        HANDLER.setRelayStateParameterName(name);
    }

    public void setCasServerUrlPrefix(final String casServerUrlPrefix) {
        HANDLER.setCasServerUrlPrefix(casServerUrlPrefix);
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!this.handlerInitialized.getAndSet(true)) {
            HANDLER.init();
        }

        if (HANDLER.process(request, response)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    public void destroy() {
        // nothing to do
    }

    protected static SingleSignOutHandler getSingleSignOutHandler() {
        return HANDLER;
    }

}
