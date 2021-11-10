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
}
