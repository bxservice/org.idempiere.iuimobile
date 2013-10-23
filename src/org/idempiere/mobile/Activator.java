/**
 * 
 */
package org.idempiere.mobile;

import org.adempiere.plugin.utils.AdempiereActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author hengsin
 *
 */
public class Activator extends AdempiereActivator {
	
	static BundleContext bundleContext;
	
	/**
	 * default constructor
	 */
	public Activator() {
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		super.start(context);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		bundleContext = null;
		super.stop(context);
	}

}
