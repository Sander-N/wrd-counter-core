package com.sander.wrdcounter.publisher;

import com.google.gson.Gson;
import com.sander.wrdcounter.config.MQConfig;
import com.sander.wrdcounter.dto.FileData;
import com.sander.wrdcounter.repository.WordRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

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
    public String text(@RequestBody MultipartFile file, @PathVariable String id) {
        String fileString = "";
        String textToSend = "";
        int payloadSize = 10000000;//10MB
        System.out.println("File size: " + file.getSize());

        //Convert file to string for further processing
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes());
            fileString = IOUtils.toString(stream, "UTF-8");
            System.out.println(fileString);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //If file size sent is under 10MB then send instantly otherwise continue and break into parts
        if(fileString.getBytes(StandardCharsets.UTF_8).length < payloadSize) {
            FileData fileData = new FileData(id, fileString);
            Gson gson = new Gson();
            String wordDataJson = gson.toJson(fileData);
            System.out.println("Sending all file contents at once!");
            template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, wordDataJson);
            return "Success";
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
                Gson gson = new Gson();
                String wordDataJson = gson.toJson(fileData);

                template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, wordDataJson);

                previousMsgEnd = i;
                i = i + payloadSize;//Move forward 1 000 000 chars

                //If the string length left is less than 1 000 000 then send the last message
                if (fileString.length() - i < payloadSize) {
                    textToSend = fileString.substring(previousMsgEnd, fileString.length());

                    fileData = new FileData(id, textToSend);
                    wordDataJson = gson.toJson(fileData);

                    template.convertAndSend(MQConfig.EXCHANGE, MQConfig.ROUTING_KEY, wordDataJson);
                    System.out.println("Messages sent!");
                    return "Success";
                }
            }
        }
        System.out.println("Messages sent!");
        return "Success";
    }
}
