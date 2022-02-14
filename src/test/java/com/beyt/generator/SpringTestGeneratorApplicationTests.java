package com.beyt.generator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@SpringBootTest
class SpringTestGeneratorApplicationTests {

	@Test
	void contextLoads() {
	}




}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;

	public User(String name) {
		this.name = name;
	}
}


@Repository
interface UserRepository extends JpaRepository<User, Long> {

}

@Service
class StartUpService implements ApplicationRunner {

	@Autowired
	private UserRepository userRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		userRepository.save(new User("Ali"));
		userRepository.save(new User("Veli"));
		userRepository.save(new User("John"));
		userRepository.save(new User("Mihriban"));
	}
}
