package com.microservice.auth.repositories;

import org.springframework.data.repository.CrudRepository;
import com.microservice.auth.entities.OTP;

public interface OtpRepository extends CrudRepository<OTP, String> {

}
