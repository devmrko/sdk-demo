package org.example;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello world!");
        test();
    }

    public static void test() {
        String configurationFilePath = "/Users/joungminko/.oci/config";
        String profile = "DEFAULT";

        final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
        final AuthenticationDetailsProvider provider =
                new ConfigFileAuthenticationDetailsProvider(configFile);

        provider

//        String compartmentId = "ocid1.compartment.oc1..aaaaaaaaivl6jenyosuvvwrgu6jjpqdf3d7w6vbfswp2er63sruy3amo5pwq";
//
//        final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parse(configurationFilePath, profile);
//
//        final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
//
//        ApiGatewayClient apiGatewayClient = ApiGatewayClient.builder().build(provider);
//        apiGatewayClient.setRegion(Region.US_ASHBURN_1);
//        ListApisRequest listApisRequest = ListApisRequest.builder().compartmentId(compartmentId).build();
//        ListApisResponse listApisResponse = apiGatewayClient.listApis(listApisRequest);
//
//        logger.info("##### getOpcRequestId: {}", listApisResponse.getOpcRequestId());
//
//        ApiCollection apiCollection = listApisResponse.getApiCollection();
//        for (ApiSummary apiSummary : apiCollection.getItems()) {
//            logger.info("##### {}", apiSummary.getDisplayName());
//        }

    }

}