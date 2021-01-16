package com.space.repository;

import com.space.model.Ship;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface  MyShipRepository extends CrudRepository<Ship,Long>  {

}
