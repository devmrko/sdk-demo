package com.oracle.sdkdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.core.requests.ListVcnsRequest;
import com.oracle.bmc.core.responses.ListVcnsResponse;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.requests.ListRegionSubscriptionsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.identity.responses.ListRegionSubscriptionsResponse;

@RestController
public class SdkController {

	private static final Logger logger = LoggerFactory.getLogger(SdkDemoApplication.class);

	@GetMapping("/test")
	public String getTest() {
		try {
			return test();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("### error: ", e.getMessage());
			
			Gson result = new Gson();
			Map<String, String> errorResult = new HashMap<String, String>();
			errorResult.put("error", e.getMessage());
			result.toJson(errorResult);
			
			return result.toString();
		}

	}

	public static AuthenticationDetailsProvider getProvider() throws IOException {
		String configurationFilePath = "/Users/joungminko/.oci/config";
		String profile = "DEFAULT";

		final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(configurationFilePath, profile);
		return new ConfigFileAuthenticationDetailsProvider(configFile);
	}
	
	public static List<RegionSubscription> getRegionSubscription(AuthenticationDetailsProvider p) throws IOException {
		
		String tenantId = p.getTenantId();
		IdentityClient client = IdentityClient.builder().build(p);
		
		ListRegionSubscriptionsRequest listRegionSubscriptionsRequest = ListRegionSubscriptionsRequest.builder().tenancyId(tenantId).build();
		ListRegionSubscriptionsResponse listRegionSubscriptionsResponse = client.listRegionSubscriptions(listRegionSubscriptionsRequest);
		
		return listRegionSubscriptionsResponse.getItems();
	}
	
	public static List<Compartment> getCompartmentResult(AuthenticationDetailsProvider p, String region) throws IOException {
		
		String tenantId = p.getTenantId();
		IdentityClient client = IdentityClient.builder().region(region).build(p);
		
		ListCompartmentsResponse listCompartmentsResponse = client.listCompartments(ListCompartmentsRequest.builder().compartmentId(tenantId).build());

		return listCompartmentsResponse.getItems();
	}
	
	public static List<Vcn> getVcn(AuthenticationDetailsProvider p, String region, String compartmentId) throws IOException {
		
		String tenantId = p.getTenantId();
		logger.info("### tenantId: {}", tenantId);
		VirtualNetworkClient virtualNetworkClient = VirtualNetworkClient.builder().region(region).build(p);
		ListVcnsRequest listVcnsRequest = ListVcnsRequest.builder().compartmentId(compartmentId).build();
		ListVcnsResponse listVcnsResponse = virtualNetworkClient.listVcns(listVcnsRequest);

		return listVcnsResponse.getItems();
	}
	
	public static String test() throws IOException {
		logger.info("### test");

		AuthenticationDetailsProvider p = getProvider();
		List<RegionSubscription> regionSubscriptions = getRegionSubscription(p);
		
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		for (RegionSubscription rs : regionSubscriptions) {
			
			Map<String, Object> curRegionMap = new HashMap<String, Object>();
			String curRegion = rs.getRegionName();
			curRegionMap.put("region", curRegion);
			logger.info("### region: {}", curRegion);
			
			List<Compartment> compartmentResults = getCompartmentResult(p, curRegion);
			
			for (Compartment compartment : compartmentResults) {
				Map<String, Object> curCompartmentMap = new HashMap<String, Object>();
				
				String curCompartmentId = compartment.getId();
				String curCompartmentName = compartment.getDescription();
				
				logger.info("### getCompartmentId: {}", curCompartmentId);
				logger.info("### getDescription: {}", curCompartmentName);
				curCompartmentMap.put("compartmentId", curCompartmentId);
				curCompartmentMap.put("compartmentName", curCompartmentName);

				List<Vcn> vcns = getVcn(p, curRegion, curCompartmentId);
				Map<String, String> curVcnMap = new HashMap<String, String>();
				for (Vcn vcn : vcns) {
					logger.info("### vcn: {}, {}", vcn.getDisplayName(), vcn.getId());
					curVcnMap.put("vcnName", vcn.getDisplayName());
					curVcnMap.put("vcnId", vcn.getId());
				}
				curCompartmentMap.put("vcns", curVcnMap);
				curRegionMap.put(curCompartmentName, curCompartmentMap);
			}
			resultList.add(curRegionMap);
		}
		Gson gson = new Gson();
		return gson.toJson(resultList);
	}
	
}
