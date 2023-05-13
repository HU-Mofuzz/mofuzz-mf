package de.hub.mse.server.controller;

import de.hub.mse.server.exceptions.NotFoundException;
import de.hub.mse.server.management.ClientDescriptor;
import de.hub.mse.server.service.ClientDescriptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientDescriptorController {

    private final ClientDescriptorService descriptorService;

    @Autowired
    public ClientDescriptorController(ClientDescriptorService descriptorService) {
        this.descriptorService = descriptorService;
    }

    @PostMapping
    public void createClientDescriptor(@RequestBody ClientDescriptor descriptor) {
        descriptorService.createClientDescriptor(descriptor);
    }

    @GetMapping
    public List<ClientDescriptor> getClientDescriptors() {
        return descriptorService.getDescriptors();
    }

    @PostMapping("/{id}")
    public void changeClientDescriptor(@PathVariable String id,
                                         @RequestBody ClientDescriptor descriptor) throws NotFoundException {
        descriptorService.changeClientDescriptor(id, descriptor);
    }
}
