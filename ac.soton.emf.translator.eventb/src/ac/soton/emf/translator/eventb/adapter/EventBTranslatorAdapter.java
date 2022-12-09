/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
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
package ac.soton.emf.translator.eventb.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.AttributeType;
import org.eventb.emf.core.CoreFactory;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedActionElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBNamedCommentedElement;
import org.eventb.emf.core.EventBNamedCommentedPredicateElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.context.ContextPackage;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.MachinePackage;

import ac.soton.emf.translator.TranslationDescriptor;
import ac.soton.emf.translator.configuration.AttributeIdentifiers;
import ac.soton.emf.translator.configuration.DefaultAdapter;
import ac.soton.emf.translator.configuration.IAdapter;
import ac.soton.emf.translator.eventb.utils.Utils;

/**
 * This implementation of IAdapter can be used for translations that target
 *  the EventB EMF meta-model and its extensions.
 * 
 *  @see ac.soton.emf.translator.configuration.IAdapter
 *  @see ac.soton.emf.translator.configuration.DefaultAdapter
 *  
 * @author cfs
 *
 */

public class EventBTranslatorAdapter extends DefaultAdapter implements IAdapter {

	/**
	 * used to store the order/position of extensions in the source
	 * The key(String) is the ExtensionID property of an AbstractExtension (if it has one)
	 * The value(Integer) is the position of that AbstractExtension in the containment of its parent.
	 * 
	 * @see org.eventb.emf.core.AbstractExtension
	 */
	private Map<String,Integer> extensionOrder = new HashMap<String,Integer>();
	
