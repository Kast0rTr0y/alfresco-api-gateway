/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.cloud.gateway;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;

public class KeycloakFilterRoute extends ZuulFilter {


    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        if (ctx.getRequest().getHeader(AUTHORIZATION_HEADER) == null) {
            addKeycloakTokenToHeader(ctx);
        }
        return null;
    }

    private void addKeycloakTokenToHeader(RequestContext ctx) {
        RefreshableKeycloakSecurityContext securityContext = getRefreshableKeycloakSecurityContext(ctx);
        if (securityContext != null) {
            ctx.addZuulRequestHeader(AUTHORIZATION_HEADER, buildBearerToken(securityContext));
        } 
    }

    private RefreshableKeycloakSecurityContext getRefreshableKeycloakSecurityContext(RequestContext ctx) {
        if (ctx.getRequest().getUserPrincipal() instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) ctx.getRequest().getUserPrincipal();
            return (RefreshableKeycloakSecurityContext) token.getCredentials();
        }
        return null;
    }

    private String buildBearerToken(RefreshableKeycloakSecurityContext securityContext) {
        return "Bearer " + securityContext.getTokenString();
    }

}
