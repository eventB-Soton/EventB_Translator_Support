/**
 * 
 */
package ac.soton.emf.translator.eventb.handler;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.PersistencePlugin;
import org.eventb.emf.persistence.SaveResourcesCommand;
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
	
	@Override
	protected EObject getEObject (Object obj){
		if (obj instanceof IInternalElement){
			return (new EMFRodinDB()).loadEventBComponent((IInternalElement)obj) ;
		}else{
			return super.getEObject(obj);
		}
	}
	
	/**
	 * Use the SaveResourcesCommand to persist into Rodin
	 * 
	 * @param editingDomain
	 * @param monitor
	 * @throws ExecutionException 
	 */
	@Override
	protected void doSave(final TransactionalEditingDomain editingDomain, IProgressMonitor monitor) throws Exception {
		// delete empty modified resources 	
		for (Resource resource : editingDomain.getResourceSet().getResources()) {
			try {
				if (resource.getContents().isEmpty() && resource.isModified()){
					resource.eSetDeliver(false); 
					resource.delete(Collections.emptyMap());
				}
				monitor.worked(2);
			} catch (IOException e) {
				e.printStackTrace();
				Status status = new Status(Status.ERROR, PersistencePlugin.PLUGIN_ID, "IO Exception while saving resource : " + resource.getURI() + " :- \n" + e.getMessage(), e);
				PersistencePlugin.getDefault().getLog().log(status);
			}
		}
		// save all resources that have been modified					
		SaveResourcesCommand saveCommand = new SaveResourcesCommand(editingDomain);
		if (saveCommand.canExecute()){
				saveCommand.execute(monitor, null);
		}
		monitor.done();
	}
	
}
