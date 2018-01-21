package com.rommamte.sleeping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${device}")
	private String device;

	@Value("${deviceRandom}")
	private String deviceRandom;

	@Value("${mutipleLotteryAllowed}")
	private String mutipleLotteryAllowed;

	@Autowired
	private HttpServletRequest request;

	public String getRandomMember() {
		int index = new Random().nextInt(members.size());
		return members.get(index);
	}

	public int getRandomNumOfMembers() {
		return new Random().nextInt(2) + 1;
	}

	@GetMapping("/lottery")
	@CrossOrigin
	public ResponseEntity<List<String>> doLottery() throws IOException, ParseException {
		List<String> winners = new ArrayList<>();
		String userAgent = request.getHeader("user-agent").toLowerCase();
		String randomDevice = getRandomDevice();

		System.out.println(userAgent);
		System.out.println(randomDevice);

		if (deviceRandom.equals("true") && (!userAgent.contains(randomDevice))) {
			winners.add("Opps!I am sorry, not your day ask somebody else to try");
			return new ResponseEntity<List<String>>(winners, HttpStatus.OK);
		}

		if (mutipleLotteryAllowed.equals("false") && isRecordExistsForToday()) {
			winners.add("Lottery completed for today");
			return new ResponseEntity<List<String>>(winners, HttpStatus.OK);
		}

		int numOfMemeber = getRandomNumOfMembers();
		while (numOfMemeber != 0) {
			String member = getRandomMember();
			if (!winners.contains(member)) {
				winners.add(member);
				numOfMemeber--;
			}
		}
		return new ResponseEntity<List<String>>(winners, HttpStatus.OK);
	}

	private boolean isRecordExistsForToday() throws IOException {
		
		long count =fetchHistory()
			.stream()
			.map(list -> list.get(0))
			.peek(str -> {System.out.println("from list "+str);})
			.filter(str -> {
				return dateMatches(str);
			})
			.peek(str -> {System.out.println("After filter "+str);})
			.count();
		
		return (count > 0 ) ? true : false;
	

	}

	private static boolean dateMatches(String str) {
		return LocalDate.parse(str).compareTo(LocalDate.now()) == 0 ? true : false;
	}

	private String getRandomDevice() {
		List<String> devices = Arrays.asList(device.split(","));
		return devices.get(new Random().nextInt(devices.size()));
	}

	@PostMapping("/completeLottery")
	@CrossOrigin
	public ResponseEntity<Void> completeLottery(@RequestBody List<String> winners) throws IOException {
		File records = new File("records.csv");

		if (!records.exists())
			records.createNewFile();

		StringBuilder csvRecord = new StringBuilder();
		csvRecord.append(LocalDate.now().toString());
		csvRecord.append(",");

		for (String member : winners) {
			csvRecord.append(member);
			csvRecord.append(",");
		}

		csvRecord.append(System.lineSeparator());

		Files.write(Paths.get(records.getAbsolutePath()), csvRecord.toString().getBytes(), StandardOpenOption.APPEND);

		return new ResponseEntity<>(HttpStatus.CREATED);

	}

	@GetMapping("/history")
	@CrossOrigin
	public ResponseEntity<List<List<String>>> getHistory() throws IOException {
		return new ResponseEntity<>(fetchHistory(), HttpStatus.OK);

	}

	private List<List<String>> fetchHistory() throws IOException {

		File records = new File("records.csv");
		if (!records.exists())
			records.createNewFile();

		List<List<String>> history = new ArrayList<>();
		try (Stream<String> lines = Files.lines(Paths.get(records.getAbsolutePath()))) {
			List<List<String>> values = lines.map(line -> Arrays.asList(line.split(","))).collect(Collectors.toList());

			values.forEach(value -> {
				System.out.println(value);
				history.add(value);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
		return history;
	}

}