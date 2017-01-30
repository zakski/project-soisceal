package it.unibo.alice.tuprolog.ws.security;

/**
 * Enum that represents a Role and therefore an access level, in the server application.
 * 
 * @author Andrea Muccioli
 *
 */
public enum Role {
	GUEST, CONFIGURATOR, ADMIN;
	
	/**
	 * Gets the role, if any, right below the one that invoked the method.
	 * 
	 * @return the role right below the current one, or null if this is already
	 * the lowest role.
	 */
	public Role getRoleBelow() {
		int newOrdinal = this.ordinal()-1;
		if (newOrdinal < 0)
			return null;
		return Role.values()[newOrdinal];
	}
	
	/**
	 * Gets the role, if any, right above the one that invoked the method.
	 * 
	 * @return the role right above the current one, or null if this is already
	 * the highest role.
	 */
	public Role getRoleAbove() {
		int newOrdinal = this.ordinal()+1;
		if (newOrdinal >= Role.values().length)
			return null;
		return Role.values()[newOrdinal];
	}
}
