/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.auth;

import java.util.Arrays;
import java.util.stream.StreamSupport;

/**
 * Authorization mechanism with single permission check for slice.
 * @since 0.8
 */
public interface Permission {

    /**
     * Check if user is authorized to perform an action.
     * @param user User name
     * @return True if authorized
     */
    boolean allowed(Authentication.User user);

    /**
     * Permission by name.
     * @since 0.8
     */
    final class ByName implements Permission {

        /**
         * All permissions.
         */
        private final Permissions perm;

        /**
         * Action to perform.
         */
        private final Action action;

        /**
         * Ctor.
         * @param perm Permissions
         * @param action Action
         */
        public ByName(final Permissions perm, final Action action) {
            this.perm = perm;
            this.action = action;
        }

        /**
         * Ctor.
         * @param action Action
         * @param perm Permissions
         */
        public ByName(final String action, final Permissions perm) {
            this(perm, new Action.ByString(action).get());
        }

        @Override
        public boolean allowed(final Authentication.User user) {
            boolean res = false;
            for (final String synonym : this.action.names()) {
                if (this.perm.allowed(user, synonym)) {
                    res = true;
                    break;
                }
            }
            return res;
        }
    }

    /**
     * Permission composed from multiple origin permissions.
     * Permission allows action when all origin permissions allow the action for specified user.
     *
     * @since 0.17
     */
    final class All implements Permission {

        /**
         * Origin permissions.
         */
        private final Iterable<Permission> origin;

        /**
         * Ctor.
         *
         * @param origin Origin permissions.
         */
        public All(final Permission... origin) {
            this(Arrays.asList(origin));
        }

        /**
         * Ctor.
         *
         * @param origin Origin permissions.
         */
        public All(final Iterable<Permission> origin) {
            this.origin = origin;
        }

        @Override
        public boolean allowed(final Authentication.User user) {
            return StreamSupport.stream(this.origin.spliterator(), false).allMatch(
                perm -> perm.allowed(user)
            );
        }
    }

    /**
     * Permission composed from multiple origin permissions.
     * Permission allows action when at least one of the origin permissions
     * allows the action for specified user.
     *
     * @since 0.17
     */
    final class Any implements Permission {

        /**
         * Origin permissions.
         */
        private final Iterable<Permission> origin;

        /**
         * Ctor.
         *
         * @param origin Origin permissions.
         */
        public Any(final Permission... origin) {
            this(Arrays.asList(origin));
        }

        /**
         * Ctor.
         *
         * @param origin Origin permissions.
         */
        public Any(final Iterable<Permission> origin) {
            this.origin = origin;
        }

        @Override
        public boolean allowed(final Authentication.User user) {
            return StreamSupport.stream(this.origin.spliterator(), false).anyMatch(
                perm -> perm.allowed(user)
            );
        }
    }
}
