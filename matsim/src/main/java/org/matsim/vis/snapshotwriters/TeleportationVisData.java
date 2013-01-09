/* *********************************************************************** *
 * project: org.matsim.*
 * TeleportationVisData
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.vis.snapshotwriters;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

/**
 *
 * @author dgrether
 *
 */
public class TeleportationVisData implements AgentSnapshotInfo {

//	private double stepsize;
	private final double startX;
	private final double startY;
//	private double normalX;
//	private double normalY;
	private double currentX;
	private double currentY;
	private final double starttime;
	private final Id agentId;
	private int userDefined;
	private int type;
	private double colorval;
	private AgentState state = AgentSnapshotInfo.AgentState.PERSON_OTHER_MODE;
	private int intX;
	private int intY;
	private final double endX;
	private final double endY;
	private final double travelTime;
	private final static int offset = 100;

	public TeleportationVisData(double now, Id personId, Coord fromCoord, Coord toCoord, double travelTime ) {
		this.starttime = now;
		this.travelTime = travelTime ;
		this.agentId = personId;
		this.startX = fromCoord.getX();
		this.startY = fromCoord.getY();
		this.endX = toCoord.getX();
		this.endY = toCoord.getY();
//		double dX = endX - startX;
//		double dY = endY - startY;
//		double euclideanLength = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));
//		this.stepsize = euclideanLength / travelTime;
//		this.normalX = dX / euclideanLength;
//		this.normalY = dY / euclideanLength;
		this.currentX = startX;
		this.currentY = startY;
		String idstr = personId.toString();
		int hashCode = idstr.hashCode();
		intX = hashCode%offset;
		hashCode -= intX;
		hashCode /= offset;
		intY = hashCode%offset;
	}

	@Override
	public double getEasting() {
		return this.currentX;
	}

	@Override
	public double getNorthing() {
		return this.currentY;
	}

	@Override
	public Id getId(){
		return this.agentId;
	}

	public void calculatePosition(double time) {
//		double step = (time - starttime) * this.stepsize;
//		this.currentX = this.startX + (step * this.normalX) + 0.1*(intX-offset/2);
//		this.currentY = this.startY  + (step * this.normalY) + 0.1*(intY-offset/2);
		
		double frac = (time - starttime) / travelTime ;
		this.currentX = (1.-frac) * this.startX + frac * this.endX + 0.1*(intX-offset/2) ;
		this.currentY = (1.-frac) * this.startY + frac * this.endY + 0.1*(intY-offset/2) ;
		
	}

	@Override
	public AgentState getAgentState() {
		return this.state;
	}

	@Override
	public double getAzimuth() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getColorValueBetweenZeroAndOne() {
		return this.colorval;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public int getUserDefined() {
		return this.userDefined;
	}

	@Override
	public void setAgentState(AgentState state) {
		this.state = state;
	}

	@Override
	public void setColorValueBetweenZeroAndOne(double tmp) {
		this.colorval = tmp;
	}

	@Override
	public void setType(int tmp) {
		this.type = tmp;
	}

	@Override
	public void setUserDefined(int tmp) {
		this.userDefined = tmp;
	}
}
