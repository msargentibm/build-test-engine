package com.example.cohort;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.MeasureReport;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ibm.cohort.engine.measure.MeasureEvaluator;
import com.ibm.cohort.fhir.client.config.FhirClientBuilder;
import com.ibm.cohort.fhir.client.config.FhirClientBuilderFactory;
import com.ibm.cohort.fhir.client.config.FhirServerConfig;

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

        // Use strongly typed objects for parameters. For examples of constructing supported types see: 
        // https://github.com/Alvearie/quality-measure-and-cohort-service/blob/0501fa4e2adec384f9f82c1333ed6c786e39c470/cohort-cli/src/main/java/com/ibm/cohort/cli/ParameterHelper.java#L87
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("Measurement Period", new Interval(new DateTime("2020-01-01T00:00:00", OffsetDateTime.now().getOffset()), true, new DateTime("2020-12-31T23:59:59", OffsetDateTime.now().getOffset()), true));
        parameters.put("FakeIntegerParameter", 10);
        parameters.put("FakeDecimalParameter", BigDecimal.valueOf(1.439832));

        MeasureReport measureReport = evaluator.evaluatePatientMeasure("wh-cohort-Over-the-Hill-Male-1.0.0-identifier", "17672577005-c9ce8b3a-1942-43a9-a886-e8b4e954b014", parameters);
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

        List<FhirServerConfig.LogInfo> loginfo = new ArrayList<>();
        loginfo.add(FhirServerConfig.LogInfo.RESPONSE_SUMMARY);
        fhirServerConfig.setLogInfo(loginfo);
        
        return fhirServerConfig;
    }
}
