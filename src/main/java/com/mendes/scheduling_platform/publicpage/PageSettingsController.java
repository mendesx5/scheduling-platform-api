package com.mendes.scheduling_platform.publicpage;

import com.mendes.scheduling_platform.security.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/page-settings")
public class PageSettingsController {
    private final PageSettingsService service;
    private final Path uploadDir;
    public PageSettingsController(PageSettingsService service,@Value("${app.upload-dir:uploads}") String uploadDir){
        this.service=service;this.uploadDir=Paths.get(uploadDir).toAbsolutePath().normalize();
    }
    @GetMapping public TenantPageSettings get(){return service.get(TenantContext.getRequired());}
    @PutMapping public TenantPageSettings save(@RequestBody TenantPageSettings input){return service.save(TenantContext.getRequired(),input);}
    @GetMapping("/gallery") public List<TenantPageGalleryImage> gallery(){return service.gallery(TenantContext.getRequired());}
    @PutMapping("/gallery") public List<TenantPageGalleryImage> gallery(@RequestBody List<TenantPageGalleryImage> items){return service.saveGallery(TenantContext.getRequired(),items);}
    @GetMapping("/highlights") public List<TenantPageHighlight> highlights(){return service.highlights(TenantContext.getRequired());}
    @PutMapping("/highlights") public List<TenantPageHighlight> highlights(@RequestBody List<TenantPageHighlight> items){return service.saveHighlights(TenantContext.getRequired(),items);}
    @PostMapping(value="/images",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty())throw new IllegalArgumentException("Arquivo vazio");
        String type=file.getContentType()==null?"":file.getContentType();
        if(!type.startsWith("image/"))throw new IllegalArgumentException("Envie apenas imagens");
        if(file.getSize()>8*1024*1024)throw new IllegalArgumentException("Imagem excede 8 MB");
        Path dir=uploadDir.resolve("public-pages").resolve(String.valueOf(TenantContext.getRequired()));
        Files.createDirectories(dir);
        String original=Optional.ofNullable(file.getOriginalFilename()).orElse("image").replaceAll("[^a-zA-Z0-9._-]","_");
        String name=UUID.randomUUID()+"-"+original;
        Files.copy(file.getInputStream(),dir.resolve(name),StandardCopyOption.REPLACE_EXISTING);
        return Map.of("url","/uploads/public-pages/"+TenantContext.getRequired()+"/"+name);
    }
}
