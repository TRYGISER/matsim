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

package org.matsim.contrib.drt.routing;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.Facility;

import com.google.inject.name.Named;

/**
 * @author jbischoff
 * @author michalm (Michal Maciejewski)
 */
class DrtRoutingModuleOld implements RoutingModule {
	private static final Logger LOGGER = Logger.getLogger(DrtRoutingModuleOld.class);

	private final DrtConfigGroup drtCfg;
	private final Network network;
	private final TravelTime travelTime;
	private final LeastCostPathCalculator router;
	private final PopulationFactory populationFactory;
	private final RoutingModule walkRouter;
	private final DrtStageActivityType drtStageActivityType;

	public DrtRoutingModuleOld(DrtConfigGroup drtCfg, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network,
			@Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime,
			TravelDisutilityFactory travelDisutilityFactory, PopulationFactory populationFactory,
			@Named(TransportMode.walk) RoutingModule walkRouter) {
		this.drtCfg = drtCfg;
		this.network = network;
		this.travelTime = travelTime;
		this.populationFactory = populationFactory;
		this.walkRouter = walkRouter;
		this.drtStageActivityType = new DrtStageActivityType(drtCfg.getMode());

		// Euclidean with overdoFactor > 1.0 could lead to 'experiencedTT < unsharedRideTT',
		// while the benefit would be a marginal reduction of computation time ==> so stick to 1.0
		router = new FastAStarEuclideanFactory().createPathCalculator(network,
				travelDisutilityFactory.createTravelDisutility(travelTime), travelTime);
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		Link fromLink = getLink(fromFacility);
		Link toLink = getLink(toFacility);
		if (toLink == fromLink) {
			if (drtCfg.isPrintDetailedWarnings()) {
				LOGGER.error("Start and end stop are the same, agent will walk using mode "
						+ drtStageActivityType.drtWalk
						+ ". Agent Id:\t"
						+ person.getId());
			}
			Leg leg = (Leg)walkRouter.calcRoute(fromFacility, toFacility, departureTime, person).get(0);
			leg.setMode(drtStageActivityType.drtWalk);
			return (Collections.singletonList(leg));
		}

		VrpPathWithTravelData unsharedPath = VrpPaths.calcAndCreatePath(fromLink, toLink, departureTime, router,
				travelTime);
		double unsharedRideTime = unsharedPath.getTravelTime();//includes first & last link
		double maxTravelTime = getMaxTravelTime(drtCfg, unsharedRideTime);
		double unsharedDistance = VrpPaths.calcDistance(unsharedPath);//includes last link

		DrtRoute route = populationFactory.getRouteFactories()
				.createRoute(DrtRoute.class, fromLink.getId(), toLink.getId());
		route.setDistance(unsharedDistance);
		route.setTravelTime(maxTravelTime);
		route.setUnsharedRideTime(unsharedRideTime);
		route.setMaxWaitTime(drtCfg.getMaxWaitTime());

		Leg drtLeg = populationFactory.createLeg(drtCfg.getMode());
		drtLeg.setDepartureTime(departureTime);
		drtLeg.setTravelTime(maxTravelTime);
		drtLeg.setRoute(route);
		return Collections.singletonList(drtLeg);
	}

	private Link getLink(Facility facility) {
		Link link = network.getLinks().get(facility.getLinkId());
		return link != null ? link : NetworkUtils.getNearestLink(network, facility.getCoord());
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		return EmptyStageActivityTypes.INSTANCE;
	}

	/**
	 * Calculates the maximum travel time defined as: drtCfg.getMaxTravelTimeAlpha() * unsharedRideTime + drtCfg.getMaxTravelTimeBeta()
	 *
	 * @param drtCfg
	 * @param unsharedRideTime ride time of the direct (shortest-time) route
	 * @return maximum travel time
	 */
	public static double getMaxTravelTime(DrtConfigGroup drtCfg, double unsharedRideTime) {
		return drtCfg.getMaxTravelTimeAlpha() * unsharedRideTime + drtCfg.getMaxTravelTimeBeta();
	}
}
