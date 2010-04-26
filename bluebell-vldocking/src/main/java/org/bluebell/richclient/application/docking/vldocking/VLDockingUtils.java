/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell VLDocking.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking;

import java.awt.Color;

import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.application.config.vldocking.WidgetDesktopStyle;
import org.springframework.richclient.util.Assert;

/**
 * Utility class for dealing with VLDocking.
 * 
 * @see WidgetDesktopStyle
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class VLDockingUtils {

    /**
     * The activation infix for <em>active</em> UI property names.
     */
    private static final String ACTIVE_INFIX = ".active";

    /**
     * The '{@value #DOT}' character.
     */
    private static final char DOT = '.';

    /**
     * The activation infix for <em>inactive</em> UI property names.
     */
    private static final String INACTIVE_INFIX = ".inactive";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private VLDockingUtils() {

    }

    /**
     * Transforms a UI object key into an activation aware key name.
     * 
     * @param key
     *            the key.
     * @param active
     *            <code>true</code> for <em>active</em> UI keys and <code>false</code> for inactive.
     * @return the transformed key.
     */
    public static String activationKey(String key, Boolean active) {

        Assert.notNull(key, "key");
        Assert.notNull(active, "active");

        final int index = StringUtils.lastIndexOf(key, VLDockingUtils.DOT);
        final String overlay = active ? VLDockingUtils.ACTIVE_INFIX : VLDockingUtils.INACTIVE_INFIX;

        return StringUtils.overlay(key, overlay, index, index);
    }

    /**
     * A set of colors with its own semantic.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum DockingColor {

        /**
         * The active widget color.
         */
        ACTIVE_WIDGET("Bluebell.activeWidgetColor"), // instead of "activeCaption"
        /**
         * The inactive widget color.
         */
        INACTIVE_WIDGET("Bluebell.inactiveWidgetColor"), // instead of ""controlLtHighlight""
        /**
         * The background color.
         */
        BACKGROUND("Bluebell.backgroundColor"), // instead of control "DockingDesktop.backgroundColor"
        /**
         * The highlight color.
         */
        HIGHLIGHT("Bluebell.highlightColor"), // instead of "controlHighlight"
        /**
         * The shadow color.
         */
        SHADOW("Bluebell.shadowColor"); // controlDkShadow

        /**
         * The key to query the UIManager for getting the associated color.
         */
        private String key;

        /**
         * Creates the enumerated type.
         * 
         * @param key
         *            the key to query the UIManager.
         */
        private DockingColor(String key) {

            this.setKey(key);
        }

        /**
         * Gets the related color.
         * 
         * @return the related color.
         */
        public Color getColor() {

            return UIManager.getColor(this.getKey());
        }

        /**
         * Gets the key.
         * 
         * @return the key
         */
        public String getKey() {

            return this.key;
        }

        /**
         * Sets the key.
         * 
         * @param key
         *            the key to set.
         */
        private void setKey(String key) {

            Assert.notNull(key, "key");

            this.key = key;
        }
    }

    /**
     * An enum to distinguish between dock view types.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum DockViewType {
        /**
         * Closed dockable state.
         */
        CLOSED,
        /**
         * Docked dockable state.
         */
        DOCKED,
        /**
         * Floating dockable state.
         */
        FLOATING,
        /**
         * Hidden dockable state.
         */
        HIDDEN,
        /**
         * Maximized dockable state.
         */
        MAXIMIZED,
        /**
         * Tabbed dockable state.
         */
        TABBED
    }
}
