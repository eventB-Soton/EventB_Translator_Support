/*******************************************************************************
 *  Copyright (c) 2016 University of Southampton.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *   
 *  Contributors:
 *  University of Southampton - Initial implementation
 *******************************************************************************/
package ac.soton.emf.translator.eventb.rules;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.context.ContextPackage;
import org.eventb.emf.core.machine.MachinePackage;

import ac.soton.emf.translator.configuration.AbstractRule;
import ac.soton.emf.translator.configuration.IRule;


/**
 * a basis for rules that translate to event-B
 * @author cfs
 *
 */
public abstract class AbstractEventBGeneratorRule extends AbstractRule implements IRule {

	protected static final EReference components = CorePackage.Literals.PROJECT__COMPONENTS;
	protected static final EReference sees = MachinePackage.Literals.MACHINE__SEES;
	protected static final EAttribute seesNames = MachinePackage.Literals.MACHINE__SEES_NAMES;
	protected static final EAttribute refinesNames = MachinePackage.Literals.EVENT__REFINES_NAMES;
	protected static final EReference variables = MachinePackage.Literals.MACHINE__VARIABLES;
	protected static final EReference invariants = MachinePackage.Literals.MACHINE__INVARIANTS;
	protected static final EReference events = MachinePackage.Literals.MACHINE__EVENTS;
	protected static final EReference parameters = MachinePackage.Literals.EVENT__PARAMETERS;
	protected static final EReference witnesses = MachinePackage.Literals.EVENT__WITNESSES;
	protected static final EReference guards = MachinePackage.Literals.EVENT__GUARDS;
	protected static final EReference actions = MachinePackage.Literals.EVENT__ACTIONS;
	protected static final EReference sets = ContextPackage.Literals.CONTEXT__SETS;
	protected static final EReference constants = ContextPackage.Literals.CONTEXT__CONSTANTS;
	protected static final EReference axioms = ContextPackage.Literals.CONTEXT__AXIOMS;
	protected static final EReference _extends = ContextPackage.Literals.CONTEXT__EXTENDS;
	protected static final EAttribute extendsNames = ContextPackage.Literals.CONTEXT__EXTENDS_NAMES;
	protected static final EReference extensions = CorePackage.Literals.EVENT_BELEMENT__EXTENSIONS;

	protected ITranslatorStorage storage = TranslatorStorage.getDefault();


}
