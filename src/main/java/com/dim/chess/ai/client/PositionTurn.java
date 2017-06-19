/**
 * 
 */
package com.dim.chess.ai.client;

import java.io.Serializable;

/**
 * @author OSVALDIMAR
 *
 */
public class PositionTurn implements Serializable {

	private static final long serialVersionUID = 2928852462251205151L;

	private String origin;
	private String destiny;

	public PositionTurn() {
	}

	public PositionTurn(final String origin, final String destiny) {
		super();
		this.origin = origin;
		this.destiny = destiny;
	}

	public String getOrigin() {
		return this.origin;
	}

	public void setOrigin(final String origin) {
		this.origin = origin;
	}

	public String getDestiny() {
		return this.destiny;
	}

	public void setDestiny(final String destiny) {
		this.destiny = destiny;
	}

	@Override
	public String toString() {
		return "PositionTurn:[origin: " + this.origin + ", destiny: " + this.destiny + "]";
	}
}
