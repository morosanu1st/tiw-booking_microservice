package bookingmicroservice.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;

import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import bookingmicroservice.dao.BookingDAO;
import bookingmicroservice.dao.HomeDAO;
import bookingmicroservice.dao.UserDAO;
import bookingmicroservice.domains.Booking;
import bookingmicroservice.domains.Home;
import bookingmicroservice.domains.User;

@RestController
@CrossOrigin
public class BookingController {

	@Autowired
	BookingDAO bookingdao;

	@Autowired
	UserDAO userdao;

	@Autowired
	HomeDAO homedao;

	@RequestMapping("/bookings")
	public List<Booking> getBooking() {
		List<Booking> bookings = bookingdao.findAll();
		bookings.forEach(x -> x.clearBooking());
		return bookings;
	}

	@RequestMapping("/bookings/{id}")
	public ResponseEntity<Booking> getBookingById(@PathVariable int id) {
		Booking booking = bookingdao.findByBookingId(id);

		ResponseEntity<Booking> response;
		if (booking == null) {
			response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			booking.clearBooking();
			response = new ResponseEntity<>(booking, HttpStatus.OK);
		}
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/bookings")
	public ResponseEntity<String> createNewBooking(@RequestBody String jsonString) {
		Booking booking;
		Gson gson = new Gson();
		JsonObject j = null;
		try {
			j = gson.fromJson(jsonString, JsonObject.class);
		} catch (JsonSyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String ds = j.get("date_start").getAsString();
		String de = j.get("date_end").getAsString();
		String db = j.get("date_booking").getAsString();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		try {
			booking = gson.fromJson(jsonString, Booking.class);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Date start, end, created;
		try {
			start = formatter.parse(ds);
			end = formatter.parse(de);
			created = formatter.parse(db);
			booking.setDate_booking(created);
			booking.setDate_start(start);
			booking.setDate_end(end);
		} catch (ParseException e1) {
			return new ResponseEntity<>("Date not valid", HttpStatus.UNAUTHORIZED);
		}
		User guest = userdao.findByUserid(booking.getGuest().getUserid());
		Home home = homedao.findByHomeid(booking.getHome().getHomeid());
		User host = userdao.findByUserid(home.getUser().getUserid());

		booking.setGuest(guest);
		booking.setHost(host);
		booking.setHome(home);
		booking.setConfirmed(0);
		booking.setBookingid(0);

		bookingdao.save(booking);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping("/bookings/guest/{id}")
	public ResponseEntity<List<Booking>> getBookingsForGuestUser(@PathVariable int id) {
		List<Booking> bookings = bookingdao.findByGuestUserid(id);
		bookings.forEach(x -> x.clearBooking());
		return new ResponseEntity<>(bookings, HttpStatus.OK);
	}

	@RequestMapping("/bookings/host/{id}")
	public ResponseEntity<List<Booking>> getBookingsForHostUser(@PathVariable int id) {
		List<Booking> bookings = bookingdao.findByHostUserid(id);
		bookings.forEach(x -> x.clearBooking());
		return new ResponseEntity<>(bookings, HttpStatus.OK);
	}

	@RequestMapping("/bookings/home/{id}")
	public ResponseEntity<List<Booking>> getBookingsForHomeId(@PathVariable int id) {
		List<Booking> bookings = bookingdao.findByHomeHomeid(id);
		bookings.forEach(x -> x.clearBooking());
		return new ResponseEntity<>(bookings, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/bookings/{id}/confirmation")
	public ResponseEntity<String> setBookingConfirmed(@PathVariable int id) {
		Booking b = bookingdao.findByBookingId(id);
		JsonObject body = new JsonObject();
		String[] card = b.getCard_number().split("/");
		body.addProperty("card_number", card[0]);
		body.addProperty("cv2", card[1]);
		body.addProperty("expiration", card[2]);
		int days = daysBetween(b.getDate_start(), b.getDate_end());
		int total = days * b.getHome().getPrice();
		body.addProperty("ammount", total);
		body.addProperty("guestid", b.getGuest().getUserid());
		body.addProperty("hostid", b.getHost().getUserid());
		
		HttpResponse response=executePost("http://localhost:8105/transaction", body);
		HttpEntity r=response.getEntity();
		String t_id="failed to confirm the transaction";
	    try {
			 t_id = EntityUtils.toString(r);
			String statusline=response.getStatusLine().toString();
			if(statusline.contains("200")) {
				b.setConfirmed(3);
				b.setCard_number(t_id);
				bookingdao.save(b);
			}
			else {
				b.setConfirmed(2);
				bookingdao.save(b);
			}
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return new ResponseEntity<>(t_id,HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/bookings/{id}/cancelation")
	public ResponseEntity<String> setBookingCanceled(@PathVariable int id) {
		Booking b = bookingdao.findByBookingId(id);
		b.setConfirmed(1);
		return new ResponseEntity<>("Canceled",HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/bookings")
	public ResponseEntity<String> updateBooking(@RequestBody String jsonString) {
		Booking booking;
		Gson gson = new Gson();
		JsonObject j = null;
		try {
			j = gson.fromJson(jsonString, JsonObject.class);
		} catch (JsonSyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String ds = j.get("date_start").getAsString();
		String de = j.get("date_end").getAsString();
		String db = j.get("date_booking").getAsString();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		try {
			booking = gson.fromJson(jsonString, Booking.class);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Date start, end, created;
		try {
			start = formatter.parse(ds);
			end = formatter.parse(de);
			created = formatter.parse(db);
			booking.setDate_booking(created);
			booking.setDate_start(start);
			booking.setDate_end(end);
		} catch (ParseException e1) {
			return new ResponseEntity<>("Date not valid", HttpStatus.UNAUTHORIZED);
		}
		User guest = userdao.findByUserid(booking.getGuest().getUserid());
		Home home = homedao.findByHomeid(booking.getHome().getHomeid());
		User host = userdao.findByUserid(home.getUser().getUserid());

		booking.setGuest(guest);
		booking.setHost(host);
		booking.setHome(home);

		bookingdao.save(booking);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/bookings/{id}")
	public ResponseEntity<String> deleteBooking(@PathVariable int id) {
		Booking b = bookingdao.findByBookingId(id);
		bookingdao.delete(b);
		return new ResponseEntity<>("deleted", HttpStatus.OK);
	}

	private int daysBetween(Date d1, Date d2) {
		return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}
	
	private static HttpResponse executePost(String targetURL, JsonObject body) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 

		try {

		    HttpPost request = new HttpPost(targetURL);
		    request.addHeader("content-type", "application/json");
		    StringEntity json = new StringEntity(body.toString());
		    request.setEntity(json);
		    HttpResponse response = httpClient.execute(request);
		    
		    
		    return response;

		}catch (Exception ex) {

		    //handle exception here

		} 
	
		return null;
	}
}
