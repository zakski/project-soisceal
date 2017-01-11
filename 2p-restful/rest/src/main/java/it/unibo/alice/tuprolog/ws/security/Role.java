package it.unibo.alice.tuprolog.ws.security;

public enum Role {
	GUEST, CONFIGURATOR, ADMIN;
	
	public Role getRoleBelow() {
		int newOrdinal = this.ordinal()-1;
		if (newOrdinal < 0)
			return null;
		return Role.values()[newOrdinal];
	}
	
	public Role getRoleAbove() {
		int newOrdinal = this.ordinal()+1;
		if (newOrdinal >= Role.values().length)
			return null;
		return Role.values()[newOrdinal];
	}
}
