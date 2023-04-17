package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    
    @Mock
    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        int nbTicket = ticketDAO.getNbTicket("ABCDEF");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        assertEquals(ticketDAO.getNbTicket("ABCDEF"), nbTicket + 1);
        assertNotEquals(parkingService.getNextParkingNumberIfAvailable(), parkingSpot);
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime());
        assertNotNull(ticket.getPrice());
    }


    public void testParkingLotExitRecurringUser() throws Exception{
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        // crée un ticket avec une entrée pour devenir un utilisateur récurrent
        Ticket ticket = new Ticket();
    
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(45);
        ticket.setInTime(new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))); // entrée il y a 10 heures
        ticket.setOutTime(new Date(System.currentTimeMillis() - (22 * 60 * 60 * 1000))); // sortie il y a 9 heures
        ticketDAO.saveTicket(ticket);
        parkingService.processIncomingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.saveTicket(ticket);

        parkingService.processExitingVehicle();
 
        verify(fareCalculatorService, times (1)).calculateFare( any(Ticket.class), eq(true));
    }

}
