/*******************************************************************************
 * Copyright (c) 2014, 2022 University of Southampton.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    University of Southampton - initial API and implementation
 *******************************************************************************/

package ac.soton.emf.translator.eventb.utils;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBNamedCommentedElement;
import org.eventb.emf.core.context.Axiom;
import org.eventb.emf.core.context.CarrierSet;
import org.eventb.emf.core.context.Constant;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.context.ContextFactory;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Convergence;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Guard;
import org.eventb.emf.core.machine.Invariant;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.MachineFactory;
import org.eventb.emf.core.machine.Parameter;
import org.eventb.emf.core.machine.Variable;
import org.eventb.emf.core.machine.Witness;

import ac.soton.emf.translator.TranslationDescriptor;

/**
 * This is a collection of static convenience methods for making objects that are needed in generator Rules
 * 
 */

public class Make {

	/**
	 * Make a TranslationDescriptor to add a newly generated element 
	 * 
	 * @param parent
	 * @param feature
	 * @param value
	 * @param before
	 * @param priority
	 * @param source
	 * @return
	 * @since 1.0
	 */
	public static TranslationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value, EObject before, int priority, EObject source){
		return new TranslationDescriptor(parent,feature,value, before, Integer.valueOf(priority), false, source);
	}
	
	/**
	 * Make a TranslationDescriptor to remove an element 
	 * 
	 * @param parent
	 * @param feature
	 * @param value
	 * @return
	 * @since 1.0
	 */
	public static TranslationDescriptor removeDescriptor(EventBElement parent, EStructuralFeature feature, Object value){
		return new TranslationDescriptor(parent,feature,value, null, null, true, null);
	}
	
	// Making Event-B elements
	
	/**
	 * Make a Variable
	 * 
	 * @param name
	 * @param comment
	 * @return
	 */
	public static Variable variable(String name, String comment) {
		Variable v =  MachineFactory.eINSTANCE.createVariable();
	    v.setName(name);
	    v.setLocalGenerated(true);
	    v.setComment(comment);
	    return v;  
	 }
	  
	/**
	 * Make an Invariant
	 * 
	 * @param name
	 * @param predicate
	 * @param comment
	 * @return
	 */
	public static Invariant invariant(String name, String predicate, String comment) {
		return invariant(name, false, predicate, comment);
	}
		
	/**
	 * Make an Invariant or Theorem
	 * 
	 * @param name
	 * @param theorem
	 * @param predicate
	 * @param comment
	 * @return
	 */
	public static Invariant invariant(String name, boolean theorem, String predicate,String comment) {
		Invariant i =  MachineFactory.eINSTANCE.createInvariant();
	    i.setName(name);
	    i.setTheorem(theorem);
	    i.setPredicate(predicate);
	    i.setLocalGenerated(true);
	    i.setComment(comment);
	    return i;  
	}

	/**
	 * Make an Event
	 * 
	 * @param name
	 * @param extended
	 * @param convergence
	 * @param refinesNames
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Event event(String name, boolean extended, Convergence convergence, List<String> refinesNames, String comment) {
		Event e =  MachineFactory.eINSTANCE.createEvent();
	    e.setName(name);
	    e.setExtended(extended);
	    e.setConvergence(convergence);
	    e.getRefinesNames().addAll(refinesNames);
	    e.setLocalGenerated(true);
	    e.setComment(comment);
	    return e;  
	}
	
	/**
	 * Make an Event with the usual default properties
	 * 
	 * @param name
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Event event(String name, String comment) {
		return event(name, false, Convergence.ORDINARY, Collections.<String> emptyList(), comment);
	}
	
	
	/**
	 * Make a Parameter
	 * 
	 * @param name
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Parameter parameter(String name, String comment) {
		Parameter p =  MachineFactory.eINSTANCE.createParameter();
	    p.setName(name);
	    p.setLocalGenerated(true);
	    p.setComment(comment);
	    return p;  
	}
	
	/**
	 * Make a Guard
	 * 
	 * @param name
	 * @param predicate
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Guard guard(String name, String predicate, String comment){
		return guard(name, false, predicate, "");
	}
	
	/**
	 * Make a Guard or Theorem
	 * 
	 * @param name
	 * @param theorem
	 * @param predicate
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Guard guard(String name, boolean theorem, String predicate, String comment) {
		Guard g =  MachineFactory.eINSTANCE.createGuard();
	    g.setName(name);
	    g.setTheorem(theorem);
	    g.setPredicate(predicate);
	    g.setLocalGenerated(true);
	    g.setComment(comment);
	    return g;  
	}
	
	/**
	 * Make an Action
	 * 
	 * @param name
	 * @param expression
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Action action(String name, String expression, String comment) {
		Action a =  MachineFactory.eINSTANCE.createAction();
	    a.setName(name);
	    a.setAction(expression);
	    a.setLocalGenerated(true);
	    a.setComment(comment);
	    return a;  
	}

	/**
	 * Make a Context
	 * 
	 * @param name
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Context context(String name, String comment) {
		Context ctx =  ContextFactory.eINSTANCE.createContext();
	    ctx.setName(name);
	    ctx.setLocalGenerated(true);
	    ctx.setComment(comment);
	    return ctx;
	}

	/**
	 * Make a Carrier Set
	 * 
	 * @param name
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static CarrierSet set(String name, String comment) {
		CarrierSet set =  ContextFactory.eINSTANCE.createCarrierSet();
	    set.setName(name);
	    set.setLocalGenerated(true);
	    set.setComment(comment);
	    return set;
	}

	/**
	 * Make a Constant
	 * 
	 * @param name
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Constant constant(String name, String comment) {
		Constant constant =  ContextFactory.eINSTANCE.createConstant();
	    constant.setName(name);
	    constant.setLocalGenerated(true);
	    constant.setComment(comment);
	    return constant;
	}

	/**
	 * Make an Axiom
	 * 
	 * @param name
	 * @param predicate
	 * @param comment
	 * @return
	 */
	public static Axiom axiom(String name, String predicate,String comment) {
		return axiom(name,false, predicate, comment);
	}
	
	/**
	 * Make an Axiom or Theorem
	 * 
	 * @param name
	 * @param theorem
	 * @param predicate
	 * @param comment
	 * @return
	 */
	public static Axiom axiom(String name, boolean theorem, String predicate,String comment) {
		Axiom axm =  ContextFactory.eINSTANCE.createAxiom();
	    axm.setName(name);
	    axm.setTheorem(theorem);
	    axm.setPredicate(predicate);
	    axm.setLocalGenerated(true);
	    axm.setComment(comment);
	    return axm;  
	}
	
	/**
	 * Make a Witness
	 * 
	 * @param name
	 * @param predicate
	 * @param comment
	 * @return
	 * @since 1.0
	 */
	public static Witness witness(String name, String predicate, String comment) {
		Witness g =  MachineFactory.eINSTANCE.createWitness();
	    g.setName(name);
	    g.setPredicate(predicate);
	    g.setLocalGenerated(true);
	    g.setComment("");
	    return g;
	}

	/**
	 * Constructs a reference to an element that will exist in the future. 
	 * The component (which must be a machine or context) is used to construct
	 * the URI of the proxy - so it must already be linked to a resource in the workspace.
	 * 
	 * @param component - Machine or Context that will contain the element (must already be in a resource so that it has a full URI to its final resource)
	 * @param proxy  - an EventB Element to be used as the proxy (use the relevant Factory to create a fresh element of the desired type)
	 * @param name - the name of the element
	 * @return
	 * @since 1.0
	 */
	public static EventBNamedCommentedElement proxyReference(EventBNamedCommentedComponentElement component, EventBNamedCommentedElement proxy, String name) {
		//work out the name of the component type .. "Machine" or "Context"
		String componentType;
		if (component instanceof Machine) {
			componentType = "Machine";
		}else if (component instanceof Context) {
			componentType = "Context";
		}else
			return proxy;
		//work out the name of the element type
		String elementType = proxy.eClass().getName();
		//create a uri to reference the named element
		URI uri = EcoreUtil.getURI(component);
		String fragment = uri.fragment().replace("::"+componentType+"::","::"+elementType+"::")+"."+name;
		uri = uri.trimFragment();
		uri = uri.appendFragment(fragment);
		//set the proxy uri
		((InternalEObject)proxy).eSetProxyURI(uri);
		return proxy;
	}
	
}
