package utility;



import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UtilityConfig {

	public static final String authUrl = "http://userservice.staging.tangentmicroservices.com:80/api-token-auth/";
	public static final String baseUrl = "http://projectservice.staging.tangentmicroservices.com:80";
	public static final String projectUrl = "/api/v1/projects/";
	
	public static HttpEntity getEntity(String token){
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept",MediaType.APPLICATION_JSON_VALUE);
		headers.set("Authorization",token);
		
		return new HttpEntity(headers);
	}
	
	public static HttpHeaders getAuth(){
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept",MediaType.APPLICATION_JSON_VALUE);
			
		return headers;
	}
	
	public static HttpHeaders getHeaders(String token){
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept",MediaType.APPLICATION_JSON_VALUE);
		
		headers.set("Authorization",token);	
		
		return headers;
	}
	
	public static JsonNode passCredentials(String username,String password,ObjectMapper mapper){
		
		ObjectNode node= mapper.createObjectNode();
		
		node.put("username",username);
		node.put("password",password);
		
		return node;	
	}
}
