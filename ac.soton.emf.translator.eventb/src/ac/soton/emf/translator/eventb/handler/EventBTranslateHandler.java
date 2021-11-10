/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
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
/**
 * 
 */
package ac.soton.emf.translator.eventb.handler;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.RodinCore;

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
	
	private final String pluginID = "ac.soton.emf.translator.eventb";
	private final String errorMessage = "failed saving resources after translation";
	
	@Override
	protected EObject getEObject (Object obj){
		if (obj instanceof IInternalElement){
			return (new EMFRodinDB()).loadEventBComponent((IInternalElement)obj) ;
		}else{
			return super.getEObject(obj);
		}
	}
	
	/**
	 * Use the SaveResourcesCommand to persist emf resources
	 * This must be done in a RodinCore runnable
	 * 
	 * @param monitor
	 * @throws ExecutionException 
	 */
	@Override
	protected IStatus save(IProgressMonitor monitor) throws Exception {
		// save all resources that have been modified	
		final SaveResourcesCommand saveCommand = new SaveResourcesCommand(getEditingDomain());
		if (saveCommand.canExecute()){
			RodinCore.run(new IWorkspaceRunnable() {
				public void run(final IProgressMonitor monitor) throws CoreException {
					try {
						saveCommand.execute(monitor, null);
					} catch (ExecutionException e) {
						IStatus status = new Status(IStatus.ERROR, pluginID , errorMessage , e);
						throw new CoreException(status);
					}

				}
			}, getSchedulingRule(getEditingDomain().getResourceSet().getResources().toArray(new Resource[0])), monitor);
		}
		monitor.done();
        return Status.OK_STATUS;
	}
	
	private ISchedulingRule getSchedulingRule(Resource[] resources) {
		if (resources.length==0){	
			return null;
		}else if (resources.length==1){
			return WorkspaceSynchronizer.getFile(resources[0]);
		}else {
			IProject project = getProject(resources[0]);
			for (Resource resource : resources) {
				if (project != getProject(resource)){
					return  ResourcesPlugin.getWorkspace().getRoot();
				}
			}
			return project;
		}
	}
	
	private IProject getProject(Resource resource) {
		IFile file = WorkspaceSynchronizer.getFile(resource);
		return file==null? null : file.getProject();
	}

}
