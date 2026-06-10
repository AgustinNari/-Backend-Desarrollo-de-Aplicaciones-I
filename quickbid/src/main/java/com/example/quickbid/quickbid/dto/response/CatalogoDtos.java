package com.example.quickbid.quickbid.dto.response;

import java.util.List;

public final class CatalogoDtos {

	private CatalogoDtos() {
	}

	public record Page<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
	}

	public record Pais(Integer id, String nombre, String nombreCorto, String nacionalidad) {
	}
}
