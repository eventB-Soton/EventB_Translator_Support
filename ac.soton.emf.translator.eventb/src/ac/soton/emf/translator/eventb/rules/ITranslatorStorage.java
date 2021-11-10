/*******************************************************************************
 * Copyright (c) 2014, 2017 University of Southampton.
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

package ac.soton.emf.translator.eventb.rules;


/**
 * <p>
 *
 * </p>
 *
 * @author htson
 * @version
 * @see
 * @since
 */
public interface ITranslatorStorage {

	/**
	 * @param string
	 */
	public abstract Object fetch(String key);

	/**
	 * @param string
	 * @param entityDeclaration
	 */
	public abstract void stash(String key, Object value);

	public abstract void reset();
}