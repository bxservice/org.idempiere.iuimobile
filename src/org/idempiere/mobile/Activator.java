/**
 * 
 */
package org.idempiere.mobile;

import org.adempiere.plugin.utils.Incremental2PackActivator;
import org.osgi.framework.BundleContext;

/**
 * @author hengsin
 *
 */
public class Activator extends Incremental2PackActivator {
	
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
