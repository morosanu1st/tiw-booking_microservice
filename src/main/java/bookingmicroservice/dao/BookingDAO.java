package bookingmicroservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import bookingmicroservice.domains.Booking;
import bookingmicroservice.domains.User;


public interface BookingDAO extends CrudRepository<Booking, Long>{
	
	@Query("Select b from Booking b where b.bookingid=:bookingid")
	public Booking findByBookingId(@Param("bookingid") long id); 

	
	public List<Booking> findByHomeHomeid(long homeid);
	public List<Booking> findByHostUserid(long userid); 
	public List<Booking> findByGuestUserid(long userid); 
	
	public List<Booking> findAll();
}
