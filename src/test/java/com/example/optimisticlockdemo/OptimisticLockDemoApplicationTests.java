package com.example.optimisticlockdemo;

import com.example.optimisticlockdemo.entity.UserEntity;
import com.example.optimisticlockdemo.model.User;
import com.example.optimisticlockdemo.repository.UserEntityRepository;
import com.example.optimisticlockdemo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles(profiles = "test")
@SpringBootTest
class OptimisticLockDemoApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private UserEntityRepository userEntityRepository;

	@BeforeEach
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	void init() {
		// Delete existing records and add one
		userEntityRepository.deleteAll();
		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setVersion(2L);
		userEntity.setName("Tony Stark");
		userEntity.setEmail("tonystark@starkindustries.com");
		userEntityRepository.save(userEntity);
	}

	@Test
	@DisplayName("Demonstrate what optimistic lock is")
	void testUpdateWithVersionChangeBetweenReadAndWrite() {
		final User user = userService.findById(1L);

		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		// Update the same entity 1 second later
		final String newEmail = String.format("%s@email.com", randomString());
		executorService.schedule(() -> {
			user.setEmail(newEmail);
			boolean updateResult = userService.updateEmail(user, 0L);
			assertTrue(updateResult);
		}, 1L, TimeUnit.SECONDS);
		executorService.shutdown();

		// Update fails with OptimisticLockingFailureException, even 3 is passed as version
		// Because version is changed by above scheduled task after read and before commit
		assertThrows(OptimisticLockingFailureException.class, () -> {
			String newEmail2 = String.format("%s@email.com", randomString());
			user.setEmail(newEmail2);
			userService.updateEmail(user, 2L);
		});

		User user2 = userService.findById(1L);
		assertNotNull(user2);
		assertEquals(newEmail, user2.getEmail());
		assertEquals(3L, user2.getVersion());

	}

	@Test
	@DisplayName("Demonstrate what optimistic lock is not")
	void testUpdateWithMismatchVersion() {
		final User user = userService.findById(1L);

		String newEmail = String.format("%s@gmail.com", randomString());
		// Update success even the input version does not match with the version in database
		// Because JPA does not care about your input version
		user.setVersion(10L);
		user.setEmail(newEmail);
		boolean updateResult = userService.updateEmail(user, 0L);
		assertTrue(updateResult);

		User user2 = userService.findById(1L);
		assertNotNull(user2);
		assertEquals(newEmail, user2.getEmail());
		assertEquals(3L, user2.getVersion());
	}

	String randomString() {
		Random random = new Random();
		return random.ints((int) 'a', (int) 'z' + 1)
				.limit(10)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
}
