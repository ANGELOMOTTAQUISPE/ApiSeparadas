package yanki.controller;

import org.springframework.web.bind.annotation.*;
import yanki.model.Client;
import yanki.repository.SavingClientRepository;

import java.util.Map;

@RestController
public class ClientController {
    private SavingClientRepository savingRepository;

    public ClientController(SavingClientRepository savingRepository) {
        this.savingRepository = savingRepository;
    }

    @GetMapping("/savings")
    public Map<String, Client> findAll() {
        return savingRepository.findAll();
    }

    @GetMapping("/savings/{id}")
    public Client findById(@PathVariable String id) {
        return savingRepository.findById(id);
    }

    @PostMapping("/savings")
    public void createStudent(@RequestBody Client student) {
        savingRepository.save(student);
    }

    @DeleteMapping("/savings/{id}")
    public void deleteStudent(@PathVariable String id) {
        savingRepository.delete(id);
    }

}
