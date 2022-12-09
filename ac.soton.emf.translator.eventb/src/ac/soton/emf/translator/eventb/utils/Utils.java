/*******************************************************************************
 * Copyright (c) 2014, 2019 University of Southampton.
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

import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Machine;

import ac.soton.emf.translator.eventb.internal.rules.TranslatorStorage;
import ac.soton.emf.translator.eventb.rules.ITranslatorStorage;

/**
 * @since 0.1
 */
public class Utils {

	protected static ITranslatorStorage storage = TranslatorStorage.getDefault();
	
	public static final String TRANSLATION_TARGET_KEY = "translationTarget";
	public static final String INITIALISATION_EVENT_NAME   = "INITIALISATION";
			
	public static void resetStorage(Object translationTarget) {
		storage.reset();
		storage.stash(Utils.TRANSLATION_TARGET_KEY, translationTarget);
	}
	
	/**
	 * gets the translation target component from storage
	 * (the translation target component must be added to the storage before the rules are run)
	 * 
	 * @return
	 */
	public static EventBNamedCommentedComponentElement getTranslationTarget() {
		return (EventBNamedCommentedComponentElement) storage.fetch(TRANSLATION_TARGET_KEY);
	}
	
	/**
	 * If the translation target of the root containing the given source Element is a Machine,
	 * returns the initialisation event of that target Machine 
	 * Otherwise returns null
	 * 
	 * @return the initialisation event in the target Machine or null
	 */
	public static Event getTargetInitialisationEvent(){
		Object container = storage.fetch(TRANSLATION_TARGET_KEY);
		if (container instanceof Machine) {
			for(Event e : ((Machine) container).getEvents()){
				if(e.getName().equals(INITIALISATION_EVENT_NAME))
					return e;
			}
		}
		return null;
	}
	
//	/**
//	 * Convenience method to check whether an element has been generated already in the list of TranslationDescriptors
//	 * (parent and/or feature may be null if not required to be matched)
//	 * 
//	 * @param generatedElements
//	 * @param parent (or null)
//	 * @param feature (or null)
//	 * @param identifier
//	 * @return
//	 */
//	public static boolean generated(List<TranslationDescriptor> generatedElements, EventBElement parent, EStructuralFeature feature, String identifier){
//		return Find.generatedElement(generatedElements, parent, feature, identifier) != null;
//	}
//	
//
//	public static boolean generatedBy(Object object, Object sourceElement){
//		if (sourceElement instanceof EventBObject){
//			AbstractExtension ae = (AbstractExtension) ((EventBObject) sourceElement).getContaining(CorePackage.Literals.ABSTRACT_EXTENSION);
//			if (ae instanceof AbstractExtension){
//				return generatedBy(object, Make.generatedById(ae));
//			}
//		}
//		return false;
//	}
//	
//	public static boolean generatedBy(Object object, String id){
//		if (object instanceof EventBElement){
//			Attribute generatedBy = ((EventBElement)object).getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY);
//			if (generatedBy!= null && id.equals(generatedBy.getValue()) ){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public static boolean generated(Object object){
//		if (object instanceof EventBElement){
//			Attribute generatedBy = ((EventBElement)object).getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY);
//			if (generatedBy!= null){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public static boolean readOnly(Object object){
//		if (object instanceof EventBElement){
//			return ((EventBElement)object).isLocalGenerated();
//		}
//		return false;
//	}
	
}
