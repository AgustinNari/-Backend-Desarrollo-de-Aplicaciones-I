package com.example.quickbid.quickbid.entity.enums;

public enum Categoria {
	COMUN("comun"),
	ESPECIAL("especial"),
	PLATA("plata"),
	ORO("oro"),
	PLATINO("platino");

	private final String databaseValue;

	Categoria(String databaseValue) {
		this.databaseValue = databaseValue;
	}

	public String getDatabaseValue() {
		return databaseValue;
	}
}
