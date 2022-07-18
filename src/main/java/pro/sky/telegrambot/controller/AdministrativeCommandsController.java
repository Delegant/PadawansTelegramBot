package pro.sky.telegrambot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.service.AdministrativeService;
import pro.sky.telegrambot.service.UserService;

@RestController
@RequestMapping(value = "/administrative")
public class AdministrativeCommandsController {

    private final Logger logger = LoggerFactory.getLogger(AdministrativeCommandsController.class);

    private final AdministrativeService administrativeService;

    private final UserService userService;

    public AdministrativeCommandsController(AdministrativeService administrativeService,
                                            UserService userService) {
        this.administrativeService = administrativeService;
        this.userService = userService;
    }

    @GetMapping(value = "/newvolunteer")
    public ResponseEntity<String> setNewVolunteer(@RequestParam Long adminId,
                                                  @RequestParam Long userId) {
        administrativeService.setNewVolunteer(adminId, userId);
        return ResponseEntity.ok("New volunteer assigned successfully!");
    }

    @GetMapping(value = "/newadmin")
    public ResponseEntity<String> setNewAdmin(@RequestParam Long adminId,
                                              @RequestParam Long userId) {
        administrativeService.setNewVolunteer(adminId, userId);
        return ResponseEntity.ok("New ADMIN assigned successfully!");
    }
}
