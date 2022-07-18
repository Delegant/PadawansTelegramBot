package pro.sky.telegrambot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import liquibase.pro.packaged.O;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.sky.telegrambot.exceptions.UserNotFoundException;
import pro.sky.telegrambot.service.TrialPeriodService;

@Tag(name = "Trial Period Controller", description = "API для управления испытательным периодом взявших животное из приюта")
@RestController
@RequestMapping("/trialperiod")
public class TrialPeriodController {

    Logger logger = LoggerFactory.getLogger(TrialPeriodController.class);

    private TrialPeriodService trialPeriodService;

    public TrialPeriodController(TrialPeriodService trialPeriodService) {
        this.trialPeriodService = trialPeriodService;
    }

    @Operation(
            summary = "Запуск испытательного периода",
            description = "Волонтер включает отсчет испытательного периода для лица, взявшего животное из приюта."
    )
    @PostMapping("/start")
    public ResponseEntity<String> startTrialPeriod(@RequestParam @Parameter(description = "Id усыновителя") Long parentId,
                                           @RequestParam @Parameter(description = "Id волонтера") Long volunteerId) throws UserNotFoundException {
        trialPeriodService.startTrialPeriod(parentId, volunteerId);
        return ResponseEntity.ok("Trial period has been successfully started!");
    }

    @Operation(
            summary = "Завершение испытательного периода",
            description = "Волонтер завершает испытательный период для усыновителя."
    )
    @PostMapping("/close")
    public ResponseEntity<String> closeTrialPeriod(@RequestParam @Parameter(description = "Id усыновителя") Long userId,
                                                   @RequestParam @Parameter(description = "Id волонтера") Long volunteerId) throws UserNotFoundException {
        trialPeriodService.closeTrialPeriod(userId, volunteerId);
        return ResponseEntity.ok("Trial period has been approved!");
    }

    @Operation(
            summary = "Продление испытательного периода",
            description = "Волонтер продлевает испытательный период на указанное количество дней."
    )
    @PostMapping("/prolong")
    public ResponseEntity<String> prolongTrialPeriod(@RequestParam @Parameter(description = "Id усыновителя")Long userId,
                                             @RequestParam @Parameter(description = "Количество дней")int addedDays,
                                             @RequestParam @Parameter(description = "Id волонтера") Long volunteerId) throws UserNotFoundException {
       trialPeriodService.prolongTrialPeriod(userId, addedDays, volunteerId);
        return ResponseEntity.ok("Trial period has been prolonged for " + addedDays + " days.");
    }

    @Operation(
            summary = "Закрытие/отклонение испытательного периода.",
            description = "Волонтер закрывает испытательный период для усыновителя в случае его неудачного прохождения!"
    )
    @PostMapping("/decline")
    public ResponseEntity<String> declineTrialPeriod(@RequestParam @Parameter(description = "Id усыновителя") Long userId,
                                                     @RequestParam @Parameter(description = "Id волонтера") Long volunteerId) throws UserNotFoundException {

        return ResponseEntity.ok("Trial period has been closed!");
    }


}
