/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package org.matsim.contrib.spatialDrt.schedule;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.spatialDrt.parkingStrategy.DefaultDrtOptimizer;
import org.matsim.contrib.spatialDrt.passenger.PassengerRequestCreator;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

/**
 * @author michalm
 */
public class DrtRequestCreator implements PassengerRequestCreator {
	private final DrtConfigGroup drtCfg;
	private final TravelTime travelTime;
	private final LeastCostPathCalculator router;
	private final EventsManager eventsManager;
	private final MobsimTimer timer;

	@Inject
	public DrtRequestCreator(DrtConfigGroup drtCfg, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network,
							 @Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime, QSim qSim,
							 @Named(DefaultDrtOptimizer.DRT_OPTIMIZER) TravelDisutility travelDisutility) {
		this.drtCfg = drtCfg;
		this.travelTime = travelTime;
		this.eventsManager = qSim.getEventsManager();
		this.timer = qSim.getSimTimer();

		router = new FastAStarEuclideanFactory().createPathCalculator(network, travelDisutility, travelTime);
		//routerT = new FastAStarEuclideanFactory().createPathCalculator(networkT, travelDisutility, travelTime);
	}

	@Override
	public AtodRequest createRequest(Id<Request> id, MobsimPassengerAgent passenger, Link fromLink, Link toLink,
									 double departureTime, double submissionTime, String mode) {
		double latestDepartureTime = departureTime + drtCfg.getMaxWaitTime();
		VrpPathWithTravelData unsharedRidePath;
		unsharedRidePath = VrpPaths.calcAndCreatePath(fromLink, toLink, departureTime, router,
					travelTime);

		double optimisticTravelTime = unsharedRidePath.getTravelTime();
		double maxTravelTime = drtCfg.getMaxTravelTimeAlpha() * optimisticTravelTime + drtCfg.getMaxTravelTimeBeta();
		double latestArrivalTime = departureTime + maxTravelTime;

		double unsharedDistance = VrpPaths.calcDistance(unsharedRidePath);

		eventsManager.processEvent(new DrtRequestSubmittedEvent(timer.getTimeOfDay(), id, passenger.getId(),
				fromLink.getId(), toLink.getId(), unsharedRidePath.getTravelTime(), unsharedDistance));

		return new AtodRequest(id, passenger, fromLink, toLink, departureTime, latestDepartureTime, latestArrivalTime,
				submissionTime, mode);
	}
}
