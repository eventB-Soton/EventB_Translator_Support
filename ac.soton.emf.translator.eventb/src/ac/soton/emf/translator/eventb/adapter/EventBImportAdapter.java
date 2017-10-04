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
package ac.soton.emf.translator.eventb.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.AttributeType;
import org.eventb.emf.core.CoreFactory;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.EventBNamedCommentedActionElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBNamedCommentedElement;
import org.eventb.emf.core.EventBNamedCommentedPredicateElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.persistence.AttributeIdentifiers;

import ac.soton.emf.translator.TranslationDescriptor;
import ac.soton.emf.translator.configuration.IAdapter;


/**
 * This implementation of IAdapter can be used for importers that target
 *  the EventB EMF meta-model and its extensions
 * 
 * @author cfs
 *
 */

public class EventBImportAdapter implements IAdapter {

	/**
	 * used to store the order position of extensions
	 */
	private Map<String,Integer> extensionOrder = new HashMap<String,Integer>();
	
	/**
	 * 
	 * @param object
	 * @return
	 */
	protected Integer getExtensionPosition(Object object) {
		Attribute generatorAttribute = ((EventBElement)object).getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY);
		String generatorID = (String) (generatorAttribute==null? null : generatorAttribute.getValue());
		Integer v_xod = extensionOrder.get(generatorID);
		if (v_xod==null) v_xod = extensionOrder.size(); // not an extension => user entered stuff comes last
		return v_xod;
	}
	
	
	
	/**
	 * 
	 */
	@Override	
	public void initialiseAdapter(Object sourceElement){
		if (sourceElement instanceof EventBObject){
			//set up map of extensions ids and their positions
			EventBObject component = ((EventBObject) sourceElement).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
			int i=0;
			for (EObject ae : component.getAllContained(CorePackage.Literals.ABSTRACT_EXTENSION, true)){
				if (ae !=null) {
					String id = ((AbstractExtension)ae).getExtensionId();
					extensionOrder.put(id, i++);
				}
			}
		}
	}

	
	/**
	 * returns a URI for..
	 *  a Rodin machine (.bum) or..
	 *  a Rodin context (.buc) or..
	 *  , for the composition machine, an EMF serialisation of the machine (.xmb)
	 * 
	 */
	@Override
	public URI getComponentURI(TranslationDescriptor translationDescriptor, EObject rootElement) {
		if (translationDescriptor.remove == false && 
				translationDescriptor.feature == CorePackage.Literals.PROJECT__COMPONENTS &&
				translationDescriptor.value instanceof EventBNamedCommentedComponentElement){
			String projectName = EcoreUtil.getURI(rootElement).segment(1);
			URI projectUri = URI.createPlatformResourceURI(projectName, true);
			String fileName = ((EventBNamed)translationDescriptor.value).getName();
			String ext = translationDescriptor.value instanceof Context? "buc" :  "bum";
			URI fileUri = projectUri.appendSegment(fileName).appendFileExtension(ext); //$NON-NLS-1$
			return fileUri;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see ac.soton.emf.translator.IAdapter#inputFilter(java.lang.Object)
	 */
	@Override
	public boolean inputFilter(Object object,  Object sourceID) {
		return !(object instanceof EventBElement && 
				sourceID.equals(
					((EventBElement)object).getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY))
				);
	}
	
	/* (non-Javadoc)
	 * @see ac.soton.emf.translator.IAdapter#outputFilter(java.lang.Object)
	 */
	@Override
	public boolean outputFilter(TranslationDescriptor translationDescriptor) {
		
		//filter any new elements that are already there 	
		if (translationDescriptor.parent!=null) { //if no parent we cannot check
			Object featureValue = translationDescriptor.parent.eGet(translationDescriptor.feature);
			if (featureValue instanceof EList){
				EList<?> list = (EList<?>)featureValue;
				for (Object el : list){
					if (match(el,translationDescriptor.value)) return false;
				}
			}
			// filter any new values which are already present by event extension
			if (translationDescriptor.parent instanceof Event){
				for (Object el : getExtendedValues((Event)translationDescriptor.parent,translationDescriptor.feature)){
					if (match(el,translationDescriptor.value)) return false;
				}
			}
		}
		return true;
	}


	/*
	 * transitively get all the elements which are present by event extension
	 */
	@SuppressWarnings("unchecked")
	private List<Object> getExtendedValues(Event event, EStructuralFeature feature) {
		if (!(event.isExtended()) || event.getRefines().isEmpty()){
			return new ArrayList<Object>();
		}else{
			Event refinedEvent = event.getRefines().get(0);
			List<Object> extended = getExtendedValues(refinedEvent,feature);

			Object refinedFeatureValue = refinedEvent.eGet(feature);
			if (refinedFeatureValue instanceof List){
				extended.addAll((List<Object>)refinedEvent.eGet(feature));
			}
			return extended;
		}
	}
///end of filter

	/**
	 * match
	 * test whether two elements should be considered to be the same in event B terms
	 */
	
	@Override
	public boolean match(Object el1, Object el2) {
		if (el1.getClass()!=el2.getClass()) return false;
		if (el1 instanceof EventBNamedCommentedPredicateElement){	
			return stringEquivalent(
					((EventBNamedCommentedPredicateElement)el1).getPredicate(),
					((EventBNamedCommentedPredicateElement)el2).getPredicate()
					);
		}else if (el1 instanceof EventBNamedCommentedActionElement){	
			return stringEquivalent(
					((EventBNamedCommentedActionElement)el1).getAction(),
					((EventBNamedCommentedActionElement)el2).getAction()
					);
		} else if (el1 instanceof EventBNamedCommentedElement){
			String s1 = ((EventBNamedCommentedElement)el1).getName();
			String s2 = ((EventBNamedCommentedElement)el2).getName();
			return (s1 != null && s1.equals(s2));
		} else if(el1 instanceof String && el2 instanceof String) {
			return (el1 != null && el1.equals(el2));
		} else return false;
	}

	private boolean stringEquivalent(String s1, String s2) {
		if (s1==null) return s2==null;
		if (s2==null) return false;
		String s1r = s1.replaceAll("\\s", "");
		String s2r = s2.replaceAll("\\s", "");
		return s1r.equals(s2r);
	}

	////end of match
	
	/**
	 * get the generator ID from the given EObject
	 */
	@Override
	public Object getSourceId(Object object){
		return object instanceof AbstractExtension ? 
				((AbstractExtension)object).getExtensionId()
			:	object instanceof EventBElement ? 
				((EventBElement)object).getReference()
			:
				null;
	}

	/**
	 * adds attributes to record:
	 * a) the id of the extension that generated this element
	 * b) the fact that this element is generated
	 * 
	 * @param generatedByID
	 * @param element
	 */
	@Override
	public void annotateTarget(Object sourceID, Object object) {
		if (object instanceof EventBElement && sourceID instanceof String){
			EventBElement element = (EventBElement)object;
			// set the generated property
			element.setLocalGenerated(true);				
			// add an attribute with this generators ID
			Attribute genID =   CoreFactory.eINSTANCE.createAttribute();
			genID.setValue(sourceID);
			genID.setType(AttributeType.STRING);
			element.getAttributes().put(AttributeIdentifiers.GENERATOR_ID_KEY,genID);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public boolean isAnnotatedWith(Object object, Object sourceID) {
		if (object instanceof EventBElement){
			Attribute translatedBy = ((EventBElement)object).getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY);
			if (translatedBy!= null && sourceID.equals(translatedBy.getValue()) ){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param v
	 * @return
	 */
	protected int getPriority(Object object) {
		Attribute priorityAttribute= ((EventBElement)object).getAttributes().get(AttributeIdentifiers.PRIORITY_KEY);
		Integer pri = (Integer) (priorityAttribute==null? null : priorityAttribute.getValue());
		if (pri==null) pri = 0; // no priority => user stuff at priority 0
		return pri;
	}
	
	/**
	 * adds attributes to record:
	 * a) the priority of this element for ordering
	 * 
	 * @param priority
	 * @param element
	 */
	@Override
	public void setPriority(int priority, Object object) {
		if (object instanceof EventBElement){
			EventBElement element = (EventBElement)object;
			// add an attribute with the priority for ordering
			Attribute pri =   CoreFactory.eINSTANCE.createAttribute();
			pri.setValue(priority);
			pri.setType(AttributeType.INTEGER);
			element.getAttributes().put(AttributeIdentifiers.PRIORITY_KEY,pri);
		}
	}

	/**
	 * 
	 * @see ac.soton.emf.translator.configuration.IAdapter#getPos(org.eclipse.emf.common.util.EList, int)
	 * 
	 * 
	 * 
	 */
	@Override
	public int getPos(List<?> list, Object object) {
		//calculate the correct index - i.e. after any higher priority elements and
		//after stuff translated by earlier extensions which have the same priority
		int pri = getPriority(object);
		int pos = 0;
		int xod = getExtensionPosition(object);
		for (int i=0; i<list.size(); i++){
			Object v = list.get(i);
			if(v instanceof EventBElement){
				
				//calculate extension order od of this value
				Integer v_xod = getExtensionPosition(v);
				
				//calculate priority order of this value
				Integer v_pri = getPriority(v);
				
				//priority order = highest 1..10,0,-1..-10
				if ((v_pri>0 && (pri<=0 || pri > v_pri )) || (v_pri < 1 && pri < v_pri ) || (v_pri==pri && v_xod<xod)){
					pos = i+1;
				};
				
			}
		}
		return pos;
	}


}
