/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Rich Client.
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
package org.bluebell.richclient.application.docking.vldocking.substance;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import com.vlsolutions.swing.docking.AutoHideButton;
import com.vlsolutions.swing.docking.ui.AutoHideButtonUI;

/**
 * A UI for the auto hide button that uses Substance.
 * <p />
 * Basically attachs installing borders to <code>installUI</code> method.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceAutoHideButtonUI extends AutoHideButtonUI {

    /**
     * The singleton UI instance.
     */
    private static final AutoHideButtonUI INSTANCE = new SubstanceAutoHideButtonUI();

    /**
     * Creates the UI.
     */
    public SubstanceAutoHideButtonUI() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installUI(JComponent comp) {

        super.installUI(comp);
        this.installBorder((AutoHideButton) comp);
    }

    /**
     * Factory method for creating the UI.
     * 
     * @param c
     *            the button.
     * @return the UI.
     */
    public static ComponentUI createUI(JComponent c) {

        return SubstanceAutoHideButtonUI.INSTANCE;
    }
}
