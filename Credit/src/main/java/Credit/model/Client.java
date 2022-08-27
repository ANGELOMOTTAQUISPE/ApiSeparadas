package Credit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("client")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    private String idClient;
    private String clientType;
    private String name;
    private String documentNumber;
    private String documentType;
}
