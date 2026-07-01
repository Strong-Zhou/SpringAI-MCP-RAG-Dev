package com.zhou.service.impl;

import com.zhou.service.DocumentService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentServiceImpl implements DocumentService {
    @Override
    public List<Document> uploadDocument(Resource resource, String fileName) {
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("fileName",fileName);
        List<Document> documents = textReader.get();
        System.out.println("documentList:" + documents);
        return documents;
    }


}
