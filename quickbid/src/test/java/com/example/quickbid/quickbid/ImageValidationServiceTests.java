package com.example.quickbid.quickbid;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.example.quickbid.quickbid.exception.BusinessException;
import com.example.quickbid.quickbid.service.ImageValidationService;

class ImageValidationServiceTests {
	@Test
	void limiteBinarioEsConfigurable() {
		byte[] oversizedPng = new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10, 0};
		var imageValidation = new ImageValidationService(8);
		assertThrows(BusinessException.class, () -> imageValidation.validate(
				new MockMultipartFile("foto", "dni.png", "image/png", oversizedPng)));
	}
}
