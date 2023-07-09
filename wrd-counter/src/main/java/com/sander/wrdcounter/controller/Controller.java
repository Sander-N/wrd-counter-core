package com.sander.wrdcounter.controller;

import com.sander.wrdcounter.dto.WordData;
import com.sander.wrdcounter.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class Controller {
    @Autowired
    private WordRepository wordRepository;

    @CrossOrigin()
    @GetMapping(value = "/getWords/{id}")
    public ResponseEntity<String> getWords(@PathVariable("id") String id) {
        Optional<WordData> wordData = wordRepository.findById(id);
        if (wordData.isPresent()) {
            return new ResponseEntity<>(wordData.get().getWords(), HttpStatus.OK);
        } else {
            throw new RuntimeException();
        }
    }
}
