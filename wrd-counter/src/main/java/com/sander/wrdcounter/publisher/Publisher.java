package com.sander.wrdcounter.publisher;

import com.google.gson.Gson;
import com.sander.wrdcounter.config.MQConfig;
import com.sander.wrdcounter.dto.CombinedData;
import com.sander.wrdcounter.dto.FileData;
import com.sander.wrdcounter.dto.MQData;
import com.sander.wrdcounter.dto.ProcessingFlags;
import com.sander.wrdcounter.repository.WordRepository;
import org.apache.commons.io.IOUtils;
import org.apache.xpath.operations.Mult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

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
    public ResponseEntity<String> text(@RequestPart("file") MultipartFile file, @RequestPart("processingFlags") String processingFlagsJson, @PathVariable String id) {
        Gson gson = new Gson();
        String fileString = "";
        String textToSend = "";
        int payloadSize = 10000000;//10MB
        ProcessingFlags processingFlags = gson.fromJson(processingFlagsJson, ProcessingFlags.class);
        System.out.println("Processing flags:" + processingFlags.ignoreStopWords + "  " + processingFlags.ignoreOutliers);
        System.out.println("File size: " + file.getSize());

        //Convert file to string for further processing
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes());
            fileString = IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //If file size sent is under specified payload then send message instantly otherwise continue and break message into parts
        if(fileString.getBytes(StandardCharsets.UTF_8).length < payloadSize) {
            FileData fileData = new FileData(id, fileString);
            MQData mqData = new MQData(fileData, processingFlags, true);
            String mqDataJson = gson.toJson(mqData);
            System.out.println("Sending all file contents at once!");
            template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, mqDataJson);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        //Variable for saving index of the end of previous message
        int previousMsgEnd = 0;
        //Variable for payload size
        for (int i = payloadSize; i < fileString.length(); i++) {
            String temp = String.valueOf(fileString.charAt(i));
            //If the char is a good spot for splitting then create substring and send message
            if(temp.equals(System.lineSeparator()) || temp.equals(" ")) {
                textToSend = fileString.substring(previousMsgEnd, i);

                FileData fileData = new FileData(id, textToSend);
                MQData mqData = new MQData(fileData, processingFlags, false);
                String mqDataJson = gson.toJson(mqData);

                template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, mqDataJson);

                previousMsgEnd = i;
                i = i + payloadSize;//Move forward by specified payload size

                //If the string length left is less than specified payload then send the last message
                if (fileString.length() - i < payloadSize) {
                    textToSend = fileString.substring(previousMsgEnd, fileString.length());

                    fileData = new FileData(id, textToSend);
                    mqData = new MQData(fileData, processingFlags, true);
                    mqDataJson = gson.toJson(mqData);

                    template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, mqDataJson);
                    System.out.println("Messages sent!");
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        }
        System.out.println("Messages sent!");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
