package at.grisa.agilemetrics.producer.jirasoftwareserver.restclient;

import at.grisa.agilemetrics.ApplicationConfig;
import at.grisa.agilemetrics.producer.jirasoftwareserver.JiraSoftwareServerRestClient;
import at.grisa.agilemetrics.producer.jirasoftwareserver.restentity.greenhopper.SprintReport;
import at.grisa.agilemetrics.util.CredentialManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JiraSoftwareServerRestClient.class, CredentialManager.class, ApplicationConfig.class})
@TestPropertySource("classpath:jira-test.properties")
public class JiraSoftwareServerRestClientSprintReportGreenhopperTest {
    @Autowired
    private JiraSoftwareServerRestClient client;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, 1080);
    private MockServerClient mockServerClient;
    private SprintReport sprintReport;

    @Before
    public void loadIssuesFromMockServer() throws URISyntaxException, IOException {
        String responseBody = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("jirasoftware/sprintreportGreenhopper.js").toURI())));

        Long sprintId = 1234L;
        Long rapidviewId = 5678L;
        mockServerClient.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/greenhopper/1.0/rapid/charts/sprintreport")
                        .withQueryStringParameter("rapidViewId", rapidviewId.toString())
                        .withQueryStringParameter("sprintId", sprintId.toString())
        )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(header("Content-Type", "application/json; charset=utf-8"))
                                .withBody(responseBody)
                );

        sprintReport = client.getSprintReportGreenhopper(rapidviewId, sprintId);
    }

    @Test
    public void checkData() {
        assertEquals("check sprintreport completed issues estimates sum", new Integer(31), sprintReport.getContents().getCompletedIssuesEstimateSum().getValue());
        assertEquals("check sprintreport not completed issues estimates sum", new Integer(45), sprintReport.getContents().getIssuesNotCompletedEstimateSum().getValue());
    }
}
