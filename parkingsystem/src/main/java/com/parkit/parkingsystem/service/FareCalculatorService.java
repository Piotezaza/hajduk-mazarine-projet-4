package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import java.math.BigDecimal;  
import java.math.RoundingMode;  

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (outHour - inHour) / (3600000);
        BigDecimal bd = new BigDecimal(duration).setScale(2, RoundingMode.HALF_UP);  
        duration = bd.doubleValue();

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if(duration <= 0.5){
                  ticket.setPrice(duration * Fare.CAR_RATE_PER_HALF_HOUR);
                  break;
                }
                
                if(discount == true){
                  ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR) * 0.95);
                } else {
                  ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                break;
            }
            case BIKE: {
              if(duration <= 0.5){
                  ticket.setPrice(duration * Fare.BIKE_RATE_PER_HALF_HOUR);
                  break;
                }
              
              if(discount == true){
                  ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR) * 0.95);
                } else {
                  ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket){
      this.calculateFare(ticket, false);
    }
}