package bookingmicroservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import bookingmicroservice.domains.User;


public interface UserDAO extends CrudRepository<User, Long>{
	
	@Query("Select u from User u where u.userid=:userid")
	public User findByUserid(@Param("userid") int id); 
	
	public User findByEmail(String email); 
	public User findByEmailAndPassword(String email, String password); 
	
	public List<User> findAll();
}