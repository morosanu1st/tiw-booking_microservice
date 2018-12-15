package bookingmicroservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import bookingmicroservice.domains.Booking;
import bookingmicroservice.domains.User;


public interface BookingDAO extends CrudRepository<Booking, Long>{
	
	@Query("Select b from Booking b where b.bookingid=:bookingid")
	public Booking findByBookingId(@Param("bookingid") int id); 

	
	public List<Booking> findByHomeHomeid(int homeid);
	public List<Booking> findByHostUserid(int userid); 
	public List<Booking> findByGuestUserid(int userid); 
	
	public List<Booking> findAll();
}
