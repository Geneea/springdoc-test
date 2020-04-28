package com.geneea.springdoc;

import com.google.common.collect.ImmutableMultimap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/get")
    public ImmutableMultimap<String, String> getSomeMap() {
        return ImmutableMultimap.of(
                "K1", "V1_1",
                "K1", "V1_2",
                "K2", "V2_1",
                "K2", "V2_2");
    }

}
