package com.sander.wrdcounter.publisher;

import com.google.gson.Gson;
import com.sander.wrdcounter.config.MQConfig;
import com.sander.wrdcounter.dto.FileData;
import com.sander.wrdcounter.dto.WordData;
import com.sander.wrdcounter.repository.WordRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@CrossOrigin()
@RestController
@RequestMapping("/upload")
public class Publisher {
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private RabbitTemplate template;
    @CrossOrigin()
    @PostMapping(value = "/{id}")
    public String text(@RequestBody MultipartFile file, @PathVariable String id) {
        String fileString = "";
        System.out.println("UUID for user: " + id);

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes());
            fileString = IOUtils.toString(stream, "UTF-8");
            System.out.println(fileString);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileData fileData = new FileData(id, fileString);
        Gson gson = new Gson();
        String wordDataJson = gson.toJson(fileData);
        System.out.println("WordDataJson: " + wordDataJson);
        template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, wordDataJson);
        return "Success";
    }
}
