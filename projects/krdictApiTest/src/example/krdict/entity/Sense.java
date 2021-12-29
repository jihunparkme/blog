package example.krdict.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sense {

    private String definition;
    private String enWord;
    private String enDefinition;

}
