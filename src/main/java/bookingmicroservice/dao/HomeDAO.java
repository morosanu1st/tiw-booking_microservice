package bookingmicroservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import bookingmicroservice.domains.Home;
import bookingmicroservice.domains.User;


public interface HomeDAO extends CrudRepository<User, Long>{
	
	@Query("Select u from Home u where u.homeid=:homeid")
	public Home findByHomeid(@Param("homeid") int id); 

	public List<User> findAll();
}
