package com.zhou.controller;


import com.zhou.service.DocumentService;
import com.zhou.utils.LeeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/rag")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @RequestMapping("/uploadRagDoc")
    public LeeResult uploadRagDoc(@RequestParam MultipartFile multipartFile) {
        documentService.uploadDocument(multipartFile.getResource(), multipartFile.getOriginalFilename());
        return LeeResult.ok();
    }

}
