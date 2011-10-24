/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.clustering.jgroups;

import java.lang.ref.WeakReference;
import java.security.AccessController;

import org.jboss.util.loading.ContextClassLoaderSwitcher;
import org.jgroups.Event;
import org.jgroups.UpHandler;

/**
 * {@link UpHandler} decorator that associates an up handler with a class loader.
 * @author Paul Ferraro
 */
public class ClassLoaderAwareUpHandler implements UpHandler {
    @SuppressWarnings("unchecked")
    private static final ContextClassLoaderSwitcher switcher = (ContextClassLoaderSwitcher) AccessController.doPrivileged(ContextClassLoaderSwitcher.INSTANTIATOR);

    private final UpHandler handler;
    private final WeakReference<ClassLoader> loaderRef;

    public ClassLoaderAwareUpHandler(UpHandler handler) {
        this(handler, Thread.currentThread().getContextClassLoader());
    }

    public ClassLoaderAwareUpHandler(UpHandler handler, ClassLoader loader) {
        this.handler = handler;
        this.loaderRef = new WeakReference<ClassLoader>(loader);
    }

    @Override
    public Object up(Event event) {
        ClassLoader loader = this.loaderRef.get();
        ContextClassLoaderSwitcher.SwitchContext context = (loader != null) ? switcher.getSwitchContext(loader) : null;
        try {
            return this.handler.up(event);
        } finally {
            if (context != null) {
                context.reset();
            }
        }
    }
}