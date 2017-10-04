/**
 * 
 */
package ac.soton.emf.translator.eventb.handler;

import org.eclipse.emf.ecore.EObject;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.IInternalElement;

import ac.soton.emf.translator.handler.TranslateHandler;


/**
 * <p>
 * 
 * </p>
 * 
 * @author cfs
 * @version
 * @see
 * @since
 */
public class EventBTranslateHandler extends TranslateHandler {	
	
	protected EObject getEObject (Object obj){
		if (obj instanceof IInternalElement){
			return (new EMFRodinDB()).loadEventBComponent((IInternalElement)obj) ;
		}else{
			return super.getEObject(obj);
		}
	}
	
}
