package com.codecool.hotel_backend.controller;

import com.codecool.hotel_backend.entity.HotelUser;
import com.codecool.hotel_backend.entity.Reservation;
import com.codecool.hotel_backend.entity.ReservedRoom;
import com.codecool.hotel_backend.repository.CategoryRepository;
import com.codecool.hotel_backend.repository.ReservationRepository;
import com.codecool.hotel_backend.repository.ReservedRoomRepository;
import com.codecool.hotel_backend.repository.UserRepository;
import com.codecool.hotel_backend.security.JwtTokenServices;
import com.codecool.hotel_backend.service.RoomOrganiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final CategoryRepository categoryRepository;
    private final RoomOrganiser roomOrganiser;
    private final ReservedRoomRepository reservedRoomRepository;
    private final ControllerUtil controllerUtil;

    @Autowired
    public ReservationController(ReservationRepository reservationRepository,
                                 CategoryRepository categoryRepository,
                                 RoomOrganiser roomOrganiser,
                                 ReservedRoomRepository reservedRoomRepository,
                                 ControllerUtil controllerUtil) {
        this.reservationRepository = reservationRepository;
        this.categoryRepository = categoryRepository;
        this.roomOrganiser = roomOrganiser;
        this.reservedRoomRepository = reservedRoomRepository;
        this.controllerUtil = controllerUtil;
    }

    // use this for the user's reservations
    @RequestMapping("/get-reservations-of-user")
    public List<Reservation> getAllReservationInfoOfUser(@RequestHeader String Authorization) {
        HotelUser loggedInUser = controllerUtil.getUserFromToken(Authorization);
        return reservationRepository.getReservationsByUser(loggedInUser);
    }

    // use this for amdmin: get all users' reservations
    @RequestMapping("/get-reserved-and-reservation-joined")
    public List<Reservation> getAllReservationInfo() {
        return reservationRepository.getAllReservationJoin();
    }
    @RequestMapping("/get-all-user-with-reservation")
    public List<HotelUser> getAllUserWithReservation() {
        List<HotelUser> hotelUsers=new ArrayList<>();
        List<Reservation> reservations=reservationRepository.getAllReservationJoin();
        for (Reservation reservation:reservations) {
            hotelUsers.add(reservation.getUser());
        }
        return hotelUsers;
    }

    @RequestMapping(value = "/finalise_reservation/{res_id}/{room_id}/{start}/{end}", method = RequestMethod.POST)
    public boolean finaliseReservation(@PathVariable("res_id") Long res_id,
                                       @PathVariable("room_id") Long room_id,
                                       @PathVariable("start") String start,
                                       @PathVariable("end") String end) {
        try {
            return roomOrganiser.finaliseReservation(res_id, room_id, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO: deprecated for users. Delete if admin does not uses it.
    @RequestMapping(value = "/get-all-reservations")
    public List<Reservation> getAllReservations() {
        return roomOrganiser.getAllReservations();
    }

    @RequestMapping(value = "/get-my-reservations")
    public List<Reservation> getMyReservations(@RequestHeader String Authorization) {
        return roomOrganiser.getMyReservations(Authorization);
    }

    // TODO: is this used at all?
    @RequestMapping(value = "/get-all-reserved-rooms")
    public List<ReservedRoom> getAllReservedRooms() {
        return reservedRoomRepository.findAll();
    }
    
    @RequestMapping(value = "/category/reserve/{category_id}/{start}/{end}", method = RequestMethod.POST)
    public boolean reserveRoom(@PathVariable("category_id") Long id,
                               @PathVariable("start") String start,
                               @PathVariable("end") String end,
                               @RequestHeader String Authorization) {
            return roomOrganiser.reserveRoomCategory(id, start, end, Authorization);
    }

    // TODO: is this used at all?
    @RequestMapping(value = "/category/available/{id}/{start}/{end}", method = RequestMethod.POST)
    public boolean checkIfCategoryAvailableInTimeFrameById(@PathVariable("id") Long id,
                                                           @PathVariable("start") String start,
                                                           @PathVariable("end") String end) {
        return roomOrganiser.getAvailableRoomsInCategory(start, end, id).size() > 0;
    }

    // TODO: identify used by token
    @DeleteMapping("/reservation/delete/{id}")
    public void removeCompleted(@PathVariable("id") Long id) {
        reservationRepository.deleteById(id);
    }
}
