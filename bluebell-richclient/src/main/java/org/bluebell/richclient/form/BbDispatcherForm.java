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

package org.bluebell.richclient.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import org.bluebell.richclient.form.util.BbDefaultFormModel;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ListListModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * Esta clase extiende {@link org.springframework.richclient.form.AbstractDetailForm} con el fin de reutilizar la
 * infraestructura "maestro - detalle" proporcionada por Spring RCP para el caso de múltiples relaciones
 * "parent - child".
 * <p>
 * El modelo de este formulario es de tipo {@link DispatcherFormModel}.
 * <p>
 * Cada formulario hijo tiene como <em>parent form</em> a este formulario y sus modelos son también hijos del módelo de
 * este formulario.
 * <p>
 * Algunas consideraciones a tener en cuenta son:
 * <ol>
 * <li>La gestión de los índices (del elemento siendo editado con respecto a la lista mantenida por el formulario
 * maestro) la lleva a cabo este formulario. Los hijos simplemente son notificados ante cambios de forma silenciosa.
 * <li>Las siguientes invocaciones pueden tener lugar sobre el formulario detalle o bien sobre el modelo del formulario
 * detalle. El modelo las delega en el formulario y éste las redirige pertinentemente a cada uno de los hijos:
 * <ul>
 * <li>{@link org.springframework.richclient.form.AbstractForm#reset()}
 * <li>{@link org.springframework.richclient.form.AbstractForm#revert()}
 * <li>{@link org.springframework.richclient.form.AbstractForm#isDirty()}
 * <li>
 * {@link org.springframework.richclient.form.AbstractForm#setEnabled(boolean)}
 * <li>
 * {@link org.springframework.richclient.form.AbstractForm#setFormObject(Object)}
 * </ul>
 * <li>Casos especiales:
 * <ul>
 * <li>{@link org.springframework.richclient.form.AbstractForm#commit()}: provoca un <em>commit</em> en el modelo
 * compuesto {@link DispatcherFormModel} . La operación <code>postCommit</code> se redirige al formulario maestro
 * distinguiendo entre inserciones y actualizaciones.
 * <li>
 * {@link org.springframework.richclient.form.AbstractDetailForm#setMasterList}: este método es protegido y no es
 * posible invocarlo sobre los formularios hijos directamente. No obstante se ha redefinido
 * {@link org.springframework.richclient.form.AbstractForm#setEditableFormObjects(ObservableList)} que establece la
 * lista de objetos editables también en los formularios hijos.
 * <li>
 * {@link org.springframework.richclient.form.AbstractDetailForm#addPropertyChangeListener} : No es necesario hacer
 * nada. Anteriormente se le añadía un <em>property change listener</em> inmediatamente después de crear un formulario
 * detalle, simulando el comportamiento de
 * {@link org.springframework.richclient.form.AbstractMasterForm.EditStateMonitor}.
 * </ul>
 * </ol>
 * 
 * @param <T>
 *            el tipo de las entidades editadas por el formulario maestro.
 * 
 * @see DispatcherFormModel
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDispatcherForm<T> extends AbstractBbDetailForm<T> {

    /**
     * El nombre de la página compuesta de los formularios detalle.
     */
    private static final String FORM_ID = "dispatcherForm";

    /**
     * Importante que sea ordenada.
     */
    private List<AbstractBbChildForm<T>> childForms;

    /**
     * Crea el formulario compuesto a partir del formulario maestro y un <em>value model</em>.
     * <p>
     * Una vez invocado el constructor de la clase padre se establece un nuevo <em>form model</em> con tipo
     * {@link DispatcherFormModel} a partir del <em>value model</em>.
     * 
     * @param masterForm
     *            el formulario maestro.
     * @param valueModel
     *            <em>value model</em> a partir del cual crear el modelo de este formulario.
     */
    public BbDispatcherForm(AbstractB2TableMasterForm<T> masterForm, ValueModel valueModel) {

        super(masterForm, BbDispatcherForm.FORM_ID, valueModel);

        this.setFormModel(new DispatcherFormModel(valueModel));

        this.getEditingIndexHolder().addValueChangeListener(new EditingIndexHolderListener());
    }

    /**
     * Devuelve el formulario hijo con el identificador dado y <code>null</code> si no existe.
     * 
     * @param formId
     *            el identificador del formulario.
     * 
     * @return el formulario hijo.
     */
    @Override
    public final AbstractBbChildForm<T> getChildForm(String formId) {

        for (final AbstractBbChildForm<T> childForm : this.getChildForms()) {
            if (formId.equals(childForm.getId())) {
                return childForm;
            }
        }

        return null;
    }

    /**
     * Obtiene los formularios hijos de este formulario.
     * 
     * @return una colección <em>unmodifiable</em> con los formularios hijos de este formulario.
     */
    public final List<AbstractBbChildForm<T>> getChildForms() {

        if (this.childForms == null) {
            this.childForms = new ArrayList<AbstractBbChildForm<T>>();
        }

        return Collections.unmodifiableList(this.childForms);
    }

    /**
     * Añade un nuevo formulario hijo, de tipo {@link AbstractBbChildForm}.
     * <p>
     * Para ello procede del siguiente modo:
     * <ol>
     * <li>Actualiza el modelo del formulario hijo (
     * {@link AbstractBbChildForm#updateFormModelUsingParentForm(BbDispatcherForm)} ).
     * <li>Invoca a {@link org.springframework.richclient.form.AbstractDetailForm#addChildForm(Form)}.
     * </ol>
     * <p>
     * Provoca una excepción en caso de que el formulario hijo no sea de tipo {@link AbstractBbChildForm}.
     * 
     * @param form
     *            el formulario hijo.
     */
    @Override
    public final void addChildForm(Form form) {

        Assert.notNull(form);
        Assert.isInstanceOf(AbstractBbChildForm.class, form);
        Assert.isNull(this.getChildForm(form.getId()), "this.getChildForm(form.getId())");

        @SuppressWarnings("unchecked")
        final AbstractBbChildForm<T> childForm = (AbstractBbChildForm<T>) form;

        // [0] Call super
        super.addChildForm(form);

        // [1] Treat child form
        childForm.setFormObject(this.getFormObject());
        childForm.setEnabled(Boolean.FALSE);
        childForm.getFormModel().setValidating(Boolean.FALSE);
        childForm.getFormModel().setParent(this.getFormModel());
        this.setEditableFormObjectsOnChildForm(childForm);

        // [2] Bidirectional linking
        childForm.setDispatcherForm(this);
        this.childForms.add(childForm);
    }

    /**
     * Elimina formulario hijo, de tipo {@link AbstractBbChildForm}.
     * <p>
     * Provoca una excepción en caso de que el formulario hijo no sea de tipo {@link AbstractBbChildForm}.
     * 
     * @param form
     *            el formulario hijo.
     */
    @Override
    public final void removeChildForm(Form form) {

        Assert.isTrue(this.getChildForms().contains(form), "The form to remove must be a children of this form");
        Assert.isInstanceOf(AbstractBbChildForm.class, form);

        // [1] Call super
        super.removeChildForm(form);

        // [2] Bidirection unlinking
        @SuppressWarnings("unchecked")
        final AbstractBbChildForm<T> childForm = (AbstractBbChildForm<T>) form;

        this.childForms.remove(childForm.getId());
        childForm.setDispatcherForm(null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If a commit is in process and index to select is <code>-1</code> then this call is ignored. This happens at
     * {@link org.springframework.richclient.form.AbstractDetailForm#postCommit(FormModel)} and it is not the expected
     * behaviour anymore:
     * 
     * <pre>
     * public void postCommit(FormModel formModel) {
     * 
     *     super.postCommit(formModel);
     * 
     *     // Now set the selected index back to -1 so that the forms properly reset
     *     setSelectedIndex(-1); // &lt;------
     * }
     * </pre>
     * 
     * @see EditingIndexHolderListener
     * @since 20110105
     */
    @Override
    public final void setSelectedIndex(int index) {

        // (JAF), 20110105, hack to avoid changing select index three times on commit!
        if (this.isCommiting() && (index == -1)) {
            return;
        }

        super.setSelectedIndex(index);

        // (JAF), 20110105, note children are notified thanks to EditingIndexHolderListener
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isDirty() {

        for (final AbstractForm childForm : this.getChildForms()) {
            if (childForm.isDirty()) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalReset()
     */
    @Override
    public final void reset() {

        this.getDispatcherFormModel().doInternalReset();

        // (JAF), 20080914, AbstractFormModel#reset internally executes #setFormObject(null) over this form and its
        // children. To avoid redundant calls its better to make a single invocation

        // for (Form childForm : this.formsById.values()) { childForm.reset(); }
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalRevert()
     */
    @Override
    public final void revert() {

        this.getDispatcherFormModel().doInternalRevert();

        for (final Form childForm : this.getChildForms()) {
            childForm.revert();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalSetEnabled(boolean)
     */
    @Override
    public final void setEnabled(boolean enabled) {

        this.getDispatcherFormModel().doInternalSetEnabled(enabled);

        for (final AbstractForm childForm : this.getChildForms()) {
            childForm.setEnabled(enabled);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalSetFormObject(Object)
     */
    @Override
    public final void setFormObject(Object formObject) {

        this.getDispatcherFormModel().doInternalSetFormObject(formObject);

        // (JAF), 20110104, if formObject is null then propagate the value set on this form
        @SuppressWarnings("unchecked")
        final T formObjectToSet = (T) this.getFormObject();

        for (final AbstractForm childForm : this.getChildForms()) {
            childForm.setFormObject(formObjectToSet);
        }

    }

    /**
     * Crea el control del formulario, formado por la botonera con los comandos <code>commit</code>, <code>cancel</code>
     * y <code>revert</code>.
     * 
     * @see org.springframework.richclient.form.AbstractDetailForm #createFormControl()
     * @see #createButtonBar()
     * 
     * @return el control del formulario.
     */
    @Override
    protected final JComponent createFormControl() {

        return this.createButtonBar();
    }

    /**
     * Estable la lista de objetos editables en este formulario a continuación de sobre sus hijos.
     * <p>
     * Todos los formularios hijos comparten la lista de objetos editables; este método la establece.
     * 
     * @param editableFormObjects
     *            la lista de objetos editables.
     * 
     * @see AbstractBbChildForm#setEditableFormObjectFromDispatcherForm(ObservableList)
     */
    @Override
    protected final void setEditableFormObjects(ObservableList editableFormObjects) {

        super.setEditableFormObjects(editableFormObjects);

        // (JAF), 20101229, this approach is not enough because child forms are not added at the moment of setting
        // editable form objects. That's why this method is also invoked at #addChildForm
        for (final AbstractBbChildForm<T> childForm : this.getChildForms()) {
            this.setEditableFormObjectsOnChildForm(childForm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void setEditingNewFormObject(boolean editingNewFormOject) {

        super.setEditingNewFormObject(editingNewFormOject);

        for (final AbstractBbChildForm<T> childForm : this.getChildForms()) {
            childForm.doSetEditingNewFormObject(editingNewFormOject);
        }
    }

    /**
     * Gets the form model ensuring it is a instance of {@link DispatcherFormModel}.
     * 
     * @return the form model.
     * 
     * @see #getFormModel()
     */
    private DispatcherFormModel getDispatcherFormModel() {

        Assert.isInstanceOf(DispatcherFormModel.class, this.getFormModel());

        @SuppressWarnings("unchecked")
        final DispatcherFormModel dispatcherFormModel = (DispatcherFormModel) this.getFormModel();

        return dispatcherFormModel;
    }

    /**
     * Set the editable form objects list on a given child form.
     * <p>
     * Child form <code>editableFormObjects</code> must be differente from dispatcher and master form one. Otherwise
     * multiple events will be raised since the same list is modified N times (i.e.:after commit)
     * 
     * @param childForm
     *            the child form.
     * @return the observableList that has been set.
     * 
     * @see AbstractBbChildForm#doSetEditableFormObjects(ObservableList)
     * @since 20110105
     */
    private ObservableList setEditableFormObjectsOnChildForm(AbstractBbChildForm<T> childForm) {

        Assert.notNull(childForm, "childForm");

        @SuppressWarnings("unchecked")
        final EventList<T> editableFormObjects = (EventList<T>) FormUtils.getEditableFormObjects(this);
        final ObservableList editableFormObjectsToSet = new ListListModel(editableFormObjects);

        editableFormObjects.addListEventListener(new ListEventListener<T>() {

            /**
             * Synchs one list with other.
             * 
             * @param listChanges
             */
            @SuppressWarnings("unchecked")
            @Override
            public void listChanged(ListEvent<T> listChanges) {

                editableFormObjectsToSet.clear();
                editableFormObjectsToSet.addAll((List<T>) editableFormObjects);
            }
        });

        childForm.doSetEditableFormObjects(editableFormObjectsToSet);

        return editableFormObjectsToSet;
    }

    /**
     * Listens to editing index holder value changes and notify child forms silently.
     * <p>
     * <b>Note</b> the typical iteration based approach over
     * {@link org.springframework.richclient.form.AbstractDetailForm#setSelectedIndex(int)} is not valid because of the
     * selected index may be changed directly without this method. For instance on
     * {@link AbstractForm#setEditingFormObjectIndexSilently}:
     * 
     * <pre>
     * protected void setEditingFormObjectIndexSilently(int index) {
     * 
     *     editingFormObjectIndexHolder.removeValueChangeListener(editingFormObjectSetter);
     *     editingFormObjectIndexHolder.setValue(new Integer(index)); // &lt;------
     *     editingFormObjectIndexHolder.addValueChangeListener(editingFormObjectSetter);
     * }
     * </pre>
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class EditingIndexHolderListener implements PropertyChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {

            final Integer newIndex = (Integer) evt.getNewValue();

            for (AbstractBbChildForm<T> childForm : BbDispatcherForm.this.getChildForms()) {

                // (JAF), 20110105, do it silently. EditingFormObject setter is not needed in this case because
                // #setFormObject does the trick
                childForm.doSetEditingFormObjectIndexSilently(newIndex);
            }
        }
    }

    /**
     * For model used as backing form model of <code>BbDispatcherForm</code> in order to propagate invocations to child
     * form and form models.
     * <p>
     * There are well known methods whose execution is delegated from form to form model. There is no consistency at
     * this point, so invokers could call either of them.
     * <ul>
     * <li>{@link FormModel#reset()}
     * <li>{@link FormModel#revert()}
     * <li>{@link FormModel#isDirty()}
     * <li>{@link FormModel#setEnabled(boolean)}
     * <li>{@link FormModel#setFormObject(Object)}
     * </ul>
     * Other important methods are:
     * <ul>
     * <li>{@link ValidatingFormModel#validate()}
     * <li>{@link ValidatingFormModel#setValidating(boolean)}
     * <li>{@link ValidatingFormModel#isDirty()}
     * </ul>
     * <p>
     * There are the following to scenarios:
     * <ol>
     * <li>
     * Invokers call directly form model method:
     * 
     * <pre>
     *             Invoker        Form Model        Form         Child Form
     *                |               |               |               | 
     *                |--------------&gt;|               |               |
     *                |               |--------------&gt;|               |
     *                |               |               |--------------&gt;|
     *                |               |               |*              |
     *                |               |doInternalXXX()|--------------&gt;|
     *                |               |&lt;--------------|               |
     *                |               |               |               |
     * </pre>
     * 
     * </li>
     * <li>
     * Invokers call form method:
     * 
     * <pre>
     *             Invoker        Form Model        Form         Child Form
     *                |               |               |               | 
     *                |------------------------------&gt;|               |
     *                |               |               |--------------&gt;|
     *                |               |               |*              |
     *                |               |doInternalXXX()|--------------&gt;|
     *                |               |&lt;--------------|               |
     *                |               |               |               |
     * </pre>
     * 
     * <p>
     * The chosen approach is to introduce <code>doInternalXXX</code> methods responsible for executing the intended
     * form model logic (calls to <code>super</code>). Original methods are delegated to dispatcher form that invokes
     * later <code>formModel#doInternalXXX</code> method.
     * <p>
     * Note that child form invocations are made to form method, on this way we ensure child forms override methods are
     * deal correctly.</li>
     * </ol>
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class DispatcherFormModel extends BbDefaultFormModel {

        /**
         * El identificador del modelo.
         */
        private static final String FORM_MODEL_ID = "delegateFormModel";

        /**
         * Construye el modelo a partir del value model sobre el que construir el modelo.
         * 
         * @param valueModel
         *            el <em>value model</em> sobre el que construir el modelo.
         * 
         * @see AbstractForm(ValueModel)
         */
        public DispatcherFormModel(ValueModel valueModel) {

            super(valueModel);

            this.setId(DispatcherFormModel.FORM_MODEL_ID);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDirty() {

            // (JAF), 20080914, BbDispatcherForm.this.isDirty() executes ValidatingFormModel#isDirty() for every child
            return BbDispatcherForm.this.isDirty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {

            // (JAF), 20080914, BbDispatcherForm.this.reset() executes this.doInternalReset()
            BbDispatcherForm.this.reset();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void revert() {

            // (JAF), 20080914, BbDispatcherForm.this.revert() executes this.doInternalRevert();
            BbDispatcherForm.this.revert();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEnabled(boolean enabled) {

            // (JAF), 20080914, BbDispatcherForm.this.setEnabled() executes this.doInternalSetEnabled(Boolean);
            BbDispatcherForm.this.setEnabled(enabled);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setFormObject(Object formObject) {

            // (JAF), 20080914, delegateForm#setFormObject() executes this.doInternalSetFormObject(Object);
            BbDispatcherForm.this.setFormObject(formObject);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValidating(boolean validating) {

            // Child form models checks parent form model validating state at first
            super.setValidating(validating);

            for (FormModel childFormModel : this.getChildren()) {
                if (childFormModel instanceof ValidatingFormModel) {
                    ((ValidatingFormModel) childFormModel).setValidating(validating);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void validate() {

            // (JAF), 20090914, this call is not needed anymore: delegate form model should not validate itself, it's
            // just a dispatcher to the child form models
            // super.validate();

            for (final FormModel childFormModel : this.getChildren()) {
                if (childFormModel instanceof ValidatingFormModel) {
                    ((ValidatingFormModel) childFormModel).validate();
                }
            }
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#reset()} .
         */
        protected final void doInternalReset() {

            super.reset();
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#revert()} .
         */
        protected final void doInternalRevert() {

            super.revert();
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#setEnabled(boolean)} .
         * 
         * @param enabled
         *            the value to apply.
         */
        protected final void doInternalSetEnabled(boolean enabled) {

            super.setEnabled(enabled);
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#setFormObject(Object)} .
         * 
         * @param formObject
         *            the value to apply.
         */
        protected final void doInternalSetFormObject(Object formObject) {

            super.setFormObject(formObject);
        }
    }
}
