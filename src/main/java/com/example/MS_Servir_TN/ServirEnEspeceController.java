package com.example.MS_Servir_TN;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.MS_Emission_TN.twilio.SMSController;
import com.example.SharedLib.entities.Compte;
import com.example.SharedLib.entities.Transfert;
import com.example.SharedLib.enums.EtatTransfert;
import com.example.SharedLib.enums.TypeFrais;

public class ServirEnEspeceController {
	private RestTemplate restTemplate;
	@GetMapping("/restituer_transfert")
	public String servirTransfert(@RequestBody Transfert transfert ) {
		
		if(transfert.getEtat()!=EtatTransfert.à_servir & transfert.getEtat()!=EtatTransfert.débloqué_a_servir) {
			return "";
		}
		
		// L'agent qui va servir le transfert
		// Ici, il faut l'id de current Agent 
		// J'ai mis celui de l'agent mais il faut le modifier 
		// avant de travailler avec 
		Long id= transfert.getAgent().getIdClient();
	      Compte compte=this.restTemplate.getForObject(
				 "http://Gestion/get_client_compte/"+id,Compte.class);
	      compte.setMontant(compte.getMontant() - transfert.getMontant_operation());
	      modifySolde(compte);
	      transfert.setEtat(EtatTransfert.paye);
	      modifyTransfert(transfert);
	      if(transfert.isNotification()) {
				 sendSMS(transfert.getEmetteur().getTelephone(),"Votre transfert national a été bien servi./n Le référence : "+transfert.getReference());
					
			 }  
		
		
		return "Le transfert a été bien servi";
	}
	public void modifySolde(Compte compte) {
		HttpHeaders headers = new HttpHeaders();
	    // set `content-type` header
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    // set `accept` header
	    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

	    
	   

	    // build the request
	    HttpEntity<Compte> entity = new HttpEntity<>(compte, headers);

	    // send PUT request to update compte
	    this.restTemplate.put("http://Gestion/update_Compte/{id}", entity, compte.getIdCompte());
	}
	
	public void modifyTransfert(Transfert transfert) {
		HttpHeaders headers = new HttpHeaders();
	    // set `content-type` header
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    // set `accept` header
	    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

	    // create a post object
	   

	    // build the request
	    HttpEntity<Transfert> entity = new HttpEntity<>(transfert, headers);

	    // send PUT request to update compte
	    this.restTemplate.put("http://Gestion/update_Transfert/{id}", entity, transfert.getId());
	}
	public void sendSMS(String num,String msg) {
		
		   
	    SMSController.sendMessages(num, msg);
	    
	}

}
