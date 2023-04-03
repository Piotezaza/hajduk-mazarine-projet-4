package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
      try{
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
      } catch (Exception e) {
        e.printStackTrace();
        throw  new RuntimeException("Failed to set up test mock objects");
      }
    }

    @Test
    public void processExitingVehicleTest(){
      try{
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
      } catch (Exception e) {
        e.printStackTrace();
        throw  new RuntimeException("Failed to set up test mock objects");
      }
      
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
      Ticket ticket = new Ticket();
      ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
      ticket.setParkingSpot(parkingSpot);
      ticket.setVehicleRegNumber("ABCDEF");
        
      when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
      when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
      when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
      
      parkingService.processExitingVehicle();
      
      verify(ticketDAO).getNbTicket("ABCDEF");
      verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testProcessIncomingVehicle() {
      try{
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
      } catch (Exception e) {
        e.printStackTrace();
        throw  new RuntimeException("Failed to set up test mock objects");
      }
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(2);
      when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

      parkingService.processIncomingVehicle();
      verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
      verify(ticketDAO).saveTicket(any(Ticket.class));
      verify(ticketDAO).getNbTicket("ABCDEF");
    }

    @Test
    public void processExitingVehicleTestUnableUpdate () {
      parkingService.processExitingVehicle();
      assertEquals(false, ticketDAO.updateTicket(any(Ticket.class)));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
      ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
      assertEquals(1, parkingSpot.getId());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
      ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
      assertEquals(null, parkingSpot);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
      when(inputReaderUtil.readSelection()).thenReturn(3);
      ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
      assertEquals(null, parkingSpot);
    } 

}
