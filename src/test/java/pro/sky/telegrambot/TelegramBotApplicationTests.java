package pro.sky.telegrambot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import pro.sky.telegrambot.controller.ReportController;
import pro.sky.telegrambot.model.Report;

import static pro.sky.telegrambot.constants.ApplicationConstants.*;
import static pro.sky.telegrambot.constants.ReportControllerConstants.reportForTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TelegramBotApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private ReportController reportController;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
		Assertions.assertThat(reportController).isNotNull();
	}

	@Test
	public void testSaveReport(){
		ResponseEntity<Report> response = restTemplate.postForEntity(LOCALHOST + port + REPORTS_ENDPOINT + NEW_SAVE_ENDPOINT, reportForTest, Report.class);
		Assertions.assertThat(response.getBody()).isEqualTo(reportForTest);
	}

}
