package plm;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		new SpringApplication(Application.class).run(args);
	}

	@Autowired
	@Bean("dataSource")
	public DataSource getDataSource() {
		//
		// Implementation and returned value are not relevant for this exercise
		//
		return null;
	}

	@Autowired
	@Bean("sessionFactory")
	public SessionFactory getSessionFactory(DataSource dataSource) {
		//
		// Implementation and returned value are not relevant for this exercise
		//
		return null;
	}
}
