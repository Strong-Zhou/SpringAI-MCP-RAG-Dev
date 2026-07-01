package com.zhou.service.impl;

import com.zhou.service.DocumentService;
import com.zhou.utils.CustomTextSplitter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final RedisVectorStore vectorStore;

    @Override
    public List<Document> uploadDocument(Resource resource, String fileName) {
        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("fileName",fileName);
        List<Document> documents = textReader.get();
        System.out.println("documentList:" + documents);

        CustomTextSplitter textSplitter = new CustomTextSplitter();
        List<Document> apply = textSplitter.apply(documents);

        vectorStore.add(apply);


        return documents;
    }


}
