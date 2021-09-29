package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL, "wiremock"})
public class GraphQLProviderTest {
    @Autowired
    GraphQLProvider graphQLProvider;



    @Before
    public void init() {}

    @Test
    public void schemaParserTest() throws IOException {
        graphQLProvider.init();
        GraphQL build = graphQLProvider.graphQL(); // bookById
        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { bookById(id: book-1) { id name pageCount } }")
                .build();
        ExecutionResult executionResult = build.execute(executionInput);
        Object data = executionResult.getData();

        if(data != null) {
            System.out.println(data.toString());
        }

    }
}
