package com.zhou.service;


import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DocumentService {

    List<Document> uploadDocument(Resource resource, String fileName);
}
