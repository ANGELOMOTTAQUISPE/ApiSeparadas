package Credit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("credit")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
    @Id
    private String idCredit;
    private String creditCardNumber;
    private Double creditLine;
    private Client client;
}
