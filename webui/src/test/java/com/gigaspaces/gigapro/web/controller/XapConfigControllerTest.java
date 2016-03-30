package com.gigaspaces.gigapro.web.controller;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.Profile;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.gigaspaces.gigapro.web.XAPTestOptions.getDefaultOptions;
import static com.gigaspaces.gigapro.web.XAPTestOptions.getNamedZoneOptions;
import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port:9999")
@ActiveProfiles("test")
public class XapConfigControllerTest {

    RestTemplate template = new TestRestTemplate();

    @Test
    public void generateIsReachableTest() {
        XapConfigOptions options = getNamedZoneOptions();
        options.setScriptType(SHELL);
        ResponseEntity<InputStreamResource> responseEntity = template.postForEntity("http://localhost:9999/generate", options, InputStreamResource.class);

        assertThat(responseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void generateHeadersTest() {
        XapConfigOptions options = getNamedZoneOptions();
        options.setScriptType(SHELL);
        ResponseEntity<InputStreamResource> responseEntity = template.postForEntity("http://localhost:9999/generate", options, InputStreamResource.class);

        assertThat(responseEntity.getHeaders().toSingleValueMap(), allOf(
                hasEntry(equalTo("Content-Length"), greaterThan("0")),
                hasEntry(equalTo("Content-Type"), containsString("application/zip")),
                hasEntry("Cache-Control", "no-cache, no-store, must-revalidate"),
                hasEntry("Pragma", "no-cache"),
                hasEntry("Expires", "0"),
                hasEntry("Content-Description", "File Transfer"))
        );
    }

    @Test
    public void profilesTest() {
        ResponseEntity<List> responseEntity = template.getForEntity("http://localhost:9999/profiles", List.class);
        List<String> profiles = responseEntity.getBody();

        assertThat(responseEntity.getStatusCode(), is(OK));
        assertThat(profiles.size(), is(1));
        assertThat(profiles.get(0), is("default"));
    }

    @Test
    public void profileTest() {
        ResponseEntity<Profile> responseEntity = template.getForEntity("http://localhost:9999/profiles/default", Profile.class);
        Profile profile = responseEntity.getBody();

        assertThat(responseEntity.getStatusCode(), is(OK));
        assertThat(profile.getName(), is("default"));
        assertThat(profile.getOptions(), is(getDefaultOptions()));
    }
}
