package Account.model;

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
public class Account {
    @Id
    private String idAccount;
    private String accountNumber;
    private String accountType;
    private Fee fee;
    private Client client;
    private List<String> headline;
    private List<String> authorizedSignatures;
}
