package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.MyShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
@Service
@Transactional
public class MyShipServiceImpl implements MyShipService {
   private MyShipRepository myShipRepository;
    public MyShipServiceImpl(){}
    @Autowired
    public MyShipServiceImpl(MyShipRepository myShipRepository) {
        super();
        this.myShipRepository = myShipRepository;
    }
    @Override
    public Ship addShip(Ship ship) {
        myShipRepository.save(ship);
        return myShipRepository.save(ship);
    }
    @Override
    public Ship getShip(Long id) {
        return myShipRepository.findById(id).orElse(null);
    }
    @Override
    public void removeShip(Ship ship) {
        myShipRepository.delete(ship);

    }
    @Override
    public List<Ship> getShips(String name,
                               String planet,
                               ShipType shipType,
                               Long after,
                               Long before,
                               Boolean isUsed,
                               Double minSpeed,
                               Double maxSpeed,
                               Integer minCrewSize,
                               Integer maxCrewSize,
                               Double minRating,
                               Double maxRating) {
        final List <Ship> list=new ArrayList<>();
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
      
        for (Ship ship:myShipRepository.findAll()) {
            if(name!=null&&!ship.getName().contains(name))  continue;
            if (planet != null && !ship.getPlanet().contains(planet)) continue;
            if (shipType != null && ship.getShipType() != shipType) continue;
            if (afterDate != null && ship.getProdDate().before(afterDate)) continue;
            if (beforeDate != null && ship.getProdDate().after(beforeDate)) continue;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) continue;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) continue;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) continue;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) continue;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) continue;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) continue;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) continue;
           
            list.add(ship);
         
            }
        return list;
    }

    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    default: return 0;
                }
            });
        }
        return ships;
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > ships.size()) to = ships.size();
        return ships.subList(from, to);
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return ship != null && isStringValid(ship.getName()) && isStringValid(ship.getPlanet())
                && isProdDateValid(ship.getProdDate())
                && isSpeedValid(ship.getSpeed())
                && isCrewSizeValid(ship.getCrewSize());
    }

    @Override
    public double computeRating(double speed, boolean isUsed, Date prod) {
        final int now=3019;
        final int dataShip=getYearFromDate(prod);
        Double rait;
        Double kof=0.5;
        if(!isUsed) {
            kof=1.0;
        }
        rait=80*speed*kof/(now-dataShip+1);
        return round(rait);

    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean changeRaiting=false;
        final String name =newShip.getName();
        if(name!=null){
            if(isStringValid(name)){
                oldShip.setName(name);
            }
            else throw  new  IllegalArgumentException();
        }
        final String planet=newShip.getPlanet();
        if(planet!=null){
            if(isStringValid(planet)){
                oldShip.setPlanet(planet);
            }
            else throw new IllegalArgumentException();
        }
        if(newShip.getShipType()!=null){
            oldShip.setShipType(newShip.getShipType());
        }
        final Date prodDate = newShip.getProdDate();
        if(prodDate!=null){
            if (isProdDateValid(prodDate)){
                oldShip.setProdDate(prodDate);
                changeRaiting=true;
            }
            else throw new IllegalArgumentException();
        }
        if(newShip.getUsed()!=null){
            oldShip.setUsed(newShip.getUsed());
            changeRaiting=true;
        }

        final Double speed=newShip.getSpeed();
        if(speed!=null){
            if(isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
                changeRaiting = true;
            }
            else throw new IllegalArgumentException();
        }

        final  Integer crewSize= newShip.getCrewSize();
        if(crewSize!=null){
            if(isCrewSizeValid(crewSize)){
                oldShip.setCrewSize(crewSize);
                changeRaiting=true;
            }
            else throw new IllegalArgumentException();
        }
        if (changeRaiting){
            final  double raiting=computeRating(oldShip.getSpeed(),oldShip.getUsed(),oldShip.getProdDate());
            oldShip.setRating(raiting);
        }
        myShipRepository.save(oldShip);
        return oldShip;
    }
    private boolean isCrewSizeValid(Integer crewSize) {
        final int minCrewSize = 1;
        final int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;
    }

    private boolean isSpeedValid(Double speed) {
        final double minSpeed = 0.01;
        final double maxSpeed = 0.99;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    private boolean isStringValid (String value) {
        final int maxStringLength = 50;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isProdDateValid(Date prodDate) {
        final Date startProd = getDateForYear(2800);
        final Date endProd = getDateForYear(3019);
        return prodDate != null && prodDate.after(startProd)&& prodDate.before(endProd);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

   private int getYearFromDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private double round(double value) {
        return Math.round(value * 100) / 100D;
    }



}
