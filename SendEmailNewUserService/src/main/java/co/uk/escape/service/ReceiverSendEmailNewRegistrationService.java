package co.uk.escape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import co.uk.escape.domain.RegistrationRequest;



@Controller
public class ReceiverSendEmailNewRegistrationService {
	
	
	public void sendEmailNewUser(RegistrationRequest newUserRegistrationRequest) {
		
        System.out.println("Sending email to new user <" + newUserRegistrationRequest.getEmailAddress() + ">");
    }
	
}
