package com.example.quickbid.quickbid.controller.subastas;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.quickbid.quickbid.dto.response.ApiResponse;
import com.example.quickbid.quickbid.service.SubastaService;

@RestController
@RequestMapping("/api/items")
public class ItemController {
	private final SubastaService subastas;

	public ItemController(SubastaService subastas) {
		this.subastas = subastas;
	}

	@GetMapping("/{id}")
	public ApiResponse<?> item(@PathVariable Integer id, Authentication authentication) {
		return ApiResponse.success(subastas.item(id, authentication != null), "Detalle de item");
	}
}
