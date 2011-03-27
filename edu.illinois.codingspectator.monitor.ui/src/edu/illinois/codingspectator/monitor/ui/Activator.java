package edu.illinois.codingspectator.monitor.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;
import org.eclipse.ltk.core.refactoring.codingspectator.RunningModes;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {

	private static final String CODINGSPECTATOR_UPDATE_SITE= "http://codingspectator.cs.illinois.edu/updates/";

	private static final int UPLOAD_PERIOD_MILLISECONDS= 1000 * 60 * 60 * 24 * 1;

	// The plug-in ID
	public static final String PLUGIN_ID= "edu.illinois.codingspectator.monitor.core.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin= this;
		// This cannot be moved into earlyStartup because the BundleContext is not available yet at that point
		if (shouldCheckForUpdate()) {
			scheduleUpdateOperation(context);
		}
	}

	private void scheduleUpdateOperation(BundleContext context) {
		ServiceReference serviceReference= context.getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
		if (serviceReference == null)
			return;

		IProvisioningAgentProvider agentProvider= (IProvisioningAgentProvider)context.getService(serviceReference);
		try {
			final IProvisioningAgent agent= agentProvider.createAgent(null); // "null" gets the default one for this workspace
			IMetadataRepositoryManager manager= (IMetadataRepositoryManager)agent.getService(IMetadataRepositoryManager.SERVICE_NAME);

			IMetadataRepository metadataRepo= manager.loadRepository(new URI(CODINGSPECTATOR_UPDATE_SITE), new NullProgressMonitor());
			Collection<IInstallableUnit> toUpdate= metadataRepo.query(QueryUtil.createIUAnyQuery(), new NullProgressMonitor()).toUnmodifiableSet();
			//TODO: Use QueryUtil.createLatestIUQuery() in the actual case to grab the latest version; using QueryUtil.createIUAnyQuery() just for testing.

			//Creating an operation and check if we have updates - this operation can take some time
			//TODO: What happens if we lose internet connection? Will this stop the entire startup?
			UpdateOperation updateOperation= new UpdateOperation(new ProvisioningSession(agent), toUpdate);
			IStatus modalResolution= updateOperation.resolveModal(new NullProgressMonitor());
			Logger.logDebug(modalResolution.toString());

			if (modalResolution.isOK()) {
				Job job= updateOperation.getProvisioningJob(new NullProgressMonitor());
				job.addJobChangeListener(new JobChangeAdapter() {

					@Override
					public void done(IJobChangeEvent event) {
						super.done(event);
						agent.stop();
					}
				});

				job.schedule();
			}

		} catch (ProvisionException e) {
			log(createErrorStatus("Provisioning exception in CodingSpectator while checking for updates", e));
		} catch (OperationCanceledException e) {
			log(createErrorStatus("Update operation canceled in CodingSpectator while checking for updates", e));
		} catch (URISyntaxException e) {
			// This should not happen at all since our URL is statically determined/valid
			// log(createErrorStatus("Invalid URL in CodingSpectator while checking for updates", e));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin= null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void earlyStartup() {
		if (shouldUpload()) {
			final Submitter submitter= new Submitter();

			if (Uploader.promptUntilValidCredentialsOrCanceled(submitter)) {
				Uploader.submit(submitter);
			}
		}
	}

	private boolean shouldCheckForUpdate() {
		//TODO: Pick a more reasonable update check
		return true;
	}

	private boolean shouldUpload() {
		return !RunningModes.isInTestMode() && enoughTimeHasElapsedSinceLastUpload();
	}

	private boolean enoughTimeHasElapsedSinceLastUpload() {
		try {
			return new Date().getTime() - PrefsFacade.getInstance().getLastUploadTime() > UPLOAD_PERIOD_MILLISECONDS;
		} catch (ParseException e) {
			createErrorStatus("Cannot parse the date that we stored", e);
			return true;
		}
	}

	public Status createInfoStatus(String message) {
		return new Status(Status.INFO, PLUGIN_ID, message);
	}

	public Status createErrorStatus(String message, Exception e) {
		return new Status(Status.ERROR, PLUGIN_ID, message, e);
	}

	public void log(Status status) {
		getLog().log(status);
	}

	public static String populateMessageWithPluginName(String formattedString) {
		return MessageFormat.format(formattedString, Messages.WorkbenchPreferencePage_PluginName);
	}


}
