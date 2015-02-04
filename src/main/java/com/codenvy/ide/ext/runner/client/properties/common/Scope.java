/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.runner.client.properties.common;

import javax.annotation.Nonnull;

/**
 * The enum represents a list of available scope of runner configurations.
 *
 * @author Andrey Plotnikov
 */
public enum Scope {
    PROJECT("Project"), SYSTEM("System");

    private final String name;

    Scope(@Nonnull String name) {
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a value of {@link Scope} that is equaled to a given content.
     *
     * @param content
     *         content that needs to analyze
     * @return an instance {@link Scope}
     */
    public static Scope detect(@Nonnull String content) {
        for (Scope scope : Scope.values()) {
            if (content.equals(scope.toString())) {
                return scope;
            }
        }

        throw new UnsupportedOperationException("You tried to detect unknown scope. Please, check your value. Your scope is " + content);
    }

}