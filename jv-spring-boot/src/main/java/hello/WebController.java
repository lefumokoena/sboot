package hello;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import model.Project;
import model.Resource;
import model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import utility.UtilityConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.pattern.Util;

@Controller
public class WebController {

	@GetMapping("/")
	public ModelAndView showForm() {
		ModelAndView map = new ModelAndView("login");
		map.addObject("userLogin", new UserLogin());

		return map;
	}

	@PostMapping("/")
	public ModelAndView userRequest(@Valid UserLogin userLogin,
			BindingResult bindingResult) {
		ObjectMapper mapper = new ObjectMapper();

		ModelAndView map = new ModelAndView("viewResults");

		JsonNode payload = UtilityConfig.passCredentials(
				userLogin.getUsername(), userLogin.getPassword(), mapper);
		ResponseEntity nw = getAuth(payload.toString());
		System.out.println("WE ARE SECURING THE APP");
		System.out.println(nw);
		System.out.println(nw.getStatusCode());

		if (nw.getStatusCode() == HttpStatus.OK) {

			try {
				JsonNode headNode = mapper.readTree(nw.getBody().toString());
				String tokenNew = headNode.get("token").asText();
				
				ResponseEntity projects = getProjetcs("", tokenNew);

				List<Project> p = new ArrayList<>();
				List<Task> t = new ArrayList<>();
				
				SimpleDateFormat formatDate = new SimpleDateFormat(
						"DD-MM-YYYY");
				
				JSONArray project_array = new JSONArray(projects.getBody()
						.toString());

				for (int i = 0; i < project_array.length(); i++) {
					JSONObject projectObject = project_array.optJSONObject(i);

					Project ps = new Project();

					String sd = projectObject.getString("start_date");					
					Date startDate = formatDate.parse(sd);

					String ed = projectObject.getString("end_date");					
					Date endDate = formatDate.parse(ed);

					ps.setPk((Integer) projectObject.getInt("pk"));
					ps.setTitle((String) projectObject.getString("title"));
					ps.setStart_date(startDate);
					ps.setEnd_date(endDate);
					ps.setIs_billable((Boolean) projectObject
							.getBoolean("is_billable"));
					ps.setIs_active((Boolean) projectObject
							.getBoolean("is_active"));

					// begin tasks data

					JSONArray task_set_array = projectObject
							.optJSONArray("task_set");

					if (task_set_array != null) {

						for (int x = 0; x < task_set_array.length(); x++) {
							JSONObject taskObject = task_set_array
									.optJSONObject(x);

							if (taskObject != null) {

								Task ts = new Task();
								Project projectTaskData = new Project();

								String dd = projectObject.getString("end_date");								
								Date dueDate = formatDate.parse(sd);

								ts.setId((Integer) taskObject.getInt("id"));
								ts.setTitle((String) taskObject
										.getString("title"));
								ts.setDue_date(dueDate);
								ts.setEstimated_hours((Double) taskObject
										.getDouble("estimated_hours"));
								ts.setProject((Integer) taskObject
										.getInt("project"));

								JSONObject projectTaskDataObject = taskObject
										.optJSONObject("project_data");

								if (projectTaskDataObject != null) {

									String pd_sd = projectTaskDataObject.getString("start_date");									
									Date pd_startDate = formatDate.parse(pd_sd);

									String pd_ed = projectTaskDataObject.getString("end_date");
									Date pd_endDate = formatDate.parse(pd_sd);
									
									
									projectTaskData.setPk((Integer) projectTaskDataObject.getInt("pk"));
									projectTaskData.setTitle((String) projectTaskDataObject.getString("title"));
									projectTaskData.setStart_date(pd_startDate);
									projectTaskData.setEnd_date(pd_endDate);
									projectTaskData.setIs_billable((Boolean) projectTaskDataObject
											.getBoolean("is_billable"));
									projectTaskData.setIs_active((Boolean) projectTaskDataObject
											.getBoolean("is_active"));
										
									ts.setProject_data(projectTaskData);
								}

								t.add(ts);
								ps.setTask_set(t);
							}

						}
					}

					// end of tasks data

					// begin resource

					JSONArray resource_set_array = projectObject
							.optJSONArray("resource_set");

					if (resource_set_array != null) {

						for (int w = 0; w < resource_set_array.length(); w++) {
							JSONObject resourceObject = resource_set_array
									.optJSONObject(w);

							List<Resource> rList = new ArrayList<>();
							Resource resource = new Resource();

							String rs_sd = resourceObject.getString("start_date");
							Date rs_startDate = formatDate.parse(rs_sd);
							
							String rs_ed = resourceObject.getString("end_date");
							Date rs_endDate = formatDate.parse(rs_ed);
							
							String rs_c = resourceObject.getString("created");
							Date rs_created = formatDate.parse(rs_c);
							
							String rs_u = resourceObject.getString("updated");
							Date rs_updated = formatDate.parse(rs_u);
							
							resource.setId((Integer) resourceObject.getInt("id"));
							resource.setUser((String)resourceObject.getString("user"));
							resource.setStart_date(rs_startDate);
							resource.setEnd_date(rs_endDate);
							resource.setAgreed_hours_per_month((Double)resourceObject.getDouble("agreed_hours_per_month"));
							resource.setCreated(rs_created);
							resource.setUpdated(rs_updated);
							
							rList.add(resource);
							ps.setResource_set(rList);
						}

					}
					// end resource
					p.add(ps);
				}

				JsonNode heaode = mapper.valueToTree(nw.getBody().toString());

				map.addObject("projects", p);

			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return map;
		}

		map = new ModelAndView("errorLogin");

		return map;
	}

	private ResponseEntity getAuth(String payload) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = UtilityConfig.getAuth();
		HttpEntity<String> entity = new HttpEntity<>(payload, headers);

		return restTemplate.exchange(UtilityConfig.authUrl, HttpMethod.POST,
				entity, String.class);
	}

	private ResponseEntity getProjetcs(String payload, String token) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = UtilityConfig.getHeaders(token);
		HttpEntity<String> entity = new HttpEntity<>(payload, headers);

		return restTemplate.exchange(UtilityConfig.baseUrl
				+ UtilityConfig.projectUrl, HttpMethod.GET, entity,
				String.class);
	}

}
