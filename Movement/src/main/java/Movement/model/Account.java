package Movement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("account")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {
    @Id
    private String idAccount;
    private String accountNumber;
    private String accountType;
    private String debitCardNumber;
    private Integer priority;
    private Double minimammount;
    private Fee fee;
    private Client client;
    private List<String> headline;
    private List<String> authorizedSignatures;
    private String associatedCard;
}
