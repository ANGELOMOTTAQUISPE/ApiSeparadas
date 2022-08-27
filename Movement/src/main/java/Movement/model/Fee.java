package Movement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("fee")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fee {
    private Double maintenanceCommission;
    private Integer monthlyMovement;
    private LocalDateTime date;
}
