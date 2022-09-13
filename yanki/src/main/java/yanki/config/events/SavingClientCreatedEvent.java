package yanki.config.events;


import lombok.Data;
import lombok.EqualsAndHashCode;
import yanki.model.Client;

@Data
@EqualsAndHashCode(callSuper = true)
public class SavingClientCreatedEvent extends Event<Client> {

}
