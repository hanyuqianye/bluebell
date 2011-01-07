/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
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
package org.bluebell.richclient.form;

import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.util.Assert;

/**
 * Utility class for dealing with forms.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FormUtils {

    /**
     * The field with name {@value #EDITING_FORM_OBJECT_INDEX_HOLDER} from {@link AbstractForm}.
     */
    private static final String EDITING_FORM_OBJECT_INDEX_HOLDER = "editingFormObjectIndexHolder";

    /**
     * The field with name {@value #EDITABLE_FORM_OBJECTS} from {@link AbstractForm}.
     */
    private static final String EDITABLE_FORM_OBJECTS = "editableFormObjects";

    /**
     * The field with name {@value #NEW_FORM_OBJECT_COMMAND} from {@link AbstractForm}.
     */
    private static final String NEW_FORM_OBJECT_COMMAND = "newFormObjectCommand";

    /**
     * Gets the list of editable form objects of a given form.
     * 
     * @param form
     *            the form.
     * @return the list of editable form objects.
     */
    public static ObservableList getEditableFormObjects(AbstractForm form) {

        Assert.notNull(form, "form");

        final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(form);
        final ObservableList observableList = (ObservableList) propertyAccessor.getPropertyValue(//
                FormUtils.EDITABLE_FORM_OBJECTS);

        return observableList;
    }

    /**
     * Gets the selected index of a given form.
     * 
     * @param form
     *            the form.
     * @return the editing index.
     */
    public static int getSelectedIndex(AbstractForm form) {

        Assert.notNull(form, "form");

        final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(form);
        final ValueModel indexHolder = (ValueModel) propertyAccessor.getPropertyValue(//
                FormUtils.EDITING_FORM_OBJECT_INDEX_HOLDER);
        final Integer index = (Integer) indexHolder.getValue();

        return index;
    }

    /**
     * Gets the new form object command of a given form.
     * 
     * @param form
     *            the form.
     * @return the list of editable form objects.
     */
    public static ActionCommand getNewFormObjectCommand(AbstractForm form) {

        Assert.notNull(form, "form");

        final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(form);
        final ActionCommand actionCommand = (ActionCommand) propertyAccessor.getPropertyValue(//
                FormUtils.NEW_FORM_OBJECT_COMMAND);

        return actionCommand;
    }
}