package com.example.cohort;

import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.MeasureReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.cohort.engine.FhirClientBuilder;
import com.ibm.cohort.engine.FhirClientBuilderFactory;
import com.ibm.cohort.engine.FhirServerConfig;
import com.ibm.cohort.engine.measure.MeasureEvaluator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Hello FHIR world!
 *
 */
public class App
{
    private static Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main( String[] args ) throws Exception {
        FhirClientBuilderFactory factory = FhirClientBuilderFactory.newInstance();
        FhirClientBuilder builder = factory.newFhirClientBuilder();
        
        FhirServerConfig config = getHardcodedConfig();
        FhirContext fhirContext = FhirContext.forR4();

        IGenericClient dataServerClient = builder.createFhirClient(config);
        IGenericClient terminologyServerClient = builder.createFhirClient(config);
        IGenericClient measureServerClient = builder.createFhirClient(config);
        MeasureEvaluator evaluator = new MeasureEvaluator(dataServerClient, terminologyServerClient, measureServerClient);

        MeasureReport measureReport = evaluator.evaluatePatientMeasure("wh-cohort-Over-the-Hill-Male-1.0.0-identifier", "17672577005-c9ce8b3a-1942-43a9-a886-e8b4e954b014", new HashMap<>());
        if (measureReport != null) {
            IParser parser = fhirContext.newJsonParser().setPrettyPrint(true);
            logger.info(parser.encodeResourceToString(measureReport));
        }
    }
    
    private static FhirServerConfig getHardcodedConfig() {
        // Provide your actual values in a more secure manner. This is for an example only
        Map<String, String> headers = new HashMap<>();
        headers.put("X-FHIR-TENANT-ID", "default");
        
        FhirServerConfig fhirServerConfig = new FhirServerConfig();
        //  FHIR Server authentication
        fhirServerConfig.setUser("fhiruser");
        fhirServerConfig.setPassword("change-password");
        
        //  FHIR Server URL
        fhirServerConfig.setEndpoint("https://localhost:9443/fhir-server/api/v4");
        fhirServerConfig.setHeaders(headers);
        
        return fhirServerConfig;
    }
}
