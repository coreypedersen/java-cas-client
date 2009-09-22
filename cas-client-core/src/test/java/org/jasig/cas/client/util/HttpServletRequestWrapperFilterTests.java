/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.util;

import junit.framework.TestCase;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AssertionImpl;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the HttpServletRequestWrapperFilter.
 *
 * @author Scott Battaglia
 * @version $Revision: 11742 $ $Date: 2007-10-05 14:03:58 -0400 (Thu, 05 Oct 2007) $
 * @since 3.0
 */

public final class HttpServletRequestWrapperFilterTests extends TestCase {

    protected HttpServletRequest mockRequest;

    public void testWrappedRequest() throws Exception {
        final HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();
        filter.init(new MockFilterConfig());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                AbstractCasFilter.CONST_CAS_ASSERTION,
                new AssertionImpl("test"));
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("test", this.mockRequest.getRemoteUser());
        
        filter.destroy();
    }
    
    public void testIsUserInRole() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();
        final MockFilterConfig config = new MockFilterConfig();

        config.addInitParameter("roleAttribute", "memberOf");
        final HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();
        filter.init(config);
        
        final Map attributes = new HashMap();
        attributes.put("memberOf", "administrators");
        final AttributePrincipal principal = new AttributePrincipalImpl("alice", attributes);
        session.setAttribute(
                AbstractCasFilter.CONST_CAS_ASSERTION,
                new AssertionImpl(principal));

        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("alice", this.mockRequest.getRemoteUser());
        assertTrue(this.mockRequest.isUserInRole("administrators"));
        assertFalse(this.mockRequest.isUserInRole("ADMINISTRATORS"));
        assertFalse(this.mockRequest.isUserInRole("users"));
        assertFalse(this.mockRequest.isUserInRole(null));

        filter.destroy();
    }
    
    public void testIsUserInRoleCaseInsensitive() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpSession session = new MockHttpSession();
        final MockFilterConfig config = new MockFilterConfig();

        config.addInitParameter("roleAttribute", "groupMembership");
        config.addInitParameter("ignoreCase", "true");
        final HttpServletRequestWrapperFilter filter = new HttpServletRequestWrapperFilter();
        filter.init(config);
        
        final Map attributes = new HashMap();
        attributes.put("groupMembership", Arrays.asList(new Object[] {"animals", "ducks"}));
        final AttributePrincipal principal = new AttributePrincipalImpl("daffy", attributes);
        session.setAttribute(
                AbstractCasFilter.CONST_CAS_ASSERTION,
                new AssertionImpl(principal));

        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), createFilterChain());
        assertEquals("daffy", this.mockRequest.getRemoteUser());
        assertTrue(this.mockRequest.isUserInRole("animals"));
        assertTrue(this.mockRequest.isUserInRole("ANIMALS"));
        assertTrue(this.mockRequest.isUserInRole("ducks"));
        assertTrue(this.mockRequest.isUserInRole("DUCKS"));
        assertFalse(this.mockRequest.isUserInRole("varmints"));
        assertFalse(this.mockRequest.isUserInRole(""));

        filter.destroy();
    }

    private FilterChain createFilterChain() {
        return new FilterChain() {
            public void doFilter(ServletRequest request,
                                 ServletResponse response) throws IOException, ServletException {
                HttpServletRequestWrapperFilterTests.this.mockRequest = (HttpServletRequest) request;
            }

        };
    }
}
