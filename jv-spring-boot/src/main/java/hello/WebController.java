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

				JSONArray project_array = new JSONArray(projects.getBody()
						.toString());

				for (int i = 0; i < project_array.length(); i++) {
					JSONObject projectObject = project_array.optJSONObject(i);

					Project ps = new Project();

					String sd = projectObject.getString("start_date");
					SimpleDateFormat formatStartDate = new SimpleDateFormat(
							"DD-MM-YYYY");
					Date startDate = formatStartDate.parse(sd);

					String ed = projectObject.getString("end_date");
					SimpleDateFormat formatEndDate = new SimpleDateFormat(
							"DD-MM-YYYY");
					Date endDate = formatEndDate.parse(sd);

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
								SimpleDateFormat formatDueDate = new SimpleDateFormat(
										"DD-MM-YYYY");
								Date dueDate = formatDueDate.parse(sd);

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

									projectTaskData
											.setPk((Integer) projectTaskDataObject
													.getInt("pk"));

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

							resource.setId((Integer) resourceObject
									.getInt("id"));

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
