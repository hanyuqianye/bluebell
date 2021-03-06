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

package org.bluebell.richclient.form.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bluebell.richclient.bean.Problem;
import org.bluebell.richclient.bean.ValidationBean;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Tabla para matener la colección de errores producidos en una página de la aplicación. La tabla distingue entre los
 * errores relacionados con la correcta estructura de un plan de organización docente, y los errores de validación para
 * los campos de un formulario. Ésto es, porque el primer tipo de errores aparecen resultado de ejecutar un comando, y
 * se añaden desde el mismo de forma explícita a la tabla de errores, mientras que los segundos son originados por
 * Hibernate Validator, e incorporados a la tabla mediante un <code>MultipleValidationResultsReporter</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ValidationTable {

    /**
     * Lista que permite mostrar el contenido de la tabla de problemas en un una <code>JTable</code>.
     */
    private final EventList<ValidationBean> eventList;

    /**
     * Tabla con los errores sobre la correcta estuctura de un plan docente.
     */
    private final Map<String, Collection<ValidationBean>> modelProblems;

    /**
     * Tabla con los errores de validación.
     */
    private final Map<String, Collection<ValidationBean>> validationProblems;

    /**
     * Constructor por defecto.
     */
    public ValidationTable() {

        this.eventList = new BasicEventList<ValidationBean>();
        this.modelProblems = new HashMap<String, Collection<ValidationBean>>();
        this.validationProblems = new HashMap<String, Collection<ValidationBean>>();
    }

    /**
     * Añade una colección de errores sobre la estructura de un plan de organización docente, o remplaza la colección
     * asociada a la etiqueta <code>id</code>, dentro de la tabla de errores en caso de que esta existiese.
     * 
     * @param id
     *            identificador de la colección dentro de la tabla de errores.
     * @param problems
     *            colección de problemas del modelo a incorporar.
     */
    public final void addModelProblems(final String id, final Collection<Problem> problems) {

        this.modelProblems.put(id, this.wrapProblems(problems));
        this.refreshEventList();
    }

    /**
     * Añade una colección de errores de validación, o remplaza la colección asociada a la etiqueta <code>id</code>,
     * dentro de la tabla de errores de validación en caso de que esta existiese.
     * 
     * @param id
     *            identificador de la colección dentro de la tabla de errores.
     * @param problems
     *            colección de problemas de validación a incorporar.
     */
    public final void addValidationProblems(final String id, final Collection<Problem> problems) {

        this.validationProblems.put(id, this.wrapProblems(problems));
        this.refreshEventList();
    }

    /**
     * Elimina todos los errores de la tabla de errores.
     */
    public final void clear() {

        this.modelProblems.clear();
        this.validationProblems.clear();
        this.eventList.clear();
    }

    /**
     * Elimina todos los errores del modelo de la tabla de errores.
     */
    public final void clearModelErrors() {

        this.modelProblems.clear();
        this.refreshEventList();
    }

    /**
     * Elimina todos los errores de validación de la tabla de errores.
     */
    public final void clearValidationErrors() {

        this.validationProblems.clear();
        this.refreshEventList();
    }

    /**
     * Devuelve la <code>EvetList</code> que facilita la impresión de la tabla de errores en una <code>JTable</code>.
     * 
     * @return <code>EventList</code> con los errores de la tabla de errores.
     */
    public final EventList<ValidationBean> getEventList() {

        return this.eventList;
    }

    /**
     * Matiene sincronizada la tabla de errores y la <code>EventList</code> asociada.
     */
    private void refreshEventList() {

        // Se limpia la EventList
        this.eventList.clear();

        // Se agregan los problemas del Modelo.
        for (final Collection<ValidationBean> problems : this.modelProblems.values()) {
            this.eventList.addAll(problems);
        }

        // Se agregan los problemas de validación
        for (final Collection<ValidationBean> problems : this.validationProblems.values()) {
            this.eventList.addAll(problems);
        }
    }

    /**
     * Envuelve una colección de <code>Problem</code>, generando como resultado una colección de Beans de tipo
     * <code>ProblemBean</code>.
     * 
     * @param problems
     *            colección de <code>Problem</code> a envolver.
     * @return colección de tipo <code>ProblemBean</code> envoltorio.
     */
    private Collection<ValidationBean> wrapProblems(final Collection<Problem> problems) {

        final Collection<ValidationBean> problemBeans = new ArrayList<ValidationBean>();

        for (final Problem problem : problems) {
            problemBeans.add(new ValidationBean(problem));
        }

        return problemBeans;
    }
}
