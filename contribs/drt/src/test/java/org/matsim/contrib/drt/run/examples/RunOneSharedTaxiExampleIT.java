/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.drt.run.examples;

import java.net.URL;

import org.junit.Test;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.examples.ExamplesUtils;

/**
 * @author michalm
 */
public class RunOneSharedTaxiExampleIT {
	@Test
	public void testRun() {
		URL configUrl = IOUtils.newUrl(ExamplesUtils.getTestScenarioURL("dvrp-grid"), "one_shared_taxi_config.xml");
		RunOneSharedTaxiExample.run(configUrl, false, 1);
	}
}
