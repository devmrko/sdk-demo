package com.oracle.sdkdemo;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

@SpringBootApplication
public class SdkDemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(SdkDemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SdkDemoApplication.class, args);
		try {
			test();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("### error: ", e.getMessage());
		}
	}

	public static void test() throws IOException {
		logger.info("### test");

		String configurationFilePath = "/Users/joungminko/.oci/config";
		String profile = "DEFAULT";

		final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(configurationFilePath, profile);
		final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

		String tenantId = provider.getTenantId();

		logger.info("### tenantId: {}", tenantId);

		IdentityClient client = IdentityClient.builder().build(provider);

		ListRegionSubscriptionsRequest listRegionSubscriptionsRequest = ListRegionSubscriptionsRequest.builder()
				.tenancyId(tenantId).build();

		ListRegionSubscriptionsResponse response = client.listRegionSubscriptions(listRegionSubscriptionsRequest);

		List<RegionSubscription> regionItems = response.getItems();
		for (RegionSubscription subscription : regionItems) {
			String curRegion = subscription.getRegionName();
			logger.info("### region: {}", curRegion);
			ListCompartmentsResponse listCompartments = client
					.listCompartments(ListCompartmentsRequest.builder().compartmentId(tenantId).build());
			List<Compartment> compartmentItems = listCompartments.getItems();

			for (Compartment compartment : compartmentItems) {
				String curCompartmentId = compartment.getCompartmentId();
				logger.info("### compartment: {}, {}", compartment.getDescription(), compartment.getCompartmentId());

				VirtualNetworkClient vNclient = VirtualNetworkClient.builder().region(curRegion).build(provider);
				
				ListVcnsRequest listVcnsRequest = ListVcnsRequest.builder().compartmentId(curCompartmentId)
						.sortBy(ListVcnsRequest.SortBy.Displayname).sortOrder(ListVcnsRequest.SortOrder.Desc).build();

				/* Send request to the Client */
				ListVcnsResponse vResponse = vNclient.listVcns(listVcnsRequest);
				List<Vcn> vcnItems = vResponse.getItems();
				for (Vcn vcn : vcnItems) {
					logger.info("### vcn: {}, {}", vcn.getDisplayName(), vcn.getId());
				}
				
			}

		}

	}

}
