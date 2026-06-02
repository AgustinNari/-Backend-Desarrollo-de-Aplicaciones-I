package com.example.quickbid.quickbid.service;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.quickbid.quickbid.exception.BusinessException;

@Service
public class DocumentValidationService {
	private final long maxSizeBytes;
	private final ImageValidationService images;

	public DocumentValidationService(@Value("${app.files.max-document-size-bytes:10485760}") long maxSizeBytes,
			ImageValidationService images) {
		this.maxSizeBytes = maxSizeBytes;
		this.images = images;
	}

	public byte[] validate(MultipartFile file) {
		if (file == null || file.isEmpty() || file.getSize() > maxSizeBytes) throw invalid();
		String type = file.getContentType();
		if (Set.of("image/jpeg", "image/png", "image/webp").contains(type)) return images.validate(file);
		try {
			byte[] bytes = file.getBytes();
			if (!"application/pdf".equals(type) || bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P'
					|| bytes[2] != 'D' || bytes[3] != 'F' || bytes[4] != '-') throw invalid();
			return bytes;
		} catch (IOException exception) {
			throw invalid();
		}
	}

	private BusinessException invalid() {
		return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "Formato de documento no soportado",
				"FILE_TYPE_NOT_SUPPORTED");
	}
}
