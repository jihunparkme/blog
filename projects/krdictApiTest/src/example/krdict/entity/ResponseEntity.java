package example.krdict.entity;

import lombok.Data;

import java.util.List;

@Data
public class ResponseEntity {

    private String word;
    private String pos;
    private List<Sense> senseList;

}
