package com.rommamte.sleeping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SleepingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SleepingApplication.class, args);
	}
}

@RestController
class Api {

	public static final List<String> members = new ArrayList(Arrays.asList("Nirvik", "Kabir", "Partha", "Shabby"));

	public String getRandomMember() {
		int index = new Random().nextInt(members.size());
		return members.get(index);
	}

	public int getRandomNumOfMembers() {
		return new Random().nextInt(2) + 1;
	}

	@GetMapping("/lottery")
	@CrossOrigin
	public ResponseEntity<List<String>> doLottery() {

		int numOfMemeber = getRandomNumOfMembers();
		List<String> winners = new ArrayList<>();
		while (numOfMemeber != 0) {
			String member = getRandomMember();
			if (!winners.contains(member)) {
				winners.add(member);
				numOfMemeber--;
			}
		}
		return new ResponseEntity<List<String>>(winners, HttpStatus.OK);
	}

	@PostMapping("/completeLottery")
	@CrossOrigin
	public ResponseEntity<Void> completeLottery(@RequestBody List<String> winners) throws IOException {
		File records = new File("records.csv");

		if (!records.exists())
			records.createNewFile();

		StringBuilder csvRecord = new StringBuilder();
		for (String member : winners) {
			csvRecord.append(member);
			csvRecord.append(",");
		}
		csvRecord.append(LocalDate.now().toString());
		csvRecord.append(System.lineSeparator());

		Files.write(Paths.get(records.getAbsolutePath()), csvRecord.toString().getBytes(), StandardOpenOption.APPEND);

		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	@GetMapping("/history")
	@CrossOrigin
	public ResponseEntity<List<List<String>>> getHistory() throws IOException {
		List<List<String>> history = new ArrayList<>();
		File records = new File("records.csv");

		if (!records.exists())
			records.createNewFile();

		try (Stream<String> lines = Files.lines(Paths.get(records.getAbsolutePath()))) {
			List<List<String>> values = lines.map(line -> Arrays.asList(line.split(","))).collect(Collectors.toList());

			values.forEach(value -> {
				System.out.println(value);
				history.add(value);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>(history, HttpStatus.OK);

	}

}