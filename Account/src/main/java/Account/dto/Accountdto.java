package Account.dto;

import Account.model.Client;
import Account.model.Fee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accountdto {
    private String accountNumber;
    private String accountType;
    private String idClient;
    private String documentNumber;
    private List<String> headline;
    private List<String> authorizedSignatures;
}
