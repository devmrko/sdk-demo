package com.oracle.sdkdemo;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.shaded.json.JSONObject;
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
			return test().toJSONString();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("### error: ", e.getMessage());
		}

		return null;
	}

	public static JSONObject test() throws IOException {
		logger.info("### test");

		String configurationFilePath = "/Users/joungminko/.oci/config";
		String profile = "DEFAULT";

		final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(configurationFilePath, profile);
		final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

		String tenantId = provider.getTenantId();

		logger.info("### tenantId: {}", tenantId);

		JSONObject outputJsonObj = new JSONObject();

		IdentityClient client = IdentityClient.builder().build(provider);

		ListRegionSubscriptionsRequest listRegionSubscriptionsRequest = ListRegionSubscriptionsRequest.builder()
				.tenancyId(tenantId).build();

		ListRegionSubscriptionsResponse response = client.listRegionSubscriptions(listRegionSubscriptionsRequest);

		List<RegionSubscription> regionItems = response.getItems();
		int i = 0;
		for (RegionSubscription subscription : regionItems) {
			JSONObject curJsonObj = new JSONObject();
			String curRegion = subscription.getRegionName();
			curJsonObj.put(curRegion, curRegion);

			logger.info("### region: {}", curRegion);
			ListCompartmentsResponse listCompartments = client
					.listCompartments(ListCompartmentsRequest.builder().compartmentId(tenantId).build());
			List<Compartment> compartmentItems = listCompartments.getItems();

			for (Compartment compartment : compartmentItems) {
				String curCompartmentId = compartment.getCompartmentId();
				curJsonObj.put(curCompartmentId, compartment.getDescription());

				logger.info("### compartment: {}, {}", compartment.getDescription(), compartment.getCompartmentId());

				VirtualNetworkClient vNclient = VirtualNetworkClient.builder().region(curRegion).build(provider);

				ListVcnsRequest listVcnsRequest = ListVcnsRequest.builder().compartmentId(curCompartmentId)
						.sortBy(ListVcnsRequest.SortBy.Displayname).sortOrder(ListVcnsRequest.SortOrder.Desc).build();

				/* Send request to the Client */
				ListVcnsResponse vResponse = vNclient.listVcns(listVcnsRequest);
				List<Vcn> vcnItems = vResponse.getItems();
				for (Vcn vcn : vcnItems) {
					curJsonObj.put(vcn.getId(), vcn.getDisplayName());
					logger.info("### vcn: {}, {}", vcn.getDisplayName(), vcn.getId());
				}

			}
			outputJsonObj.put(i++ + "", curJsonObj);
		}

		return outputJsonObj;
	}
}
