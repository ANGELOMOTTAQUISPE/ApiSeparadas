package Account.model;

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
/*
* Esta es el modelo de las cuentas ()
* @autor Angelo Motta
* */
public class Account {
    /*
    * Se crea una entidad Account con los parametros:
    * @param idAccount idcuenta
    * @param accountNumber
    * @param accountType
    * @param minimammount
    * @param fee
    * @param client
    * @param headline
    * @param authorizedSignatures
    * */
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
