/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package playground.kai.usecases.invertednetwork;

import org.matsim.core.controler.Controler;

/**
 * @author nagel
 *
 */
public class Main {

	private Main() {
	}

	public static void main(String[] args) {
		Controler ctrl = new Controler(args) ;
		
		ctrl.setTripRouterFactory(new InvertedNetworkForCarsRouterFactoryImpl(ctrl.getScenario(), ctrl.getTravelDisutilityFactory()));
		
		
		ctrl.run();
		
	}

}