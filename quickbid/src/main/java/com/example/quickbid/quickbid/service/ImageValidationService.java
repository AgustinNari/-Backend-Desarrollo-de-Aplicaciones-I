package com.example.quickbid.quickbid.service;
import java.util.Set; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.web.multipart.MultipartFile; import com.example.quickbid.quickbid.exception.BusinessException;
@Service public class ImageValidationService {
 private final long maxImageSizeBytes; public ImageValidationService(@Value("${app.files.max-image-size-bytes:10485760}")long maxImageSizeBytes){this.maxImageSizeBytes=maxImageSizeBytes;}
 public byte[] validate(MultipartFile file){try{if(file==null||file.isEmpty()||file.getSize()>maxImageSizeBytes)throw invalid();String type=file.getContentType();byte[] b=file.getBytes();if(!Set.of("image/jpeg","image/png","image/webp").contains(type)||!matches(type,b))throw invalid();return b;}catch(java.io.IOException e){throw invalid();}}
 private boolean matches(String type,byte[] b){if(type.equals("image/jpeg"))return b.length>=3&&(b[0]&255)==255&&(b[1]&255)==216&&(b[2]&255)==255;if(type.equals("image/png"))return b.length>=8&&(b[0]&255)==137&&b[1]==80&&b[2]==78&&b[3]==71&&b[4]==13&&b[5]==10&&b[6]==26&&b[7]==10;return b.length>=12&&b[0]=='R'&&b[1]=='I'&&b[2]=='F'&&b[3]=='F'&&b[8]=='W'&&b[9]=='E'&&b[10]=='B'&&b[11]=='P';}
 private BusinessException invalid(){return new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY,"Formato de imagen no soportado","FILE_TYPE_NOT_SUPPORTED");}
}