	/**
	 * 
	 * Gets the position of the source extension from which the given object was generated.
	 * The idea of this is that the translator will maintain the order of generated elements in accordance with the 
	 * source from which they were generated. If the target is not an EventBElement or does not have an appropriate 
	 * reference to a Extension in an attribute whose key is AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY, 
	 * the returned position is the end of the list
	 * 
	 * @see ac.soton.emf.translator.eventb.adapter.EventBTranslatorAdapter.extensionOrder
	 * @see AttributeIdentifiers
	 * 
	 * @param target
	 * @return position in the extensionOrder list 
	 * 
	 */
	protected Integer getExtensionPosition(Object target) {
		if (target instanceof EventBElement){
			Attribute attribute = ((EventBElement)target).getAttributes().get(AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY);
			if (attribute==null) return extensionOrder.size();
			String translation_ID = (String) attribute.getValue();
			if (translation_ID==null || !translation_ID.contains("::")) return extensionOrder.size();
			String extensionID = translation_ID.substring(translation_ID.lastIndexOf("::")+2);
			Integer v_xod = extensionOrder.get(extensionID);
			if (v_xod==null) return extensionOrder.size(); // not an extension => user entered stuff comes last
			return v_xod;
		}else{
			return extensionOrder.size();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * Resets any storage in Utils.storage, then calculates and records the extensionOrder list.
	 * 
	 * @see ac.soton.emf.translator.eventb.utils.Utils
	 * 
	 */
	@Override	
	public void initialiseAdapter(Object sourceElement){
		if (sourceElement instanceof EventBObject){
			Object targetComponent = getTargetComponent(sourceElement); 
			Utils.resetStorage(targetComponent);
			//set up map of extensions ids and their positions
			if (targetComponent instanceof EventBObject) {
				int i=0;
				for (EObject ae : ((EventBObject) targetComponent).getAllContained(CorePackage.Literals.ABSTRACT_EXTENSION, true)){
					if (ae !=null) {
						String id = ((AbstractExtension)ae).getExtensionId();
						if (id != null && !extensionOrder.containsKey(id)) {
							extensionOrder.put(id, i++);
							EClass eclass = ae.eClass();
							EList<EReference> refs = eclass.getEReferences();
							for (EReference r : refs) {
								Object rae = ae.eGet(r);
								if (rae instanceof AbstractExtension) {
									id = ((AbstractExtension)rae).getExtensionId();
									extensionOrder.put(id, i++);
								}
							}
						}
					}
				}
			}
		}
		super.initialiseAdapter(sourceElement);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * if sourceElement is an EventBObject, returns its containing EventBNamedCommentedComponentElement.
	 * otherwise defers to super
	 * 
	 * @since 1.0
	 */
	@Override
	public Object getTargetComponent(Object sourceElement) {
		return sourceElement instanceof EventBObject? 
				((EventBObject)sourceElement).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT)
				: super.getTargetComponent(sourceElement);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * Return true if feature is Components in a Project and value is an EventBNamedCommentedComponentElement
	 * Otherwise return false.
	 * 
	 */
	@Override
	public boolean isRoot(TranslationDescriptor translationDescriptor) {
		if (translationDescriptor.feature == CorePackage.Literals.PROJECT__COMPONENTS &&
				translationDescriptor.value instanceof EventBNamedCommentedComponentElement){
			return true;
		}else{
			return false;
		}
	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * If the translation descriptor is for a removal, returns null. Otherwise,
	 * Uses the given root source element to form the basis of a URL (the project is assumed to be the same)
	 * and the generated 'value' in the translation descriptor to find a component name and file extension
	 * Return a URI for..
	 *  a Rodin machine (.bum) or..
	 *  a Rodin context (.buc) or..
	 *  If this does not succeed, defers to super.
	 *  
	 */
	@Override
	public URI getComponentURI(TranslationDescriptor translationDescriptor, EObject rootSourceElement) {
		if (translationDescriptor.remove == true) return null;
		String projectName = EcoreUtil.getURI(rootSourceElement).segment(1);
		URI projectUri = URI.createPlatformResourceURI(projectName, true);
		EventBNamedCommentedComponentElement component = null;
		if (translationDescriptor.feature == CorePackage.Literals.PROJECT__COMPONENTS &&
				translationDescriptor.value instanceof EventBNamedCommentedComponentElement){
			component = (EventBNamedCommentedComponentElement) translationDescriptor.value;
			
		}else if (translationDescriptor.parent instanceof EventBElement){
			component = (EventBNamedCommentedComponentElement) ((EventBElement)translationDescriptor.parent).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		}
		if (component != null){
			String fileName = component.getName();
			String ext = 	component instanceof Context? "buc" :  
							component instanceof Machine? "bum" :
								"";
			URI fileUri = projectUri.appendSegment(fileName).appendFileExtension(ext); //$NON-NLS-1$
			return fileUri;
		}
		return super.getComponentURI(translationDescriptor, rootSourceElement);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * Return all EMF resources in the same project as the source element that are EventB Machines or Contexts 
	 * 
	 */
	@Override
	public Collection<Resource> getAffectedResources(TransactionalEditingDomain editingDomain, EObject sourceElement) throws IOException {
		List<Resource> affectedResources = new ArrayList<Resource>();
		String projectName = EcoreUtil.getURI(sourceElement).segment(1);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()){
			try {
				IResource[] members = project.members();
				ResourceSet resourceSet = editingDomain.getResourceSet();
				for (IResource res : members){
					final URI fileURI = URI.createPlatformResourceURI(projectName + "/" + res.getName(), true);
					if ("bum".equals(fileURI.fileExtension()) || "buc".equals(fileURI.fileExtension())){ 
						Resource resource = resourceSet.getResource(fileURI, false);
						if (resource != null) {
							if (!resource.isLoaded()) {
								resource.load(Collections.emptyMap());
							}
							if (resource.isLoaded()) {
								affectedResources.add(resource);
							} 
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return affectedResources;
	}
	

	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * defers to super
	 * 
	 */
	@Override
	public boolean inputFilter(Object object,  String translationId) {
		return super.inputFilter(object, translationId);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * The following are filtered out (i.e. return false):
	 * 1) invariants and axioms which are already present in the parent or in scope via context extension/sees or machine refinement
	 * 2) features of the parent event which are already present or present via event extension
	 * 3) anything filtered out by super
	 * 
	 */
	@Override
	public boolean outputFilter(TranslationDescriptor translationDescriptor) {
		
		//filter any new elements that are already there 	
		if (translationDescriptor.parent!=null) { //if no parent we cannot check
			Object featureValue = translationDescriptor.parent.eGet(translationDescriptor.feature);
			
			if (MachinePackage.Literals.MACHINE__INVARIANTS.equals(translationDescriptor.feature) ||
					ContextPackage.Literals.CONTEXT__AXIOMS.equals(translationDescriptor.feature)	){
				return constraintFilter((EventBNamedCommentedComponentElement) translationDescriptor.parent, 
						(EventBNamedCommentedPredicateElement) translationDescriptor.value);
			}
			if (featureValue instanceof EList){
				EList<?> list = (EList<?>)featureValue;
				for (Object el : list){
					if (match(el,translationDescriptor.value)) 
						return false;
				}
			}

			// filter any new values which are already present by event extension
			if (translationDescriptor.parent instanceof Event){
				for (Object el : getExtendedValues((Event)translationDescriptor.parent,translationDescriptor.feature)){
					if (match(el,translationDescriptor.value)) 
						return false;
				}
			}
		}
		return super.outputFilter(translationDescriptor);
	}

	/**
	 * Local method used by outputFilter.
	 * Prevents repeating invariants and axioms when they are already in scope via machine/context relationships.
	 * 
	 * @param component
	 * @param newConstraint
	 * @return whether the new constraint should be added or not
	 * @since 0.1
	 */
	protected boolean constraintFilter (EventBNamedCommentedComponentElement component, EventBNamedCommentedPredicateElement newConstraint) {
		EList<? extends EventBNamedCommentedPredicateElement> constraintsToCheck = null;
		List<EventBNamedCommentedComponentElement> inScope = new ArrayList<EventBNamedCommentedComponentElement>();
		if (component instanceof Machine) {
			inScope.addAll(((Machine)component).getRefines());
			inScope.addAll(((Machine)component).getSees());
			constraintsToCheck = ((Machine)component).getInvariants();
		}
		if (component instanceof Context) {
			inScope.addAll(((Context)component).getExtends());
			constraintsToCheck = ((Context)component).getAxioms();
		}
		for (EventBNamedCommentedPredicateElement existingConstraint : constraintsToCheck){
			if (stringEquivalent(
					((EventBNamedCommentedPredicateElement)existingConstraint).getPredicate(),
					((EventBNamedCommentedPredicateElement)newConstraint).getPredicate()
					)
					){
				return false;			
			}
		}
		for (EventBNamedCommentedComponentElement  cp1 : inScope) {
			if (cp1!=component && constraintFilter(cp1, newConstraint) == false) return false;
		}
		return true;
	}

	/**
	 * Local method used by outputFilter.
	 * for a particular feature, transitively get all the elements which are present by event extension
	 * 
	 * @param event
	 * @param feature
	 * @return a list of elements in the feature, either directly or by event extension
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
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * Tests whether two elements should be considered to be the same in event B terms.
	 * I.e. two predicate elements have equivalent predicate strings
	 * 		two action elements have equivalent action strings
	 * 		two named elements have equivalent name strings
	 * 		two strings are equal
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
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * construct a translation ID from the given basic part and the given root source element
	 * using "::" as a separator. The part from the source element depends on its type.
	 * For a AbstractExtension it is the Extension ID and for a EventBelement it is its reference ID.
	 * If it is neither of these, defer to super.
	 * 
	 */
	@Override
	public String getTranslationId(String basicTranslatorID, Object rootSourceElement){
		return  rootSourceElement instanceof AbstractExtension ? 
					basicTranslatorID+"::"+((AbstractExtension)rootSourceElement).getExtensionId()
			:	rootSourceElement instanceof EventBElement ? 
					basicTranslatorID+"::"+((EventBElement)rootSourceElement).getReference()
			:
				super.getTranslationId(basicTranslatorID, rootSourceElement);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * if target is an EventBElement and translationID is not null,
	 * 	1) sets the local generated property of the element,
	 * 	2) adds a string attribute to the element whose
	 * 		key is AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY and whose
	 * 		value is the given translation ID string
	 * otherwise, defers to super.
	 * @see AttributeIdentifiers
	 * 
	 */
	@Override
	public void setGeneratedBy(Object target, String translationID) {
		if (target instanceof EventBElement && translationID!=null){
				// set the generated property
				((EventBElement)target).setLocalGenerated(true);				
				// add an attribute with this translation ID
				Attribute attribute =   CoreFactory.eINSTANCE.createAttribute();
				attribute.setValue(translationID);
				attribute.setType(AttributeType.STRING);
				((EventBElement)target).getAttributes().put(AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY,attribute);
		}else{
			super.setGeneratedBy(target, translationID);
		}	
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * if target is an EventBElement and translationID is not null,
	 *  returns true if the target has an attribute whose 
	 *    key is AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY and whose
	 *    value matches the translationID.
	 *    and false if not.
	 * Otherwise defers to super.
	 * @see AttributeIdentifiers
	 * 
	 */
	@Override
	public boolean wasGeneratedBy(Object target, String translationId) {
		if (target instanceof EventBElement && translationId!=null){
			Attribute attribute = ((EventBElement)target).getAttributes().get(AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY);
			return attribute!= null && translationId.equals(attribute.getValue());
		}
		return super.wasGeneratedBy(target, translationId);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * if the source and target are both EventBElement, 
	 * adds to target, a string attribute whose
	 *   key is AttributeIdentifiers.TRANSLATOR__SOURCE_ELEMENT_KEY and whose
	 *   value is the the uri of the source as a string.
	 * Otherwise defers to super
	 * @see AttributeIdentifiers
	 */
	public void setSourceElement(Object target, Object source) {
		if (target instanceof EventBElement && source instanceof EventBElement) {
			Attribute attribute =   CoreFactory.eINSTANCE.createAttribute();
			attribute.setValue(EcoreUtil.getURI((EObject)source).toString());
			attribute.setType(AttributeType.STRING);
			((EventBElement)target).getAttributes().put(AttributeIdentifiers.TRANSLATOR__SOURCE_ELEMENT_KEY,attribute);
		}else {
			super.setSourceElement(target, source);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * if the object is an EventBElement,
	 *   adds to the element, an integer attribute whose 
	 *   key is AttributeIdentifiers.TRANSLATOR__PLACEMENT_PRIORITY_KEY and whose
	 *   value is the given priority
	 * @see AttributeIdentifiers
	 * 
	 */
	@Override
	public void setPriority(Object object, int priority) {
		if (object instanceof EventBElement){
			EventBElement element = (EventBElement)object;
			// add an attribute with the priority for ordering
			Attribute attribute =   CoreFactory.eINSTANCE.createAttribute();
			attribute.setValue(priority);
			attribute.setType(AttributeType.INTEGER);
			element.getAttributes().put(AttributeIdentifiers.TRANSLATOR__PLACEMENT_PRIORITY_KEY,attribute);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * EventBTranslatorAdapter implementation:
	 * If the object is an EventBElement, the position is after any higher priority elements and after
	 * any elements that were generated by extensions that occur earlier in the model than the extension that generated this object.
	 * 
	 * @see ac.soton.emf.translator.configuration.IAdapter#getPos(org.eclipse.emf.common.util.EList, int)
	 *
	 */
	@Override
	public int getPos(List<?> list, Object object) {
		if(object instanceof EventBElement){
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
					if ((v_pri>0 && (pri<=0 || pri > v_pri )) || (v_pri < 1 && pri < v_pri ) || (v_pri==pri && v_xod<=xod)){
						pos = i+1;
					};
					
				}
			}
			return pos;
		}else{
			return list.size();
		}
	}

	
	/**
	 * Local method to get the priority value of the given object.
	 * If the object is an EventBObject and has an attribute with key AttributeIdentifiers.TRANSLATOR__PLACEMENT_PRIORITY_KEY
	 * the attributes value is returned.
	 * Otherwise return 0.
	 * @see AttributeIdentifiers
	 * 
	 * @param object
	 * @return
	 */
	protected int getPriority(Object object) {
		if (object instanceof EventBObject){
			Attribute attribute= ((EventBElement)object).getAttributes().get(AttributeIdentifiers.TRANSLATOR__PLACEMENT_PRIORITY_KEY);
			Integer pri = (Integer) (attribute==null? null : attribute.getValue());
			if (pri==null) pri = 0; // no priority => user stuff at priority 0
			return pri;
		}else{
			return 0;
		}
	}
}
