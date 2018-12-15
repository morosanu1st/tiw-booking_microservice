package bookingmicroservice.domains;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.Date;


/**
 * The persistent class for the BOOKING database table.
 * 
 */

@Entity
@Table(name="BOOKING")
@NamedQuery(name="Booking.findAll", query="SELECT b FROM Booking b")
public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long bookingid;

	private String card_number;

	private Date date_booking;

	private Date date_end;

	private Date date_start;

	//bi-directional many-to-one association to Home
	@ManyToOne
	@JoinColumn(name="home_homeid", referencedColumnName="homeid")
	private Home home;

	@ManyToOne
	@JoinColumn(name="user_hostid", referencedColumnName="userid")
	private User host;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="user_guestid", referencedColumnName="userid")
	private User guest;
	
	//0-pending
	//1-canceled by the host
	//2-canceled by the bank
	//3-confirmed
	private int confirmed;

	public Booking() {
	}

	public long getBookingid() {
		return this.bookingid;
	}

	public void setBookingid(long bookingid) {
		this.bookingid = bookingid;
	}
	
	public String getCard_number() {
		return card_number;
	}

	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}

	public Date getDate_booking() {
		return date_booking;
	}

	public void setDate_booking(Date date_booking) {
		this.date_booking = date_booking;
	}

	public Date getDate_end() {
		return date_end;
	}

	public void setDate_end(Date date_end) {
		this.date_end = date_end;
	}

	public Date getDate_start() {
		return date_start;
	}

	public void setDate_start(Date date_start) {
		this.date_start = date_start;
	}

	
	public Home getHome() {
		return home;
	}

	public void setHome(Home home) {
		this.home = home;
	}

	
	public User getHost() {
		return host;
	}

	public void setHost(User host) {
		this.host = host;
	}

	public User getGuest() {
		return guest;
	}

	public void setGuest(User guest) {
		this.guest = guest;
	}

	public int getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(int confirmed) {
		this.confirmed = confirmed;
	}
	
	public void clearBooking() {
		Booking x= this;
		User guest=x.getGuest();
		Home home=x.getHome();
		User host=x.getHost();
		
		guest.setHomes(null);
		guest.setBookings(null);
		guest.setMessages1(null);
		guest.setMessages2(null);
		
		home.setBookings1(null);
		home.setUser(null);			
		
		host.setHomes(null);
		host.setBookings(null);
		host.setMessages1(null);
		host.setMessages2(null);
		
		x.setGuest(guest);
		x.setHome(home);
		x.setHost(host);
		
	}
}