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
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.EventBElement;
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
	
	public static String generatedById(AbstractExtension sourceElement){
		return sourceElement.getExtensionId();
	}
	
	public static TranslationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value, int priority, boolean remove){
		return new TranslationDescriptor(parent,feature,value, Integer.valueOf(priority), remove);
	}
	
	public static TranslationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value, int priority){
		return new TranslationDescriptor(parent,feature,value,Integer.valueOf(priority));
	}
	
	public static TranslationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value , Boolean remove) {
		return new TranslationDescriptor(parent, feature, value, remove);
	}
	
	/**
	 * @since 1.0
	 */
	public static TranslationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value, EObject before, int priority) {
		return new TranslationDescriptor(parent, feature, value, before, priority);
	}
	
	 public static Variable variable(String name, String comment) {
		Variable v =  MachineFactory.eINSTANCE.createVariable();
	    v.setName(name);
	    v.setLocalGenerated(true);
	    v.setComment(comment);
	    return v;  
	 }
	  
	public static Invariant invariant(String name, String predicate, String comment) {
		return invariant(name, false, predicate, comment);
	}
		
	public static Invariant invariant(String name, boolean theorem, String predicate,String comment) {
		Invariant i =  MachineFactory.eINSTANCE.createInvariant();
	    i.setName(name);
	    i.setTheorem(theorem);
	    i.setPredicate(predicate);
	    i.setLocalGenerated(true);
	    i.setComment(comment);
	    return i;  
	}

	public static Object event(String name) {
		return event(name, false, Convergence.ORDINARY, Collections.<String> emptyList(), "");
	}
		
	public static Object event(String name, boolean extended, Convergence convergence, List<String> refinesNames, String comment) {
		Event e =  MachineFactory.eINSTANCE.createEvent();
	    e.setName(name);
	    e.setExtended(extended);
	    e.setConvergence(convergence);
	    e.getRefinesNames().addAll(refinesNames);
	    e.setLocalGenerated(true);
	    e.setComment(comment);
	    return e;  
	}

	public static Object parameter(String name){
		return parameter(name,"");
	}
	
	public static Object parameter(String name, String comment) {
		Parameter p =  MachineFactory.eINSTANCE.createParameter();
	    p.setName(name);
	    p.setLocalGenerated(true);
	    p.setComment(comment);
	    return p;  
	}
	
	public static Object guard(String name, String predicate){
		return guard(name, false, predicate, "");
	}
	
	public static Object guard(String name, boolean theorem, String predicate, String comment) {
		Guard g =  MachineFactory.eINSTANCE.createGuard();
	    g.setName(name);
	    g.setTheorem(theorem);
	    g.setPredicate(predicate);
	    g.setLocalGenerated(true);
	    g.setComment(comment);
	    return g;  
	}
	
	public static Object action(String name, String expression){
		return action(name, expression, "");
	}
	
	public static Object action(String name, String expression, String comment) {
		Action a =  MachineFactory.eINSTANCE.createAction();
	    a.setName(name);
	    a.setAction(expression);
	    a.setLocalGenerated(true);
	    a.setComment(comment);
	    return a;  
	}

	public static Object context(String name, String comment) {
		Context ctx =  ContextFactory.eINSTANCE.createContext();
	    ctx.setName(name);
	    ctx.setLocalGenerated(true);
	    ctx.setComment(comment);
	    return ctx;
	}

	public static Object set(String name, String comment) {
		CarrierSet set =  ContextFactory.eINSTANCE.createCarrierSet();
	    set.setName(name);
	    set.setLocalGenerated(true);
	    set.setComment(comment);
	    return set;
	}

	public static Object constant(String name, String comment) {
		Constant constant =  ContextFactory.eINSTANCE.createConstant();
	    constant.setName(name);
	    constant.setLocalGenerated(true);
	    constant.setComment(comment);
	    return constant;
	}

	public static Axiom axiom(String name, String predicate,String comment) {
		return axiom(name,false, predicate, comment);
	}
	
	public static Axiom axiom(String name, boolean theorem, String predicate,String comment) {
		Axiom axm =  ContextFactory.eINSTANCE.createAxiom();
	    axm.setName(name);
	    axm.setTheorem(theorem);
	    axm.setPredicate(predicate);
	    axm.setLocalGenerated(true);
	    axm.setComment(comment);
	    return axm;  
	}

	@Deprecated
	public static Object witness(String name, String predicate) {
		return witness(name,predicate,"");
	}
	
	public static Object witness(String name, String predicate, String comment) {
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
